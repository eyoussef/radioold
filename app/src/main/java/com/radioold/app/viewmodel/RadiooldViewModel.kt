package com.radioold.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radioold.app.data.catalog.CatalogRepository
import com.radioold.app.data.geo.WorldMap
import com.radioold.app.data.model.Mood
import com.radioold.app.data.model.Selection
import com.radioold.app.data.model.Stop
import com.radioold.app.data.model.Track
import com.radioold.app.domain.CatalogLogic
import com.radioold.app.playback.PlayerController
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Holds the world map, the current selection and the taxi journey, and drives
 * playback from radiooooo stations. Tuning a country/decade fetches a fresh
 * random station queue; the queue is topped up just in time as tracks are
 * consumed (the signed stream links expire after ~15 minutes).
 */
class RadiooldViewModel(
    private val repo: CatalogRepository,
    private val player: PlayerController,
) : ViewModel() {

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _world = MutableStateFlow(WorldMap(emptyList()))
    val world: StateFlow<WorldMap> = _world.asStateFlow()

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

    private val _filtered = MutableStateFlow<List<Track>>(emptyList())
    val filtered: StateFlow<List<Track>> = _filtered.asStateFlow()

    private val _selection = MutableStateFlow(Selection())
    val selection: StateFlow<Selection> = _selection.asStateFlow()

    private val _journey = MutableStateFlow<List<Stop>>(emptyList())
    val journey: StateFlow<List<Stop>> = _journey.asStateFlow()

    val current: StateFlow<Track?> = player.current
    val queue: StateFlow<List<Track>> = player.queue
    val isPlaying: StateFlow<Boolean> = player.isPlaying
    val positionMs: StateFlow<Long> = player.positionMs
    val durationMs: StateFlow<Long> = player.durationMs

    private var tuneJob: Job? = null
    private var tunedKey: String? = null
    private var topUpRunning = false
    private val emptyStations = HashSet<String>()

    init {
        viewModelScope.launch {
            runCatching { _world.value = repo.loadWorld() }
            // Alive on open: random music from the whole time machine.
            val base = runCatching { repo.baseTracks() }.getOrDefault(emptyList())
            remember(base)
            if (base.isNotEmpty()) player.setQueue(CatalogLogic.shuffled(base), 0)
            _loading.value = false

            // Keep the station playing: top the queue up as tracks are consumed.
            player.current.collect { up ->
                if (up != null) topUp()
            }
        }
    }

    fun selectCountry(country: String, name: String) {
        _selection.value = _selection.value.copy(country = country, countryName = name)
        tune()
    }

    fun selectDecade(decade: Int?) {
        _selection.value = _selection.value.copy(decade = decade)
        tune()
    }

    fun selectMood(mood: Mood?) {
        _selection.value = _selection.value.copy(mood = mood)
        tune()
    }

    fun togglePlay() = player.toggle()
    fun next() = player.next()
    fun prev() = player.prev()
    fun seekTo(positionMs: Long) = player.seekTo(positionMs)
    fun playAt(index: Int) = player.playAt(index)

    fun addStopToJourney() {
        val s = _selection.value
        val country = s.country ?: return
        _journey.value = _journey.value + Stop(country, s.countryName ?: country, s.decade ?: Track.NOW_DECADE, s.mood)
    }

    fun removeStop(index: Int) {
        _journey.value = _journey.value.filterIndexed { i, _ -> i != index }
    }

    fun playJourney() {
        val stops = _journey.value
        if (stops.isEmpty()) return
        tuneJob?.cancel()
        tuneJob = viewModelScope.launch {
            val queue = ArrayList<Track>()
            for (stop in stops) {
                queue.addAll(repo.stationQueue(stop.country, stop.countryName, stop.decade, stop.mood, JOURNEY_PER_STOP))
            }
            remember(queue)
            if (queue.isNotEmpty()) player.setQueue(CatalogLogic.shuffled(queue), 0)
        }
    }

    fun clearJourney() {
        _journey.value = emptyList()
    }

    /** Tune the current selection as a radiooooo station. */
    private fun tune() {
        val s = _selection.value
        val key = key(s)
        if (key == tunedKey) return
        tunedKey = key
        tuneJob?.cancel()
        tuneJob = viewModelScope.launch {
            val fresh = if (key in emptyStations) {
                emptyList()
            } else {
                runCatching { repo.stationQueue(s.country, s.countryName, s.decade, s.mood) }
                    .getOrDefault(emptyList())
            }
            if (_selection.value != s) return@launch
            if (fresh.isEmpty()) {
                if (s.country != null || s.decade != null || s.mood != null) emptyStations.add(key)
                // "This Area/Decade is EMPTY for now" — silence beats wrong music.
                player.setQueue(emptyList(), 0)
            } else {
                remember(fresh)
                player.setQueue(CatalogLogic.shuffled(fresh, avoidFirstId = player.current.value?.id), 0)
            }
        }
    }

    /** Just-in-time queue top-up (stream links are valid for ~15 minutes). */
    private fun topUp() {
        if (topUpRunning) return
        val s = _selection.value
        val key = key(s)
        if (key in emptyStations) return
        val q = player.queue.value
        val idx = q.indexOfFirst { it.id == player.current.value?.id }
        if (q.size - (idx + 1) >= AHEAD) return
        topUpRunning = true
        viewModelScope.launch {
            try {
                val more = runCatching { repo.stationQueue(s.country, s.countryName, s.decade, s.mood, AHEAD) }
                    .getOrDefault(emptyList())
                if (more.isEmpty() && (s.country != null || s.decade != null)) emptyStations.add(key)
                remember(more)
                player.appendQueue(more)
            } finally {
                topUpRunning = false
            }
        }
    }

    private fun remember(fetched: List<Track>) {
        if (fetched.isEmpty()) return
        _tracks.update { (it + fetched).distinctBy(Track::id) }
        val s = _selection.value
        _filtered.value = CatalogLogic.filter(_tracks.value, s.country, s.decade, s.mood)
    }

    private fun key(s: Selection) = "${s.country}|${s.decade}|${s.mood}"

    override fun onCleared() {
        player.release()
    }

    private companion object {
        const val AHEAD = 2
        const val JOURNEY_PER_STOP = 2
    }
}
