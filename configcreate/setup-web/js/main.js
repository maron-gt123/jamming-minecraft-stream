fetch(CSV_URL)
  .then(r => r.text())
  .then(parseCSV);

function parseCSV(text) {
  const lines = text.trim().split("\n");
  const header = lines.shift().split(",");
  gifts = lines.map(l => {
    const c = l.split(",");
    const o = {};
    header.forEach((h,i)=>o[h]=c[i]);
    return o;
  });
  renderGifts();
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
          <select class="gift-command" onchange="updateOptionUI(this)"></select>
          <span class="gift-option-wrap"></span>
        </div>
        <button onclick="addGiftCommand(this)">＋ command</button>
      </div>
    `;

    (isStreak ? yes : non).appendChild(d);
  });
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

function buildCommand(select) {
  if (!select.value) return null;
  const wrap =
    select.parentElement.querySelector(".gift-option-wrap")
    || select.parentElement.querySelector(".like-option-wrap")
    || select.parentElement.querySelector(".follow-option-wrap")
    || select.parentElement.querySelector(".share-option-wrap")
    || select.parentElement.querySelector(".subscribe-option-wrap")
    || select.parentElement.querySelector(".comment-option-wrap");
  if (!wrap) return select.value;
  const msg = wrap.querySelector(".option-msg");
  if (msg) return `${select.value} ${msg.value}`;
  const index = wrap.querySelector(".option-index");
  if (index) return `${select.value} ${index.value}`;
  const sel = wrap.querySelector(".option-select");
  if (sel) return `${select.value} ${sel.value}`;
  return select.value;
}

function populateCommandSelects() {
  document.querySelectorAll(
    ".gift-command, .like-command, .follow-command, .share-command, .subscribe-command, .comment-command"
  ).forEach(sel => {
    const current = sel.value;
    sel.innerHTML = "<option value=''>コマンド選択</option>";
    Object.keys(commandMap).forEach(cmd => {
      const opt = document.createElement("option");
      opt.value = cmd;
      opt.textContent = cmd;
      sel.appendChild(opt);
    });
    sel.value = current;
    sel.onchange = () => updateOptionUI(sel);
  });
}

function loadCommands() {
  fetch(COMMAND_CSV_URL)
    .then(r => r.text())
    .then(text => {
      commandMap = {};
      const lines = text.trim().split("\n").slice(1);

      lines.forEach(l => {
        const [cmd, opt] = l.split(",");
        const c = cmd.trim();
        const o = opt.trim();

        if (o.toLowerCase() === "msg") {
          commandMap[c] = { type: "msg" };
        }
        else if (o.toLowerCase() === "index") {
          commandMap[c] = { type: "index" };
        }
        else if (o.toLowerCase() === "none") {
          commandMap[c] = { type: "none" };
        }
        else {
          if (!commandMap[c]) {
            commandMap[c] = { type: "select", values: [] };
          }
          commandMap[c].values.push(o);
        }
      });

      populateCommandSelects();

      ["follow", "share", "subscribe", "comment"].forEach(e => {
        const sel = document.getElementById("cmd_" + e);
        sel.innerHTML = "<option value=''>コマンド選択</option>";
        Object.keys(commandMap).forEach(cmd => {
          const opt = document.createElement("option");
          opt.value = cmd;
          opt.textContent = cmd;
          sel.appendChild(opt);
        });
      });
    });
}

loadBlocks();
loadCommands();
populateCommandSelects();
