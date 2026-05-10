import { loadGiftMap } from "./gifts.js";
import { parseYaml } from "./yaml.js";
import { renderOverlay } from "./render.js";
import { exportOverlay } from "./export.js";

const yamlInput = document.getElementById("yamlInput");
const overlay = document.getElementById("overlay");
const bgColorInput = document.getElementById("bgColor");
const preview = document.querySelector(".preview");
const exportBtn = document.getElementById("exportBtn");

const giftMap = await loadGiftMap();

function render() {

  const config = parseYaml(yamlInput.value);

  if (!config) {
    overlay.innerHTML = `<div>YAML解析エラー</div>`;
    return;
  }

  renderOverlay(config, giftMap, overlay);
}

yamlInput.addEventListener("input", render);

bgColorInput.addEventListener("input", e => {

  overlay.style.background = e.target.value;
  preview.style.background = e.target.value;
});

exportBtn.addEventListener("click", () => {

  exportOverlay(overlay, bgColorInput.value);
});

render();