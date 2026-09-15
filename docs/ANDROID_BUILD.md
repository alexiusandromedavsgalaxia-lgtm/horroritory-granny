# Android APK build plan

The repository is currently a specification scaffold. The game implementation should use an Android-capable game engine and produce a signed release APK through a reproducible build pipeline.

Required project layers:
1. Boot and settings
2. Input abstraction for touch controls
3. Procedural/randomized level manager
4. Player controller
5. Interaction and inventory system
6. Puzzle/lock state machine
7. Hunter AI and perception
8. Rat ambient AI
9. Dynamic audio manager
10. Lighting and AE visibility system
11. Ending manager
12. Save/settings layer
13. Android packaging and release configuration

The final APK should support offline single-player play, adaptive graphics, touch controls, safe-save checkpoints, and deterministic debugging seeds.

Do not package copyrighted Granny assets, sounds, models, textures, maps, or source code. Use original equivalents.
