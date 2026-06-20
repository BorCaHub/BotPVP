# BotPvP Mod ⚔️

Practice PvP against AI bots, solo or on a server — no internet required.  
Command: `/botpvp` | Platform: **Fabric** | Minecraft: **26.1.1 & 26.1.2**

---

## Requirements

| | |
|---|---|
| Minecraft Java Edition | **26.1.1** or **26.1.2** |
| [Fabric Loader](https://fabricmc.net/use/installer/) | `≥ 0.19.0` |
| [Fabric API](https://modrinth.com/mod/fabric-api) | any version for 26.1.x |
| Java | **25 or newer** |

---

## Installation (Step-by-Step)

### Step 1 — Install Fabric Loader

1. Go to **https://fabricmc.net/use/installer/**
2. Download and run the Fabric installer.
3. Select your Minecraft version (**26.1.1** or **26.1.2**) and click **Install**.
4. Open the Minecraft Launcher → select the new **fabric-loader** profile → click **Play** once to let it create folders, then close.

### Step 2 — Install Fabric API

1. Go to **https://modrinth.com/mod/fabric-api**
2. Download the version that matches your Minecraft version (26.1.x).
3. Place the downloaded `.jar` file in your **mods folder**:
   - **Windows:** `%AppData%\.minecraft\mods\`  
     *(Press Win+R, type `%AppData%\.minecraft\mods` and hit Enter)*
   - **macOS:** `~/Library/Application Support/minecraft/mods/`
   - **Linux:** `~/.minecraft/mods/`
4. Create the `mods` folder if it does not exist yet.

### Step 3 — Install BotPvP Mod

1. **Extract** the `botpvp-mod.zip` file you downloaded:
   - **Windows:** Right-click the ZIP → **Extract All…** → choose a folder → click **Extract**
   - **macOS:** Double-click the ZIP — it extracts automatically beside it
   - **Linux:** `unzip botpvp-mod.zip -d botpvp-mod`
2. Inside the extracted folder you will find a file named **`botpvp-1.0.0.jar`**.  
   *(If you are building from source, follow the **Build** section below first.)*
3. Copy **`botpvp-1.0.0.jar`** into your `mods` folder (same folder as Step 2).

### Step 4 — Launch Minecraft

1. Open the **Minecraft Launcher**.
2. Select the **fabric-loader-26.1.x** profile.
3. Click **Play**.
4. In-game, type `/botpvp help` to confirm the mod is working.

> The mod icon will be visible under **Mods** in the main menu (requires a mod menu mod like [Mod Menu](https://modrinth.com/mod/modmenu)).

---

## Commands

```
/botpvp                            → Show help
/botpvp help                       → Show help
/botpvp spawn [difficulty] [name]  → Spawn a MOVING bot (chases and attacks you)
/botpvp spawn static [diff] [name] → Spawn a STATIC bot (stands still, attacks if you come close)
/botpvp kill <name>                → Remove a specific bot by name
/botpvp killall                    → Remove all your bots
/botpvp list                       → List all your active bots
/botpvp heal [name]                → Heal a bot (leave name empty to heal all)
/botpvp info                       → Show mod version and active bot count
```

### Examples

```
/botpvp spawn                       → Moving bot, medium difficulty, auto name
/botpvp spawn hard                  → Moving bot, hard difficulty, auto name
/botpvp spawn nightmare GodSlayer   → Moving bot, nightmare, named GodSlayer
/botpvp spawn static easy           → Static bot, easy difficulty
/botpvp spawn static hard Dummy     → Static bot, hard difficulty, named Dummy
/botpvp kill Dummy                  → Remove bot named Dummy
/botpvp killall                     → Remove all your bots
/botpvp heal GodSlayer              → Heal GodSlayer to full HP
/botpvp heal                        → Heal ALL your bots
/botpvp list                        → See all your active bots and their status
```

---

## Bot Modes

| Mode | Description |
|------|-------------|
| **Moving** *(default)* | Bot chases you, attacks when close, teleports if you go too far away |
| **Static** | Bot stands exactly where it spawned; attacks if you walk within range |

Static bots are ideal for training your aim and timing without the bot moving.

---

## Difficulties

| Difficulty | HP | Damage | Armor | Weapon | Speed | 1.8 Combat |
|---|---|---|---|---|---|---|
| `easy` | 20❤ | 3❤/hit | Leather | Wooden Sword | Slow | ✗ |
| `medium` | 20❤ | 5❤/hit | Iron | Iron Sword | Normal | ✗ |
| `hard` | 20❤ | 7❤/hit | Diamond | Diamond Sword | Fast | ✓ |
| `nightmare` | 20❤ | 10❤/hit | Netherite | Netherite Sword | Very fast | ✓ |

**1.8 Combat** = spam clicking and block hitting enabled (Hard and Nightmare only).

---

## Bot Name Rules

Bot names follow the same rules as real **Minecraft Java/Bedrock usernames**:

- **3 to 16 characters** long
- **Letters (a–z, A–Z), numbers (0–9), and underscores (_) only**
- No spaces, no special characters

Examples of **valid** names: `Fighter`, `Bot_123`, `GodSlayer`, `PvP_Bot`  
Examples of **invalid** names: `ab` (too short), `MyBotWithAVeryLongName` (too long), `Bot-01` (dash not allowed)

The mod will show an error message if you enter an invalid name.

---

## Building from Source

**Requirements:**
- Java 25 or newer (`java -version` to check)
- Git (to clone, optional)

**Steps:**

```bash
# 1. Navigate into the extracted project folder
cd botpvp-mod

# 2. Build the mod (skips tests)
./gradlew build -x test

# On Windows use:
gradlew.bat build -x test

# 3. Find your built JAR here:
#    build/libs/botpvp-1.0.0.jar

# 4. Run a dev client to test in-game:
./gradlew runClient

# 5. Run a dev server to test server-side:
./gradlew runServer
```

The output JAR is in `build/libs/` — copy it to your `mods` folder.

---

## Project Structure

```
botpvp-mod/
├── src/main/java/com/botpvp/
│   ├── BotPvPMod.java            ← Mod entry point, registers commands & tick
│   ├── BotManager.java           ← Manages all active bots (spawn/kill/list/heal)
│   ├── BotPvPDataGenerator.java  ← Fabric data gen stub
│   ├── bot/
│   │   ├── PvPBot.java           ← Bot AI logic (moving / static modes, equipment)
│   │   └── BotEntity.java        ← Custom mob entity used by the bot
│   ├── commands/
│   │   └── BotPvPCommand.java    ← All /botpvp subcommands
│   └── mixin/
│       └── ServerPlayerMixin.java ← Notifies player about bots on death
├── src/main/resources/
│   ├── fabric.mod.json           ← Mod metadata (name, version, icon, MC versions)
│   ├── botpvp.mixins.json        ← Mixin config
│   └── assets/botpvp/
│       └── icon.png              ← Mod icon (shown in Mods screen)
├── build.gradle
├── gradle.properties
└── settings.gradle
```

---

## FAQ

**Q: My bot just stands there doing nothing (moving mode).**  
A: Make sure you are within 20 blocks of the bot. If further away, it will teleport to you every 2 seconds.

**Q: The bot name I typed was rejected.**  
A: Bot names follow Minecraft username rules — 3-16 chars, letters/numbers/underscore only. Try a name like `Fighter` or `Bot_01`.

**Q: I can't see the mod icon in-game.**  
A: You need a mod menu mod (e.g. [Mod Menu](https://modrinth.com/mod/modmenu)) installed to see the Mods screen.

**Q: Can I use this on a server?**  
A: Yes! Install on the server side. Players can each spawn up to 5 bots.

---

## License

MIT — free to use, modify, and redistribute.  
*Not affiliated with Mojang or Microsoft.*
