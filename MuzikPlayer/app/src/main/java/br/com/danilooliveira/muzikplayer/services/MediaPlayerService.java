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
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.danilooliveira.muzikplayer.domain.Track;
import br.com.danilooliveira.muzikplayer.utils.AppNotification;
import br.com.danilooliveira.muzikplayer.utils.AppPreferences;
import br.com.danilooliveira.muzikplayer.utils.Constants;

public class MediaPlayerService extends MediaBrowserServiceCompat {
    /**
     * Duração mínima reproduzida da música para que ela seja
     * reiniciada ao voltar uma música
     * @see MediaPlayerService#playPrevious(boolean, boolean)
     */
    private static final int DEFAULT_MIN_TIME_TO_RESTART = 2 * 1000;

    private MediaSessionCompat mediaSession;
    private MediaPlayer mediaPlayer;
    private AppNotification appNotification;

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

        PlaybackStateCompat.Builder playbackStateBuilder = new PlaybackStateCompat.Builder()
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
                playPause();
            }
        });

        setSessionToken(mediaSession.getSessionToken());

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
//                MediaPlayerService.this.mediaPlayer = null;
                return false;
            }
        });

        queue = new ArrayList<>();

        currentPosition = 0;
        isShuffle = AppPreferences.with(this).isShuffleEnabled();
        repeatType = AppPreferences.with(this).getRepeatType();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new TrackBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        if (intent != null && intent.getAction() != null && !intent.getAction().isEmpty()) {
            Log.d(MediaPlayerService.class.getSimpleName(), "Action: " + intent.getAction());
            switch (intent.getAction()) {
                case Constants.ACTION_PLAY_PAUSE:
                    playPause();
                    break;

                case Constants.ACTION_NEXT_TRACK:
                    playNext(true);
                    break;

                case Constants.ACTION_PREVIOUS_TRACK:
                    playPrevious(true, true);
                    break;

                default:
                    break;
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return null;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        List<MediaBrowserCompat.MediaItem> mediaItemList = new ArrayList<>();
        result.sendResult(mediaItemList);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                if (appNotification != null) {
                    appNotification.cancelNotification();
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } finally {
                mediaPlayer.release();
            }
        }
    }

    /**
     * Pausa uma música que já esteja em execução,
     * Se estiver pausada, é reproduzida a partir do ponto
     * em que foi dado pause
     */
    public void playPause() {
        boolean isPlaying = mediaPlayer.isPlaying();

        appNotification = new AppNotification.Builder(this)
                // O valor de isPlaying vai se inverter nas próximas linhas
                .setDefault(getCurrentTrack(), !isPlaying)
                .build()
                .show();

        if (isPlaying) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Constants.ACTION_PLAY_PAUSE));
    }

    /**
     * Reproduz uma nova faixa
     * @param track Faixa a ser reproduzida
     * @param defaultBehavior true para reproduzir imediatamente a música atual
     *                        dependendo se a anterior estava em reprodução ou não.
     *                        false para reproduzir imediatamente.
     */
    private void play(final Track track, boolean defaultBehavior) {
        final boolean playNow = !defaultBehavior || mediaPlayer.isPlaying();
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
                                playNext(false);
                            }
                            break;

                        case Constants.TYPE_REPEAT_CURRENT:
                            play(track, false);
                            break;

                        case Constants.TYPE_REPEAT_ALL:
                            playNext(false);
                            break;
                    }
                }
            });

            Intent trackChanged = new Intent(Constants.ACTION_TRACK_CHANGED);
            trackChanged.putExtra(Constants.BUNDLE_TRACK, track);
            LocalBroadcastManager.getInstance(this).sendBroadcast(trackChanged);

            appNotification = new AppNotification.Builder(this)
                    .setDefault(track, playNow)
                    .build()
                    .show();

            if (playNow) {
                mediaPlayer.start();
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Constants.ACTION_PLAY_PAUSE));
            }
        } catch (IOException e) {
            e.printStackTrace();
            playNext(false);
        }
    }

    public void playFromQueue(Track track) {
        if (isShuffle) {
            currentPosition = queue.indexOf(track);
        } else {
            currentPosition = trackList.indexOf(track);
        }

        play(track, true);
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
     *
     * @param defaultBehavior true para reproduzir imediatamente a música atual
     *                        dependendo se a anterior estava em reprodução ou não.
     *                        false para reproduzir imediatamente.
     *                        @see MediaPlayerService#play(Track, boolean)
     *
     * @param defaultRestartBehavior true para verificar se a música deve
     *                        ser reiniciada ou se a música anterior
     *                        deve ser tocada, dependendo da duração
     *                        já reproduzida pela música atual
     *                        @see MediaPlayerService#DEFAULT_MIN_TIME_TO_RESTART
     */
    public void playPrevious(boolean defaultBehavior, boolean defaultRestartBehavior) {
        Track track;

        if (defaultRestartBehavior && getCurrentDuration() >= DEFAULT_MIN_TIME_TO_RESTART) {
            setCurrentDuration(0);
            return;
        }

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

        play(track, defaultBehavior);
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
     *
     * @param defaultBehavior true para reproduzir imediatamente a música atual
     *                        dependendo se a anterior estava em reprodução ou não.
     *                        false para reproduzir imediatamente.
     *                        @see MediaPlayerService#play(Track, boolean)
     */
    public void playNext(boolean defaultBehavior) {
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

        play(track, defaultBehavior);
    }

    /**
     * Reproduz faixas aleatoriamente
     */
    public void playShuffle(@Nullable Track track) {
        isShuffle = true;
        mixUpQueue();

        if (track == null) {
            track = queue.get(currentPosition = 0);
        } else {
            currentPosition = queue.indexOf(track);
        }
        play(track, false);

        Intent i = new Intent(Constants.ACTION_SHUFFLE_CHANGED);
        i.putExtra(Constants.BUNDLE_SHUFFLE, isShuffle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
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

    public void moveTrackTo(int fromPos, int toPos) {
        Track track = getCurrentTrack();
        if (isShuffle) {
            queue.add(toPos, queue.remove(fromPos));
            currentPosition = queue.indexOf(track);
        } else {
            trackList.add(toPos, trackList.remove(fromPos));
            currentPosition = trackList.indexOf(track);
        }
    }

    public void removeFromQueue(int position) {
        Track track = getCurrentTrackList().get(position);
        if (position <= currentPosition) {
            if (position == currentPosition) {
                track.setSelected(false);
                playNext(true);
            }
            currentPosition--;
        }
        getCurrentTrackList().remove(position);
    }

    public void resetTrackList() {
        appNotification.cancelNotification();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        mediaPlayer.stop();
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

    public int getCurrentPosition() {
        return currentPosition;
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
     * Retorna a duração já reproduzida da faixa
     * @return  int
     */
    public int getCurrentDuration() {
        return mediaPlayer.getCurrentPosition();
    }

    /**
     * Retorna a duração total da faixa que está tocando
     * @return  int
     */
    public int getTotalDuration() {
        return mediaPlayer.getDuration();
    }

    public List<Track> getCurrentTrackList() {
        if (isShuffle) {
            return queue;
        } else {
            return trackList;
        }
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
