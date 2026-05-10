import { showToast } from "./toast.js";

export function renderOverlay(config, giftMap, overlay) {

  overlay.innerHTML = "";

  const gifts = config.commands?.gift;

  if (!gifts || !Array.isArray(gifts)) {
    overlay.innerHTML = `<div>commands.gift が見つかりません</div>`;
    return;
  }

  gifts.forEach(item => {

    const name = item.gift_name;
    const imgUrl = giftMap[name];

    const div = document.createElement("div");
    div.className = "gift";

    if (imgUrl) {
      const img = document.createElement("img");
      img.crossOrigin = "anonymous";
      img.src = imgUrl + "?v=" + Date.now();
      div.appendChild(img);
    }

    const label = document.createElement("div");
    label.className = "gift-name";
    label.textContent = name;
    div.appendChild(label);

    const countEl = document.createElement("div");
    countEl.className = "gift-count";
    countEl.textContent = item.count ? "×" + item.count : "";
    div.appendChild(countEl);

    div.addEventListener("click", async () => {

      let text = "";

      if (item.command) {
        text = item.command;
      }

      if (item.commands) {
        text = item.commands.join("\n");
      }

      if (!text) {
        showToast("コピーするコマンドがありません");
        return;
      }

      await navigator.clipboard.writeText(text);

      showToast("コピーしました: " + name);
    });

    overlay.appendChild(div);
  });
}