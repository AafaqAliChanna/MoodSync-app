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

import com.example.moodsync.views.MoodChartView;

import java.util.List;

public class MoodHistoryActivity extends AppCompatActivity {

    private MoodManager    moodManager;
    private MoodChartView  chartView;
    private LinearLayout   llEntries;
    private TextView       tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_history);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mood History");
        }

        moodManager = MoodManager.getInstance(this);
        chartView   = findViewById(R.id.mood_chart);
        llEntries   = findViewById(R.id.ll_entries);
        tvEmpty     = findViewById(R.id.tv_empty);

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        // Set chart data
        MoodEntry[] week = moodManager.getEntriesForLastDays(7);
        chartView.setEntries(week);

        // Build entry list
        llEntries.removeAllViews();
        List<MoodEntry> entries = moodManager.getRecentEntries(30);
        if (entries.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            llEntries.setVisibility(View.GONE);
            return;
        }
        tvEmpty.setVisibility(View.GONE);
        llEntries.setVisibility(View.VISIBLE);

        for (MoodEntry entry : entries) {
            llEntries.addView(buildEntryCard(entry));
        }
    }

    private View buildEntryCard(MoodEntry entry) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, dpToPx(10));
        card.setLayoutParams(cardParams);
        card.setRadius(dpToPx(12));
        card.setCardElevation(dpToPx(2));
        card.setCardBackgroundColor(Color.WHITE);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dpToPx(14), dpToPx(12), dpToPx(14), dpToPx(12));
        row.setGravity(Gravity.CENTER_VERTICAL);

        // Left: color stripe
        View stripe = new View(this);
        LinearLayout.LayoutParams stripeParams =
                new LinearLayout.LayoutParams(dpToPx(6), LinearLayout.LayoutParams.MATCH_PARENT);
        stripeParams.setMarginEnd(dpToPx(12));
        stripe.setLayoutParams(stripeParams);
        stripe.setMinimumHeight(dpToPx(56));

        android.graphics.drawable.GradientDrawable stripeBg =
                new android.graphics.drawable.GradientDrawable();
        stripeBg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        stripeBg.setCornerRadius(dpToPx(3));
        stripeBg.setColor(moodColor(entry.getMoodLabel()));
        stripe.setBackground(stripeBg);

        // Middle: text info
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        info.setLayoutParams(infoParams);

        TextView tvDate = new TextView(this);
        tvDate.setText(entry.getFormattedDate() + " · " + entry.getFormattedTime());
        tvDate.setTextSize(11f);
        tvDate.setTextColor(Color.parseColor("#9CA3AF"));

        TextView tvMood = new TextView(this);
        tvMood.setText(entry.getMoodEmoji() + "  " + entry.getMoodLabel()
                + "  (" + entry.getMoodScore() + "/5)");
        tvMood.setTextSize(15f);
        tvMood.setTextColor(Color.parseColor("#1F2937"));
        tvMood.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

        info.addView(tvDate);
        info.addView(tvMood);

        if (entry.getEntryText() != null && !entry.getEntryText().isEmpty()) {
            TextView tvSnippet = new TextView(this);
            String snippet = entry.getEntryText();
            if (snippet.length() > 80) snippet = snippet.substring(0, 80) + "…";
            tvSnippet.setText(snippet);
            tvSnippet.setTextSize(12f);
            tvSnippet.setTextColor(Color.parseColor("#6B7280"));
            LinearLayout.LayoutParams snippetParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            snippetParams.topMargin = dpToPx(2);
            tvSnippet.setLayoutParams(snippetParams);
            info.addView(tvSnippet);
        }

        // Right: score badge
        TextView tvScore = new TextView(this);
        tvScore.setText(String.valueOf(entry.getMoodScore()));
        tvScore.setTextSize(18f);
        tvScore.setTextColor(Color.WHITE);
        tvScore.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        tvScore.setGravity(Gravity.CENTER);
        int badgeSize = dpToPx(38);
        LinearLayout.LayoutParams scoreParams = new LinearLayout.LayoutParams(badgeSize, badgeSize);
        scoreParams.setMarginStart(dpToPx(8));
        tvScore.setLayoutParams(scoreParams);

        android.graphics.drawable.GradientDrawable badge =
                new android.graphics.drawable.GradientDrawable();
        badge.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        badge.setColor(moodColor(entry.getMoodLabel()));
        tvScore.setBackground(badge);

        row.addView(stripe);
        row.addView(info);
        row.addView(tvScore);
        card.addView(row);
        return card;
    }

    private int moodColor(String label) {
        switch (label) {
            case MoodEntry.MOOD_HAPPY:   return Color.parseColor("#34D399");
            case MoodEntry.MOOD_CALM:    return Color.parseColor("#60A5FA");
            case MoodEntry.MOOD_ANXIOUS: return Color.parseColor("#FBBF24");
            case MoodEntry.MOOD_SAD:     return Color.parseColor("#818CF8");
            case MoodEntry.MOOD_ANGRY:   return Color.parseColor("#F87171");
            default:                     return Color.parseColor("#9CA3AF");
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
