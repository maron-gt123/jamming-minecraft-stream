function parseCSV(text) {
  const lines = text.trim().split("\n");
  const header = lines.shift().split(",");

  return lines.map(l => {
    const c = l.split(",");
    const o = {};
    header.forEach((h, i) => o[h] = c[i]);
    return o;
  });
}

async function loadGifts() {
  try {
    const res = await fetch(GIFT_CSV_URL);
    if (!res.ok) throw new Error("CSV取得失敗");

    const text = await res.text();
    gifts = parseCSV(text);
    renderGifts();

  } catch (e) {
    console.error("ギフト読み込みエラー:", e);
  }
}

function renderGifts() {
  const non = document.getElementById("gifts-nonstreak");
  const yes = document.getElementById("gifts-streak");
  non.innerHTML = "";
  yes.innerHTML = "";

  gifts.forEach(g => {
    const isStreak = g.streakable === "True" || g.streakable === true;

    const d = document.createElement("div");
    d.className = "gift " + (isStreak ? "streakable" : "non-streakable");
    d.dataset.name = g.gift_name;

    d.innerHTML = `
      <input type="checkbox" class="gift-enable">
      <img src="${g.image_url}">
      <div>
        <div class="streak-label">${isStreak ? "連続可" : "連続不可"}</div>
        <strong>${g.gift_name}</strong><br>
        💎 ${g.diamond}<br>
        コマンド<br>
        <div class="gift-commands">
          <select class="gift-command" onchange="updateOptionUI(this)">
            <option value="">コマンド選択</option>
            ${Object.keys(commandMap).map(cmd =>
              `<option value="${cmd}">${cmd}</option>`
            ).join("")}
          </select>
          <span class="gift-option-wrap"></span>
        </div>
        <button onclick="addGiftCommand(this)">＋ command</button>
      </div>
    `;

    (isStreak ? yes : non).appendChild(d);
  });
}