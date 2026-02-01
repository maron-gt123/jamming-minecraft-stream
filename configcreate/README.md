# TikTok Stream config.yml Generator

## Overview

This tool is a web UI that allows you to easily generate a `config.yml` for Minecraft servers.  
It enables automatic handling based on TikTok stream events.  
➡ [Access the tool here](https://maron-gt123.github.io/jamming-minecraft-stream/configcreate/)

Using this tool, you can configure the following:

- **Automatic BOX conversion settings**
    - Select Minecraft version
    - Choose Top / Middle / Bottom blocks (searchable)
- **Event settings**
    - Enable/disable Gift / Like / Follow / Share / Subscribe
    - Configure commands for each event (multiple commands allowed)
- **Gift settings**
    - Manage streakable / non-streakable gifts
    - Add commands for each gift (multiple commands allowed)
- **Like settings**
    - Add conditional rules based on count
    - Assign commands for each rule
- **YAML generation**
    - Output settings in `config.yml` format

---

## How to Use

### 1. Select Minecraft Version

Choose the Minecraft version to use from the dropdown at the top of the page.  
Block information and textures will update automatically according to the selected version.

---

### 2. Configure BOX Blocks

- You can set **Top / Middle / Bottom** blocks
- Enter a block name in the search box to filter the list of options
- Selecting a block from the dropdown will display its image on the right

---

### 3. Event Settings

Use the checkboxes to enable or disable each event (Gift / Like / Follow / Share / Subscribe).

#### Adding Commands

1. Click the "＋ command" button
2. Select a command from the dropdown
3. Enter options if needed (message, number, selection)
4. You can add multiple commands

---

### 4. Gift Settings

- **Non-streakable gifts** are shown under `Gifts (Non-Streakable)`
- **Streakable gifts** are shown under `Gifts (Streakable)`
- Toggle each gift with the checkbox to enable/disable
- Click "＋ command" to add commands for the gift
- For streakable gifts, you can set the repeat count

---

### 5. Like Settings (count-based)

- Click "＋ Add Rule" to create a new rule
- Enter a value for `count`
- Assign commands for each rule
- Remove unnecessary rules with the "Delete" button

---

### 6. YAML Generation

- Click the "Generate YAML" button at the bottom
- The tool generates the `config.yml` based on your settings and displays it in the textarea below
- Copy the content and apply it to your Minecraft server

---

## Notes

- Commands and gift information are loaded from CSV files in the repository
- Block textures and language data are fetched from the official Minecraft asset repository
- Always review the generated YAML before applying it to the server

---

## License

You are free to use and modify this tool, but please comply with the usage terms of official Minecraft assets and external CSV files.
