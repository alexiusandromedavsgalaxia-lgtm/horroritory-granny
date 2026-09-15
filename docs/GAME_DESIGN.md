# Horroritory Granny - Game Design

## Core loop
Explore a large multi-room house, find randomized items and keys, solve environmental puzzles, avoid the hunter AI, unlock an escape route, and reach an ending. Item locations, selected locks, ambient events, and optional encounters are randomized each run.

## Difficulty matrix

| Mode | Player speed | Hunter speed | Visibility | Hunter count | House instability | Rats |
|---|---:|---:|---:|---:|---:|---|
| Easy | 1.0x | 0.70x | 100% | 1 | 10% | No |
| Normal | 1.0x | 1.0x | 100% | 1 | 20% | No |
| Hard | 1.0x | 1.15x | 90% | 1 | 35% | Rare |
| Extreme | 1.0x | 1.30x | 75% | 1 | 50% | Yes |
| Impossible | 1.0x | 1.50x | 65% | 1 | 60% | Yes |
| Crazy | 1.0x | 2.0x | 45% | 2 | 70% | Yes |
| Super Crazy | 1.0x | 2.5x | 25% | 2 | 80% | Yes |
| AE [❔] | 1.0x | 3.0x base | 10% | 2-3 | 80%+ | Yes |

AE is deliberately chaotic. The player has a very small visible area while the hunter AI retains full environmental awareness through a separate perception system. Multiple hunter instances coordinate through shared search events. Rats are ambient AI creatures that can trigger sounds and distractions.

## House
Original multi-floor horror house with basement, ground floor, upper floor, attic, hidden maintenance spaces, exterior escape area, and a randomized secret area. Doors can be locked, jammed, trapped, or opened through puzzle states.

## AI
Hunter AI has patrol, investigate-noise, search-last-known-position, chase, lose-target, regroup, and dynamic route-selection states. Difficulty changes perception, reaction time, path choice, movement speed, and coordination rather than simply spawning enemies.

## Sound
Procedural ambient system for floorboards, doors, distant impacts, plumbing, wind, object interactions, and randomized house creaks. AE substantially increases the probability and intensity of creak events without relying on copyrighted audio.

## Items and puzzles
Keys, tools, batteries, fuses, codes, locks, movable objects, hiding spots, and multi-stage escape mechanisms are randomized within validated puzzle graphs so every run remains solvable.

## Endings
Each main difficulty has its standard escape ending plus a randomized alternative ending. The alternative ending is intentionally absurd and unexpected: after completing a hidden trigger chain, the player is transported to a randomly selected original location such as a deserted supermarket aisle, an empty cinema projection room, a rooftop greenhouse, a closed train carriage, or a bizarre maintenance corridor. The exact destination and final sequence are selected at runtime.

## Mobile controls
Virtual joystick, look drag, interact button, crouch, sprint, inventory, pause, accessibility options, sensitivity controls, graphics presets, subtitles, and vibration toggle.

## Performance target
Android-first architecture targeting stable 30 FPS on low-end devices and 60 FPS on capable devices. Dynamic quality scaling controls shadows, view distance, particles, audio voices, and post-processing.
