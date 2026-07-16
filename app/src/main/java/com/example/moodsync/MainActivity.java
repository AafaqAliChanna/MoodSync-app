package com.example.moodsync;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private MoodManager moodManager;

    private TextView tvGreeting;
    private TextView tvDate;
    private TextView tvTodayMood;
    private TextView tvTodayEmoji;
    private TextView tvTodayHint;
    private LinearLayout llWeekDots;
    private TextView tvWeekAvg;
    private CardView cardToday;
    private Button btnLogMood;
    private Button btnHistory;
    private Button btnRecommend;
    private Button btnAboutDeveloper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        moodManager = MoodManager.getInstance(this);

        tvGreeting   = findViewById(R.id.tv_greeting);
        tvDate       = findViewById(R.id.tv_date);
        tvTodayMood  = findViewById(R.id.tv_today_mood);
        tvTodayEmoji = findViewById(R.id.tv_today_emoji);
        tvTodayHint  = findViewById(R.id.tv_today_hint);
        llWeekDots   = findViewById(R.id.ll_week_dots);
        tvWeekAvg    = findViewById(R.id.tv_week_avg);
        cardToday    = findViewById(R.id.card_today);
        btnLogMood   = findViewById(R.id.btn_log_mood);
        btnHistory   = findViewById(R.id.btn_history);
        btnRecommend = findViewById(R.id.btn_recommend);
        btnAboutDeveloper = findViewById(R.id.btn_about_developer);

        btnLogMood.setOnClickListener(v ->
                startActivity(new Intent(this, MoodEntryActivity.class)));
        btnHistory.setOnClickListener(v ->
                startActivity(new Intent(this, MoodHistoryActivity.class)));
        btnRecommend.setOnClickListener(v ->
                startActivity(new Intent(this, RecommendationsActivity.class)));
        btnAboutDeveloper.setOnClickListener(v ->
                startActivity(new Intent(this, AboutDeveloperActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUI();
    }

    private void refreshUI() {
        // Greeting + date
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        String greeting = hour < 12 ? "Good morning" : hour < 17 ? "Good afternoon" : "Good evening";
        tvGreeting.setText(greeting + " \uD83C\uDF1F");
        tvDate.setText(new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(new Date()));

        // Today's mood
        MoodEntry today = moodManager.getTodayEntry();
        if (today != null) {
            tvTodayEmoji.setText(today.getMoodEmoji());
            tvTodayMood.setText(today.getMoodLabel());
            tvTodayHint.setText("Logged at " + today.getFormattedTime());
            cardToday.setCardBackgroundColor(getBgColorForMood(today.getMoodLabel()));
        } else {
            tvTodayEmoji.setText("\uD83D\uDCDD");
            tvTodayMood.setText("Not logged yet");
            tvTodayHint.setText("Tap \"Log Mood\" to record how you feel");
            cardToday.setCardBackgroundColor(Color.parseColor("#F3F4F6"));
        }

        // 7-day dot strip
        buildWeekDots();

        // Weekly average
        float avg = moodManager.getAverageMoodScore(7);
        if (avg > 0) {
            tvWeekAvg.setText(String.format(Locale.getDefault(),
                    "7-day average: %.1f / 5.0  %s", avg, avgEmoji(avg)));
        } else {
            tvWeekAvg.setText("No data for this week yet");
        }
    }

    private void buildWeekDots() {
        llWeekDots.removeAllViews();
        MoodEntry[] week = moodManager.getEntriesForLastDays(7);
        String[] dayShort = {"S", "M", "T", "W", "T", "F", "S"};

        // Show Mon-Sun order based on today backwards
        java.util.Calendar cal = java.util.Calendar.getInstance();

        for (int d = 6; d >= 0; d--) {
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.setTimeInMillis(cal.getTimeInMillis());
            c.add(java.util.Calendar.DAY_OF_YEAR, -d);

            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams colParams =
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            col.setLayoutParams(colParams);

            // Colored dot
            View dot = new View(this);
            int size = dpToPx(28);
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(size, size);
            dotParams.bottomMargin = dpToPx(4);
            dot.setLayoutParams(dotParams);

            MoodEntry entry = week[d];
            int dotColor = entry != null
                    ? getDotColorForMood(entry.getMoodLabel())
                    : Color.parseColor("#E5E7EB");
            dot.setBackgroundColor(dotColor);

            // Make dot circular
            dot.post(() -> {
                dot.setBackground(createCircleDrawable(dotColor));
            });

            // Day label
            TextView label = new TextView(this);
            String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            int dow = c.get(java.util.Calendar.DAY_OF_WEEK); // 1=Sun
            String dayName = days[(dow + 5) % 7]; // shift so Mon=0
            label.setText(dayName.substring(0, 1));
            label.setTextSize(11f);
            label.setTextColor(d == 0
                    ? Color.parseColor("#6366F1")
                    : Color.parseColor("#9CA3AF"));
            label.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams labelP =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            label.setLayoutParams(labelP);

            col.addView(dot);
            col.addView(label);
            llWeekDots.addView(col);
        }
    }

    private android.graphics.drawable.GradientDrawable createCircleDrawable(int color) {
        android.graphics.drawable.GradientDrawable shape =
                new android.graphics.drawable.GradientDrawable();
        shape.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        shape.setColor(color);
        return shape;
    }

    private int getDotColorForMood(String label) {
        switch (label) {
            case MoodEntry.MOOD_HAPPY:   return Color.parseColor("#34D399");
            case MoodEntry.MOOD_CALM:    return Color.parseColor("#60A5FA");
            case MoodEntry.MOOD_ANXIOUS: return Color.parseColor("#FBBF24");
            case MoodEntry.MOOD_SAD:     return Color.parseColor("#818CF8");
            case MoodEntry.MOOD_ANGRY:   return Color.parseColor("#F87171");
            default:                     return Color.parseColor("#9CA3AF");
        }
    }

    private int getBgColorForMood(String label) {
        switch (label) {
            case MoodEntry.MOOD_HAPPY:   return Color.parseColor("#ECFDF5");
            case MoodEntry.MOOD_CALM:    return Color.parseColor("#EFF6FF");
            case MoodEntry.MOOD_ANXIOUS: return Color.parseColor("#FFFBEB");
            case MoodEntry.MOOD_SAD:     return Color.parseColor("#EEF2FF");
            case MoodEntry.MOOD_ANGRY:   return Color.parseColor("#FEF2F2");
            default:                     return Color.parseColor("#F9FAFB");
        }
    }

    private String avgEmoji(float avg) {
        if (avg >= 4.5f) return "\uD83D\uDE0A";
        if (avg >= 3.5f) return "\uD83D\uDE42";
        if (avg >= 2.5f) return "\uD83D\uDE10";
        if (avg >= 1.5f) return "\uD83D\uDE1F";
        return "\uD83D\uDE22";
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
