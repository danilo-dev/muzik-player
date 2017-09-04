package br.com.danilooliveira.muzikplayer.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import br.com.danilooliveira.muzikplayer.R;
import br.com.danilooliveira.muzikplayer.adapters.QueueAdapter;

/**
 * Criado por Danilo de Oliveira (danilo.desenvolvedor@outlook.com) em 03/09/2017.
 */
public class QueueActivity extends BaseActivity {
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

        mQueueAdapter = new QueueAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mQueueAdapter);
        playerBottom.setVisibility(View.VISIBLE);
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
    }
}
