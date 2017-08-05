package br.com.danilooliveira.muzikplayer.domain;

import android.database.Cursor;
import android.provider.MediaStore;

/**
 * Criado por Danilo de Oliveira (danilo.desenvolvedor@outlook.com) em 04/08/2017.
 */

public class Audio {
    private String id;
    private String title;
    private String artist;
    private String albumId;
    private String data;

    public Audio(Cursor cursor) {
        id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
        title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
        artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
        albumId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
        data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbumId() {
        return albumId;
    }

    public String getData() {
        return data;
    }
}
