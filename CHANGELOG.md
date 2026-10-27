# Changelog

## v1.1.0 (2026-10-27)

- **Lowest tier tool, with the actual item icon.** Instead of a bare "requires correct tool" line,
  it now shows which tool you need and what tier — `[⛏] Iron Pickaxe` for diamond ore,
  `[⛏] Stone Pickaxe` for iron ore. Copper tools included.
- **Crop growth stage.** Wheat, carrots, potatoes, beetroot, torchflower, pitcher crop, nether wart,
  cocoa and sweet berries show `Growth: 5 / 7` plus `Ready to harvest` / `Still growing`
  (green when ripe). Sugar cane, cactus and bamboo are excluded on purpose — their `age` is
  segment count, not ripeness.
- **English name follows the localized one.** Title is the in-game name, the derived English name
  sits underneath in grey when it differs (`石头` → `Stone`). Applies to blocks, mobs and crops.
- **The lowest-tool line now shows whether the tool in your hand works** — green `✓` / red `✗`,
  so you can see at a glance that a wooden pickaxe won't cut diamond ore.
- `show_tool` now controls the lowest-tool line.
- Hardness line removed, and the whole panel renders at 85% scale.

## v1.0.0 (2026-10-27)

First release.

- **Block info**: name, source mod, hardness, "requires correct tool", container slot count.
- **Mob info**: name, source mod, health (red at 30% or below), armor.
- Panel sits at the top center of the screen; `I` toggles it.
- Everything is client-side — no server round-trips, works on vanilla servers.
- Config at `config/lookinfo.properties` to turn individual lines off.
