package com.example.moodsync;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Manages all mood entries using SharedPreferences for persistence.
 * No Room, no Firebase — just serialized JSON in prefs.
 */
public class MoodManager {

    private static final String PREFS_NAME   = "mood_sync_prefs";
    private static final String KEY_ENTRIES  = "entries";
    private static final String KEY_SEEDED   = "dummy_seeded";

    private static MoodManager instance;
    private final SharedPreferences prefs;

    private MoodManager(Context ctx) {
        prefs = ctx.getApplicationContext()
                   .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        seedDummyDataIfNeeded();
    }

    public static synchronized MoodManager getInstance(Context ctx) {
        if (instance == null) instance = new MoodManager(ctx);
        return instance;
    }

    // ---- Public API ----

    public void saveEntry(MoodEntry entry) {
        List<MoodEntry> all = getAllEntries();
        all.add(entry);
        persistAll(all);
    }

    public List<MoodEntry> getAllEntries() {
        String json = prefs.getString(KEY_ENTRIES, "[]");
        List<MoodEntry> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                list.add(MoodEntry.fromJson(arr.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Sort newest first
        Collections.sort(list, new Comparator<MoodEntry>() {
            @Override
            public int compare(MoodEntry a, MoodEntry b) {
                return Long.compare(b.getTimestamp(), a.getTimestamp());
            }
        });
        return list;
    }

    /** Returns up to {@code n} most recent entries. */
    public List<MoodEntry> getRecentEntries(int n) {
        List<MoodEntry> all = getAllEntries();
        return all.subList(0, Math.min(n, all.size()));
    }

    /**
     * Returns one entry per day for the last {@code days} days (most recent day = index 0).
     * Returns null for days with no entry.
     */
    public MoodEntry[] getEntriesForLastDays(int days) {
        MoodEntry[] result = new MoodEntry[days];
        List<MoodEntry> all = getAllEntries();

        Calendar cal = Calendar.getInstance();
        for (int d = 0; d < days; d++) {
            // d=0 → today, d=1 → yesterday, etc.
            Calendar day = Calendar.getInstance();
            day.setTimeInMillis(cal.getTimeInMillis());
            day.add(Calendar.DAY_OF_YEAR, -d);
            int targetYear  = day.get(Calendar.YEAR);
            int targetMonth = day.get(Calendar.MONTH);
            int targetDay   = day.get(Calendar.DAY_OF_MONTH);

            for (MoodEntry entry : all) {
                Calendar eCal = Calendar.getInstance();
                eCal.setTimeInMillis(entry.getTimestamp());
                if (eCal.get(Calendar.YEAR)         == targetYear
                 && eCal.get(Calendar.MONTH)        == targetMonth
                 && eCal.get(Calendar.DAY_OF_MONTH) == targetDay) {
                    result[d] = entry;
                    break; // one per day — take the newest
                }
            }
        }
        return result;
    }

    /** Returns today's most recent entry, or null. */
    public MoodEntry getTodayEntry() {
        MoodEntry[] week = getEntriesForLastDays(1);
        return week[0];
    }

    /** Computes the average mood score over the last {@code days} days. */
    public float getAverageMoodScore(int days) {
        MoodEntry[] entries = getEntriesForLastDays(days);
        int total = 0, count = 0;
        for (MoodEntry e : entries) {
            if (e != null) { total += e.getMoodScore(); count++; }
        }
        return count == 0 ? 0 : (float) total / count;
    }

    public void clearAll() {
        prefs.edit().remove(KEY_ENTRIES).remove(KEY_SEEDED).apply();
    }

    // ---- Internal ----

    private void persistAll(List<MoodEntry> entries) {
        JSONArray arr = new JSONArray();
        for (MoodEntry e : entries) {
            try { arr.put(e.toJson()); } catch (JSONException ex) { ex.printStackTrace(); }
        }
        prefs.edit().putString(KEY_ENTRIES, arr.toString()).apply();
    }

    /**
     * Seeds 7 days of dummy data on first launch so the charts are not empty.
     */
    private void seedDummyDataIfNeeded() {
        if (prefs.getBoolean(KEY_SEEDED, false)) return;

        String[][] seedData = {
            // { moodLabel, entryText, daysAgo }
            { MoodEntry.MOOD_HAPPY,   "Had a really great day! Finished my assignment early and went for a walk.", "6" },
            { MoodEntry.MOOD_ANXIOUS, "Stressed about the upcoming presentation. Feeling nervous and overwhelmed.", "5" },
            { MoodEntry.MOOD_CALM,    "Took a long walk in the park. Feeling relaxed and peaceful now.", "4" },
            { MoodEntry.MOOD_SAD,     "Feeling a bit down and lonely today. Miss my friends back home.", "3" },
            { MoodEntry.MOOD_HAPPY,   "Great team meeting! Everyone was excited about the new project.", "2" },
            { MoodEntry.MOOD_NEUTRAL, "Normal day. Nothing special. Just getting through the tasks.", "1" },
        };

        List<MoodEntry> entries = new ArrayList<>();
        long now = System.currentTimeMillis();
        long dayMs = 24L * 60 * 60 * 1000;

        for (String[] row : seedData) {
            String label  = row[0];
            String text   = row[1];
            int daysAgo   = Integer.parseInt(row[2]);
            int score     = MoodDetector.getScoreForMood(label);

            try {
                MoodEntry e = MoodEntry.fromJson(new JSONObject()
                    .put("id",           java.util.UUID.randomUUID().toString())
                    .put("timestamp",    now - (daysAgo * dayMs) + (8 * 60 * 60 * 1000L)) // 8am offset
                    .put("moodLabel",    label)
                    .put("moodScore",    score)
                    .put("entryText",    text)
                    .put("autoDetected", true));
                entries.add(e);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }

        persistAll(entries);
        prefs.edit().putBoolean(KEY_SEEDED, true).apply();
    }
}
