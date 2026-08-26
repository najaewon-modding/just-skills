# Just Skills

Just Skills is a NeoForge mod that gives the player one random skill at a time.

After using a skill, a five-minute cooldown begins. Once the cooldown has ended, press the skill key to receive a random available skill, then press it again to cast that skill.

## Requirements

- Minecraft 26.1.2
- NeoForge 26.1.2.97
- Java 25

## Skills

Just Skills currently includes seven skills:

- Crimson Wave
- Azure Wave
- Amber Disk
- Emerald Spikes
- Golden Flicker
- Indigo Orbit
- Violet Rift

## Controls

The default skill key is `G`.

When no skill is currently assigned and the cooldown has finished:

1. Press `G` to receive a random skill.
2. Press `G` again to begin casting it.
3. Successfully casting the skill consumes it and starts the five-minute cooldown.

Cancelling a cast does not consume the skill or start the cooldown.

## Casting

All current skills use a stationary cast.

Movement is restricted while casting, and moving too far from the starting position cancels the cast.

Skill activation and damage are handled by the server.

## Cooldown

The cooldown after a successful skill use is five minutes.

The HUD shows the currently assigned skill and the remaining cooldown state.

## Multiplayer

Skill selection, casting, cooldowns, and damage are server-authoritative.

No Mixins are used.

## Commands

`/justskills cast <skill>`

Immediately activates the selected skill for testing and administration.

This command bypasses the normal skill assignment, casting, and cooldown flow and requires Game Master permission.

## Skill Unlocking

The skill system supports unlock conditions, including advancement-based conditions.

The seven skills included in the current release are available by default.

## License

All Rights Reserved.