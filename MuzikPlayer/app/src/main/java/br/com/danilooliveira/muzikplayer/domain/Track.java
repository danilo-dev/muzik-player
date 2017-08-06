package br.com.danilooliveira.muzikplayer.domain;

import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

/**
 * Criado por Danilo de Oliveira (danilo.desenvolvedor@outlook.com) em 04/08/2017.
 */

public class Track {
    private String id;
    private String title;
    private String artist;
    private String albumId;
    private String albumArt;
    private String data;

    public Track(Cursor cursor) {
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

    @Nullable
    public String getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(@Nullable String albumArt) {
        this.albumArt = albumArt;
    }

    public String getData() {
        return data;
    }
}
