package br.com.danilooliveira.muzikplayer.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import br.com.danilooliveira.muzikplayer.R;
import br.com.danilooliveira.muzikplayer.fragments.TrackInfoFragment;
import br.com.danilooliveira.muzikplayer.utils.Constants;

/**
 * Criado por Danilo de Oliveira (danilo.desenvolvedor@outlook.com) em 06/08/2017.
 */
public class PlayerActivity extends BaseActivity {
    private ViewPager mViewPager;
    private TextView txtCurrentDuration, txtTotalDuration;
    private SeekBar seekTrackIndicator;
    private ImageButton btnShuffle, btnStateControl, btnRepeat;

    private TrackSwipePager trackSwipePager;
    private SimpleDateFormat timeFormatter = new SimpleDateFormat("mm:ss", Locale.getDefault());
    private Timer timer;

    /**
     * Booleano para indicar se o usuário está alterando a posição
     * da música atual.
     * É utilizado para evitar a atualização da "currentDuration"
     * da música nas views, enquanto o usuário está alterando-na.
     */
    private boolean isChangingCurrentDuration = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(null);
        }

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        txtCurrentDuration = (TextView) findViewById(R.id.txt_current_duration);
        txtTotalDuration = (TextView) findViewById(R.id.txt_total_duration);
        seekTrackIndicator = (SeekBar) findViewById(R.id.seek_track_indicator);
        btnShuffle = (ImageButton) findViewById(R.id.btn_shuffle);
        btnStateControl = (ImageButton) findViewById(R.id.btn_state_control);
        btnRepeat = (ImageButton) findViewById(R.id.btn_repeat);

        mViewPager.setOffscreenPageLimit(1);

        trackSwipePager = new TrackSwipePager(getSupportFragmentManager());

        seekTrackIndicator.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private boolean isPlaying;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                txtCurrentDuration.setText(timeFormatter.format(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isChangingCurrentDuration = true;
                if (isPlaying = mediaPlayerService.isPlaying()) {
                    mediaPlayerService.playPause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isChangingCurrentDuration = false;
                mediaPlayerService.setCurrentDuration(seekBar.getProgress());
                if (isPlaying) {
                    mediaPlayerService.playPause();
                }
            }
        });
        btnStateControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayerService.playPause();
            }
        });
        findViewById(R.id.btn_previous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayerService.playPrevious(true, true);
            }
        });
        findViewById(R.id.btn_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayerService.playNext(true);
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_queue:
                startActivity(new Intent(this, QueueActivity.class));
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (timer != null) {
            timer.cancel();
        }
    }

    private void changeShuffle(boolean isShuffle) {
        if (isShuffle) {
            btnShuffle.setImageResource(R.drawable.ic_shuffle_active);
        } else {
            btnShuffle.setImageResource(R.drawable.ic_shuffle_normal);
        }
        trackSwipePager.notifyDataSetChanged();
        mViewPager.setCurrentItem(mediaPlayerService.getCurrentPosition(), false);
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

    private void updateTrackInfo() {
        txtCurrentDuration.setText(timeFormatter.format(mediaPlayerService.getCurrentDuration()));
        txtTotalDuration.setText(timeFormatter.format(mediaPlayerService.getTotalDuration()));
        seekTrackIndicator.setMax(mediaPlayerService.getMediaPlayer().getDuration());
        mViewPager.setCurrentItem(mediaPlayerService.getCurrentPosition(), true);
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
                updateTrackInfo();
            }
        };
    }

    @Override
    protected BroadcastReceiver onPlayPauseTrack() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mediaPlayerService.isPlaying()) {
                    btnStateControl.setImageResource(R.drawable.ic_pause_circle);
                } else {
                    btnStateControl.setImageResource(R.drawable.ic_play_circle);
                }
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
        mViewPager.setAdapter(trackSwipePager);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Do nothing...
            }

            @Override
            public void onPageSelected(int position) {
                if (position > mediaPlayerService.getCurrentPosition()) {
                    mediaPlayerService.playNext(true);
                } else if (position < mediaPlayerService.getCurrentPosition()) {
                    mediaPlayerService.playPrevious(true, false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Do nothing...
            }
        });
        trackSwipePager.notifyDataSetChanged();

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isChangingCurrentDuration
                        && mediaPlayerService != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateDurationInfo();
                        }
                    });
                }
            }
        }, 0, 17);

        btnStateControl.setImageResource(mediaPlayerService.isPlaying()? R.drawable.ic_pause_circle : R.drawable.ic_play_circle);
        updateTrackInfo();
        updateDurationInfo();
        changeShuffle(mediaPlayerService.isShuffle());
        changeRepeat(mediaPlayerService.getRepeatType());
    }

    private class TrackSwipePager extends FragmentStatePagerAdapter {

        TrackSwipePager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return TrackInfoFragment.newInstance(mediaPlayerService.getQueue().get(position));
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return mediaPlayerService.getQueue().size();
        }
    }
}
