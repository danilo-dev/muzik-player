package br.com.danilooliveira.muzikplayer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import br.com.danilooliveira.muzikplayer.services.MediaPlayerService;
import br.com.danilooliveira.muzikplayer.utils.Constants;

/**
 * Criado por Danilo de Oliveira (danilo.desenvolvedor@outlook.com) em 06/08/2017.
 */
public abstract class BaseActivity extends AppCompatActivity {
    private BroadcastReceiver onPauseTrackReceiver;
    private BroadcastReceiver onPlayTrackReceiver;
    private BroadcastReceiver onRepeatTypeChangedReceiver;
    private BroadcastReceiver onShuffleChangedReceiver;
    private BroadcastReceiver onTrackChangedReceiver;

    protected MediaPlayerService mediaPlayerService;

    private Intent mediaIntent;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MediaPlayerService.TrackBinder binder = (MediaPlayerService.TrackBinder) iBinder;

            mediaPlayerService = binder.getService();
            BaseActivity.this.onServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onPauseTrackReceiver = onPauseTrack();
        onPlayTrackReceiver = onPlayTrack();
        onRepeatTypeChangedReceiver = onRepeatTypeChanged();
        onShuffleChangedReceiver = onShuffleChanged();
        onTrackChangedReceiver = onTrackChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mediaIntent == null) {
            mediaIntent = new Intent(this, MediaPlayerService.class);
            startService(mediaIntent);
            bindService(mediaIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(onPauseTrackReceiver, new IntentFilter(Constants.ACTION_PAUSE));
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(onPlayTrackReceiver, new IntentFilter(Constants.ACTION_PLAY));
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(onRepeatTypeChangedReceiver, new IntentFilter(Constants.ACTION_REPEAT_TYPE_CHANGED));
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(onShuffleChangedReceiver, new IntentFilter(Constants.ACTION_SHUFFLE_CHANGED));
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(onTrackChangedReceiver, new IntentFilter(Constants.ACTION_TRACK_CHANGED));
    }

    @Override
    protected void onDestroy() {
        stopService(mediaIntent);
        unbindService(serviceConnection);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onPauseTrackReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onPlayTrackReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onRepeatTypeChangedReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onShuffleChangedReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onTrackChangedReceiver);
        super.onDestroy();
    }

    protected abstract BroadcastReceiver onPauseTrack();

    protected abstract BroadcastReceiver onPlayTrack();

    protected abstract BroadcastReceiver onRepeatTypeChanged();

    protected abstract BroadcastReceiver onShuffleChanged();

    protected abstract BroadcastReceiver onTrackChanged();

    protected abstract void onServiceConnected();
}
