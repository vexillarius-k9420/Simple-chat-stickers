# Simple Chat Stickers
**v1.0.4**

| Minecraft: Java Edition | Platforms | Supported environments |
|:-----------------------:|:---------:|:----------------------:|
|    1.20.x - 1.21.x      |  Fabric   |      Client-side       |

## Description:

**This mod adds stickers to the in-game chat, just like those in messaging apps like *Telegram* or *WhatsApp*. You can also create and download custom sticker packs!**

> **Note:** For other players to see your stickers, they must have the same sticker pack installed.

---

![demonstration of stickers and menus](https://cdn.modrinth.com/data/cached_images/2289afee641c26f7bb3d02804ba90320d7950aa2.png)

---

<details>
<summary>How to install a sticker pack:</summary>

Sticker packs are `.zip` archives that need to be placed in the **"sticker-packs"** folder inside your main game directory.

```
.minecraft/sticker-packs/ [packs... (.zip)]
```

---
</details>

<details>
<summary>How to create your own sticker pack:</summary>

The ZIP archive must contain a `pack.json` file that defines the pack's name and all included stickers.
Sticker images (`.png`) can be organized inside the archive however you like, just make sure to specify the correct relative paths to them in the `pack.json` file.

### `pack.json` Structure:

``` json
{
  "name": "[sticker pack name]",
  "stickers": {
    "[sticker code]": "[relative path to the sticker image]"
  }
}
```

`"sticker code"` is the sticker's unique identifier (it can be a word or a random set of characters). It acts as a text fallback in chat for players who do not have this mod or this specific sticker pack installed.

### Example of a `pack.json` file:

``` json
{
  "name": "Cool Stickers",
  "stickers": {
    "happy": "happy.png",
    "cat": "animals/cat.png"
  }
}
```
</details>

---
## Links:
![Modrinth Badge](https://img.shields.io/modrinth/dt/simple-chat-stickers?logo=modrinth&label=&suffix=%20&style=flat&color=242629&labelColor=5CA424&logoColor=1C1C1C)
**Modrinth:** https://modrinth.com/mod/simple-chat-stickers
