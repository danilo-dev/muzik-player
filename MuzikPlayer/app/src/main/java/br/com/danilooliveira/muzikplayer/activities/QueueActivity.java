package br.com.danilooliveira.muzikplayer.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import br.com.danilooliveira.muzikplayer.R;
import br.com.danilooliveira.muzikplayer.adapters.QueueAdapter;
import br.com.danilooliveira.muzikplayer.domain.Track;

/**
 * Criado por Danilo de Oliveira (danilo.desenvolvedor@outlook.com) em 03/09/2017.
 */
public class QueueActivity extends BaseActivity {
    private static final Uri.Builder uriBuilder = new Uri.Builder().scheme("file");

    private ImageView imgCurrentTrackAlbum;
    private TextView txtCurrentTrackTitle;
    private TextView txtCurrentTrackArtist;

    private QueueAdapter mQueueAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(android.R.id.list);
        View playerBottom = findViewById(R.id.player_bottom_control);
        imgCurrentTrackAlbum = (ImageView) playerBottom.findViewById(R.id.img_album_art);
        txtCurrentTrackTitle = (TextView) playerBottom.findViewById(R.id.txt_current_track_title);
        txtCurrentTrackArtist = (TextView) playerBottom.findViewById(R.id.txt_current_track_artist);

        mQueueAdapter = new QueueAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mQueueAdapter);
        playerBottom.setVisibility(View.VISIBLE);
    }

    private void updateTrackInfo(Track track) {
        txtCurrentTrackTitle.setText(track.getTitle());
        txtCurrentTrackArtist.setText(track.getArtist());

        if (track.getAlbumArt() != null) {
            Picasso.with(this)
                    .load(uriBuilder.path(track.getAlbumArt()).build())
                    .into(imgCurrentTrackAlbum);
        } else {
            imgCurrentTrackAlbum.setImageResource(R.drawable.ic_placeholder_album_small);
        }

        mQueueAdapter.setSelectedTrack(track);
    }

    @Override
    protected BroadcastReceiver onPauseTrack() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO: Atualizar mini-player
            }
        };
    }

    @Override
    protected BroadcastReceiver onPlayTrack() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO: Atualizar mini-player (adapter também??)
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
    protected BroadcastReceiver onTrackChanged() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO: Atualizar mini-player e adapter
            }
        };
    }

    @Override
    protected BroadcastReceiver onTrackListChanged() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Do nothing...
            }
        };
    }

    @Override
    protected void onServiceConnected() {
        mQueueAdapter.setTrackList(mediaPlayerService.getCurrentTrackList());
        updateTrackInfo(mediaPlayerService.getCurrentTrack());
    }
}