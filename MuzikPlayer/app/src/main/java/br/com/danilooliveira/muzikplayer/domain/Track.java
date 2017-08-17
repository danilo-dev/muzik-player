package br.com.danilooliveira.muzikplayer.domain;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

/**
 * Criado por Danilo de Oliveira (danilo.desenvolvedor@outlook.com) em 04/08/2017.
 */

public class Track implements Parcelable {
    private String id;
    private String title;
    private String artist;
    private String albumId;
    private String albumName;
    private String albumArt;
    private int duration;
    private String data;

    public Track(Cursor cursor) {
        id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
        title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
        artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
        albumId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
        duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
        data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
    }

    private Track(Parcel in) {
        id = in.readString();
        title = in.readString();
        artist = in.readString();
        albumId = in.readString();
        albumName = in.readString();
        albumArt = in.readString();
        duration = in.readInt();
        data = in.readString();
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

    public String getAlbumName() {
        return albumName;
    }

    @Nullable
    public String getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(String albumArt) {
        this.albumArt = albumArt;
    }

    public int getDuration() {
        return duration;
    }

    public String getData() {
        return data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(title);
        parcel.writeString(artist);
        parcel.writeString(albumId);
        parcel.writeString(albumName);
        parcel.writeString(albumArt);
        parcel.writeInt(duration);
        parcel.writeString(data);
    }

    public static final Creator<Track> CREATOR = new Creator<Track>() {
        @Override
        public Track createFromParcel(Parcel in) {
            return new Track(in);
        }

        @Override
        public Track[] newArray(int size) {
            return new Track[size];
        }
    };
}
