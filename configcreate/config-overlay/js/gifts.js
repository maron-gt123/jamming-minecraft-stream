export async function loadGiftMap() {
  const csvUrl =
    "https://raw.githubusercontent.com/maron-gt123/jamming-minecraft-stream/refs/heads/main/configcreate/gift&command/list/gifts.csv";

  const csvText = await fetch(csvUrl).then(r => r.text());

  const parsed = Papa.parse(csvText, { header: true });

  const giftMap = {};

  parsed.data.forEach(row => {
    if (row.gift_name && row.image_url) {
      giftMap[row.gift_name] = row.image_url;
    }
  });

  return giftMap;
}