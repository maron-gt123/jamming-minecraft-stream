import { showToast } from "./toast.js";

export async function exportOverlay(overlay, bgColor) {

  try {

    const imgs = overlay.querySelectorAll("img");

    await Promise.all([...imgs].map(img => {

      if (img.complete) {
        return Promise.resolve();
      }

      return new Promise(res => {
        img.onload = img.onerror = res;
      });
    }));

    const rect = overlay.getBoundingClientRect();

    const canvas = await html2canvas(overlay, {
      useCORS: true,
      allowTaint: false,
      backgroundColor: bgColor === "#ffffff" ? null : bgColor,
      width: rect.width,
      height: rect.height,
      scale: 2
    });

    const a = document.createElement("a");

    a.href = canvas.toDataURL("image/png");
    a.download = "overlay.png";
    a.click();

    showToast("画像を保存しました");

  } catch (e) {

    console.error(e);

    showToast("画像保存に失敗しました");
  }
}