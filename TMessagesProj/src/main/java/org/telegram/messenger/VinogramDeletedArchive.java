package org.telegram.messenger;

import android.content.SharedPreferences;

import java.util.ArrayList;

/**
 * A small local archive of the messages that were deleted by the other side.
 *
 * <p>Upstream Telegram drops a message as soon as the server reports it
 * deleted, so the text is gone. When the "view deleted messages" switch is on,
 * the deletion path copies what it still has (text or the media kind, sender,
 * date) here first. The archive is not a database: it is capped and it only
 * keeps text and metadata, not the files themselves.
 *
 * <p>The records are stored in one shared preference as a flat string. A
 * separator pair of control characters keeps it dependency free and readable
 * without a JSON parser.
 */
public class VinogramDeletedArchive {

    private static final String PREFS_NAME = "vinogram_deleted";
    private static final String KEY_ENTRIES = "entries";

    private static final char SEP_ENTRY = '\u0001';
    private static final char SEP_FIELD = '\u0002';
    private static final int MAX_ENTRIES = 300;
    private static final int MAX_TEXT = 500;

    public static class Entry {
        public long dialogId;
        public int messageId;
        public int date;
        public String fromName = "";
        public String text = "";
        public boolean media;
        public boolean out;
    }

    private static SharedPreferences prefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREFS_NAME, 0);
    }

    private static String clean(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        final String trimmed = value.length() > MAX_TEXT ? value.substring(0, MAX_TEXT) : value;
        return trimmed.replace(SEP_ENTRY, ' ').replace(SEP_FIELD, ' ').replace('\n', ' ');
    }

    /** Adds freshly deleted messages, newest first, and persists the archive. */
    public static synchronized void add(ArrayList<Entry> entries) {
        if (entries == null || entries.isEmpty()) {
            return;
        }
        final ArrayList<Entry> all = getEntries();
        // The caller walks the deletion list in order and the newest deletion
        // usually has the highest id, so keep the incoming order but reverse it
        // so the most recent one ends up on top.
        for (int a = entries.size() - 1; a >= 0; a--) {
            all.add(0, entries.get(a));
        }
        while (all.size() > MAX_ENTRIES) {
            all.remove(all.size() - 1);
        }
        save(all);
    }

    public static synchronized ArrayList<Entry> getEntries() {
        final ArrayList<Entry> result = new ArrayList<>();
        final String raw = prefs().getString(KEY_ENTRIES, "");
        if (raw == null || raw.isEmpty()) {
            return result;
        }
        for (String block : raw.split(String.valueOf(SEP_ENTRY), -1)) {
            if (block.isEmpty()) {
                continue;
            }
            final String[] parts = block.split(String.valueOf(SEP_FIELD), -1);
            if (parts.length < 7) {
                continue;
            }
            try {
                final Entry entry = new Entry();
                entry.dialogId = Long.parseLong(parts[0]);
                entry.messageId = Integer.parseInt(parts[1]);
                entry.date = Integer.parseInt(parts[2]);
                entry.fromName = parts[3];
                entry.text = parts[4];
                entry.media = "1".equals(parts[5]);
                entry.out = "1".equals(parts[6]);
                result.add(entry);
            } catch (NumberFormatException ignore) {
            }
        }
        return result;
    }

    public static synchronized int getCount() {
        return getEntries().size();
    }

    public static synchronized void clear() {
        prefs().edit().remove(KEY_ENTRIES).apply();
    }

    private static void save(ArrayList<Entry> entries) {
        final StringBuilder builder = new StringBuilder();
        for (Entry entry : entries) {
            if (builder.length() > 0) {
                builder.append(SEP_ENTRY);
            }
            builder.append(entry.dialogId).append(SEP_FIELD)
                    .append(entry.messageId).append(SEP_FIELD)
                    .append(entry.date).append(SEP_FIELD)
                    .append(clean(entry.fromName)).append(SEP_FIELD)
                    .append(clean(entry.text)).append(SEP_FIELD)
                    .append(entry.media ? '1' : '0').append(SEP_FIELD)
                    .append(entry.out ? '1' : '0');
        }
        prefs().edit().putString(KEY_ENTRIES, builder.toString()).apply();
    }
}
