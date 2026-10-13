#!/usr/bin/env python3
"""Render vintage-styled app screenshots for the Radioold repo/store gallery."""
import os, math
from PIL import Image, ImageDraw, ImageFilter

W, H = 1080, 2400
PA, PB = (238, 224, 190), (208, 186, 139)   # parchment top→bottom
INK    = (88, 62, 34)
RED    = (150, 44, 38)
BRASS  = (198, 152, 88)

def parchment():
    img = Image.new('RGB', (W, H))
    d = ImageDraw.Draw(img)
    for y in range(H):
        t = y/H
        c = tuple(int(PA[i]+(PB[i]-PA[i])*t) for i in range(3))
        d.line([(0, y), (W, y)], fill=c)
    # faint stains
    import random
    random.seed(7)
    for _ in range(24):
        x, y = random.randint(0, W), random.randint(0, H)
        r = random.randint(30, 110)
        d.ellipse((x-r, y-r, x+r, y+r), fill=(212, 190, 150))
    img = img.filter(ImageFilter.GaussianBlur(40))
    d = ImageDraw.Draw(img)
    return img, d

def header(d, title):
    d.text((W//2, 110), title, anchor='mm', fill=INK, font_size=64)
    d.line([(180, 160), (W-180, 160)], fill=BRASS, width=3)

def world_grid(d, sel_world=False):
    # simplified map blob: continents as rounded blobs
    blobs = [  # (cx, cy, rx, ry) in screen coords, fake continents
        (300, 620, 130, 90), (520, 560, 90, 70),   # n.america, s.america top
        (500, 760, 70, 110),                        # s.america
        (720, 540, 150, 80), (800, 700, 90, 90),    # europe, africa
        (880, 560, 180, 80),                        # asia
        (800, 840, 80, 55), (900, 880, 60, 40),     # s.e. asia
        (350, 800, 55, 40),
    ]
    for i, (x, y, rx, ry) in enumerate(blobs):
        sel = (i == 4)
        fill = RED if sel else (176, 138, 92)
        d.ellipse((x-rx, y-ry, x+rx, y+ry), fill=fill, outline=INK, width=2)

def screen_map():
    img, d = parchment()
    header(d, 'RADIOOLD')
    d.text((W//2, 250), 'a musical time machine', anchor='mm', fill=INK, font_size=42)
    world_grid(d)
    # wax seal play disc bottom center
    d.ellipse((W//2-120, H-360, W//2+120, H-120), fill=RED, outline=(110,26,22), width=8)
    d.ellipse((W//2-96, H-336, W//2+96, H-144), outline=(240,220,190), width=4)
    d.polygon([(W//2-30, H-290), (W//2+55, H-240), (W//2-30, H-190)], fill=(240,220,190))
    # decade strip
    for i, t in enumerate(['1900s','1910s','1920s','1930s','1940s']):
        x = 90+ i*185
        d.rounded_rectangle((x-80, H-560, x+80, H-470), radius=22,
                            fill=RED if t=='1930s' else (222, 204, 162),
                            outline=INK, width=2)
        d.text((x, H-515), t, anchor='mm', fill=(240,220,190) if t=='1930s' else INK, font_size=34)
    return img

def screen_player():
    img, d = parchment()
    header(d, 'NOW ON AIR')
    # cover frame
    d.rounded_rectangle((W//2-320, 300, W//2+320, 940), radius=24, fill=(214, 196, 158), outline=INK, width=4)
    d.rounded_rectangle((W//2-290, 330, W//2+290, 910), radius=16, fill=(180, 148, 104))
    # vinyl record inside
    d.ellipse((W//2-230, 390, W//2+230, 850), fill=(52, 40, 28))
    d.ellipse((W//2-190, 430, W//2+190, 810), outline=(180,160,130), width=3)
    d.ellipse((W//2-60, 560, W//2+60, 680), fill=RED)
    d.ellipse((W//2-18, 602, W//2+18, 638), fill=(52, 40, 28))
    d.text((W//2, 1030), 'La Vie En Rose', anchor='mm', fill=INK, font_size=58)
    d.text((W//2, 1100), 'Édith Piaf • 1947 • France', anchor='mm', fill=(120, 90, 50), font_size=40)
    # controls: prev, wax play, next
    y = H-520
    d.ellipse((W//2-360, y-70, W//2-240, y+50), outline=INK, width=5)
    d.polygon([(W//2-340, y-30), (W//2-270, y-10), (W//2-340, y+10)], fill=INK)
    d.ellipse((W//2-140, y, W//2+140, y+280), fill=RED, outline=(110,26,22), width=8)
    d.polygon([(W//2-45, y+75), (W//2+60, y+140), (W//2-45, y+205)], fill=(240,220,190))
    d.ellipse((W//2+240, y-70, W//2+360, y+50), outline=INK, width=5)
    d.polygon([(W//2+340, y-30), (W//2+270, y-10), (W//2+340, y+10)], fill=INK)
    return img

def screen_taxi():
    img, d = parchment()
    header(d, 'TAXI MODE')
    d.text((W//2, 250), 'a multi-stop journey', anchor='mm', fill=INK, font_size=42)
    stops = [('FRANCE', '1940s', 'La Vie En Rose'), ('USA', '1950s', 'Blue Moon'),
             ('BRAZIL', '1960s', 'Garota de Ipanema'), ('JAPAN', '1970s', 'Sukiyaki')]
    y = 380
    for i, (c, dec, song) in enumerate(stops):
        d.rounded_rectangle((140, y, W-140, y+300), radius=28, fill=(222, 204, 162), outline=INK, width=3)
        d.ellipse((180, y+95, 300, y+215), fill=RED if i == 0 else (216, 196, 150), outline=INK, width=3)
        d.text((240, y+155), str(i+1), anchor='mm', fill=(240,220,190) if i == 0 else INK, font_size=52)
        d.text((350, y+90), c, fill=INK, font_size=52)
        d.text((350, y+165), dec, fill=RED if i == 0 else (120, 90, 50), font_size=40)
        d.text((350, y+230), 'Now: ' + song, fill=(120, 90, 50), font_size=36)
        y += 340
    d.rounded_rectangle((W//2-280, y+20, W//2+280, y+150), radius=35, fill=RED)
    d.text((W//2, y+85), '+ ADD A STOP', anchor='mm', fill=(240,220,190), font_size=44)
    return img

os.makedirs('/root/radiold/screenshots', exist_ok=True)
screen_map().save('/root/radiold/screenshots/1_map.png')
screen_player().save('/root/radiold/screenshots/2_player.png')
screen_taxi().save('/root/radiold/screenshots/3_taxi.png')
print('3 screenshots saved')
EOF_MARKER = None