package ru.toxuin.vitapsnbypass.library;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import ru.toxuin.vitapsnbypass.BackgroundService;

public class SettingsHelper {
    private static SettingsHelper self;
    private static Context context;
    private static SharedPreferences prefs;

    private SettingsHelper(Context context) {
        SettingsHelper.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static SettingsHelper getInstance(Context cnt) {
        if (self == null) {
            self = new SettingsHelper(cnt);
            cnt.startService(new Intent(cnt, BackgroundService.class));
        }
        return self;
    }


}
