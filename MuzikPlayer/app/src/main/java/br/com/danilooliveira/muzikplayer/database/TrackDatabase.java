package br.com.danilooliveira.muzikplayer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

import br.com.danilooliveira.muzikplayer.domain.Track;
import br.com.danilooliveira.muzikplayer.interfaces.TrackColumns;

/**
 * Criado por Danilo de Oliveira (danilo.desenvolvedor@outlook.com) em 10/08/2017.
 */
public class TrackDatabase extends Database {

    public TrackDatabase(Context context) {
        super(context);
    }

    public List<Track> getTracksFromAndroidDB() {
        Uri trackUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Uri albumUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;

        String[] trackColumns = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
        };
        String[] albumColumns = {
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.AlbumColumns.ALBUM_ART
        };

        String trackConditions = MediaStore.Audio.Media.IS_MUSIC + "=1";
        String albumConditions = MediaStore.Audio.Albums._ID + "= ?";

        String trackOrder = MediaStore.Audio.Media.TITLE + " ASC";

        String albumArgs;

        List<Track> trackList = new ArrayList<>();
        Cursor trackCursor = mContext.getContentResolver().query(trackUri, trackColumns, trackConditions, null, trackOrder);
        if (trackCursor != null) {

            Cursor albumCursor;
            Track track;
            while (trackCursor.moveToNext()) {
                track = new Track(trackCursor);

                if (exists(track.getId())) {
                    break;
                }

                albumArgs = trackCursor.getString(trackCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));

                albumCursor = mContext.getContentResolver().query(albumUri, albumColumns, albumConditions, new String[] {albumArgs}, null);
                if (albumCursor != null) {
                    if (albumCursor.moveToNext()) {
                        track.setAlbumArt(albumCursor.getString(albumCursor.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ALBUM_ART)));
                    }
                    albumCursor.close();
                }

                trackList.add(track);
                add(track);
            }
            trackCursor.close();
        }

        return trackList;
    }

    private void add(Track track) {
        if (exists(track.getId())) {
            return;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(TrackColumns.ID, track.getId());
        contentValues.put(TrackColumns.TITLE, track.getTitle());
        contentValues.put(TrackColumns.ARTIST, track.getArtist());
        contentValues.put(TrackColumns.ALBUM_NAME, track.getAlbumName());
        contentValues.put(TrackColumns.ALBUM_ART, track.getAlbumArt());
        contentValues.put(TrackColumns.DURATION, track.getDuration());
        contentValues.put(TrackColumns.DATA, track.getData());

        getWritableDatabase().insert(TABLE_TRACK, null, contentValues);
        close();
    }

    private boolean exists(String id) {
        Cursor cursor = getReadableDatabase().query(TABLE_TRACK, new String[] {TrackColumns.ID}, TrackColumns.ID + "=?",
                new String[] {id}, null, null, null, "1");

        boolean exists = false;
        if (cursor != null) {
            exists = cursor.moveToNext();
            cursor.close();
        }
        close();

        return exists;
    }
}
