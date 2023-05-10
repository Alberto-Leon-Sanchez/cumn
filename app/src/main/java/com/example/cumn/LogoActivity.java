package com.example.cumn;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cumn.MainActivity;
import com.example.cumn.R;

public class LogoActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);

        int duration = 2000;

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(LogoActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, duration);
    }
}
