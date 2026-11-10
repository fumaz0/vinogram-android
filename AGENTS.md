# Vinogram Android

Rebranded fork of Telegram for Android (upstream: `drklo/telegram`).

## Workflow rule

After **every** change: commit and push immediately to `origin`
(https://github.com/fumaz0/vinogram-android). Do not leave work uncommitted.

## Releases

Publish a GitHub release (tag, notes, APK asset) **only** when the user
explicitly asks for it. Otherwise stop after the commit/push and leave the
APK for local testing.

## What was rebranded

- App name: `R.string.AppName` / `R.string.AppNameBeta` -> "vinogram".
- Default theme is purple: `Theme.java` ("Blue" theme accents) and
  `assets/bluebubbles.attheme` (recolored with the app's own accent algorithm).
- Launcher icons: `mipmap-*/ic_launcher*.png`, `icon_foreground*.png`,
  monochrome `drawable-nodpi/vinogram_icon_mono.png`.

## Website

`docs/` is the landing page, published by `.github/workflows/pages.yml` to
https://fumaz0.github.io/vinogram-android/ on every push that touches `docs/`.
It is a single self-contained `index.html` in Material 3 (M3 colour roles,
type scale and components, light/dark toggle) plus a language toggle
(English + Russian) and `favicon.svg`. Both toggles are remembered in
`localStorage`. The logo is the splash plane path inlined as SVG, so keep it
in sync with `drawable/tg_splash_320.xml`.

The Pages environment only allows branch `master`: if a deploy fails with
"Branch ... is not allowed to deploy to github-pages", add the branch under
repo Settings -> Environments -> github-pages -> Deployment branch policy.

## Do not rename

Package names (`org.telegram.*`), protocol strings and URLs (`t.me`,
`telegram.me`, `tg://`, `telegram.org`), JS bridges, MIME types, intent
actions, resource names - renaming them breaks the build or the connection
to Telegram servers.

## Build

See README.md. Requires Android SDK 36, NDK 27.2.12479018, filled
`BuildVars.java` and `google-services.json`.