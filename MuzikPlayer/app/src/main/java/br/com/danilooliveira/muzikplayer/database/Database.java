package br.com.danilooliveira.muzikplayer.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore;

/**
 * Criado por Danilo de Oliveira (danilo.desenvolvedor@outlook.com) em 10/08/2017.
 */
abstract class Database extends SQLiteOpenHelper {
    private static final String DB_NAME = "muzik";
    private static final int DB_VERSION = 1;

    static final String TABLE_TRACK = "track";

    Context mContext;

    Database(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String string = "CREATE TABLE " + TABLE_TRACK + " (" + MediaStore.Audio.Media._ID + " INTEGER, "
                + MediaStore.Audio.Media.TITLE + " TEXT, "
                + MediaStore.Audio.Media.ARTIST + " TEXT, "
                + MediaStore.Audio.Media.ALBUM_ID + " TEXT, "
                + MediaStore.Audio.AlbumColumns.ALBUM_ART + " TEXT, "
                + MediaStore.Audio.Media.DURATION + " INTEGER, "
                + MediaStore.Audio.Media.DATA + " TEXT, "
                + "PRIMARY KEY(" + MediaStore.Audio.Media._ID + "))";
        db.execSQL(string);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACK);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACK);
        onCreate(db);
    }
}
