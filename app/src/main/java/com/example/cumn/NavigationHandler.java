package com.example.cumn;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationHandler {

    public static void setupNavigation(Context context, BottomNavigationView bottomNav) {
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.button1:
                    context.startActivity(new Intent(context, MainActivity.class));
                    break;
                case R.id.button2:
                    break;
                case R.id.button3:
                    break;
            }
            return true;
        });
    }
}
