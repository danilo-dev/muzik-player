package br.com.danilooliveira.muzikplayer.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
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

import java.util.List;

import br.com.danilooliveira.muzikplayer.R;
import br.com.danilooliveira.muzikplayer.adapters.TrackAdapter;
import br.com.danilooliveira.muzikplayer.asynctasks.TrackFinderTask;
import br.com.danilooliveira.muzikplayer.domain.Track;
import br.com.danilooliveira.muzikplayer.interfaces.OnAdapterListener;
import br.com.danilooliveira.muzikplayer.utils.Constants;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final Uri.Builder uriBuilder = new Uri.Builder().scheme("file");

    private DrawerLayout mDrawerLayout;
    private View playerBottomControl, emptyState;
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
        playerBottomControl = findViewById(R.id.mini_player);
        emptyState = findViewById(android.R.id.empty);
        imgAlbumArt = (ImageView) findViewById(R.id.img_album_art);
        txtCurrentMediaTitle = (TextView) findViewById(R.id.txt_current_track_title);
        txtCurrentMediaArtist = (TextView) findViewById(R.id.txt_current_track_artist);
        btnPlayerBottomStateControl = (ImageButton) findViewById(R.id.btn_play_pause);
        ImageButton btnPlayerBottomPrevious = (ImageButton) findViewById(R.id.btn_previous);
        ImageButton btnPlayerBottomNext = (ImageButton) findViewById(R.id.btn_next);
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
                mediaPlayerService.setTrackList(mTrackAdapter.getTrackList());
                mediaPlayerService.playShuffle(track);
            }

            @Override
            public void onShuffleClick() {
                mediaPlayerService.setTrackList(mTrackAdapter.getTrackList());
                mediaPlayerService.playShuffle(null);
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
                mediaPlayerService.playPause();
            }
        });
        btnPlayerBottomPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayerService.playPrevious(true, true);
            }
        });
        btnPlayerBottomNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayerService.playNext(true);
            }
        });

        findTrackFiles();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.REQUEST_PERMISSION_STORAGE:
                boolean granted = grantResults.length > 0;
                for (int result : grantResults) {
                    if (result == PackageManager.PERMISSION_DENIED) {
                        granted = false;
                    }
                }

                if (granted) {
                    findTrackFiles();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private boolean checkStoragePermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.AppTheme_AlertDialog);
                dialog.setIcon(R.drawable.ic_sd_card)
                        .setTitle(R.string.dialog_permission_storage_title)
                        .setMessage(R.string.dialog_permission_storage_message)
                        .setCancelable(false);
                dialog.setPositiveButton(R.string.dialog_permission_storage_btn_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestStoragePermissions();
                    }
                });
                dialog.show();
            } else {
                requestStoragePermissions();
            }
            return false;
        }
    }

    private void requestStoragePermissions() {
        ActivityCompat.requestPermissions(this,
                new String[] {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, Constants.REQUEST_PERMISSION_STORAGE);
    }

    private void findTrackFiles() {
        if (!checkStoragePermissions()) {
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.info_getting_track_list));
        progressDialog.show();
        new TrackFinderTask(this, false, new TrackFinderTask.OnCompleteListener() {
            @Override
            public void onComplete(List<Track> trackList) {
                progressDialog.dismiss();

                if (!trackList.isEmpty()) {
                    emptyState.setVisibility(View.GONE);
                }
                mTrackAdapter.setTrackList(trackList);
                if (mediaPlayerService != null) {
                    mediaPlayerService.setTrackList(trackList);
                }
            }
        }).execute();
    }

    @Override
    public BroadcastReceiver onTrackChanged() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateTrackInfo((Track) intent.getParcelableExtra(Constants.BUNDLE_TRACK));
            }
        };
    }

    private void updateTrackInfo(Track track) {
        playerBottomControl.setVisibility(View.VISIBLE);
        txtCurrentMediaTitle.setText(track.getTitle());
        txtCurrentMediaArtist.setText(track.getArtist());

        if (track.getAlbumArt() != null) {
            Picasso.with(this)
                    .load(uriBuilder.path(track.getAlbumArt()).build())
                    .into(imgAlbumArt);
        } else {
            imgAlbumArt.setImageResource(R.drawable.ic_placeholder_album_small);
        }

        mTrackAdapter.setSelectedTrack(track);
    }

    @Override
    protected BroadcastReceiver onPlayPauseTrack() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mediaPlayerService.isPlaying()) {
                    btnPlayerBottomStateControl.setImageResource(R.drawable.ic_pause);
                } else {
                    btnPlayerBottomStateControl.setImageResource(R.drawable.ic_play);
                }
            }
        };
    }

    @Override
    protected BroadcastReceiver onRepeatTypeChanged() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Do nothing...
            }
        };
    }

    @Override
    protected BroadcastReceiver onShuffleChanged() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Do nothing...
            }
        };
    }

    @Override
    protected void onServiceConnected() {
        if (mediaPlayerService.getTotalDuration() > 0) {
            updateTrackInfo(mediaPlayerService.getCurrentTrack());
            if (mediaPlayerService.isPlaying()) {
                btnPlayerBottomStateControl.setImageResource(R.drawable.ic_pause);
            } else {
                btnPlayerBottomStateControl.setImageResource(R.drawable.ic_play);
            }
        }

    }
}
