// ===============================
// コマンドCSV読み込み＆UI反映
// ===============================
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

// ===============================
// コマンド文字列生成
// ===============================
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

// ===============================
// コマンドセレクトUI更新
// ===============================
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