import { showToast } from "./toast.js";

export function renderOverlay(config, giftMap, overlay) {

  overlay.innerHTML = "";

  const gifts = config.commands?.gift;

  if (!gifts || !Array.isArray(gifts)) {
    overlay.innerHTML = `<div>commands.gift が見つかりません</div>`;
    return;
  }

  gifts.forEach((item, index) => {

    const name = item.gift_name;
    const imgUrl = giftMap[name];

    const div = document.createElement("div");
    div.className = "gift";

    // ===== 画像 =====
    if (imgUrl) {
      const img = document.createElement("img");
      img.crossOrigin = "anonymous";
      img.src = imgUrl + "?v=" + Date.now();
      div.appendChild(img);
    }

    // ===== 名前 =====
    const label = document.createElement("div");
    label.className = "gift-name";
    label.textContent = name;
    div.appendChild(label);

    // ===== 個数 =====
    const countEl = document.createElement("div");
    countEl.className = "gift-count";
    countEl.textContent = item.count ? "×" + item.count : "";
    div.appendChild(countEl);

    // ===== コメント表示 =====
    const commentPreview = document.createElement("div");
    commentPreview.className = "gift-comment";
    commentPreview.textContent = "";
    div.appendChild(commentPreview);

    // ===== コメント入力欄 =====
    const input = document.createElement("input");
    input.type = "text";
    input.placeholder = "コメント";
    input.className = "gift-comment-input";

    // 入力されたら即反映
    input.addEventListener("input", () => {
      commentPreview.textContent = input.value;
    });

    div.appendChild(input);

    // ===== コピー =====
    div.addEventListener("click", async (e) => {

      // inputクリック時はコピーしない
      if (e.target.tagName === "INPUT") {
        return;
      }

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