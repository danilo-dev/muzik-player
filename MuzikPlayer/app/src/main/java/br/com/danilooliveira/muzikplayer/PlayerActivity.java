package br.com.danilooliveira.muzikplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
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

    private SimpleDateFormat timeFormatter = new SimpleDateFormat("mm:ss", Locale.getDefault());
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

        seekTrackIndicator.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                txtCurrentDuration.setText(timeFormatter.format(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mediaPlayerService.changeTrackRunningState();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayerService.setCurrentDuration(seekBar.getProgress());
                mediaPlayerService.changeTrackRunningState();
            }
        });
        btnStateControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayerService.changeTrackRunningState();
            }
        });
        findViewById(R.id.btn_previous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayerService.playPrevious();
            }
        });
        findViewById(R.id.btn_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayerService.playNext();
            }
        });
        btnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayerService.changeShuffleState();
            }
        });
        btnRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayerService.changeRepeatType();
            }
        });

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mediaPlayerService != null && mediaPlayerService.isPlaying()) {
                            updateDurationInfo();
                        }
                    }
                });
            }
        }, 0, 17);
    }

    private void changeShuffle(boolean isShuffle) {
        if (isShuffle) {
            btnShuffle.setImageResource(R.drawable.ic_shuffle_active);
        } else {
            btnShuffle.setImageResource(R.drawable.ic_shuffle_normal);
        }
    }

    private void changeRepeat(int repeatType) {
        switch (repeatType) {
            case Constants.TYPE_NO_REPEAT:
                btnRepeat.setImageResource(R.drawable.ic_no_repeat);
                break;

            case Constants.TYPE_REPEAT_CURRENT:
                btnRepeat.setImageResource(R.drawable.ic_repeat_current);
                break;

            case Constants.TYPE_REPEAT_ALL:
                btnRepeat.setImageResource(R.drawable.ic_repeat);
                break;
        }
    }

    private void updateTrackInfo(Track track) {
        txtTitle.setText(track.getTitle());
        txtArtist.setText(track.getArtist());

        if (track.getAlbumArt() != null) {
            Picasso.with(this).load(Uri.fromFile(new File(track.getAlbumArt()))).into(imgAlbumArt);
        } else {
            imgAlbumArt.setImageResource(R.drawable.ic_placeholder_album_large);
        }

        txtTotalDuration.setText(timeFormatter.format(mediaPlayerService.getTotalDuration()));
        seekTrackIndicator.setMax(mediaPlayerService.getMediaPlayer().getDuration());
    }

    private void updateDurationInfo() {
        txtCurrentDuration.setText(timeFormatter.format(mediaPlayerService.getCurrentDuration()));
        seekTrackIndicator.setProgress(mediaPlayerService.getMediaPlayer().getCurrentPosition());
    }

    @Override
    protected BroadcastReceiver onTrackChanged() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateTrackInfo((Track) intent.getParcelableExtra(Constants.BUNDLE_TRACK));
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
    protected BroadcastReceiver onRepeatTypeChanged() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                changeRepeat(intent.getIntExtra(Constants.BUNDLE_REPEAT_TYPE, Constants.TYPE_REPEAT_ALL));
            }
        };
    }

    @Override
    protected BroadcastReceiver onShuffleChanged() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                changeShuffle(intent.getBooleanExtra(Constants.BUNDLE_SHUFFLE, false));
            }
        };
    }

    @Override
    protected void onServiceConnected() {
        btnStateControl.setImageResource(mediaPlayerService.isPlaying()? R.drawable.ic_pause_circle : R.drawable.ic_play_circle);
        updateTrackInfo(mediaPlayerService.getCurrentTrack());
        updateDurationInfo();
        changeShuffle(mediaPlayerService.isShuffle());
        changeRepeat(mediaPlayerService.getRepeatType());
    }
}
