package br.com.danilooliveira.muzikplayer.services;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.danilooliveira.muzikplayer.domain.Track;
import br.com.danilooliveira.muzikplayer.utils.AppPreferences;
import br.com.danilooliveira.muzikplayer.utils.Constants;

public class MediaPlayerService extends MediaBrowserServiceCompat {
    private IBinder trackBinder = new TrackBinder();

    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder playbackStateBuilder;
    private MediaPlayer mediaPlayer;

    /**
     * Faixas ordenadas alfabeticamente
     */
    private List<Track> trackList;

    /**
     * Fila de reprodução
     */
    private List<Track> queue;

    private int currentPosition;
    private boolean isShuffle;
    @IntRange(from = Constants.TYPE_NO_REPEAT, to = Constants.TYPE_REPEAT_ALL)
    private int repeatType;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaSession = new MediaSessionCompat(this, MediaPlayerService.class.getSimpleName());
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        playbackStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY);

        mediaSession.setActive(true);
        mediaSession.setPlaybackState(playbackStateBuilder.build());
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                // TODO: Corrigir implementação do click dos botões de headset
                if (mediaPlayer == null || trackList == null || trackList.isEmpty()) {
                    return;
                }
                if (mediaPlayer.isPlaying()) {
                    pause();
                } else {
                    play();
                }
            }
        });

        setSessionToken(mediaSession.getSessionToken());

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);

        queue = new ArrayList<>();

        currentPosition = 0;
        isShuffle = AppPreferences.with(this).isShuffleEnabled();
        repeatType = AppPreferences.with(this).getRepeatType();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return trackBinder;
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot("abc", null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        List<MediaBrowserCompat.MediaItem> mediaItemList = new ArrayList<>();
        result.sendResult(mediaItemList);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.release();
        return false;
    }

    /**
     * Pausa uma faixa
     */
    public void pause() {
        mediaPlayer.pause();
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Constants.ACTION_PAUSE));
    }

    /**
     * Dá play em uma faixa que já esteja em execução
     */
    public void play() {
        mediaPlayer.start();
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Constants.ACTION_PLAY));
    }

    /**
     * Reproduz uma nova faixa
     * @param track Faixa a ser reproduzida
     */
    public void playTrack(final Track track) {
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(track.getData());
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    switch (repeatType) {
                        case Constants.TYPE_NO_REPEAT:
                            if (currentPosition < queue.size()) {
                                playNext();
                            }
                            break;

                        case Constants.TYPE_REPEAT_CURRENT:
                            playTrack(track);
                            break;

                        case Constants.TYPE_REPEAT_ALL:
                            playNext();
                            break;
                    }
                }
            });

            Intent i = new Intent(Constants.ACTION_TRACK_CHANGED);
            i.putExtra(Constants.BUNDLE_TRACK, track);
            LocalBroadcastManager.getInstance(this).sendBroadcast(i);

            /*NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
            notificationBuilder
                    .setContentTitle(track.getTitle())
                    .setContentText(track.getArtist())
                    .setLargeIcon(BitmapFactory.decodeFile(track.getAlbumArt()))

                    .setContentIntent(mediaSession.getController().getSessionActivity())
                    .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP))

                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setSmallIcon(R.drawable.ic_logo)
                    .setColor(ContextCompat.getColor(this, R.color.background_primary))

                    .addAction(new NotificationCompat.Action(
                            R.drawable.ic_pause, getString(R.string.btn_pause),
                            MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                                    PlaybackStateCompat.ACTION_PLAY_PAUSE)))

                    .setStyle(new NotificationCompat.MediaStyle()
                            .setMediaSession(mediaSession.getSessionToken())
                            .setShowActionsInCompactView(0));

            startForeground(10, notificationBuilder.build());*/

            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            playNext();
        }
    }

    /**
     * Retorna uma faixa
     *
     * Se estiver no modo aleatório {@link MediaPlayerService#isShuffle}
     * e voltar a uma faixa antes que a primeira (index = 0), cria uma
     * nova fila {@link MediaPlayerService#mixUpQueue()} e começa a
     * reproduzir a partir da primeira (index = 0)
     *
     * Se estiver no modo normal {@link MediaPlayerService#isShuffle} e
     * voltar a uma faixa antes que a primeira (index = 0), reproduz a
     * última (index = list.size() - 1)
     */
    public void playPrevious() {
        Track track;

        currentPosition--;

        if (isShuffle) {
            if (currentPosition < 0) {
                currentPosition = 0;
                mixUpQueue();
            }
            track = queue.get(currentPosition);
        } else {
            if (currentPosition < 0) {
                currentPosition = trackList.size() - 1;
            }
            track = trackList.get(currentPosition);
        }

        playTrack(track);
    }

    /**
     * Avança uma faixa
     *
     * Se estiver no modo aleatório {@link MediaPlayerService#isShuffle}
     * e avançar além da última faixa (index = list.size() - 1), cria uma
     * nova lista {@link MediaPlayerService#mixUpQueue()} e começa a
     * reproduzir a partir da primeira (index = 0)
     *
     * Se estiver no modo normal {@link MediaPlayerService#isShuffle}
     * e avançar além da última faixa (index = list.size() -1), reproduz
     * a primeira (index = 0)
     */
    public void playNext() {
        Track track;

        currentPosition++;

        if (isShuffle) {
            if (currentPosition >= queue.size()) {
                currentPosition = 0;
                mixUpQueue();
            }
            track = queue.get(currentPosition);
        } else {
            if (currentPosition >= trackList.size()) {
                currentPosition = 0;
            }
            track = trackList.get(currentPosition);
        }

        playTrack(track);
    }

    /**
     * Reproduz faixas aleatoriamente
     */
    public void playShuffle() {
        currentPosition = 0;
        isShuffle = true;

        mixUpQueue();

        Intent i = new Intent(Constants.ACTION_SHUFFLE_CHANGED);
        i.putExtra(Constants.BUNDLE_SHUFFLE, isShuffle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);

        playTrack(queue.get(currentPosition));
    }

    public void setCurrentDuration(int duration) {
        mediaPlayer.seekTo(duration);
    }

    /**
     * Habilita/desabilita o modo aleatório
     * @return  true se o aleatório tiver sido ativado
     */
    public boolean changeShuffleState() {
        isShuffle = !isShuffle;

        if (isShuffle) {
            mixUpQueue();
            currentPosition = queue.indexOf(trackList.get(currentPosition));
        } else {
            currentPosition = trackList.indexOf(queue.get(currentPosition));
        }

        AppPreferences.with(this).setShuffleEnabled(isShuffle);

        Intent i = new Intent(Constants.ACTION_SHUFFLE_CHANGED);
        i.putExtra(Constants.BUNDLE_SHUFFLE, isShuffle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);

        return isShuffle;
    }

    public int changeRepeatType() {
        if (--repeatType < Constants.TYPE_NO_REPEAT) {
            repeatType = Constants.TYPE_REPEAT_ALL;
        }

        AppPreferences.with(this).setRepeatType(repeatType);

        Intent i = new Intent(Constants.ACTION_REPEAT_TYPE_CHANGED);
        i.putExtra(Constants.BUNDLE_REPEAT_TYPE, repeatType);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);

        return repeatType;
    }

    /**
     * Reseta a fila de músicas
     * Toca uma faixa, se não for null
     */
    public void resetQueue(@Nullable Track track) {
        currentPosition = 0;
        mixUpQueue();

        if (track != null) {
            if (isShuffle) {
                currentPosition = queue.indexOf(track);
            } else {
                currentPosition = trackList.indexOf(track);
            }
            playTrack(track);
        }
    }

    /**
     * Recupera a faixa atual
     */
    public Track getCurrentTrack() {
        if (isShuffle) {
            return queue.get(currentPosition);
        } else {
            return trackList.get(currentPosition);
        }
    }

    /**
     * Retorna se há alguma música tocando
     * @return  true se estiver tocando
     */
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    /**
     * Retorna se o modo aleatório está ativado
     * @return  true se o aleatório estiver ativado
     */
    public boolean isShuffle() {
        return isShuffle;
    }

    public int getRepeatType() {
        return repeatType;
    }

    /**
     * Retorna a duração formatada já reproduzida da faixa
     * @return  String
     */
    public int getCurrentDuration() {
        return mediaPlayer.getCurrentPosition();
    }

    /**
     * Retorna a duração total formatada da faixa que está tocando
     * @return  String
     */
    public int getTotalDuration() {
        return mediaPlayer.getDuration();
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setTrackList(List<Track> trackList) {
        this.trackList = trackList;
    }

    /**
     * Recria uma fila aleatória
     */
    private void mixUpQueue() {
        queue.clear();
        queue.addAll(trackList);

        Collections.shuffle(queue);
    }

    public class TrackBinder extends Binder {

        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }
}
