package com.example.moodsync;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.Arrays;
import java.util.List;

public class RecommendationsActivity extends AppCompatActivity {

    private static class Recommendation {
        final String emoji;
        final String title;
        final String body;
        final String category;

        Recommendation(String emoji, String title, String body, String category) {
            this.emoji    = emoji;
            this.title    = title;
            this.body     = body;
            this.category = category;
        }
    }

    private MoodManager moodManager;
    private LinearLayout llRecommendations;
    private TextView     tvMoodHeading;
    private TextView     tvMoodSubheading;
    private TextView     tvMoodEmoji;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendations);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Recommendations");
        }

        moodManager         = MoodManager.getInstance(this);
        llRecommendations   = findViewById(R.id.ll_recommendations);
        tvMoodHeading       = findViewById(R.id.tv_mood_heading);
        tvMoodSubheading    = findViewById(R.id.tv_mood_subheading);
        tvMoodEmoji         = findViewById(R.id.tv_mood_emoji);

        // Determine mood to show recs for
        String moodLabel = getIntent().getStringExtra("mood_label");
        if (moodLabel == null || moodLabel.isEmpty()) {
            MoodEntry today = moodManager.getTodayEntry();
            moodLabel = (today != null) ? today.getMoodLabel() : MoodEntry.MOOD_NEUTRAL;
        }

        showRecommendations(moodLabel);
    }

    private void showRecommendations(String moodLabel) {
        String emoji = emojiForMood(moodLabel);
        tvMoodEmoji.setText(emoji);
        tvMoodHeading.setText("You're feeling " + moodLabel);
        tvMoodSubheading.setText(subtitleForMood(moodLabel));

        // Set background tint for header card
        CardView headerCard = findViewById(R.id.card_mood_header);
        if (headerCard != null) {
            headerCard.setCardBackgroundColor(bgColorForMood(moodLabel));
        }

        List<Recommendation> recs = getRecommendations(moodLabel);
        llRecommendations.removeAllViews();
        for (int i = 0; i < recs.size(); i++) {
            llRecommendations.addView(buildRecCard(recs.get(i), i));
        }
    }

    private View buildRecCard(Recommendation rec, int index) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, dpToPx(12));
        card.setLayoutParams(cardParams);
        card.setRadius(dpToPx(16));
        card.setCardElevation(dpToPx(3));
        card.setCardBackgroundColor(Color.WHITE);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dpToPx(18), dpToPx(16), dpToPx(18), dpToPx(16));

        // Category tag
        TextView tvCategory = new TextView(this);
        tvCategory.setText(rec.category.toUpperCase());
        tvCategory.setTextSize(10f);
        tvCategory.setTextColor(Color.parseColor("#6B7280"));
        tvCategory.setLetterSpacing(0.15f);
        tvCategory.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams catParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        catParams.bottomMargin = dpToPx(4);
        tvCategory.setLayoutParams(catParams);

        // Row: emoji + title
        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams titleRowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        titleRowParams.bottomMargin = dpToPx(6);
        titleRow.setLayoutParams(titleRowParams);

        TextView tvEmoji = new TextView(this);
        tvEmoji.setText(rec.emoji);
        tvEmoji.setTextSize(22f);
        LinearLayout.LayoutParams emojiParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        emojiParams.setMarginEnd(dpToPx(10));
        tvEmoji.setLayoutParams(emojiParams);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(rec.title);
        tvTitle.setTextSize(15f);
        tvTitle.setTextColor(Color.parseColor("#111827"));
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvTitle.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        titleRow.addView(tvEmoji);
        titleRow.addView(tvTitle);

        // Body
        TextView tvBody = new TextView(this);
        tvBody.setText(rec.body);
        tvBody.setTextSize(13f);
        tvBody.setTextColor(Color.parseColor("#4B5563"));
        tvBody.setLineSpacing(dpToPx(2), 1f);

        content.addView(tvCategory);
        content.addView(titleRow);
        content.addView(tvBody);
        card.addView(content);
        return card;
    }

    private List<Recommendation> getRecommendations(String label) {
        switch (label) {
            case MoodEntry.MOOD_HAPPY:
                return Arrays.asList(
                    new Recommendation("📔", "Journal your happiness",
                        "Write down exactly what made today great. You can re-read it on harder days to remind yourself of what's possible.",
                        "Reflection"),
                    new Recommendation("📞", "Spread the joy",
                        "Reach out to a friend or family member you haven't spoken to in a while. Your positive energy is contagious.",
                        "Social"),
                    new Recommendation("🎯", "Set a new challenge",
                        "High-energy moments are perfect for tackling something you've been putting off — start that project, learn that skill.",
                        "Productivity"),
                    new Recommendation("🎉", "Celebrate intentionally",
                        "Don't just let the good feeling pass. Mark it: treat yourself to something small, take a photo, or share the moment.",
                        "Wellbeing")
                );
            case MoodEntry.MOOD_SAD:
                return Arrays.asList(
                    new Recommendation("🚶", "Get some fresh air",
                        "A 10–15 minute gentle walk outside can shift your mood more than you expect. You don't need a destination — just move.",
                        "Activity"),
                    new Recommendation("🙏", "Practice gratitude",
                        "Write down 3 small things you're grateful for right now, however minor they seem. This rewires your focus gradually.",
                        "Mindset"),
                    new Recommendation("🎵", "Use music as medicine",
                        "Create a comfort playlist — not necessarily happy songs, but ones that feel like being understood. Let yourself feel it.",
                        "Mood Lift"),
                    new Recommendation("💛", "Be compassionate with yourself",
                        "You wouldn't tell a friend they're weak for feeling sad. Offer yourself the same kindness. Hard days are part of living.",
                        "Self-care")
                );
            case MoodEntry.MOOD_ANGRY:
                return Arrays.asList(
                    new Recommendation("🫁", "Box breathing",
                        "Inhale for 4 counts → hold for 4 → exhale for 4 → hold for 4. Repeat 4 times. This directly calms your nervous system.",
                        "Breathing"),
                    new Recommendation("✍️", "Vent on paper",
                        "Write everything you're feeling, unfiltered. You don't need to be fair or rational. Get it out, then decide what to do.",
                        "Release"),
                    new Recommendation("🏃", "Move the energy",
                        "Physical exercise is one of the fastest ways to metabolize anger. A brisk walk, jog, or workout session helps enormously.",
                        "Activity"),
                    new Recommendation("⏱️", "The 10-minute pause",
                        "Before responding to the situation that made you angry, wait 10 minutes. Most reactions soften when given a little space.",
                        "Strategy")
                );
            case MoodEntry.MOOD_ANXIOUS:
                return Arrays.asList(
                    new Recommendation("👁️", "The 5-4-3-2-1 grounding technique",
                        "Name 5 things you can see, 4 you can touch, 3 you can hear, 2 you can smell, 1 you can taste. This anchors you to now.",
                        "Grounding"),
                    new Recommendation("📋", "Break tasks into tiny steps",
                        "Overwhelm often comes from seeing everything at once. Pick the single next action, not the whole mountain. Just one step.",
                        "Productivity"),
                    new Recommendation("📵", "Reduce stimulation",
                        "Limit news and social media for the rest of today. Give your nervous system a rest from the constant stream of information.",
                        "Digital Wellness"),
                    new Recommendation("💬", "Talk it through",
                        "Saying worries out loud to a trusted person (or even to yourself in a voice memo) often reveals they're smaller than they feel.",
                        "Social")
                );
            case MoodEntry.MOOD_CALM:
                return Arrays.asList(
                    new Recommendation("🗺️", "Plan ahead",
                        "Calm, clear-headed moments are ideal for planning. Draft your week, set goals, or map out something you've been meaning to organize.",
                        "Productivity"),
                    new Recommendation("📚", "Learn something new",
                        "Calm focus is perfect for absorbing knowledge. Read an article, watch a documentary, or start an online course you've bookmarked.",
                        "Growth"),
                    new Recommendation("🎨", "Create something",
                        "Use this peaceful energy for creative work — writing, drawing, music, cooking. Creation feels effortless in a calm state.",
                        "Creativity"),
                    new Recommendation("💌", "Connect meaningfully",
                        "Send a heartfelt message to someone you appreciate. Calm energy makes for thoughtful, genuine communication.",
                        "Social")
                );
            default: // NEUTRAL
                return Arrays.asList(
                    new Recommendation("📊", "Check in with yourself",
                        "Neutral days are a good moment for honest self-assessment. How are you doing across the areas that matter — health, relationships, work?",
                        "Reflection"),
                    new Recommendation("✅", "Build momentum",
                        "Start small: pick one task from your to-do list and complete it. The act of finishing something generates its own energy.",
                        "Productivity"),
                    new Recommendation("🌿", "Add a micro-moment of joy",
                        "Make your favourite drink, take a short walk, or put on a song you love. Small intentional choices add up to a better day.",
                        "Wellbeing"),
                    new Recommendation("🔋", "Check the basics",
                        "Neutral moods are often tied to sleep, hydration, or nutrition. Have you had enough water and a proper meal today?",
                        "Self-care")
                );
        }
    }

    private String subtitleForMood(String label) {
        switch (label) {
            case MoodEntry.MOOD_HAPPY:   return "Great energy today — here's how to make the most of it";
            case MoodEntry.MOOD_SAD:     return "It's okay to feel this way. Here are some gentle steps forward";
            case MoodEntry.MOOD_ANGRY:   return "Let's find ways to process and release that tension";
            case MoodEntry.MOOD_ANXIOUS: return "You're safe right now. Let's bring that anxiety down a notch";
            case MoodEntry.MOOD_CALM:    return "What a great state to be in — here's how to use it well";
            default:                     return "A steady day — small actions can make it a good one";
        }
    }

    private String emojiForMood(String label) {
        switch (label) {
            case MoodEntry.MOOD_HAPPY:   return "😊";
            case MoodEntry.MOOD_SAD:     return "😢";
            case MoodEntry.MOOD_ANGRY:   return "😠";
            case MoodEntry.MOOD_ANXIOUS: return "😰";
            case MoodEntry.MOOD_CALM:    return "😌";
            default:                     return "😐";
        }
    }

    private int bgColorForMood(String label) {
        switch (label) {
            case MoodEntry.MOOD_HAPPY:   return Color.parseColor("#ECFDF5");
            case MoodEntry.MOOD_CALM:    return Color.parseColor("#EFF6FF");
            case MoodEntry.MOOD_ANXIOUS: return Color.parseColor("#FFFBEB");
            case MoodEntry.MOOD_SAD:     return Color.parseColor("#EEF2FF");
            case MoodEntry.MOOD_ANGRY:   return Color.parseColor("#FEF2F2");
            default:                     return Color.parseColor("#F9FAFB");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
