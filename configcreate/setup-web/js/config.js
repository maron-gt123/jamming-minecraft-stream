let MC_VERSION = "1.20";

let MC_VERSION_REPO = "https://raw.githubusercontent.com/PrismarineJS/minecraft-data/refs/heads/master/data/pc/common/versions.json";

let GIFT_CSV_URL =
  "https://raw.githubusercontent.com/maron-gt123/jamming-minecraft-stream/refs/heads/main/configcreate/gifts.csv";

let COMMAND_CSV_URL =
  "https://raw.githubusercontent.com/maron-gt123/jamming-minecraft-stream/refs/heads/main/configcreate/command.csv";

function getBlocksUrl() {
  return `https://raw.githubusercontent.com/PrismarineJS/minecraft-data/master/data/pc/${MC_VERSION}/blocks.json`;
}

function getLangUrl() {
  return `https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/${MC_VERSION}/assets/minecraft/lang/ja_jp.json`;
}

function getTextureBase() {
  return `https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/${MC_VERSION}/assets/minecraft/textures/block/`;
}

let gifts = [];
let BLOCKS = [];
let JA_LANG = {};
let commandMap = {};