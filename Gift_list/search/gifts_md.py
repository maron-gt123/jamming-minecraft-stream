import csv

INPUT_CSV = "gifts.csv"
OUTPUT_MD = "gifts.md"
IMAGE_WIDTH = 40

with open(INPUT_CSV, newline="", encoding="utf-8") as f:
    reader = csv.DictReader(f)

    rows = []
    for r in reader:
        rows.append(
            f"| {r['gift_id']} | {r['gift_name']} | {r['diamond']} | "
            f"<img src=\"{r['image_url']}\" width=\"{IMAGE_WIDTH}\"> | {r['streakable']} |"
        )

with open(OUTPUT_MD, "w", encoding="utf-8") as f:
    f.write("| gift_id | Gift Name | Diamond Value | Image | Streakable |\n")
    f.write("|---:|---|---:|---|---|\n")
    f.write("\n".join(rows))

print(f"✅ Markdown table generated: {OUTPUT_MD} (image width={IMAGE_WIDTH}px)")