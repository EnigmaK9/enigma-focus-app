import os
import math
from PIL import Image, ImageDraw, ImageFilter

def create_focus_icon(size):
    # Create base image with alpha
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    center = size / 2.0
    radius = size * 0.44

    # 1. Background circle with rich dark emerald gradient
    # Draw concentric circles to simulate radial gradient
    steps = int(radius)
    for i in range(steps, 0, -1):
        ratio = i / steps
        # Color from center (#1E3A2B) to edge (#0D1410)
        r = int(13 + (30 - 13) * (1 - ratio))
        g = int(20 + (58 - 20) * (1 - ratio))
        b = int(16 + (43 - 16) * (1 - ratio))
        cur_rad = radius * ratio
        draw.ellipse(
            [center - cur_rad, center - cur_rad, center + cur_rad, center + cur_rad],
            fill=(r, g, b, 255)
        )

    # 2. Subtle outer ring
    draw.ellipse(
        [center - radius, center - radius, center + radius, center + radius],
        outline=(52, 211, 153, 90),
        width=max(1, int(size * 0.015))
    )

    # 3. Viewfinder focus corner brackets
    bracket_len = size * 0.12
    bracket_dist = radius * 0.82
    bw = max(2, int(size * 0.025))

    # Top-Left
    tl_x = center - bracket_dist
    tl_y = center - bracket_dist
    draw.line([(tl_x, tl_y + bracket_len), (tl_x, tl_y), (tl_x + bracket_len, tl_y)], fill=(255, 255, 255, 200), width=bw)

    # Top-Right
    tr_x = center + bracket_dist
    tr_y = center - bracket_dist
    draw.line([(tr_x - bracket_len, tr_y), (tr_x, tr_y), (tr_x, tr_y + bracket_len)], fill=(255, 255, 255, 200), width=bw)

    # Bottom-Left
    bl_x = center - bracket_dist
    bl_y = center + bracket_dist
    draw.line([(bl_x, bl_y - bracket_len), (bl_x, bl_y), (bl_x + bracket_len, bl_y)], fill=(255, 255, 255, 200), width=bw)

    # Bottom-Right
    br_x = center + bracket_dist
    br_y = center + bracket_dist
    draw.line([(br_x - bracket_len, br_y), (br_x, br_y), (br_x, br_y - bracket_len)], fill=(255, 255, 255, 200), width=bw)

    # 4. Central Split Lens (Half Grayscale / Half Emerald Green)
    inner_rad = radius * 0.58
    bbox = [center - inner_rad, center - inner_rad, center + inner_rad, center + inner_rad]

    # Left half: Monochrome Gray
    draw.pieslice(bbox, 90, 270, fill=(130, 130, 130, 240))

    # Right half: Emerald Focus Green
    draw.pieslice(bbox, 270, 90, fill=(16, 185, 129, 255))

    # Outer border for split circle
    draw.ellipse(bbox, outline=(255, 255, 255, 230), width=max(2, int(size * 0.025)))

    # 5. Hourglass center glyph in pure white
    hg_w = inner_rad * 0.45
    hg_h = inner_rad * 0.65
    top_y = center - hg_h
    bot_y = center + hg_h
    left_x = center - hg_w
    right_x = center + hg_w

    # Top triangle and bottom triangle meeting at center
    draw.polygon([(left_x, top_y), (right_x, top_y), (center, center)], fill=(255, 255, 255, 255))
    draw.polygon([(left_x, bot_y), (right_x, bot_y), (center, center)], fill=(255, 255, 255, 255))

    # Central spark dot
    dot_rad = max(2, int(size * 0.02))
    draw.ellipse([center - dot_rad, center - dot_rad, center + dot_rad, center + dot_rad], fill=(13, 20, 16, 255))

    return img

def main():
    res_dir = "/home/enigma/github/kotlin/enigma-focus-app/app/src/main/res"
    densities = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }

    # Generate 512px icon
    master_icon = create_focus_icon(512)
    master_path = os.path.join(res_dir, "drawable/ic_launcher_master.png")
    master_icon.save(master_path)
    print(f"Saved master icon to {master_path}")

    for folder, dim in densities.items():
        folder_path = os.path.join(res_dir, folder)
        os.makedirs(folder_path, exist_ok=True)

        scaled = create_focus_icon(dim)

        # Save both png and webp to override any cached placeholder
        png_path = os.path.join(folder_path, "ic_launcher.png")
        png_round = os.path.join(folder_path, "ic_launcher_round.png")
        webp_path = os.path.join(folder_path, "ic_launcher.webp")
        webp_round = os.path.join(folder_path, "ic_launcher_round.webp")

        scaled.save(png_path, "PNG")
        scaled.save(png_round, "PNG")
        scaled.save(webp_path, "WEBP")
        scaled.save(webp_round, "WEBP")
        print(f"Generated {folder}: {dim}x{dim}")

if __name__ == "__main__":
    main()
