function generateYaml() {
  const y = [];

  y.push("http:");
  y.push("  port: 8080");
  y.push("  path: /event\n");

  y.push("events:");
  ["gift","like","follow","share","subscribe","comment"].forEach(e => {
    y.push(`  ${e}:`);
    y.push(`    enabled: ${document.getElementById("ev_"+e).checked}`);
  });

  y.push("\njammingbox:");
  const countdown = document.getElementById("clear_countdown").value || 15;
  y.push("  clear:");
  y.push(`    countdown: ${countdown}`);
  y.push("  replace:");
  y.push("    enabled: true");
  y.push(`    bottom: ${block_bottom.value}`);
  y.push(`    middle: ${block_middle.value}`);
  y.push(`    top: ${block_top.value}\n`);

  y.push("\ncommands:");
  // like
  y.push("  like:");
  document.querySelectorAll("#like-rules .rule").forEach(rule => {
    const count = rule.querySelector(".like-count").value;
    const cmds = Array.from(rule.querySelectorAll(".like-command"))
      .map(buildCommand)
      .filter(v => v);

    y.push(`    - count: ${count}`);
    if (cmds.length === 1) {
      y.push(`      command: "${cmds[0]}"`);
    } else if (cmds.length > 1) {
      y.push(`      commands:`);
      cmds.forEach(c => {
        y.push(`        - "${c}"`);
      });
    }
  });

  // follow / share / subscribe
  ["follow", "share", "subscribe", "comment"].forEach(e => {
    y.push(`  ${e}:`);

    const cmds = Array.from(
      document.querySelectorAll(`.${e}-command`)
    ).map(buildCommand).filter(v => v);

    if (cmds.length === 1) {
      y.push(`    - command: "${cmds[0]}"`);
    } else if (cmds.length > 1) {
      y.push(`    - commands:`);
      cmds.forEach(c => {
        y.push(`        - "${c}"`);
      });
    }
  });

  // gift
  y.push("  gift:");
  document.querySelectorAll(".gift").forEach(d => {
    if (!d.querySelector(".gift-enable").checked) return;

    y.push(`    - gift_name: "${d.dataset.name}"`);

    const cmds = Array.from(d.querySelectorAll(".gift-command"))
      .map(buildCommand)
      .filter(v => v);

    if (cmds.length === 1) {
      y.push(`      command: "${cmds[0]}"`);
    } else if (cmds.length > 1) {
      y.push(`      commands:`);
      cmds.forEach(c => {
        y.push(`        - "${c}"`);
      });
    }
  });

  output.value = y.join("\n");
}