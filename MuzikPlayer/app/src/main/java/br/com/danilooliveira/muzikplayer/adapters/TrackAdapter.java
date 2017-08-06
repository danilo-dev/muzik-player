package br.com.danilooliveira.muzikplayer.adapters;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import br.com.danilooliveira.muzikplayer.R;
import br.com.danilooliveira.muzikplayer.domain.Track;
import br.com.danilooliveira.muzikplayer.interfaces.OnTrackClickListener;

/**
 * Criado por Danilo de Oliveira (danilo.desenvolvedor@outlook.com) em 04/08/2017.
 */
public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {
    private OnTrackClickListener audioClickListener;
    private LayoutInflater layoutInflater;

    private Context mContext;
    private List<Track> trackList;
    private Picasso picasso;

    public TrackAdapter(Context context, OnTrackClickListener audioClickListener) {
        layoutInflater = LayoutInflater.from(context);
        mContext = context;
        this.audioClickListener = audioClickListener;
        picasso = Picasso.with(context);
    }

    @Override
    public TrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TrackViewHolder(layoutInflater.inflate(R.layout.adapter_track, parent, false));
    }

    @Override
    public void onBindViewHolder(TrackViewHolder holder, int position) {
        holder.onBind(trackList.get(position));
    }

    @Override
    public int getItemCount() {
        return trackList != null ? trackList.size() : 0;
    }

    public List<Track> getTrackList() {
        return trackList;
    }

    public void setTrackList(List<Track> trackList) {
        this.trackList = trackList;
        notifyDataSetChanged();
    }

    class TrackViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgAlbumArt;
        private TextView txtTitle;
        private TextView txtArtist;

        TrackViewHolder(View itemView) {
            super(itemView);
            imgAlbumArt = itemView.findViewById(R.id.img_album_art);
            txtTitle = itemView.findViewById(R.id.txt_title);
            txtArtist = itemView.findViewById(R.id.txt_artist);
        }

        void onBind(final Track track) {
            txtTitle.setText(track.getTitle());
            txtArtist.setText(track.getArtist());

            if (track.getAlbumId() != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (track.getAlbumArt() == null) {
                            Uri albumUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
                            String[] albumColumns = {
                                    MediaStore.Audio.Albums._ID,
                                    MediaStore.Audio.AlbumColumns.ALBUM_ART
                            };
                            String albumConditions = MediaStore.Audio.Albums._ID + "= ?";
                            String albumArgs = track.getAlbumId();


                            Cursor albumCursor = mContext.getContentResolver().query(albumUri, albumColumns, albumConditions, new String[] {albumArgs}, null);
                            if (albumCursor != null) {
                                if (albumCursor.moveToNext()) {
                                    track.setAlbumArt(albumCursor.getString(albumCursor.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ALBUM_ART)));
                                }
                                albumCursor.close();
                            }
                        }

                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (track.getAlbumArt() == null) {
                                    picasso.load(R.drawable.ic_placeholder_album_small).into(imgAlbumArt);
                                } else {
                                    picasso.load(Uri.fromFile(new File(track.getAlbumArt()))).into(imgAlbumArt);
                                }
                            }
                        });

                    }
                }).start();
            }

            this.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    audioClickListener.onTrackClick(track);
                }
            });
        }
    }
}
