#!/usr/bin/env python3
"""
Prepares the world-map geometry for the Android app.

Reads countries.geo.json (features keyed by ISO alpha-3 `id`), simplifies each
polygon ring (Douglas-Peucker) so runtime Path building stays cheap on phones,
normalizes coordinates to [0,1] equirectangular space (x=(lon+180)/360,
y=(90-lat)/180) to match the app's WorldMap model, and emits assets/world.json.
"""
import json
import math
import os

HERE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(HERE, "geo", "countries.geo.json")
ASSETS = os.path.join(HERE, "assets")
os.makedirs(ASSETS, exist_ok=True)

EPSILON_DEG = 0.35   # ~35km; crisp enough for a stylized world map


def perp_dist(p, a, b):
    (x, y), (x1, y1), (x2, y2) = p, a, b
    dx, dy = x2 - x1, y2 - y1
    if dx == 0 and dy == 0:
        return math.hypot(x - x1, y - y1)
    t = ((x - x1) * dx + (y - y1) * dy) / (dx * dx + dy * dy)
    t = max(0.0, min(1.0, t))
    px, py = x1 + t * dx, y1 + t * dy
    return math.hypot(x - px, y - py)


def dp(points, eps):
    if len(points) < 3:
        return points
    a, b = points[0], points[-1]
    idx, dmax = 0, 0.0
    for i in range(1, len(points) - 1):
        d = perp_dist(points[i], a, b)
        if d > dmax:
            idx, dmax = i, d
    if dmax > eps:
        left = dp(points[:idx + 1], eps)
        right = dp(points[idx:], eps)
        return left[:-1] + right
    return [a, b]


def simplify_ring(ring, eps):
    if len(ring) <= 4:
        return ring
    closed = ring[0] == ring[-1]
    pts = ring[:-1] if closed else ring[:]
    out = dp(pts, eps)
    if closed and out[0] != out[-1]:
        out = out + [out[0]]
    return out


def to_norm(lon, lat):
    return (lon + 180.0) / 360.0, (90.0 - lat) / 180.0


def geom_to_rings(geom):
    t = geom.get("type")
    coords = geom.get("coordinates", [])
    rings = []
    if t == "Polygon":
        for r in coords:
            rings.append(r)
    elif t == "MultiPolygon":
        for poly in coords:
            for r in poly:
                rings.append(r)
    return rings


def main():
    with open(SRC) as f:
        gj = json.load(f)
    countries = []
    total_before = total_after = 0
    for feat in gj.get("features", []):
        cid = feat.get("id") or feat.get("properties", {}).get("name")
        name = feat.get("properties", {}).get("name", cid)
        rings_out = []
        for ring in geom_to_rings(feat.get("geometry", {})):
            total_before += len(ring)
            simp = simplify_ring(ring, EPSILON_DEG)
            out_pts = []
            for lon, lat in simp:
                nx, ny = to_norm(lon, lat)
                out_pts.append([round(nx, 4), round(ny, 4)])
            total_after += len(out_pts)
            if len(out_pts) >= 3:
                rings_out.append(out_pts)
        if rings_out:
            countries.append({"id": cid, "name": name, "rings": rings_out})
    countries.sort(key=lambda c: c["id"])
    out = {"projection": "equirectangular-normalized", "countries": countries}
    path = os.path.join(ASSETS, "world.json")
    with open(path, "w") as f:
        json.dump(out, f, separators=(",", ":"))
    size = os.path.getsize(path)
    print(f"countries={len(countries)} points {total_before} -> {total_after} "
          f"({size/1024:.0f} KB) -> {path}")
    # sanity: values should be normalized to [0,1]
    sample = countries[0]["rings"][0][0]
    print(f"  sample normalized point: {sample} (expect both in [0,1])")
    for probe in ["FRA", "USA", "JPN", "BRA", "ZAF", "AUS", "RUS", "IND"]:
        print(f"  {probe}: {'OK' if any(c['id'] == probe for c in countries) else 'MISSING'}")


if __name__ == "__main__":
    main()
