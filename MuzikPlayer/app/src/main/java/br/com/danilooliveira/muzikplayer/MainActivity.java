package br.com.danilooliveira.muzikplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import br.com.danilooliveira.muzikplayer.adapters.TrackAdapter;
import br.com.danilooliveira.muzikplayer.domain.Track;
import br.com.danilooliveira.muzikplayer.interfaces.OnAdapterListener;
import br.com.danilooliveira.muzikplayer.utils.Constants;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout mDrawerLayout;
    private View playerBottomControl;
    private ImageView imgAlbumArt;
    private TextView txtCurrentMediaTitle;
    private TextView txtCurrentMediaArtist;
    private ImageButton btnPlayerBottomStateControl;

    private TrackAdapter mTrackAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        playerBottomControl = findViewById(R.id.player_bottom_control);
        imgAlbumArt = (ImageView) findViewById(R.id.img_album_art);
        txtCurrentMediaTitle = (TextView) findViewById(R.id.txt_current_track_title);
        txtCurrentMediaArtist = (TextView) findViewById(R.id.txt_current_track_artist);
        btnPlayerBottomStateControl = (ImageButton) findViewById(R.id.btn_player_bottom_state_control);
        ImageButton btnPlayerBottomPrevious = (ImageButton) findViewById(R.id.btn_player_bottom_previous);
        ImageButton btnPlayerBottomNext = (ImageButton) findViewById(R.id.btn_player_bottom_next);
        RecyclerView recyclerView = (RecyclerView) findViewById(android.R.id.list);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        txtCurrentMediaTitle.setSelected(true);
        txtCurrentMediaArtist.setSelected(true);

        mTrackAdapter = new TrackAdapter(this, new OnAdapterListener() {
            @Override
            public void onTrackClick(Track track) {
                mediaPlayerService.resetHistoryList(track);
            }

            @Override
            public void onShuffleClick() {
                mediaPlayerService.playShuffle();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mTrackAdapter);

        navigationView.setNavigationItemSelectedListener(this);

        playerBottomControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, PlayerActivity.class));
            }
        });
        btnPlayerBottomStateControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mediaPlayerService.isPlaying()) {
                    btnPlayerBottomStateControl.setImageResource(R.drawable.ic_pause);
                    mediaPlayerService.play();
                } else {
                    btnPlayerBottomStateControl.setImageResource(R.drawable.ic_play);
                    mediaPlayerService.pause();
                }
            }
        });
        btnPlayerBottomPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayerService.playPreviousTrack();
            }
        });
        btnPlayerBottomNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayerService.playNextTrack();
            }
        });

        findTrackFiles();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        mDrawerLayout.closeDrawer(GravityCompat.START, true);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void findTrackFiles() {
        Uri trackUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] trackColumns = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
        };

        String trackConditions = MediaStore.Audio.Media.IS_MUSIC + "=1";
        String trackOrder = MediaStore.Audio.Media.TITLE + " ASC";

        List<Track> trackList = new ArrayList<>();

        Cursor trackCursor = getContentResolver().query(trackUri, trackColumns, trackConditions, null, trackOrder);
        if (trackCursor != null) {
            while (trackCursor.moveToNext()) {
                trackList.add(new Track(trackCursor));
            }
            trackCursor.close();
        }

        mTrackAdapter.setTrackList(trackList);
    }

    @Override
    public BroadcastReceiver onTrackChanged() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Track track = intent.getParcelableExtra(Constants.BUNDLE_TRACK);

                playerBottomControl.setVisibility(View.VISIBLE);
                txtCurrentMediaTitle.setText(track.getTitle());
                txtCurrentMediaArtist.setText(track.getArtist());

                if (track.getAlbumArt() != null) {
                    Picasso.with(context)
                            .load(Uri.fromFile(new File(track.getAlbumArt())))
                            .into(imgAlbumArt);
                } else {
                    imgAlbumArt.setImageResource(R.drawable.ic_placeholder_album_small);
                }
            }
        };
    }

    @Override
    public BroadcastReceiver onPauseTrack() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                btnPlayerBottomStateControl.setImageResource(R.drawable.ic_play);
            }
        };
    }

    @Override
    public BroadcastReceiver onPlayTrack() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                btnPlayerBottomStateControl.setImageResource(R.drawable.ic_pause);
            }
        };
    }

    @Override
    protected void onServiceConnected() {
        mediaPlayerService.setTrackList(mTrackAdapter.getTrackList());
    }
}
