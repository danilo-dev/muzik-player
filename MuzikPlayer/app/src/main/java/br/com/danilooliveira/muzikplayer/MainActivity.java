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
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.com.danilooliveira.muzikplayer.adapters.AudioAdapter;
import br.com.danilooliveira.muzikplayer.domain.Audio;
import br.com.danilooliveira.muzikplayer.interfaces.OnAudioClickListener;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout mDrawerLayout;
    private View playerBottomControl;
    private TextView txtCurrentMediaTitle;
    private TextView txtCurrentMediaArtist;
    private ImageButton btnPlayerStateControl;

    private MediaPlayer mediaPlayer;
    private AudioAdapter mAudioAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        playerBottomControl = findViewById(R.id.player_bottom_control);
        txtCurrentMediaTitle = (TextView) findViewById(R.id.txt_current_media_title);
        txtCurrentMediaArtist = (TextView) findViewById(R.id.txt_current_media_artist);
        btnPlayerStateControl = (ImageButton) findViewById(R.id.btn_player_bottom_state_control);
        RecyclerView recyclerView = (RecyclerView) findViewById(android.R.id.list);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        mediaPlayer = new MediaPlayer();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mAudioAdapter = new AudioAdapter(this, new OnAudioClickListener() {
            @Override
            public void onAudioClick(Audio audio) {
                onPlayAudio(audio);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAudioAdapter);

        navigationView.setNavigationItemSelectedListener(this);

        findAudioFiles();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        mDrawerLayout.closeDrawer(GravityCompat.START, true);
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

    private void onPlayAudio(Audio audio) {
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(audio.getData());
            mediaPlayer.prepare();

            txtCurrentMediaTitle.setText(audio.getTitle());
            txtCurrentMediaArtist.setText(audio.getArtist());
            btnPlayerStateControl.setImageResource(R.drawable.ic_pause);
            playerBottomControl.setVisibility(View.VISIBLE);

            btnPlayerStateControl.setOnClickListener(new View.OnClickListener() {
                private boolean isPlaying = true;

                @Override
                public void onClick(View view) {
                    isPlaying = !isPlaying;
                    if (isPlaying) {
                        btnPlayerStateControl.setImageResource(R.drawable.ic_pause);
                        mediaPlayer.start();
                    } else {
                        btnPlayerStateControl.setImageResource(R.drawable.ic_play);
                        mediaPlayer.pause();
                    }
                }
            });

            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void findAudioFiles() {
        Uri audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] audioColumns = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DATA
        };

        String audioConditions = MediaStore.Audio.Media.IS_MUSIC + "=1";
        String audioOrder = MediaStore.Audio.Media.TITLE + " ASC";

        List<Audio> audioList = new ArrayList<>();

        Cursor audioCursor = getContentResolver().query(audioUri, audioColumns, audioConditions, null, audioOrder);
        if (audioCursor != null) {
            while (audioCursor.moveToNext()) {
                audioList.add(new Audio(audioCursor));
            }
            audioCursor.close();
        }

        mAudioAdapter.setAudioList(audioList);
    }
}
