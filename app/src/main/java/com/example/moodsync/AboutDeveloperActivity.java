package com.example.moodsync;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class AboutDeveloperActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_developer);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("About Developer");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
