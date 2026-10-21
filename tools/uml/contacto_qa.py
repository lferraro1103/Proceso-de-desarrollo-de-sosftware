"""Hoja de contacto para inspección visual; Pillow solo es necesario para este auxiliar."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

root = Path(__file__).resolve().parents[2]
files = sorted((root / 'docs/iteracion-4/diagramas').glob('*.png'))
font = ImageFont.truetype('C:/Windows/Fonts/arial.ttf', 20)
for part in range(0, len(files), 6):
    page = Image.new('RGB', (2100, 1500), '#DFE7EF')
    draw = ImageDraw.Draw(page)
    for i, path in enumerate(files[part:part + 6]):
        im = Image.open(path).convert('RGB')
        im.thumbnail((1020, 440))
        x = (i % 2) * 1050 + 15
        y = (i // 2) * 500 + 45
        draw.text((x, y - 32), path.stem, font=font, fill='#172D45')
        page.paste(im, (x + (1020 - im.width) // 2, y))
    page.save(root / '.tools' / f'contacto-{part // 6 + 1}.png')
print('Hojas de contacto creadas en .tools/')
