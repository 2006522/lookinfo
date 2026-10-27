# LookInfo

Shows what you are looking at — a lightweight, client-side alternative for snapshots where
[Jade](https://modrinth.com/mod/jade) hasn't caught up yet.

Aim at a block or a mob and a small panel appears at the top of the screen.

**Block**

- In-game name, with the English name (derived from the registry id, `iron_ore` → `Iron Ore`)
  underneath in grey when it differs
- Source mod — only shown for modded blocks, so vanilla blocks stay clean
- **Which tool to use, with the item icon, plus a green `✓` / red `✗` for whether the tool
  you're holding will do.** Blocks that *require* a tool show the lowest tier that works
  (`[⛏] ✓ Min. tool: Iron Pickaxe` for diamond ore); blocks that drop by hand but are faster
  with a tool show the tool class instead (`[⛏] ✓ Any pickaxe`)
- **Crop growth stage** — `Growth: 5 / 7` plus `Ready to harvest` / `Still growing`
- Container slot count

**Mob**

- In-game name + English name underneath
- Source mod (modded entities only)
- Health `current / max` — turns red at 30% or below
- Armor, when it has any

---

## Requirements

| | |
| --- | --- |
| Minecraft | 26.4-snapshot-5 |
| Fabric Loader | 0.19.5+ |
| Fabric API | required |

**Client-side only.** Nothing is sent to the server, so you can join vanilla servers with it.

## Install

1. Install [Fabric Loader](https://fabricmc.net/use/) and [Fabric API](https://modrinth.com/mod/fabric-api);
2. Drop `LookInfo-<version>.jar` into `mods/`;
3. Launch.

> Only one copy of the same mod id can exist. Delete the old jar before upgrading.

## Usage

Just look at something. Press `I` to toggle the panel on and off (rebindable in
Options → Controls → Key Binds).

## Config

`config/lookinfo.properties`, pick what to show:

```properties
# Master switch (the I key writes this back)
enable=true

# Distance from the top of the screen, in GUI pixels
offset_y=6

# Show which mod the block/entity comes from
show_source=true

# Lowest tier tool needed to harvest the block (with icon)
show_tool=true

# Container slot count
show_container=true

# Mob health / armor
show_health=true
```

## Known limitations

- **Container contents are not shown.** Minecraft only syncs a container's inventory to the
  client while the screen is open, so outside of that the client genuinely doesn't know what's
  inside. Only the slot count is available without a server round-trip.
- Only the thing your crosshair is currently on — no "what does this drop", no NBT viewer.

## Why not just use Jade?

Jade 26.3 can't run on 26.4-snapshot-5 and can't be patched into working: `Mth.clamp` was removed
from the game, `AbstractTexture` was replaced by `TextureHandle`, `CycleButton` was refactored, and
`Renderable#extractRenderState` lost its mouse-coordinate parameters. Those are in Jade's rendering
core, so it needs a real port rather than a patch. This mod covers the common case in the meantime.

## License

[MIT](LICENSE)
