# Water Turbine

A NeoForge mod for Minecraft 1.21.1 (NeoForge `21.1.176`) — the same versions used by the **All the Mods 10** modpack.

Place the Water Turbine block directly into flowing or source water and it waterlogs itself, generating Forge Energy (FE) every tick and pushing it into any adjacent block that accepts FE (cables, machines, batteries, etc. from any FE-compatible mod — Mekanism, Thermal, Applied Energistics, Create-adjacent add-ons, and ATM10 itself).

## How it works

- **Placement**: right-click a water source or flowing water block with the Water Turbine item. It occupies that space and becomes waterlogged (same mechanic as a furnace or chest placed in water).
- **Generation**: while waterlogged, the block entity generates a flat amount of FE per tick into its internal buffer.
- **Output**: every tick it pushes as much energy as it can into any of the 6 adjacent blocks that expose an FE receiver capability.
- Breaking the turbine or removing the water it's waterlogged in stops generation.

Defaults (configurable in `config/waterturbine-common.toml` after first run):

| Setting | Default | Meaning |
|---|---|---|
| `generationRate` | 20 FE/t | Energy generated per tick while submerged |
| `energyCapacity` | 32,000 FE | Internal buffer size |
| `maxTransfer` | 200 FE/t | Max FE pushed to a single adjacent side per tick |

## Crafting

```
C C C
I P I
C C C
```
`C` = Copper Ingot, `I` = Iron Ingot, `P` = Piston → 1× Water Turbine

## Compatibility

Built against NeoForge `21.1.176` for Minecraft `1.21.1`. Works as a plain NeoForge mod on both dedicated servers and client instances (including modpacks like ATM10) — it has no client-only dependencies and doesn't require any other mod, since it uses NeoForge's built-in Forge Energy capability API directly.

## Building

Requires JDK 21 (Gradle itself can be run with JDK 17+; NeoGradle provisions a JDK 21 toolchain automatically for compiling/running the mod).

```
./gradlew build
```

The output jar is written to `build/libs/`.

## Development

```
./gradlew runServer   # dedicated server, headless
./gradlew runClient   # client with the mod loaded
./gradlew runData     # data generation (if/when generators are added)
```

## License

MIT — see [LICENSE](LICENSE).
