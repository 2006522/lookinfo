# Changelog

## v1.0.0 (2026-10-27)

First release.

- **Block info**: name, source mod, hardness, "requires correct tool", container slot count.
- **Mob info**: name, source mod, health (red at 30% or below), armor.
- Panel sits at the top center of the screen; `I` toggles it.
- Everything is client-side — no server round-trips, works on vanilla servers.
- Config at `config/lookinfo.properties` to turn individual lines off.
