package br.com.danilooliveira.muzikplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Criado por Danilo de Oliveira (danilo.desenvolvedor@outlook.com) em 11/08/2017.
 */
public class AppPreferences {
    private static final String PREFERENCES_FILE_NAME = "muzik";

    private static AppPreferences ourInstance = null;
    private static SharedPreferences sharedPreferences = null;

    public static AppPreferences with(Context context) {
        if (ourInstance == null) {
            ourInstance = new AppPreferences(context);
        }
        return ourInstance;
    }

    private AppPreferences(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
    }

    public boolean isShuffleEnabled() {
        return sharedPreferences.getBoolean(PreferencesKeys.SHUFFLE_ENABLED, true);
    }

    public AppPreferences setShuffleEnabled(boolean enable) {
        sharedPreferences.edit().putBoolean(PreferencesKeys.SHUFFLE_ENABLED, enable).apply();
        return ourInstance;
    }

    public int getRepeatType() {
        return sharedPreferences.getInt(PreferencesKeys.REPEAT_TYPE, Constants.TYPE_REPEAT_ALL);
    }

    public AppPreferences setRepeatType(int repeatMode) {
        sharedPreferences.edit().putInt(PreferencesKeys.REPEAT_TYPE, repeatMode).apply();
        return ourInstance;
    }

    public boolean isStorageScanned() {
        return sharedPreferences.getBoolean(PreferencesKeys.STORAGE_SCANNED, false);
    }

    public AppPreferences setStorageScanned(boolean scanned) {
        sharedPreferences.edit().putBoolean(PreferencesKeys.STORAGE_SCANNED, scanned).apply();
        return ourInstance;
    }

    private interface PreferencesKeys {
        /**
         * Chave que armazena o último tipo de repetição utilizado
         * pelo usuário
         * @see br.com.danilooliveira.muzikplayer.utils.Constants#TYPE_NO_REPEAT
         * @see br.com.danilooliveira.muzikplayer.utils.Constants#TYPE_REPEAT_CURRENT
         * @see br.com.danilooliveira.muzikplayer.utils.Constants#TYPE_REPEAT_ALL
         */
        String REPEAT_TYPE = "repeat_type";

        /**
         * Chave que armazena um booleano para indicar se o
         * usuário estava reproduzindo músicas aleatoriamente
         * true para aleatório habilitado
         * false para ordem alfabética
         */
        String SHUFFLE_ENABLED = "shuffle_enable";

        /**
         * Chave que armazena um booleano para indicar se o
         * armazenamento já foi escaneado e as músicas foram
         * adicionadas ao banco de dados local
         */
        String STORAGE_SCANNED = "storage_scanned";
    }
}
