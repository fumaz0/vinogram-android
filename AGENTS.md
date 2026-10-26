# Vinogram Android

Rebranded fork of Telegram for Android (upstream: `drklo/telegram`).

## Workflow rule

After **every** change: commit and push immediately to `origin`
(https://github.com/fumaz0/vinogram-android). Do not leave work uncommitted.

## What was rebranded

- App name: `R.string.AppName` / `R.string.AppNameBeta` -> "Vinogram".
- Default theme is purple: `Theme.java` ("Blue" theme accents) and
  `assets/bluebubbles.attheme` (recolored with the app's own accent algorithm).
- Launcher icons: `mipmap-*/ic_launcher*.png`, `icon_foreground*.png`,
  monochrome `drawable-nodpi/vinogram_icon_mono.png`.

## Do not rename

Package names (`org.telegram.*`), protocol strings and URLs (`t.me`,
`telegram.me`, `tg://`, `telegram.org`), JS bridges, MIME types, intent
actions, resource names - renaming them breaks the build or the connection
to Telegram servers.

## Build

See README.md. Requires Android SDK 36, NDK 27.2.12479018, filled
`BuildVars.java` and `google-services.json`.