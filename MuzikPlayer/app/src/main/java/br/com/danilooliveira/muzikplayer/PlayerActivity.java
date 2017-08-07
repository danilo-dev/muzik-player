package br.com.danilooliveira.muzikplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import br.com.danilooliveira.muzikplayer.domain.Track;
import br.com.danilooliveira.muzikplayer.utils.Constants;

/**
 * Criado por Danilo de Oliveira (danilo.desenvolvedor@outlook.com) em 06/08/2017.
 */
public class PlayerActivity extends BaseActivity {
    private ImageView imgAlbumArt;
    private TextView txtTitle, txtArtist, txtCurrentDuration, txtTotalDuration;
    private SeekBar seekTrackIndicator;
    private ImageButton btnShuffle, btnStateControl, btnRepeat;

    private Timer timer = new Timer();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        imgAlbumArt = (ImageView) findViewById(R.id.img_album_art);
        txtTitle = (TextView) findViewById(R.id.txt_title);
        txtArtist = (TextView) findViewById(R.id.txt_artist);
        txtCurrentDuration = (TextView) findViewById(R.id.txt_current_duration);
        txtTotalDuration = (TextView) findViewById(R.id.txt_total_duration);
        seekTrackIndicator = (SeekBar) findViewById(R.id.seek_track_indicator);
        btnShuffle = (ImageButton) findViewById(R.id.btn_shuffle);
        btnStateControl = (ImageButton) findViewById(R.id.btn_state_control);
        btnRepeat = (ImageButton) findViewById(R.id.btn_repeat);

        txtTitle.setSelected(true);
        txtArtist.setSelected(true);

        btnStateControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayerService.isPlaying()) {
                    mediaPlayerService.onPause();
                } else {
                    mediaPlayerService.onPlay();
                }
            }
        });
        findViewById(R.id.btn_previous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayerService.onPreviousTrack();
            }
        });
        findViewById(R.id.btn_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayerService.onNextTrack();
            }
        });

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mediaPlayerService != null && mediaPlayerService.isPlaying()) {
                            txtCurrentDuration.setText(mediaPlayerService.getCurrentDuration());
                            seekTrackIndicator.setProgress(mediaPlayerService.getMediaPlayer().getCurrentPosition());
                        }
                    }
                });
            }
        }, 0, 17);
    }

    @Override
    protected BroadcastReceiver onTrackChanged() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Track track = intent.getParcelableExtra(Constants.BUNDLE_TRACK);

                txtTitle.setText(track.getTitle());
                txtArtist.setText(track.getArtist());

                txtTotalDuration.setText(mediaPlayerService.getTotalDuration());
                seekTrackIndicator.setMax(mediaPlayerService.getMediaPlayer().getDuration());
            }
        };
    }

    @Override
    protected BroadcastReceiver onPauseTrack() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                btnStateControl.setImageResource(R.drawable.ic_play_circle);
            }
        };
    }

    @Override
    protected BroadcastReceiver onPlayTrack() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                btnStateControl.setImageResource(R.drawable.ic_pause_circle);
            }
        };
    }

    @Override
    protected void onServiceConnected() {
        Track track = mediaPlayerService.getCurrentTrack();

        txtTitle.setText(track.getTitle());
        txtArtist.setText(track.getArtist());

        txtTotalDuration.setText(mediaPlayerService.getTotalDuration());
        seekTrackIndicator.setMax(mediaPlayerService.getMediaPlayer().getDuration());
    }
}
