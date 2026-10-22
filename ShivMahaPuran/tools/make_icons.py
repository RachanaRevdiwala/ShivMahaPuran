#!/usr/bin/env python3
"""Renders the app icon (ॐ on a Neelkantha-blue medallion with a gold ring) for Android and iOS.

Usage: python3 tools/make_icons.py
"""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageFilter

ROOT = Path(__file__).resolve().parent.parent
FONT = ROOT / "shared/src/commonMain/composeResources/font/tiro_devanagari_sanskrit.ttf"
# Neelkantha palette: the blue of the poison Shiva held in his throat.
SAFFRON, MAROON, GOLD, CREAM = (52, 120, 196), (12, 30, 66), (212, 176, 96), (236, 244, 255)


def radial(size, inner, outer):
    img = Image.new("RGB", (size, size), outer)
    draw = ImageDraw.Draw(img)
    steps = size // 2
    for i in range(steps, 0, -1):
        t = i / steps
        color = tuple(int(inner[c] * (1 - t) + outer[c] * t) for c in range(3))
        draw.ellipse([size / 2 - i * 1.25, size / 2 - i * 1.25, size / 2 + i * 1.25, size / 2 + i * 1.25], fill=color)
    return img


def om_layer(size, scale, color=CREAM):
    layer = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    font = ImageFont.truetype(str(FONT), int(size * scale))
    draw = ImageDraw.Draw(layer)
    bbox = draw.textbbox((0, 0), "ॐ", font=font)
    w, h = bbox[2] - bbox[0], bbox[3] - bbox[1]
    pos = ((size - w) / 2 - bbox[0], (size - h) / 2 - bbox[1])
    shadow = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    ImageDraw.Draw(shadow).text((pos[0], pos[1] + size * 0.012), "ॐ", font=font, fill=(0, 8, 30, 150))
    layer = Image.alpha_composite(shadow.filter(ImageFilter.GaussianBlur(size * 0.01)), layer)
    ImageDraw.Draw(layer).text(pos, "ॐ", font=font, fill=color)
    return layer


def full_icon(size, rounded=False):
    """Square icon: medallion background, gold rings, Om."""
    img = radial(size, SAFFRON, MAROON).convert("RGBA")
    draw = ImageDraw.Draw(img)
    for inset, width in ((0.06, 0.018), (0.095, 0.006)):
        d = size * inset
        draw.ellipse([d, d, size - d, size - d], outline=GOLD, width=max(1, int(size * width)))
    img = Image.alpha_composite(img, om_layer(size, 0.58))
    if rounded:
        mask = Image.new("L", (size, size), 0)
        ImageDraw.Draw(mask).rounded_rectangle([0, 0, size, size], radius=size * 0.22, fill=255)
        img.putalpha(mask)
    return img


def foreground(size):
    """Adaptive-icon foreground: Om and gold ring inside the 66% safe zone, transparent elsewhere."""
    layer = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(layer)
    d = size * 0.2
    draw.ellipse([d, d, size - d, size - d], outline=GOLD, width=max(1, int(size * 0.012)))
    return Image.alpha_composite(layer, om_layer(size, 0.4))


def main():
    res = ROOT / "androidApp/src/main/res"
    densities = {"mdpi": 1, "hdpi": 1.5, "xhdpi": 2, "xxhdpi": 3, "xxxhdpi": 4}
    for name, f in densities.items():
        d = res / f"mipmap-{name}"
        d.mkdir(parents=True, exist_ok=True)
        full_icon(int(48 * f), rounded=True).save(d / "ic_launcher.png")
        foreground(int(108 * f)).save(d / "ic_launcher_foreground.png")
        radial(int(108 * f), SAFFRON, MAROON).save(d / "ic_launcher_background.png")
    play = ROOT / "androidApp/play-store-icon-512.png"
    full_icon(512).save(play)

    drawable = res / "drawable"
    drawable.mkdir(parents=True, exist_ok=True)
    splash = Image.new("RGBA", (288, 288), (0, 0, 0, 0))
    splash = Image.alpha_composite(splash, om_layer(288, 0.42, color=SAFFRON))
    splash.save(drawable / "ic_splash_om.png")

    ios = ROOT / "iosApp/iosApp/Assets.xcassets/AppIcon.appiconset"
    ios.mkdir(parents=True, exist_ok=True)
    full_icon(1024).convert("RGB").save(ios / "AppIcon-1024.png")
    print("Icons written.")


if __name__ == "__main__":
    main()
