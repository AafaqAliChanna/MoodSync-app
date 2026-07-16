package com.example.moodsync.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.example.moodsync.MoodEntry;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Custom bar chart showing mood scores over the last N days.
 * Drawn entirely with android.graphics.Canvas — no external charting library needed.
 */
public class MoodChartView extends View {

    private static final int MAX_SCORE    = 5;
    private static final int BAR_COUNT    = 7;
    private static final float CORNER_R   = 16f;

    private MoodEntry[] entries; // index 0 = today, 6 = 6 days ago
    private String[]    dayLabels;

    private final Paint barPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint emptyPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint scorePaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint titlePaint  = new Paint(Paint.ANTI_ALIAS_FLAG);

    public MoodChartView(Context context) {
        super(context);
        init();
    }

    public MoodChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MoodChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        emptyPaint.setColor(Color.parseColor("#E8EAF0"));
        emptyPaint.setStyle(Paint.Style.FILL);

        labelPaint.setColor(Color.parseColor("#6B7280"));
        labelPaint.setTextSize(28f);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTypeface(Typeface.DEFAULT);

        scorePaint.setColor(Color.parseColor("#1F2937"));
        scorePaint.setTextSize(26f);
        scorePaint.setTextAlign(Paint.Align.CENTER);
        scorePaint.setTypeface(Typeface.DEFAULT_BOLD);

        gridPaint.setColor(Color.parseColor("#E5E7EB"));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(2f);

        axisPaint.setColor(Color.parseColor("#D1D5DB"));
        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setStrokeWidth(3f);

        titlePaint.setColor(Color.parseColor("#9CA3AF"));
        titlePaint.setTextSize(24f);
        titlePaint.setTextAlign(Paint.Align.LEFT);

        buildDayLabels();
    }

    private void buildDayLabels() {
        dayLabels = new String[BAR_COUNT];
        SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < BAR_COUNT; i++) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(cal.getTimeInMillis());
            c.add(Calendar.DAY_OF_YEAR, -i);
            dayLabels[i] = sdf.format(new Date(c.getTimeInMillis()));
        }
    }

    /** Feed in entries[0]=today … entries[6]=6 days ago. Nulls are allowed (no data). */
    public void setEntries(MoodEntry[] entries) {
        this.entries = entries;
        buildDayLabels();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (entries == null) return;

        float w = getWidth();
        float h = getHeight();

        float paddingLeft   = 24f;
        float paddingRight  = 24f;
        float paddingTop    = 32f;
        float paddingBottom = 72f; // room for day labels

        float chartW = w - paddingLeft - paddingRight;
        float chartH = h - paddingTop - paddingBottom;

        float barW    = chartW / (BAR_COUNT * 1.5f);
        float barGap  = chartW / BAR_COUNT;

        // Draw light horizontal grid lines for scores 1-5
        for (int score = 1; score <= MAX_SCORE; score++) {
            float y = paddingTop + chartH - (chartH * score / MAX_SCORE);
            canvas.drawLine(paddingLeft, y, w - paddingRight, y, gridPaint);
        }

        // Draw baseline axis
        float baseY = paddingTop + chartH;
        canvas.drawLine(paddingLeft, baseY, w - paddingRight, baseY, axisPaint);

        // Draw bars (newest = rightmost)
        for (int i = 0; i < BAR_COUNT; i++) {
            // i=0 → today (rightmost), i=6 → oldest (leftmost)
            int barIndex = BAR_COUNT - 1 - i; // flip so today is on the right
            float cx = paddingLeft + barGap * i + barGap / 2f;
            float left   = cx - barW / 2f;
            float right  = cx + barW / 2f;

            MoodEntry entry = entries[barIndex]; // barIndex 0=today

            if (entry == null) {
                // No data — draw faint empty bar
                RectF rect = new RectF(left, baseY - chartH * 0.05f, right, baseY);
                canvas.drawRoundRect(rect, CORNER_R, CORNER_R, emptyPaint);
            } else {
                float barHeight = chartH * entry.getMoodScore() / MAX_SCORE;
                float top = baseY - barHeight;
                RectF rect = new RectF(left, top, right, baseY);

                barPaint.setColor(getColorForMood(entry.getMoodLabel()));
                barPaint.setStyle(Paint.Style.FILL);
                canvas.drawRoundRect(rect, CORNER_R, CORNER_R, barPaint);

                // Score label above bar
                canvas.drawText(String.valueOf(entry.getMoodScore()),
                        cx, top - 10f, scorePaint);
            }

            // Day label below axis
            String label = dayLabels[barIndex];
            // Highlight "today" label
            if (barIndex == 0) {
                labelPaint.setColor(Color.parseColor("#6366F1"));
                labelPaint.setTypeface(Typeface.DEFAULT_BOLD);
            } else {
                labelPaint.setColor(Color.parseColor("#6B7280"));
                labelPaint.setTypeface(Typeface.DEFAULT);
            }
            canvas.drawText(label, cx, baseY + 48f, labelPaint);
        }
    }

    private int getColorForMood(String label) {
        switch (label) {
            case MoodEntry.MOOD_HAPPY:   return Color.parseColor("#34D399"); // green
            case MoodEntry.MOOD_CALM:    return Color.parseColor("#60A5FA"); // blue
            case MoodEntry.MOOD_ANXIOUS: return Color.parseColor("#FBBF24"); // amber
            case MoodEntry.MOOD_SAD:     return Color.parseColor("#818CF8"); // indigo
            case MoodEntry.MOOD_ANGRY:   return Color.parseColor("#F87171"); // red
            default:                     return Color.parseColor("#9CA3AF"); // gray
        }
    }
}
