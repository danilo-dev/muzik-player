package br.com.danilooliveira.muzikplayer.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

import br.com.danilooliveira.muzikplayer.R;
import br.com.danilooliveira.muzikplayer.activities.PlayerActivity;
import br.com.danilooliveira.muzikplayer.domain.Track;
import br.com.danilooliveira.muzikplayer.services.MediaPlayerService;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Criado por Danilo de Oliveira (danilo.desenvolvedor@outlook.com) em 03/09/2017.
 */
public class AppNotification {
    private static final int NOTIFICATION_ID = 1;

    private final NotificationManager notificationManager;
    private NotificationCompat.Builder notification;

    private AppNotification(Context context, NotificationCompat.Builder notification) {
        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        this.notification = notification;
    }

    public AppNotification show() {
        notificationManager.notify(NOTIFICATION_ID, notification.build());
        return this;
    }

    public void cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID);
    }

    public static class Builder {
        private final Context context;

        private final NotificationCompat.Builder notificationBuilder;

        public Builder(Context context) {
            this.context = context;
            notificationBuilder = new NotificationCompat.Builder(context);
        }

        public Builder setDefault(Track track, boolean isPlaying) {
            // INTENTS
            Intent openPlayerIntent = new Intent(context, PlayerActivity.class);

            Intent playPauseIntent = new Intent(context, MediaPlayerService.class);
            playPauseIntent.setAction(Constants.ACTION_PLAY_PAUSE);

            Intent nextIntent = new Intent(context, MediaPlayerService.class);
            nextIntent.setAction(Constants.ACTION_NEXT_TRACK);

            Intent previousIntent = new Intent(context, MediaPlayerService.class);
            previousIntent.setAction(Constants.ACTION_PREVIOUS_TRACK);

            // VIEWS
            RemoteViews smallBodyView = new RemoteViews(context.getPackageName(), R.layout.notification_body_small);
            smallBodyView.setTextViewText(R.id.txt_title, track.getTitle());
            smallBodyView.setTextViewText(R.id.txt_artist, track.getArtist());

            if (isPlaying) {
                smallBodyView.setImageViewResource(R.id.btn_play_pause, R.drawable.ic_pause);
            } else {
                smallBodyView.setImageViewResource(R.id.btn_play_pause, R.drawable.ic_play);
            }

            // ALBUM ART
            if (track.getAlbumArt() != null) {
                smallBodyView.setImageViewBitmap(R.id.img_album_art, BitmapFactory.decodeFile(track.getAlbumArt()));
            } else {
                smallBodyView.setImageViewResource(R.id.img_album_art, R.drawable.ic_placeholder_album_small);
            }

            // LISTENERS
            smallBodyView.setOnClickPendingIntent(R.id.btn_previous, PendingIntent.getService(context, 0, previousIntent, 0));
            smallBodyView.setOnClickPendingIntent(R.id.btn_play_pause, PendingIntent.getService(context, 0, playPauseIntent, 0));
            smallBodyView.setOnClickPendingIntent(R.id.btn_next, PendingIntent.getService(context, 0, nextIntent, 0));

            // NOTIFICATION
            notificationBuilder
                    .setContentIntent(PendingIntent.getActivity(context, 0, openPlayerIntent, 0))
                    .setCustomContentView(smallBodyView)

                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.ic_logo_flat_white);
            return this;
        }

        public AppNotification build() {
            return new AppNotification(context, notificationBuilder);
        }
    }
}