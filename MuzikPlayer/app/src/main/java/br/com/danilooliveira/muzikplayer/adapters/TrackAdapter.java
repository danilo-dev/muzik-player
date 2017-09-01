package br.com.danilooliveira.muzikplayer.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
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
    private Resources resources;
    private Picasso picasso;

    private List<Track> trackList;

    public TrackAdapter(Context context, OnAdapterListener audioClickListener) {
        uriBuilder.scheme("file");
        this.audioClickListener = audioClickListener;
        layoutInflater = LayoutInflater.from(context);
        resources = context.getResources();
        picasso = Picasso.with(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_BUTTON:
                return new ButtonShuffleViewHolder(layoutInflater.inflate(R.layout.adapter_item_shuffle, parent, false));

            default:
                return new TrackViewHolder(layoutInflater.inflate(R.layout.adapter_track, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_BUTTON:
                ((ButtonShuffleViewHolder) holder).onBind();
                break;

            case TYPE_TRACK:
                ((TrackViewHolder) holder).onBind(trackList.get(--position));
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

    public List<Track> getTrackList() {
        return trackList;
    }

    public void setTrackList(List<Track> trackList) {
        this.trackList = trackList;
        notifyDataSetChanged();
    }

    private class TrackViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgAlbumArt;
        private TextView txtTitle;
        private TextView txtArtist;
        private TextView txtDuration;

        TrackViewHolder(View itemView) {
            super(itemView);
            imgAlbumArt = itemView.findViewById(R.id.img_album_art);
            txtTitle = itemView.findViewById(R.id.txt_title);
            txtArtist = itemView.findViewById(R.id.txt_artist);
            txtDuration = itemView.findViewById(R.id.txt_duration);
        }

        void onBind(final Track track) {
            txtTitle.setText(track.getTitle());
            txtArtist.setText(track.getArtist());
            txtDuration.setText(timeFormatter.format(track.getDuration()));

            if (track.getAlbumArt() == null) {
                imgAlbumArt.setImageResource(R.drawable.ic_placeholder_album_small);
            } else {
                picasso.load(uriBuilder.path(track.getAlbumArt()).build())
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

            txtShuffleInfo = itemView.findViewById(R.id.txt_shuffle_info);
        }

        void onBind() {
            int itemCount = getItemCount();
            long timeCount = 0;

            for (Track t : trackList) {
                timeCount += t.getDuration();
            }

            if (itemCount == 1) {
                txtShuffleInfo.setText(resources.getString(
                        R.string.txt_shuffle_info_singular,
                        timeFormatter.format(new Date(timeCount))
                ));
            } else {
                txtShuffleInfo.setText(resources.getString(
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
