package com.example.ar.moviesproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by ar on 24/10/2015.
 */
public class Utility {

    public static String getSortBy(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sort_key), context.getString(R.string.pref_sort_popularity));
    }
}
