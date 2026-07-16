package com.example.moodsync;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Detects mood from free-text input using keyword matching.
 * No machine learning or external APIs — pure hardcoded heuristics.
 */
public class MoodDetector {

    public static class DetectionResult {
        public final String moodLabel;
        public final int    moodScore;
        public final int    confidence; // 0-100
        public final String detectedKeywords;

        public DetectionResult(String label, int score, int confidence, String keywords) {
            this.moodLabel        = label;
            this.moodScore        = score;
            this.confidence       = confidence;
            this.detectedKeywords = keywords;
        }

        public boolean isDetected() {
            return confidence > 0;
        }
    }

    // ---- Keyword lists per mood ----
    private static final Map<String, List<String>> MOOD_KEYWORDS = new HashMap<>();

    static {
        MOOD_KEYWORDS.put(MoodEntry.MOOD_HAPPY, Arrays.asList(
                "happy", "great", "good", "wonderful", "joy", "joyful", "excited",
                "love", "amazing", "fantastic", "awesome", "blessed", "grateful",
                "thankful", "smile", "laugh", "fun", "cheerful", "elated", "thrilled",
                "pleased", "content", "satisfied", "delighted", "positive", "upbeat",
                "energized", "bright", "sunny", "optimistic", "enjoying", "celebrate"
        ));
        MOOD_KEYWORDS.put(MoodEntry.MOOD_SAD, Arrays.asList(
                "sad", "unhappy", "depressed", "depression", "down", "cry", "crying",
                "upset", "miserable", "lonely", "alone", "hopeless", "heartbroken",
                "grief", "grieve", "loss", "lost", "broken", "hurt", "pain",
                "tearful", "gloomy", "sorrowful", "dismal", "melancholy", "low",
                "worthless", "helpless", "empty", "numb", "exhausted", "drained"
        ));
        MOOD_KEYWORDS.put(MoodEntry.MOOD_ANGRY, Arrays.asList(
                "angry", "anger", "mad", "furious", "frustrated", "frustration",
                "annoyed", "rage", "hate", "irritated", "irritating", "aggressive",
                "outraged", "infuriated", "livid", "bitter", "hostile", "tense",
                "boiling", "snapped", "yell", "yelling", "explode", "fed up",
                "unfair", "injustice", "betrayed", "betrayal", "resentful"
        ));
        MOOD_KEYWORDS.put(MoodEntry.MOOD_ANXIOUS, Arrays.asList(
                "anxious", "anxiety", "worried", "worry", "nervous", "stress",
                "stressed", "afraid", "scared", "fear", "fearful", "panic",
                "panicking", "overwhelmed", "dread", "dreading", "uneasy",
                "restless", "tense", "jittery", "on edge", "apprehensive",
                "unsettled", "racing thoughts", "overthinking", "what if",
                "uncertain", "insecure", "pressured", "deadline"
        ));
        MOOD_KEYWORDS.put(MoodEntry.MOOD_CALM, Arrays.asList(
                "calm", "relaxed", "peaceful", "serene", "tranquil", "at ease",
                "comfortable", "steady", "balanced", "centered", "mindful",
                "meditate", "meditation", "breathing", "still", "quiet",
                "zen", "chilled", "chill", "easy", "mellow", "settled"
        ));
    }

    // ---- Mood score mappings ----
    private static final Map<String, Integer> MOOD_SCORES = new HashMap<>();
    static {
        MOOD_SCORES.put(MoodEntry.MOOD_HAPPY,   5);
        MOOD_SCORES.put(MoodEntry.MOOD_CALM,    4);
        MOOD_SCORES.put(MoodEntry.MOOD_NEUTRAL, 3);
        MOOD_SCORES.put(MoodEntry.MOOD_ANXIOUS, 2);
        MOOD_SCORES.put(MoodEntry.MOOD_SAD,     1);
        MOOD_SCORES.put(MoodEntry.MOOD_ANGRY,   1);
    }

    /**
     * Analyses free text and returns the best-matched mood.
     */
    public static DetectionResult detect(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new DetectionResult(MoodEntry.MOOD_NEUTRAL, 3, 0, "");
        }

        String lower = text.toLowerCase(Locale.getDefault());

        // Count keyword hits per mood
        Map<String, Integer> hits   = new HashMap<>();
        Map<String, StringBuilder> found = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : MOOD_KEYWORDS.entrySet()) {
            String mood = entry.getKey();
            int count = 0;
            StringBuilder matched = new StringBuilder();

            for (String keyword : entry.getValue()) {
                if (lower.contains(keyword)) {
                    count++;
                    if (matched.length() > 0) matched.append(", ");
                    matched.append(keyword);
                }
            }
            hits.put(mood, count);
            found.put(mood, matched);
        }

        // Find mood with most hits
        String bestMood  = MoodEntry.MOOD_NEUTRAL;
        int    bestCount = 0;
        for (Map.Entry<String, Integer> entry : hits.entrySet()) {
            if (entry.getValue() > bestCount) {
                bestCount = entry.getValue();
                bestMood  = entry.getKey();
            }
        }

        if (bestCount == 0) {
            return new DetectionResult(MoodEntry.MOOD_NEUTRAL, 3, 0, "");
        }

        // Confidence: scales from 1 match = 20% up to 5+ matches = 100%
        int confidence = Math.min(100, bestCount * 20);
        int score      = MOOD_SCORES.getOrDefault(bestMood, 3);
        String keywords = found.get(bestMood) != null ? found.get(bestMood).toString() : "";

        return new DetectionResult(bestMood, score, confidence, keywords);
    }

    /**
     * Returns the moodScore for a given mood label (manual selection).
     */
    public static int getScoreForMood(String moodLabel) {
        return MOOD_SCORES.getOrDefault(moodLabel, 3);
    }
}
