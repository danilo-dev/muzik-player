package br.com.danilooliveira.muzikplayer.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import br.com.danilooliveira.muzikplayer.domain.Track;
import br.com.danilooliveira.muzikplayer.utils.Constants;

public class MediaPlayerService extends Service {
    private IBinder trackBinder = new TrackBinder();

    private MediaPlayer mediaPlayer;

    private Random random;
    private SimpleDateFormat timeFormatter;

    private List<Track> mTrackList;
    private List<Track> trackHistoryList;
    private int currentPosition;
    private boolean isShuffle;

    public MediaPlayerService() {
        mediaPlayer = new MediaPlayer();

        random = new Random();
        timeFormatter = new SimpleDateFormat("mm:ss", Locale.getDefault());

        mTrackList = new ArrayList<>();
        trackHistoryList = new ArrayList<>();
        currentPosition = 0;
        isShuffle = true;
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

    /**
     * Pausa uma faixa
     */
    public void onPause() {
        mediaPlayer.pause();
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Constants.ACTION_PAUSE));
    }

    /**
     * Dá play em uma faixa que já esteja em execução
     */
    public void onPlay() {
        mediaPlayer.start();
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Constants.ACTION_PLAY));
    }

    /**
     * Reproduz uma nova faixa
     * @param track Faixa a ser reproduzida
     */
    public void playTrack(Track track) {
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(track.getData());
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    playNextTrack();
                }
            });

            Intent i = new Intent(Constants.ACTION_TRACK_CHANGED);
            i.putExtra(Constants.BUNDLE_TRACK, track);
            LocalBroadcastManager.getInstance(this).sendBroadcast(i);

            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            playNextTrack();
        }
    }

    /**
     * Retorna uma faixa
     * Se houver faixas no histórico, é reproduzida
     * Senão, reproduz uma nova faixa e a adiciona ao histórico
     */
    public void playPreviousTrack() {
        Track track;

        currentPosition--;

        if (isShuffle) {
            if (currentPosition < 0) {
                currentPosition = 0;
                track = getRandomTrack();
                trackHistoryList.add(0, track);
            } else {
                track = trackHistoryList.get(currentPosition);
            }
        } else {
            track = mTrackList.get(currentPosition);
        }

        playTrack(track);
    }

    /**
     * Avança uma faixa
     * Se houver faixas no histórico relativa a posição, é reproduzida
     * Senão, reproduz uma nova faixa e a adiciona ao histórico
     */
    public void playNextTrack() {
        Track track;

        currentPosition++;

        if (isShuffle) {
            if (currentPosition >= trackHistoryList.size()) {
                track = getRandomTrack();
                trackHistoryList.add(track);
            } else {
                track = trackHistoryList.get(currentPosition);
            }
        } else {
            track = mTrackList.get(currentPosition);
        }

        playTrack(track);
    }

    /**
     * Reproduz faixas aleatoriamente
     */
    public void playShuffle() {
        currentPosition = 0;
        trackHistoryList.clear();

        Track track = getRandomTrack();
        trackHistoryList.add(track);

        playTrack(track);
    }

    /**
     * Habilita/desabilita o modo aleatório
     * @return  true se o aleatório tiver sido ativado
     */
    public boolean changeShuffleState() {
        isShuffle = !isShuffle;

        if (isShuffle) {
            trackHistoryList.add(mTrackList.get(currentPosition));
            currentPosition = 0;
        } else {
            currentPosition = mTrackList.indexOf(trackHistoryList.get(currentPosition));
            trackHistoryList.clear();
        }

        return isShuffle;
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
            if (isShuffle) {
                trackHistoryList.add(track);
            } else {
                currentPosition = mTrackList.indexOf(track);
            }
            playTrack(track);
        }
    }

    /**
     * Recupera a faixa atual
     * @return  Faixa
     */
    public Track getCurrentTrack() {
        if (isShuffle) {
            return trackHistoryList.get(currentPosition);
        } else {
            return mTrackList.get(currentPosition);
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

    /**
     * Retorna a duração formatada já reproduzida da faixa
     * @return  String
     */
    public String getCurrentDuration() {
        return timeFormatter.format(mediaPlayer.getCurrentPosition());
    }

    /**
     * Retorna a duração total formatada da faixa que está tocando
     * @return  String
     */
    public String getTotalDuration() {
        return timeFormatter.format(mediaPlayer.getDuration());
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setTrackList(List<Track> trackList) {
        mTrackList = trackList;
    }

    /**
     * Obtém uma faixa aleatória da lista
     * @return  Faixa
     */
    private Track getRandomTrack() {
        return mTrackList.get(random.nextInt(mTrackList.size()));
    }

    public class TrackBinder extends Binder {

        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }
}
