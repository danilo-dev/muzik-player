package br.com.danilooliveira.muzikplayer.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import br.com.danilooliveira.muzikplayer.interfaces.TrackColumns;

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
        String string = "CREATE TABLE " + TABLE_TRACK + " (" + TrackColumns.ID + " INTEGER, "
                + TrackColumns.TITLE + " TEXT, " + TrackColumns.ARTIST + " TEXT, "
                + TrackColumns.ALBUM_NAME + " TEXT, " + TrackColumns.ALBUM_ART + " TEXT, "
                + TrackColumns.DURATION + " INTEGER, " + TrackColumns.DATA + " TEXT, " +
                "PRIMARY KEY(" + TrackColumns.ID + "))";
        db.execSQL(string);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS ?", new String[] {TABLE_TRACK});
        onCreate(db);
    }
}
