// ===============================
// MCバージョン一覧を動的取得
// ===============================
async function loadMcVersions() {
  try {
    const res = await fetch(MC_VERSION_REPO);
    const versions = await res.json();

    const select = document.getElementById("mcVersion");
    select.innerHTML = "";

    // lts only
    const stableVersions = versions.filter(v => {
      return !v.includes("pre") &&
             !v.includes("rc") &&
             !v.includes("snapshot") &&
             !v.includes("w");
    });

    stableVersions.sort((a, b) => compareVersions(b, a));

    stableVersions.forEach((ver, index) => {
      const opt = document.createElement("option");
      opt.value = ver;
      opt.textContent = ver;

      // デフォルト選択（1.21.1優先）
      if (ver === "1.21.1" || index === 0) {
        opt.selected = true;
      }

      select.appendChild(opt);
    });

    // 初期バージョン反映（既存処理に合わせる）
    changeVersion();

  } catch (e) {
    console.error("MCバージョン取得失敗", e);
  }
}

// ===============================
// バージョン比較（ソート用）
// ===============================
function compareVersions(a, b) {
  const pa = a.split('.').map(Number);
  const pb = b.split('.').map(Number);

  for (let i = 0; i < Math.max(pa.length, pb.length); i++) {
    const na = pa[i] || 0;
    const nb = pb[i] || 0;
    if (na !== nb) return na - nb;
  }
  return 0;
}

window.addEventListener("DOMContentLoaded", () => {
  loadMcVersions();
});