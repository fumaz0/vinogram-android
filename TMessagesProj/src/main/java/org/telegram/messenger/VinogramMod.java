package org.telegram.messenger;

import android.content.SharedPreferences;

/**
 * Settings for the vinogram-only extras that upstream Telegram does not have.
 *
 * <p>The values live in the main shared preferences and are mirrored into
 * static fields, because they are read from the hot paths in
 * {@link MessagesController} (typing, presence, read receipts) where loading
 * preferences would be wasteful. {@link #loadConfig()} runs once from
 * {@link ApplicationLoader#onCreate()} and {@link #saveConfig()} persists a
 * change made in {@code VinogramModActivity}.
 */
public class VinogramMod {

    /** Ghost mode: never announce that you are online. */
    public static final String KEY_GHOST_ONLINE = "vinogram_ghost_online";
    /** Ghost mode: never send "typing" / "recording voice" / "uploading". */
    public static final String KEY_GHOST_TYPING = "vinogram_ghost_typing";
    /** Ghost mode: never send read receipts. */
    public static final String KEY_GHOST_READ = "vinogram_ghost_read";
    /** Keep messages the other side deleted instead of dropping them. */
    public static final String KEY_KEEP_DELETED = "vinogram_keep_deleted";
    /** Auto-download media of any size on any network. */
    public static final String KEY_UNLIMITED_DOWNLOADS = "vinogram_unlimited_downloads";

    public static boolean ghostHideOnline;
    public static boolean ghostHideTyping;
    public static boolean ghostHideRead;
    public static boolean keepDeletedMessages;
    public static boolean unlimitedDownloads;

    public static void loadConfig() {
        final SharedPreferences prefs = MessagesController.getGlobalMainSettings();
        ghostHideOnline = prefs.getBoolean(KEY_GHOST_ONLINE, false);
        ghostHideTyping = prefs.getBoolean(KEY_GHOST_TYPING, false);
        ghostHideRead = prefs.getBoolean(KEY_GHOST_READ, false);
        keepDeletedMessages = prefs.getBoolean(KEY_KEEP_DELETED, false);
        unlimitedDownloads = prefs.getBoolean(KEY_UNLIMITED_DOWNLOADS, false);
    }

    public static void saveConfig() {
        MessagesController.getGlobalMainSettings().edit()
                .putBoolean(KEY_GHOST_ONLINE, ghostHideOnline)
                .putBoolean(KEY_GHOST_TYPING, ghostHideTyping)
                .putBoolean(KEY_GHOST_READ, ghostHideRead)
                .putBoolean(KEY_KEEP_DELETED, keepDeletedMessages)
                .putBoolean(KEY_UNLIMITED_DOWNLOADS, unlimitedDownloads)
                .apply();
    }

    /** True when at least one ghost switch is on, i.e. ghost mode is in effect. */
    public static boolean isGhostModeActive() {
        return ghostHideOnline || ghostHideTyping || ghostHideRead;
    }
}
