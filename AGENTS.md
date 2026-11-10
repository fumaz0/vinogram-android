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

## Mod features

Extras that upstream Telegram does not have. All are off by default and are
grouped under **Settings -> Mod features** (row id 30 ->
`VinogramModActivity`). The switches are mirrored into static fields in
`VinogramMod` (loaded once from `ApplicationLoader.onCreate`), because they are
read from hot paths where loading `SharedPreferences` would be wasteful.

- **Ghost mode** suppresses outbound requests instead of faking state, so the
  server is never told: `sendTyping` returns early (`ghostHideTyping`), the
  `updateTimerProc` online update is skipped (`ghostHideOnline`), and read
  receipts (`completeReadTask`, mentions, reactions, poll votes) are dropped
  (`ghostHideRead`). The read state still advances locally.
- **View deleted messages** keeps a capped local archive. The deletion path in
  `MessagesController` (`archiveDeletedMessages`, called before
  `NotificationCenter.messagesDeleted`) copies text/metadata into
  `VinogramDeletedArchive` (one pref, 300 entries, 500 chars, no media files);
  `VinogramDeletedActivity` lists them and opens the chat on tap. Deletions in
  private chats carry no peer, so the dialog is resolved through
  `MessageObject.getDialogId()`.
- **Unlimited downloads** short-circuits the auto-download decision helpers in
  `DownloadController` (`canDownloadMediaInternal` x2, `canDownloadMedia` ->
  `true`, `canDownloadMedia(int,long)` -> `true`): any size, any network.

## Website

`docs/` is the landing page, published by `.github/workflows/pages.yml` to
https://fumaz0.github.io/vinogram-android/ on every push that touches `docs/`.
It is a single self-contained `index.html` in Material 3 (M3 colour roles,
type scale and components, light/dark toggle) plus a language toggle
(English + Russian) and `favicon.svg`. Both toggles are remembered in
`localStorage`. The logo is the splash plane path inlined as SVG, so keep it
in sync with `drawable/tg_splash_320.xml`.

The page is responsive from 320px up: single-column cards, 48px touch targets
under `(pointer: coarse)`, safe-area insets for notched phones, and two hero
buttons sharing one row (equal `flex: 1 1 132px` basis under 600px, so they
only wrap below a ~304px container). Their phone padding is 10px rather than
12px so the longest label (`Исходный код`) keeps a cushion inside
`overflow: hidden` at 320px. Keep the `.grid` track as
`minmax(min(296px, 100%), 1fr)` - a plain `296px` minimum overflows a 320px
phone, because the gutters leave less than the track width.

The primary action uses `--brand` (`#793DF7`) with white text, not the M3
`--primary` token: in the dark scheme `--primary` is a pastel `#CFBCFF` with
dark navy text, which reads as a disabled control. The rest of the palette
still comes from the M3 baseline. On phones the hero is deliberately compact
(96px badge, 3-line lead) so the first screen is not all intro.

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