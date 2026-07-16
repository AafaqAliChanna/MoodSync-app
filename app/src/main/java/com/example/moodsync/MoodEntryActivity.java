package com.example.moodsync;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MoodEntryActivity extends AppCompatActivity {

    private EditText etJournal;
    private TextView tvDetectedMood;
    private TextView tvDetectedEmoji;
    private TextView tvConfidence;
    private TextView tvKeywords;
    private CardView cardDetection;
    private Button btnSave;
    private LinearLayout llMoodButtons;

    private String selectedMoodLabel = null;
    private int    selectedMoodScore = 3;
    private boolean manualSelection  = false;

    private MoodManager moodManager;

    private static final String[][] QUICK_MOODS = {
        { MoodEntry.MOOD_HAPPY,   "😊", "#34D399", "#ECFDF5" },
        { MoodEntry.MOOD_CALM,    "😌", "#60A5FA", "#EFF6FF" },
        { MoodEntry.MOOD_NEUTRAL, "😐", "#9CA3AF", "#F9FAFB" },
        { MoodEntry.MOOD_ANXIOUS, "😰", "#FBBF24", "#FFFBEB" },
        { MoodEntry.MOOD_SAD,     "😢", "#818CF8", "#EEF2FF" },
        { MoodEntry.MOOD_ANGRY,   "😠", "#F87171", "#FEF2F2" },
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_entry);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Log Your Mood");
        }

        moodManager     = MoodManager.getInstance(this);
        etJournal       = findViewById(R.id.et_journal);
        tvDetectedMood  = findViewById(R.id.tv_detected_mood);
        tvDetectedEmoji = findViewById(R.id.tv_detected_emoji);
        tvConfidence    = findViewById(R.id.tv_confidence);
        tvKeywords      = findViewById(R.id.tv_keywords);
        cardDetection   = findViewById(R.id.card_detection);
        btnSave         = findViewById(R.id.btn_save);
        llMoodButtons   = findViewById(R.id.ll_mood_buttons);

        buildMoodButtons();

        etJournal.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!manualSelection) analyzeText(s.toString());
            }
        });

        btnSave.setOnClickListener(v -> saveEntry());

        // Default state
        updateDetectionCard(null);
    }

    private void buildMoodButtons() {
        llMoodButtons.removeAllViews();
        for (String[] mood : QUICK_MOODS) {
            String label = mood[0];
            String emoji = mood[1];
            int    tint  = Color.parseColor(mood[2]);
            int    bg    = Color.parseColor(mood[3]);

            Button btn = new Button(this);
            btn.setText(emoji + "\n" + label);
            btn.setTextSize(12f);
            btn.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
            btn.setTextColor(tint);

            android.graphics.drawable.GradientDrawable shape =
                    new android.graphics.drawable.GradientDrawable();
            shape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            shape.setCornerRadius(dpToPx(12));
            shape.setColor(bg);
            shape.setStroke(dpToPx(2), tint);
            btn.setBackground(shape);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            btn.setLayoutParams(params);

            btn.setOnClickListener(v -> {
                manualSelection   = true;
                selectedMoodLabel = label;
                selectedMoodScore = MoodDetector.getScoreForMood(label);
                updateDetectionCard(new MoodDetector.DetectionResult(label, selectedMoodScore, 100, "manual"));
                highlightButton(btn, tint, bg);
            });

            llMoodButtons.addView(btn);
        }
    }

    private Button lastHighlighted = null;
    private void highlightButton(Button btn, int tint, int bg) {
        if (lastHighlighted != null) {
            // Reset previous button to normal
            lastHighlighted.setAlpha(1f);
        }
        btn.setAlpha(1f);
        android.graphics.drawable.GradientDrawable shape =
                new android.graphics.drawable.GradientDrawable();
        shape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        shape.setCornerRadius(dpToPx(12));
        shape.setColor(tint);
        shape.setStroke(dpToPx(2), tint);
        btn.setTextColor(Color.WHITE);
        btn.setBackground(shape);
        lastHighlighted = btn;
    }

    private void analyzeText(String text) {
        if (text.trim().isEmpty()) {
            updateDetectionCard(null);
            selectedMoodLabel = null;
            return;
        }
        MoodDetector.DetectionResult result = MoodDetector.detect(text);
        if (result.isDetected()) {
            selectedMoodLabel = result.moodLabel;
            selectedMoodScore = result.moodScore;
        }
        updateDetectionCard(result);
    }

    private void updateDetectionCard(MoodDetector.DetectionResult result) {
        if (result == null || !result.isDetected()) {
            cardDetection.setCardBackgroundColor(Color.parseColor("#F9FAFB"));
            tvDetectedEmoji.setText("🔍");
            tvDetectedMood.setText("Analyzing your words…");
            tvConfidence.setText("Start typing to detect your mood");
            tvKeywords.setVisibility(View.GONE);
            return;
        }

        tvDetectedEmoji.setText(emojiForMood(result.moodLabel));
        tvDetectedMood.setText(result.moodLabel);
        tvConfidence.setText("Confidence: " + result.confidence + "%"
                + (result.detectedKeywords.equals("manual") ? " (manually selected)" : ""));
        cardDetection.setCardBackgroundColor(bgColorForMood(result.moodLabel));

        if (!result.detectedKeywords.isEmpty() && !result.detectedKeywords.equals("manual")) {
            tvKeywords.setVisibility(View.VISIBLE);
            tvKeywords.setText("Keywords: " + result.detectedKeywords);
        } else {
            tvKeywords.setVisibility(View.GONE);
        }
    }

    private void saveEntry() {
        String text = etJournal.getText().toString().trim();

        // If no mood detected and no text, prompt
        if (selectedMoodLabel == null && text.isEmpty()) {
            Toast.makeText(this, "Please write something or select a mood", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fall back to detecting from text one more time
        if (selectedMoodLabel == null) {
            MoodDetector.DetectionResult r = MoodDetector.detect(text);
            selectedMoodLabel = r.isDetected() ? r.moodLabel : MoodEntry.MOOD_NEUTRAL;
            selectedMoodScore = r.isDetected() ? r.moodScore : 3;
        }

        MoodEntry entry = new MoodEntry(selectedMoodLabel, selectedMoodScore, text, !manualSelection);
        moodManager.saveEntry(entry);

        Toast.makeText(this, "Mood logged: " + selectedMoodLabel + " " + emojiForMood(selectedMoodLabel),
                Toast.LENGTH_SHORT).show();

        // Navigate to recommendations
        Intent intent = new Intent(this, RecommendationsActivity.class);
        intent.putExtra("mood_label", selectedMoodLabel);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // ---- Helpers ----

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

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
