## SkinMorph
A Minecraft plugin that allows seamless skin changing, featuring a modern GUI and advanced features.

# `/skin` Command Usage

The `/skin` command lets players change, reset, favorite, and manage skins. All subcommands require the `skinmorph.command.skin` permission.

## Usage

| Command | Description |
|---|---|
| `/skin` | Opens the skin selection menu. **Player only.** |
| `/skin set <skinName>` | Sets your own skin to `<skinName>`. **Player only.** |
| `/skin set <skinName> <targetName>` | Sets `<targetName>`'s skin to `<skinName>`. |
| `/skin default` | Resets your own skin to default. **Player only.** |
| `/skin default <targetName>` | Resets `<targetName>`'s skin to default. |
| `/skin random` | Applies a random skin to yourself. **Player only.** |
| `/skin random <targetName>` | Applies a random skin to `<targetName>`. |
| `/skin favorites add <skinName>` | Adds `<skinName>` to your favorites list. **Player only.** |
| `/skin favorites remove <skinName>` | Removes `<skinName>` from your favorites list. **Player only.** |
| `/skin settings` | Opens the settings menu. **Player only.** |
| `/skin cache clear` | Clears the entire skin cache. |
| `/skin cache remove <skinName>` | Removes a specific skin from the cache. |
| `/skin cache info` | Displays cache statistics (total skins cached, cache size). |
| `/skin update` | Update your active skin |
| `/skin update <targetName>` | Update another player's active skin |
| `/skin help [page]` | Displays the help menu. Defaults to page 1 if no page is given. |

> **Player only** commands will respond with an error message if run from console or another non-player sender.

---

## Subcommand Details

### `/skin`
Opens the skin selection GUI menu, resuming on whatever page the player last had open.

### `/skin set`
- `/skin set <skinName>` — Applies `<skinName>` to yourself.
- `/skin set <skinName> <targetName>` — Applies `<skinName>` to another online player. Fails with a "player-not-found" message if `<targetName>` isn't online.

### `/skin default`
- `/skin default` — Resets your skin to the default.
- `/skin default <targetName>` — Resets another online player's skin. Fails if `<targetName>` isn't online.

### `/skin random`
- `/skin random` — Applies a randomly selected skin to yourself.
- `/skin random <targetName>` — Applies a random skin to another online player. Fails if `<targetName>` isn't online.

### `/skin favorites`
- `add <skinName>` — Adds a skin to your favorites.
- `remove <skinName>` — Removes a skin from your favorites.

Both actions are player-only.

### `/skin settings`
Opens the plugin's settings menu for the executing player.

### `/skin cache`
- `clear` — Wipes the entire skin cache.
- `remove <skinName>` — Removes one skin from the cache. Responds with whether it was actually cached.
- `info` — Shows the number of skins cached and total cache size.

### `/skin update`
- `/skin update` — Updates your own active skin, re-fetching it from Mojang's servers.
- `/skin update <targetName>` — Updates another player's active skin, re-fetching it from Mojang's servers. Fails if `<targetName>` isn't online.

### `/skin help [page]`
Displays a paginated help menu.

- If `<page>` is omitted, page 1 is shown.
- If `<page>` isn't a valid number, an "invalid-help-page" error is shown.
- If `<page>` doesn't exist, an "invalid-help-page" error is shown.

**Player view:** Clickable/hoverable entries — clicking suggests the command in chat, hovering shows the description. Includes `←` / `→` navigation buttons between pages.

**Console/non-player view:** Plain text list of `command - description` pairs.

**Help pages:**

| Page | Contents |
|---|---|
| 1 | `/skin`, `/skin set <skinName>`, `/skin set <skinName> <targetName>`, `/skin default`, `/skin default <targetName>` |
| 2 | `/skin random`, `/skin random <targetName>`, `/skin favorites add <skinName>`, `/skin favorites remove <skinName>`, `/skin settings` |
| 3 | `/skin cache clear`, `/skin cache remove <skinName>`, `/skin cache info`, `/skin update`, `/skin update <targetName>` |

---

## Tab Completion

| Argument position | Context | Suggestions |
|---|---|---|
| 1st arg | always | `set`, `default`, `help`, `favorites`, `random`, `settings`, `cache`, `update` |
| 2nd arg | `set` | Online player names + your favorited skins |
| 2nd arg | `random`, `default`, `update` | Online player names |
| 2nd arg | `favorites` | `add`, `remove` |
| 2nd arg | `help` | `1`, `2`, `3` |
| 2nd arg | `cache` | `clear`, `remove`, `info` |
| 3rd arg | `set <skinName> ...` | Online player names |
| 3rd arg | `favorites add ...` | Online player names + your current skin (if not already favorited) |
| 3rd arg | `favorites remove ...` | Your favorited skins |
| 3rd arg | `cache remove ...` | Your favorited skins* |

---

## Argument Limits

| Subcommand | Valid argument counts |
|---|---|
| *(none)* | 0 |
| `set` | 2 or 3 |
| `default` | 1 or 2 |
| `random` | 1 or 2 |
| `favorites` | 3 (with `add` or `remove` as 2nd arg) |
| `help` | 1 or 2 |
| `settings` | 1 |
| `cache` | 2 (`clear`/`info`) or 3 (`remove`) |
| `update` | 1 or 2 |

Any other combination (including unknown first arguments, or more than 3 total arguments) returns a usage error.

## Permission

| Permission | Applies to |
|---|---|
| `skinmorph.command.skin` | All `/skin` subcommands |

~ Developed with 💚 by Luvoos
