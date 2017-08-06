package br.com.danilooliveira.muzikplayer.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.com.danilooliveira.muzikplayer.domain.Track;
import br.com.danilooliveira.muzikplayer.interfaces.OnMediaPlayerListener;

public class MediaPlayerService extends Service {
    private OnMediaPlayerListener listener;
    private IBinder trackBinder = new TrackBinder();

    private MediaPlayer mediaPlayer;

    private Random random;

    private List<Track> mTrackList;
    private List<Track> trackHistoryList;
    private int currentPosition;

    public MediaPlayerService() {
        mediaPlayer = new MediaPlayer();

        random = new Random();

        mTrackList = new ArrayList<>();
        trackHistoryList = new ArrayList<>();
        currentPosition = 0;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return trackBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.release();
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    /**
     * Pausa uma faixa
     */
    public void onPause() {
        mediaPlayer.pause();
        listener.onPauseTrack();
    }

    /**
     * Dá play em uma faixa que já esteja em execução
     */
    public void onPlay() {
        mediaPlayer.start();
        listener.onPlayTrack();
    }

    /**
     * Reproduz uma nova faixa
     * @param track Faixa a ser reproduzida
     */
    public void onPlayTrack(Track track) {
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(track.getData());
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    onNextTrack();
                }
            });

            listener.onTrackChanged(track);

            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            onNextTrack();
        }
    }

    /**
     * Retorna uma faixa
     * Se houver faixas no histórico, é reproduzida
     * Senão, reproduz uma nova faixa e a adiciona ao histórico
     */
    public void onPreviousTrack() {
        Track track;

        currentPosition--;

        if (currentPosition < 0) {
            currentPosition = 0;
            track = getRandomTrack();
            trackHistoryList.add(0, track);
        } else {
            track = trackHistoryList.get(currentPosition);
        }

        onPlayTrack(track);
    }

    /**
     * Avança uma faixa
     * Se houver faixas no histórico relativa a posição, é reproduzida
     * Senão, reproduz uma nova faixa e a adiciona ao histórico
     */
    public void onNextTrack() {
        Track track;

        currentPosition++;

        if (currentPosition >= trackHistoryList.size()) {
            track = getRandomTrack();
            trackHistoryList.add(track);
        } else {
            track = trackHistoryList.get(currentPosition);
        }

        onPlayTrack(track);
    }

    /**
     * Reproduz faixas aleatoriamente
     */
    public void onShuffle() {
        currentPosition = 0;
        trackHistoryList.clear();

        Track track = getRandomTrack();
        trackHistoryList.add(track);

        onPlayTrack(track);
    }

    /**
     * Limpa o histórico de faixas
     * Reproduz uma nova faixa, se não for null
     * @param track Faixa a ser tocada
     */
    public void resetHistoryList(@Nullable Track track) {
        currentPosition = 0;
        trackHistoryList.clear();

        if (track != null) {
            trackHistoryList.add(track);
            onPlayTrack(track);
        }
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void setTrackList(List<Track> trackList) {
        mTrackList = trackList;
    }

    public void setListener(OnMediaPlayerListener listener) {
        this.listener = listener;
    }

    private Track getRandomTrack() {
        return mTrackList.get(random.nextInt(mTrackList.size()));
    }

    public class TrackBinder extends Binder {

        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }
}
