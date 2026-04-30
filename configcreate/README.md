# TikTok Stream config.yml Generator

## Overview

This tool is a web UI that allows you to easily generate a `config.yml` for Minecraft servers.  
It enables automatic handling based on TikTok stream events.  
➡ [Access the tool here](https://maron-gt123.github.io/jamming-minecraft-stream/configcreate/setup-web/)<br>
➡ [The preset table for the generated config can be created here](https://maron-gt123.github.io/jamming-minecraft-stream/configcreate/config-overlay/)<br>

Using this tool, you can configure the following:

- **Automatic BOX conversion settings**
    - Select Minecraft version
    - Choose Top / Middle / Bottom blocks (searchable)
- **Event settings**
    - Enable/disable Gift / Like / Follow / Share / Subscribe / Comment
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

Use the checkboxes to enable or disable each event (Gift / Like / Follow / Share / Subscribe / Comment).

#### Adding Commands

1. Click the "＋ command" button
2. Select a command from the dropdown
3. Enter options if needed (message, number, selection)
4. You can add multiple commands

#### commmand
|                       Command | Option | Description                                           | Option Details                   |
| ----------------------------: | ------ | ----------------------------------------------------- | -------------------------------- |
|             jammingevent text | msg    | Displays a message in the chat                        | Enter text in `msg`              |
|            jammingevent title | msg    | Displays a title in the center of the screen          | Enter text in `msg`              |
|              jammingevent tnt | Index  | Spawns TNT                                            | Specifies the number of spawns   |
|            jammingevent extnt | Index  | Spawns enhanced TNT                                   | Specifies the number of spawns   |
|            jammingevent reset | dragon | Destroys all blocks in the BOX using the Ender Dragon | -                                |
|            jammingevent reset | wither | Destroys all blocks in the BOX using the Wither       | -                                |
|             jammingevent fill | none   | Fills the BOX completely with blocks                  | -                                |
|        jammingevent fillblock | Index  | Fills the BOX with blocks up to a specified height    | Specifies the fill height        |
|           jammingevent prison | Index  | Traps the player                                      | Specifies the duration           |
|         jammingevent addclear | Index  | Increases the clear count                             | Specifies the increment amount   |
|         jammingevent delclear | Index  | Decreases the clear count                             | Specifies the decrement amount   |
|           jammingevent rocket | Index  | Launches firework rockets                             | Specifies the number of launches |
|          jammingevent creeper | Index  | Spawns creepers                                       | Specifies the number of spawns   |
|      jammingevent mob chicken | Index  | Spawns chickens                                       | Specifies the number of spawns   |
|          jammingevent mob pig | Index  | Spawns pigs                                           | Specifies the number of spawns   |
|        jammingevent mob sheep | Index  | Spawns sheep                                          | Specifies the number of spawns   |
|       jammingevent mob salmon | Index  | Spawns salmon                                         | Specifies the number of spawns   |
| jammingevent doubleplace time | Index  | Doubles block placement for a certain time            | Time is specified by `time`      |
|         jammingevent heightup | Index  | Increases the BOX height                              | Expands upward                   |
|       jammingevent heightdown | Index  | Decreases the BOX height                              | Shrinks downward                 |
|           jammingevent sizeup | Index  | Expands the BOX size                                  | Expands horizontally             |
|         jammingevent sizedown | Index  | Shrinks the BOX size                                  | Shrinks horizontally             |
|       jammingevent size_reset | Index  | Resets the BOX size                                   | Returns to initial size          |

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
