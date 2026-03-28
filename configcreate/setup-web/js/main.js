function changeLang(lang) {
    currentLang = lang;
    // 既存のスクリプト削除
    const oldScript = document.getElementById("langScript");
    if (oldScript) oldScript.remove();

    // 言語に応じてスクリプトパスを決定
    let src;
    switch(lang) {
        case "JP":
            src = "js/lang/lang_jp.js";break;
        case "EN":
            src = "js/lang/lang_en.js";
            break;
        case "ES":
            src = "js/lang/lang_es.js";
            break;
        case "FR":
            src = "js/lang/lang_fr.js";
            break;
        case "DE":
            src = "js/lang/lang_de.js";
            break;
        case "ZH":
            src = "js/lang/lang_zh.js";
            break;
        case "KO":
            src = "js/lang/lang_ko.js";
            break;
        default:
            src = "js/lang/lang_jp.js"; // デフォルトは日本語
    }
    // スクリプト生成して読み込み
    let langScript = document.createElement("script");
    langScript.src = src;
    langScript.id = "langScript";
    langScript.onload = () => applyText();
    document.body.appendChild(langScript);
}

// HTML に反映
function applyText() {
    document.getElementById("box_setting_title").textContent = TEXT.box_setting;
    document.getElementById("clear_countdown_title").textContent = TEXT.clear_countdown;
    document.getElementById("event_setting_title").textContent = TEXT.event_setting;
    document.getElementById("gift_setting_nonstreak_title").textContent = TEXT.gift_setting_nonstreak;
    document.getElementById("gift_setting_streak_title").textContent = TEXT.gift_setting_streak;
    document.getElementById("like_setting_title").textContent = TEXT.like_setting;
    document.getElementById("follow_setting_title").textContent = TEXT.follow_setting;
    document.getElementById("share_setting_title").textContent = TEXT.share_setting;
    document.getElementById("subscribe_setting_title").textContent = TEXT.subscribe_setting;
    document.getElementById("result_title").textContent = TEXT.result;
    document.getElementById("search_input").placeholder = TEXT.search;
}

function updateOptionUI(commandSelect) {
  const wrap =
    commandSelect.parentElement.querySelector(".gift-option-wrap")
    || commandSelect.parentElement.querySelector(".like-option-wrap")
    || commandSelect.parentElement.querySelector(".follow-option-wrap")
    || commandSelect.parentElement.querySelector(".share-option-wrap")
    || commandSelect.parentElement.querySelector(".subscribe-option-wrap")
    || commandSelect.parentElement.querySelector(".comment-option-wrap");
  if (!wrap) return;
  wrap.innerHTML = "";

  const cmd = commandSelect.value;
  if (!cmd || !commandMap[cmd]) return;

  const info = commandMap[cmd];

  if (info.type === "msg") {
    const input = document.createElement("input");
    input.type = "text";
    input.className = "option-msg";
    input.placeholder = "メッセージ";
    wrap.appendChild(input);
  }
  else if (info.type === "index") {
    const input = document.createElement("input");
    input.type = "number";
    input.className = "option-index";
    input.min = 1;
    wrap.appendChild(input);
  }

  else if (info.type === "select") {
    const sel = document.createElement("select");
    sel.className = "option-select";
    info.values.forEach(v => {
      const o = document.createElement("option");
      o.value = v;
      o.textContent = v;
      sel.appendChild(o);
    });
    wrap.appendChild(sel);
  }
  else if (info.type === "none") {
  }
}

function addGiftCommand(btn) {
  const gift = btn.closest(".gift");
  const commandsWrap = gift.querySelector(".gift-commands");
  const row = document.createElement("div");
  const sel = document.createElement("select");
  sel.className = "gift-command";
  sel.innerHTML = "<option value=''>コマンド選択</option>";
  Object.keys(commandMap).forEach(cmd => {
    const opt = document.createElement("option");
    opt.value = cmd;
    opt.textContent = cmd;
    sel.appendChild(opt);
  });
  const optionWrap = document.createElement("span");
  optionWrap.className = "gift-option-wrap";
  sel.onchange = () => updateOptionUI(sel);
  row.appendChild(sel);
  row.appendChild(optionWrap);
  commandsWrap.appendChild(row);
}

function toggleSection(header, id) {

  const el = document.getElementById(id);
  const arrow = header.querySelector(".arrow");

  if (el.style.display === "none") {
    el.style.display = "grid";
    arrow.textContent = "▼";
  } else {
    el.style.display = "none";
    arrow.textContent = "▶";
  }
}

function addLikeRule() {
  const div = document.createElement("div");
  div.className = "rule";
  div.innerHTML = `
    count <input type="number" class="like-count" min="1"><br><br>
    commands:<br>
    <div class="like-commands">
      <select class="like-command" onchange="updateOptionUI(this)"></select>
      <span class="like-option-wrap"></span>
    </div>
    <button onclick="addLikeCommand(this)">＋ command</button>
    <button onclick="this.parentElement.remove()">削除</button>
  `;
  document.getElementById("like-rules").appendChild(div);
  const sel = div.querySelector(".like-command");
  Object.keys(commandMap).forEach(cmd => {
    const opt = document.createElement("option");
    opt.value = cmd;
    opt.textContent = cmd;
    sel.appendChild(opt);
  });
  sel.value = "";
  populateCommandSelects();
}

function addEventCommand(type, btn) {
  const commandsWrap = btn.previousElementSibling;

  const row = document.createElement("div");

  const sel = document.createElement("select");
  sel.className = `${type}-command`;
  sel.innerHTML = "<option value=''>コマンド選択</option>";
  Object.keys(commandMap).forEach(cmd => {
    const opt = document.createElement("option");
    opt.value = cmd;
    opt.textContent = cmd;
    sel.appendChild(opt);
  });

  const optionWrap = document.createElement("span");
  optionWrap.className = `${type}-option-wrap`;

  sel.onchange = () => updateOptionUI(sel);

  row.appendChild(sel);
  row.appendChild(optionWrap);
  commandsWrap.appendChild(row);
}

function addLikeCommand(btn) {
  const commandsWrap = btn.previousElementSibling;

  const row = document.createElement("div");

  const sel = document.createElement("select");
  sel.className = "like-command";
  sel.innerHTML = "<option value=''>コマンド選択</option>";
  Object.keys(commandMap).forEach(cmd => {
    const opt = document.createElement("option");
    opt.value = cmd;
    opt.textContent = cmd;
    sel.appendChild(opt);
  });

  const optionWrap = document.createElement("span");
  optionWrap.className = "like-option-wrap";

  sel.onchange = () => updateOptionUI(sel);

  row.appendChild(sel);
  row.appendChild(optionWrap);
  commandsWrap.appendChild(row);
}

async function init() {
  loadBlocks();
  await loadCommands(); // ←待つ
  await loadGifts();    // ←その後
}

window.addEventListener("DOMContentLoaded", async () => {
    changeLang("JP");  // 言語を先にロード
    await init();      // ブロック・コマンド・ギフトのロード
});