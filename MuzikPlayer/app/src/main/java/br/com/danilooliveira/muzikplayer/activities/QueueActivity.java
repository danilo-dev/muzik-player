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
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import br.com.danilooliveira.muzikplayer.R;
import br.com.danilooliveira.muzikplayer.adapters.QueueAdapter;
import br.com.danilooliveira.muzikplayer.domain.Track;
import br.com.danilooliveira.muzikplayer.interfaces.OnAdapterListener;
import br.com.danilooliveira.muzikplayer.utils.Constants;

/**
 * Criado por Danilo de Oliveira (danilo.desenvolvedor@outlook.com) em 03/09/2017.
 */
public class QueueActivity extends BaseActivity {
    private static final Uri.Builder uriBuilder = new Uri.Builder().scheme("file");

    private RecyclerView mRecyclerView;
    private ImageView imgCurrentTrackAlbum;
    private TextView txtCurrentTrackTitle;
    private TextView txtCurrentTrackArtist;
    private ImageButton btnPlayPause;

    private QueueAdapter mQueueAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mRecyclerView = (RecyclerView) findViewById(android.R.id.list);
        View miniPlayer = findViewById(R.id.mini_player);
        imgCurrentTrackAlbum = (ImageView) miniPlayer.findViewById(R.id.img_album_art);
        txtCurrentTrackTitle = (TextView) miniPlayer.findViewById(R.id.txt_current_track_title);
        txtCurrentTrackArtist = (TextView) miniPlayer.findViewById(R.id.txt_current_track_artist);
        btnPlayPause = (ImageButton) miniPlayer.findViewById(R.id.btn_play_pause);

        txtCurrentTrackTitle.setSelected(true);
        txtCurrentTrackArtist.setSelected(true);

        mQueueAdapter = new QueueAdapter(this, new OnAdapterListener() {
            @Override
            public void onTrackClick(Track track) {
                mediaPlayerService.playFromQueue(track);
            }

            @Override
            public void onShuffleClick() {
                // Do nothing...
            }
        });
        ItemTouchHelper recyclerTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.START | ItemTouchHelper.END) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                Toast.makeText(mediaPlayerService, "Moved", Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                mediaPlayerService.removeFromQueue(position);
                mQueueAdapter.notifyItemRemoved(position);
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mQueueAdapter);
        recyclerTouchHelper.attachToRecyclerView(mRecyclerView);
        miniPlayer.setVisibility(View.VISIBLE);

        miniPlayer.findViewById(R.id.btn_previous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerService.playPrevious(true, true);
            }
        });
        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerService.playPause();
            }
        });
        miniPlayer.findViewById(R.id.btn_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerService.playNext(true);
            }
        });
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
                btnPlayPause.setImageResource(R.drawable.ic_play);
            }
        };
    }

    @Override
    protected BroadcastReceiver onPlayTrack() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                btnPlayPause.setImageResource(R.drawable.ic_pause);
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
                updateTrackInfo((Track) intent.getParcelableExtra(Constants.BUNDLE_TRACK));
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
        if (mediaPlayerService.isPlaying()) {
            btnPlayPause.setImageResource(R.drawable.ic_pause);
        } else {
            btnPlayPause.setImageResource(R.drawable.ic_play);
        }
        mRecyclerView.scrollToPosition(mediaPlayerService.getCurrentPosition());
    }
}
