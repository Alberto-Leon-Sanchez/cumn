package com.example.cumn;

import android.content.Context;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationHandler {

    public static void setupNavigation(Context context, BottomNavigationView bottomNav) {
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.button1:
                    if (!(context instanceof MainActivity)) {
                        context.startActivity(new Intent(context, MainActivity.class));
                    }
                    break;
                case R.id.button2:
                    if (!(context instanceof RecipeActivity)) {
                        context.startActivity(new Intent(context, RecipeActivity.class));
                    }
                    break;
                case R.id.button3:
                    if (!(context instanceof FavouriteRecipes)) {
                        context.startActivity(new Intent(context, FavouriteRecipes.class));
                    }
                    break;
            }
            return true;
        });
    }
}
