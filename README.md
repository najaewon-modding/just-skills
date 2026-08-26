# Just Skills

A NeoForge mod that lets players unlock skills through advancements and use a random unlocked skill every five minutes.

## Minecraft Version

* Minecraft: 26.1.2
* Mod Loader: NeoForge

## Features

* Unlock skills by completing advancements.
* Optional integration with BlazeandCave's Advancements Pack (BACAP).
* Receive one random unlocked skill every 5 minutes.
* Each skill can be used once during its 5-minute cycle.
* Skills are handled server-side for multiplayer compatibility.
* No Mixins are used initially.

## Current Status

This mod is currently in early development and mainly serves as a playground for experimenting with different skills and NeoForge features.

## Planned

* Random skill cycle
* Player skill data
* Skill activation key
* Skill HUD
* BACAP advancement integration
* More skills
* Optional advanced skill mechanics

## Design Principle

Use vanilla Minecraft and NeoForge APIs/events whenever possible.

Mixins will only be introduced when a skill cannot reasonably be implemented through the available APIs.
