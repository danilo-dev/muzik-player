package br.com.danilooliveira.muzikplayer.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;

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

        private final MediaSessionCompat.Token mediaToken;

        public Builder(Context context, MediaSessionCompat mediaSessionCompat) {
            this.context = context;
            notificationBuilder = new NotificationCompat.Builder(context);

            mediaToken = mediaSessionCompat.getSessionToken();
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

            // ALBUM ART
            Bitmap albumArt;
            if (track.getAlbumArt() != null) {
                albumArt = BitmapFactory.decodeFile(track.getAlbumArt());
            } else {
                albumArt = ImageUtil.getInstance()
                        .drawableToBitmap(ContextCompat.getDrawable(context, R.drawable.ic_placeholder_album_small));
            }

            // NOTIFICATION
            notificationBuilder
                    .setContentIntent(PendingIntent.getActivity(context, 0, openPlayerIntent, 0))
                    // TODO: Pausar reprodução na intent de deletar
//                    .setDeleteIntent(PendingIntent.getService(context, 0, playPauseIntent, 0))

                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setSmallIcon(R.drawable.ic_logo_flat_white)
                    .setColor(ContextCompat.getColor(context, R.color.colorAccent))

                    .setContentTitle(track.getTitle())
                    .setContentText(track.getArtist())
                    .setLargeIcon(albumArt)

                    .setStyle(new NotificationCompat.MediaStyle()
                            .setMediaSession(mediaToken)
                            .setShowActionsInCompactView(0, 1, 2)
                            .setCancelButtonIntent(PendingIntent.getService(context, 0, playPauseIntent, 0)));

            notificationBuilder
                    .addAction(R.drawable.ic_skip_previous, context.getString(R.string.btn_previous_track),
                            PendingIntent.getService(context, 0, previousIntent, 0));

            if (isPlaying) {
                notificationBuilder.addAction(R.drawable.ic_pause, context.getString(R.string.btn_pause),
                        PendingIntent.getService(context, 0, playPauseIntent, 0));
            } else {
                notificationBuilder.addAction(R.drawable.ic_play, context.getString(R.string.btn_play),
                        PendingIntent.getService(context, 0, playPauseIntent, 0));
            }

            notificationBuilder
                    .addAction(R.drawable.ic_skip_next, context.getString(R.string.btn_next_track),
                            PendingIntent.getService(context, 0, nextIntent, 0));

            return this;
        }

        public AppNotification build() {
            return new AppNotification(context, notificationBuilder);
        }
    }
}