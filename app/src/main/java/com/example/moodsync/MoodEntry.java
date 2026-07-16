package com.example.moodsync;

import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Represents a single mood log entry stored by the user.
 */
public class MoodEntry {

    public static final String MOOD_HAPPY    = "Happy";
    public static final String MOOD_SAD      = "Sad";
    public static final String MOOD_ANGRY    = "Angry";
    public static final String MOOD_ANXIOUS  = "Anxious";
    public static final String MOOD_CALM     = "Calm";
    public static final String MOOD_NEUTRAL  = "Neutral";

    private String id;
    private long timestamp;
    private String moodLabel;
    private int moodScore;       // 1 = worst, 5 = best
    private String entryText;
    private boolean autoDetected; // true if mood was detected from text keywords

    public MoodEntry(String moodLabel, int moodScore, String entryText, boolean autoDetected) {
        this.id           = UUID.randomUUID().toString();
        this.timestamp    = System.currentTimeMillis();
        this.moodLabel    = moodLabel;
        this.moodScore    = moodScore;
        this.entryText    = entryText;
        this.autoDetected = autoDetected;
    }

    // Private constructor used for deserialization
    private MoodEntry() {}

    // ---------- Getters ----------

    public String getId()          { return id; }
    public long getTimestamp()     { return timestamp; }
    public String getMoodLabel()   { return moodLabel; }
    public int getMoodScore()      { return moodScore; }
    public String getEntryText()   { return entryText; }
    public boolean isAutoDetected(){ return autoDetected; }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public String getMoodEmoji() {
        switch (moodLabel) {
            case MOOD_HAPPY:   return "😊";
            case MOOD_SAD:     return "😢";
            case MOOD_ANGRY:   return "😠";
            case MOOD_ANXIOUS: return "😰";
            case MOOD_CALM:    return "😌";
            default:           return "😐";
        }
    }

    // ---------- JSON serialization ----------

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id",           id);
        obj.put("timestamp",    timestamp);
        obj.put("moodLabel",    moodLabel);
        obj.put("moodScore",    moodScore);
        obj.put("entryText",    entryText != null ? entryText : "");
        obj.put("autoDetected", autoDetected);
        return obj;
    }

    public static MoodEntry fromJson(JSONObject obj) throws JSONException {
        MoodEntry entry = new MoodEntry();
        entry.id           = obj.getString("id");
        entry.timestamp    = obj.getLong("timestamp");
        entry.moodLabel    = obj.getString("moodLabel");
        entry.moodScore    = obj.getInt("moodScore");
        entry.entryText    = obj.optString("entryText", "");
        entry.autoDetected = obj.optBoolean("autoDetected", false);
        return entry;
    }
}
