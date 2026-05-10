export function showToast(text) {
  const toast = document.getElementById("toast");

  toast.textContent = text;
  toast.classList.add("show");

  setTimeout(() => {
    toast.classList.remove("show");
  }, 1200);
}