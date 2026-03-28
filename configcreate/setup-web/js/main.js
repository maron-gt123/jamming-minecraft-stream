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

window.addEventListener("DOMContentLoaded", init);