// ===============================
// Minecraftバージョン変更処理
// ===============================
function changeVersion() {
  MC_VERSION = document.getElementById("mcVersion").value;
  loadBlocks();
}

// ===============================
// ブロックデータ読み込み
// ===============================
function loadBlocks() {
  const BLOCKS_URL = getBlocksUrl();
  const LANG_URL = getLangUrl();
  const BLOCK_TEXTURE_BASE = getTextureBase();

  Promise.all([
    fetch(LANG_URL).then(r => r.json()),
    fetch(BLOCKS_URL).then(r => r.json())
  ]).then(([lang, blocks]) => {
    JA_LANG = lang;
    BLOCKS = blocks
      .filter(b => b.name.includes("_block"))
      .map(b => {
        const key = "block.minecraft." + b.name.toLowerCase();
        return {
          id: b.name.toUpperCase(),
          name: b.name,
          label: JA_LANG[key] || b.name.replace(/_/g, " ").toUpperCase(),
          image: BLOCK_TEXTURE_BASE + b.name.toLowerCase() + ".png"
        };
      });

    renderAllBlocks();
  });
}

// ===============================
// 全ブロックUI初期描画
// ===============================
function renderAllBlocks() {
  ["bottom", "middle", "top"].forEach(pos => {
    renderBlockSelect(pos, BLOCKS);
  });

  block_bottom.value = "IRON_BLOCK";
  block_middle.value = "GOLD_BLOCK";
  block_top.value = "DIAMOND_BLOCK";

  updateBlockImage("bottom");
  updateBlockImage("middle");
  updateBlockImage("top");
}

// ===============================
// セレクトボックス描画
// ===============================
function renderBlockSelect(pos, list) {
  const sel = document.getElementById("block_" + pos);
  const current = sel.value;

  sel.innerHTML = "";
  list.forEach(b => {
    const opt = document.createElement("option");
    opt.value = b.id;
    opt.textContent = b.label;
    opt.dataset.img = b.image;
    sel.appendChild(opt);
  });

  // 可能なら選択を維持
  if ([...sel.options].some(o => o.value === current)) {
    sel.value = current;
  }

  updateBlockImage(pos);
}

// ===============================
// ブロック検索（フィルタ）
// ===============================
function searchBlock(pos, keyword) {
  const list = filterBlocks(keyword);
  renderBlockSelect(pos, list);
}

// ===============================
// ブロック画像更新
// ===============================
function updateBlockImage(pos) {
  const sel = document.getElementById("block_" + pos);
  const img = document.getElementById("img_" + pos);
  if (!sel || !img) return;
  const opt = sel.selectedOptions[0];
  if (!opt) return;
  img.src = opt.dataset.img;
}

// ===============================
// ブロック一覧フィルタリング
// ===============================
function filterBlocks(keyword) {
  if (!keyword) return BLOCKS;
  const k = keyword.toLowerCase();
  return BLOCKS.filter(b =>
    b.label.toLowerCase().includes(k) ||
    b.name.toLowerCase().includes(k) ||
    b.id.toLowerCase().includes(k)
  );
}