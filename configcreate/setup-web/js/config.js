let MC_VERSION_REPO = "https://raw.githubusercontent.com/PrismarineJS/minecraft-data/refs/heads/master/data/pc/common/versions.json";

function getMcVersion() {
  const el = document.getElementById("mcVersion");
  return el ? el.value : "1.21.1"; // fallback
}

let GIFT_CSV_URL =
  "https://raw.githubusercontent.com/maron-gt123/jamming-minecraft-stream/refs/heads/main/configcreate/setup-web/list/gifts.csv";

let COMMAND_CSV_URL =
  "https://raw.githubusercontent.com/maron-gt123/jamming-minecraft-stream/refs/heads/main/configcreate/setup-web/list/command.csv";

function getBlocksUrl() {
  return `https://raw.githubusercontent.com/PrismarineJS/minecraft-data/master/data/pc/${getMcVersion()}/blocks.json`;
}

function getLangUrl() {
  return `https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/${getMcVersion()}/assets/minecraft/lang/ja_jp.json`;
}

function getTextureBase() {
  return `https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/${getMcVersion()}/assets/minecraft/textures/block/`;
}

let gifts = [];
let BLOCKS = [];
let JA_LANG = {};
let commandMap = {};