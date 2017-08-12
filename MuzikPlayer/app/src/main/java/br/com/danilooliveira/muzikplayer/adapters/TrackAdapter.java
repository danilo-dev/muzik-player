package br.com.danilooliveira.muzikplayer.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
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

    private OnAdapterListener audioClickListener;
    private LayoutInflater layoutInflater;
    private Picasso picasso;
    private SimpleDateFormat timeFormatter;

    private List<Track> trackList;

    public TrackAdapter(Context context, OnAdapterListener audioClickListener) {
        this.audioClickListener = audioClickListener;
        layoutInflater = LayoutInflater.from(context);
        picasso = Picasso.with(context);
        timeFormatter = new SimpleDateFormat("mm:ss", Locale.getDefault());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_BUTTON:
                return new ButtonViewHolder(layoutInflater.inflate(R.layout.adapter_item_shuffle, parent, false));

            default:
                return new TrackViewHolder(layoutInflater.inflate(R.layout.adapter_track, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_BUTTON:
                ((ButtonViewHolder) holder).onBind();
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
                picasso.load(Uri.fromFile(new File(track.getAlbumArt()))).into(imgAlbumArt);
            }

            this.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    audioClickListener.onTrackClick(track);
                }
            });
        }
    }

    private class ButtonViewHolder extends RecyclerView.ViewHolder {

        ButtonViewHolder(View itemView) {
            super(itemView);
        }

        void onBind() {
            this.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    audioClickListener.onShuffleClick();
                }
            });
        }
    }
}
