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
import java.util.List;
import java.util.Locale;

import br.com.danilooliveira.muzikplayer.R;
import br.com.danilooliveira.muzikplayer.domain.Track;
import br.com.danilooliveira.muzikplayer.interfaces.OnAdapterListener;

/**
 * Criado por Danilo de Oliveira (danilo.desenvolvedor@outlook.com) em 03/09/2017.
 */
public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.TrackViewHolder> {
    private static final Uri.Builder uriBuilder = new Uri.Builder();
    private static final SimpleDateFormat timeFormatter = new SimpleDateFormat("mm:ss", Locale.getDefault());

    private final OnAdapterListener onAdapterListener;
    private final Context context;
    private final LayoutInflater inflater;
    private final Picasso picasso;

    private List<Track> trackList;

    public QueueAdapter(Context context, OnAdapterListener onAdapterListener) {
        uriBuilder.scheme("file");

        this.onAdapterListener = onAdapterListener;
        this.context = context;
        inflater = LayoutInflater.from(context);
        picasso = Picasso.with(context);
    }

    @Override
    public TrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TrackViewHolder(inflater.inflate(R.layout.adapter_queue_track, parent, false));
    }

    @Override
    public void onBindViewHolder(TrackViewHolder holder, int position) {
        holder.onBind(trackList.get(position));
    }

    @Override
    public int getItemCount() {
        return trackList == null ? 0 : trackList.size();
    }

    public void setSelectedTrack(Track track) {
        // Desseleciona a última faixa
        for (Track t : trackList) {
            if (t.isSelected()) {
                t.setSelected(false);
                notifyItemChanged(trackList.indexOf(t));
                break;
            }
        }

        // Seleciona a faixa atual
        track.setSelected(true);
        notifyItemChanged(trackList.indexOf(track));
    }

    public void setTrackList(List<Track> trackList) {
        this.trackList = trackList;
        notifyDataSetChanged();
    }

    class TrackViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgAlbumArt;
        private TextView txtTitle;
        private TextView txtArtist;
        private TextView txtDuration;

        TrackViewHolder(View itemView) {
            super(itemView);

            imgAlbumArt = (ImageView) itemView.findViewById(R.id.img_album_art);
            txtTitle = (TextView) itemView.findViewById(R.id.txt_title);
            txtArtist = (TextView) itemView.findViewById(R.id.txt_artist);
            txtDuration = (TextView) itemView.findViewById(R.id.txt_duration);
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

            if (track.isSelected()) {
                itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.background_primary));
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onAdapterListener.onTrackClick(track);
                }
            });
        }
    }
}
