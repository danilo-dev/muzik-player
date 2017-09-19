package br.com.danilooliveira.muzikplayer.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.com.danilooliveira.muzikplayer.R;
import br.com.danilooliveira.muzikplayer.domain.Track;
import br.com.danilooliveira.muzikplayer.interfaces.OnAdapterListener;

/**
 * Criado por Danilo de Oliveira (danilo.desenvolvedor@outlook.com) em 04/08/2017.
 */
public class TrackAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_BUTTON = 0;
    private static final int TYPE_TRACK = 1;

    private static final Uri.Builder uriBuilder = new Uri.Builder();
    private static final SimpleDateFormat timeFormatter = new SimpleDateFormat("mm:ss", Locale.getDefault());

    private OnAdapterListener audioClickListener;
    private LayoutInflater layoutInflater;
    private Context context;

    private List<Track> trackList;

    public TrackAdapter(Context context, OnAdapterListener audioClickListener) {
        uriBuilder.scheme("file");
        this.audioClickListener = audioClickListener;
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_TRACK:
                return new TrackViewHolder(layoutInflater.inflate(R.layout.adapter_track, parent, false));

            default:
                return new ButtonShuffleViewHolder(layoutInflater.inflate(R.layout.adapter_item_shuffle, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_TRACK:
                ((TrackViewHolder) holder).onBind(trackList.get(--position));
                break;

            case TYPE_BUTTON:
                ((ButtonShuffleViewHolder) holder).onBind();
                break;
        }
    }

    @Override
    public int getItemCount() {
        return trackList != null ? trackList.size() + 1 : 0;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_BUTTON : TYPE_TRACK;
    }

    public void setSelectedTrack(Track track) {
        // Desseleciona a última faixa
        for (Track t : trackList) {
            if (t.isSelected()) {
                t.setSelected(false);
                notifyItemChanged(trackList.indexOf(t) + 1);
                break;
            }
        }

        // Seleciona a faixa atual
        track.setSelected(true);
        notifyItemChanged(trackList.indexOf(track) + 1);
    }

    public List<Track> getTrackList() {
        return new ArrayList<>(trackList);
    }

    public void setTrackList(List<Track> trackList) {
        this.trackList = trackList;
        notifyDataSetChanged();
    }

    private class TrackViewHolder extends RecyclerView.ViewHolder {
        private View container;
        private ImageView imgAlbumArt;
        private TextView txtTitle;
        private TextView txtArtist;
        private TextView txtDuration;

        TrackViewHolder(View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            imgAlbumArt = (ImageView) itemView.findViewById(R.id.img_album_art);
            txtTitle = (TextView) itemView.findViewById(R.id.txt_title);
            txtArtist = (TextView) itemView.findViewById(R.id.txt_artist);
            txtDuration = (TextView) itemView.findViewById(R.id.txt_duration);
        }

        void onBind(final Track track) {
            txtTitle.setText(track.getTitle());
            txtArtist.setText(track.getArtist());
            txtDuration.setText(timeFormatter.format(track.getDuration()));

            if (track.isSelected()) {
                itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
                container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.background_primary));
                container.setBackgroundColor(ContextCompat.getColor(context, R.color.background_secondary));
            }

            if (track.getAlbumArt() == null) {
                imgAlbumArt.setImageResource(R.drawable.ic_placeholder_album_small);
            } else {
                Picasso.with(context)
                        .load(uriBuilder.path(track.getAlbumArt()).build())
                        .into(imgAlbumArt);
            }

            this.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    audioClickListener.onTrackClick(track);
                }
            });
        }
    }

    private class ButtonShuffleViewHolder extends RecyclerView.ViewHolder {
        private final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH'h'mm'm'", Locale.getDefault());
        private TextView txtShuffleInfo;

        ButtonShuffleViewHolder(View itemView) {
            super(itemView);

            txtShuffleInfo = (TextView) itemView.findViewById(R.id.txt_shuffle_info);
        }

        void onBind() {
            int itemCount = getItemCount() - 1;
            long timeCount = 0;

            for (Track t : trackList) {
                timeCount += t.getDuration();
            }

            if (itemCount == 1) {
                txtShuffleInfo.setText(context.getString(
                        R.string.txt_shuffle_info_singular,
                        timeFormatter.format(new Date(timeCount))
                ));
            } else {
                txtShuffleInfo.setText(context.getString(
                        R.string.txt_shuffle_info_plural,
                        itemCount,
                        timeFormatter.format(new Date(timeCount))
                ));
            }

            this.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    audioClickListener.onShuffleClick();
                }
            });
        }
    }
}
