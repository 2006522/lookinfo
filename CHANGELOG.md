# Changelog

## v1.1.0 (2026-10-27)

- **Lowest tier tool, with the actual item icon.** Instead of a bare "requires correct tool" line,
  it now shows which tool you need and what tier — `[⛏] Iron Pickaxe` for diamond ore,
  `[⛏] Stone Pickaxe` for iron ore. Copper tools included.
- **Crop growth stage.** Wheat, carrots, potatoes, beetroot, torchflower, pitcher crop, nether wart,
  cocoa and sweet berries show `Growth: 5 / 7` plus `Ready to harvest` / `Still growing`
  (green when ripe). Sugar cane, cactus and bamboo are excluded on purpose — their `age` is
  segment count, not ripeness.
- **English name first.** The title is now the name derived from the registry id
  (`iron_ore` → `Iron Ore`), with the localized name underneath in grey when it differs.
  Applies to blocks, mobs and crops.
- `show_tool` now controls the lowest-tool line.

## v1.0.0 (2026-10-27)

First release.

- **Block info**: name, source mod, hardness, "requires correct tool", container slot count.
- **Mob info**: name, source mod, health (red at 30% or below), armor.
- Panel sits at the top center of the screen; `I` toggles it.
- Everything is client-side — no server round-trips, works on vanilla servers.
- Config at `config/lookinfo.properties` to turn individual lines off.
