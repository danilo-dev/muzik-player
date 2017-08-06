package br.com.danilooliveira.muzikplayer;

import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.com.danilooliveira.muzikplayer.adapters.TrackAdapter;
import br.com.danilooliveira.muzikplayer.domain.Track;
import br.com.danilooliveira.muzikplayer.interfaces.OnAdapterListener;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout mDrawerLayout;
    private View playerBottomControl;
    private ImageView imgAlbumArt;
    private TextView txtCurrentMediaTitle;
    private TextView txtCurrentMediaArtist;
    private ImageButton btnPlayerBottomStateControl;

    private MediaPlayer mediaPlayer;
    private TrackAdapter mTrackAdapter;

    private List<Track> trackHistoryList = new ArrayList<>();
    private int currentPosition = 0;

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

        mediaPlayer = new MediaPlayer();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        txtCurrentMediaTitle.setSelected(true);
        txtCurrentMediaArtist.setSelected(true);

        mTrackAdapter = new TrackAdapter(this, new OnAdapterListener() {
            @Override
            public void onTrackClick(Track track) {
                currentPosition = 0;

                if (!trackHistoryList.isEmpty()) {
                    trackHistoryList.clear();
                }

                trackHistoryList.add(track);
                onPlayTrack(track);
            }

            @Override
            public void onShuffleClick() {
                onShuffle();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mTrackAdapter);

        navigationView.setNavigationItemSelectedListener(this);

        btnPlayerBottomStateControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mediaPlayer.isPlaying()) {
                    btnPlayerBottomStateControl.setImageResource(R.drawable.ic_pause);
                    mediaPlayer.start();
                } else {
                    btnPlayerBottomStateControl.setImageResource(R.drawable.ic_play);
                    mediaPlayer.pause();
                }
            }
        });
        btnPlayerBottomPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPreviousTrack();
            }
        });
        btnPlayerBottomNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNextTrack();
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

    private void onPlayTrack(Track track) {
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(track.getData());
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    btnPlayerBottomStateControl.setImageResource(R.drawable.ic_play);
                    onNextTrack();
                }
            });

            if (track.getAlbumArt() != null) {
                Picasso.with(this)
                        .load(Uri.fromFile(new File(track.getAlbumArt())))
                        .into(imgAlbumArt);
            } else {
                imgAlbumArt.setImageResource(R.drawable.ic_placeholder_album_small);
            }
            txtCurrentMediaTitle.setText(track.getTitle());
            txtCurrentMediaArtist.setText(track.getArtist());
            btnPlayerBottomStateControl.setImageResource(R.drawable.ic_pause);
            playerBottomControl.setVisibility(View.VISIBLE);

            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onPreviousTrack() {
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

    private void onNextTrack() {
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

    private void onShuffle() {
        currentPosition = 0;
        trackHistoryList.clear();

        Track track = getRandomTrack();
        trackHistoryList.add(track);

        onPlayTrack(track);
    }

    private Track getRandomTrack() {
        return mTrackAdapter.getTrackList().get(new Random().nextInt(mTrackAdapter.getItemCount()));
    }

    private void findTrackFiles() {
        Uri trackUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] trackColumns = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
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
}
