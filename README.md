# FishTycoon (Paper 1.21.8)

Tycoon-style fishing mode plugin for Minecraft Paper 1.21.8.

## Requirements
- Java 21
- Paper 1.21.8
- Vault + economy plugin (required)
- PlaceholderAPI / WorldGuard (optional)

## Build
```bash
mvn clean package
```

## Install
1. Build the jar using Maven.
2. Place jar into `plugins/`.
3. Install Vault and an economy plugin.
4. Start server to generate default configs.

## Features
- Starter rod and backpack on first join, both UUID-bound via PDC.
- `/fish rod` restoration with cooldown and protected backpack item.
- 25+ custom fish with rarity, non-uniform size roll, and dynamic value.
- 3 default locations with multiple hotspots and custom pools.
- Rod XP/levels + upgrade system.
- Fish are stored in backpack on catch and sold directly from backpack.
- Rebirth (prestige) flow with confirmation and permanent bonuses.
- Bestiary GUI with hidden uncaught fish.
- SQLite player storage with async load/save and in-memory session cache.

## Commands
### Player
- `/fish`
- `/fish rod`
- `/fish backpack`
- `/fish backpack upgrade`
- `/fish upgrades`
- `/fish bestiary`
- `/fish sell`
- `/fish locations`
- `/fish rebirth`
- `/fish rebirth confirm`

### Admin
- `/fish admin reload`
- `/fish admin givefish <player> <fishId> [size]`
- `/fish admin setlevel <player> <level>`
- `/fish admin addxp <player> <amount>`
- `/fish admin debug`

## Permissions
- `fishtycoon.use`
- `fishtycoon.sell`
- `fishtycoon.rebirth`
- `fishtycoon.admin.*`

## Config Files
- `config.yml` - progression, rebirth, multipliers, upgrade definitions
- `fish.yml` - custom fish definitions
- `locations.yml` - locations + hotspots
