# Kust Clock

Kust Clock is a free and open-source chess clock for Android, designed for real-life, face-to-face board games. Whether you are playing blitz at a café or a long tournament game at home, put a phone between the two players and you have a fully featured game timer.

Current version: **2.0** (`versionCode 2`).

## Features

- **Ready-to-use presets** — one-tap access to the most common time controls: 1|0, 1|1, 2|1, 3|0, 3|2, 5|0, 5|3, 10|0, 10|5, 15|10, 30|0, 30|20, 60|30 and a multi-stage tournament control (2 hr for 40 moves + 1 hr).
- **English + Russian, with auto-detect** — the app follows the device language out of the box (Russian device → Russian, everything else → English). Override anytime in App Settings → Language (System default / English / Русский).
- **Custom time controls** — create your own controls with a name, base time (hours/minutes/seconds) and increment:
  - **Fischer** — players receive the full increment at the end of each turn.
  - **Bronstein** — players receive the used portion of the increment at the end of each turn.
  - **Delay** — the player's clock starts after the delay period.
  - **None** — no extra time is added.
- **Multi-stage time controls** — build tournament-style controls with up to three stages, each with its own move count and duration (for example "40 moves in 2 hours + game in 60 minutes"). Stage markers on the clock show the current stage at a glance.
- **Per-player handicap** — assign a different time control to each player, or keep both sides identical with "Same as Player One".
- **Face-to-face design** — the top player's panel is rotated 180° so both players read their clock upright.
- **Accurate timing** — the engine is based on the system's monotonic clock, so time is kept accurately even if the app is interrupted. The clock pauses automatically when the app goes to the background.
- **Adjust time mid-game** — add or remove time for either player without stopping the game.
- **Sounds** — distinct switch sounds for each player, plus pause, reset and flag-fall alerts, with a quick mute toggle right on the clock screen.
- **Customizable theme color** — six accent colors applied live across the whole app.
- **Light and dark themes** — follows your system's dark mode setting, with careful contrast in both.
- **Adaptive app icon** — themed monochrome icon support on Android 13+, with light and dark variants.
- **Keep-screen-on** — the display stays awake while a game is running.

## Screenshots

| Time Controls | Clock | Custom Time | App Settings |
|---|---|---|---|

_Screenshots will be added in a future release._

## Getting Started

### Requirements

- Android Studio (Ladybug or newer recommended)
- JDK 17+
- Android SDK with compile SDK 37

### Build and run

```bash
git clone https://github.com/Shapak-Apps/Kust-Clock.git
cd Kust-Clock
./gradlew assembleDebug
```

Then install `app/build/outputs/apk/debug/app-debug.apk` on a device, or simply open the project in Android Studio and press **Run**.

## Project Structure

```
app/src/main/java/com/shapakapps/kustclock/
├── MainActivity.kt          # Entry point
├── audio/SoundManager.kt    # SoundPool wrapper for the clock sounds
├── engine/ClockEngine.kt    # Clock state machine: turns, increments, stages, flags
├── model/                   # Time controls, presets and repository
├── storage/                 # Persistence (custom controls + app preferences)
├── ui/                      # Navigation shell and Compose screens
└── util/                    # Time formatting, locale and haptics helpers
```

The UI is built entirely with **Jetpack Compose** and **Material 3**. There are no third-party dependencies. Strings live in `app/src/main/res/values/strings.xml` (English) and `app/src/main/res/values-ru/strings.xml` (Russian); per-app language is handled by `util/LocaleHelper` + `MainActivity.attachBaseContext()` with a user override stored in `AppPreferences.language`.

## Sounds

The app ships with five sounds, located in `app/src/main/res/raw/`:

| File | Purpose |
|---|---|
| `kust_switch1.wav` | Player one presses their clock |
| `kust_switch2.wav` | Player two presses their clock |
| `kust_time_ended.wav` | Flag fall — a player ran out of time |
| `kust_pause.wav` | Pause pressed |
| `kust_reset.wav` | Clock reset |

The bundled files are simple placeholder tones. To personalize the app, replace them with your own audio files **using the same file names** — no code changes are required.

## Roadmap

- [ ] Landscape layout
- [ ] Move history and PGN-style export
- [ ] More sound packs
- [x] Translations (English + Russian shipped in v2.0)
- [ ] Wear OS companion

## Changelog

- **2.0** — Russian language support with device-language auto-detect and a manual Language setting; all UI strings, presets and summaries localized.

## Contributing

Contributions are welcome. If you would like to help:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/my-feature`)
3. Commit your changes (`git commit -am 'Add my feature'`)
4. Push to the branch (`git push origin feature/my-feature`)
5. Open a pull request

Please keep PRs focused, and open an issue first if you plan a larger change.

## License

This project is licensed under the **GNU General Public License v3.0** — see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- The user interface of Kust Clock is inspired by the excellent open-source [Chess Clock](https://github.com/chesscom/android-chessclock) by Chess.com (MPL-2.0). Kust Clock is an independent implementation and shares no code or assets with that project.
- Thanks to everyone in the over-the-board chess community for the feedback that shaped this app.
