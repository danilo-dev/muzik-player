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
import br.com.danilooliveira.muzikplayer.domain.Audio;
import br.com.danilooliveira.muzikplayer.interfaces.OnAudioClickListener;

/**
 * Criado por Danilo de Oliveira (danilo.desenvolvedor@outlook.com) em 04/08/2017.
 */
public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.AudioViewHolder> {
    private OnAudioClickListener audioClickListener;
    private Context mContext;
    private LayoutInflater layoutInflater;
    private List<Audio> audioList;

    public AudioAdapter(Context context, OnAudioClickListener audioClickListener) {
        mContext = context;
        layoutInflater = LayoutInflater.from(context);
        this.audioClickListener = audioClickListener;
    }

    @Override
    public AudioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AudioViewHolder(layoutInflater.inflate(R.layout.adapter_audio, parent, false));
    }

    @Override
    public void onBindViewHolder(AudioViewHolder holder, int position) {
        holder.onBind(audioList.get(position));
    }

    @Override
    public int getItemCount() {
        return audioList != null ? audioList.size() : 0;
    }

    public List<Audio> getAudioList() {
        return audioList;
    }

    public void setAudioList(List<Audio> audioList) {
        this.audioList = audioList;
        notifyDataSetChanged();
    }

    class AudioViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgAlbumArt;
        private TextView txtTitle;
        private TextView txtArtist;

        AudioViewHolder(View itemView) {
            super(itemView);
            imgAlbumArt = itemView.findViewById(R.id.img_album_art);
            txtTitle = itemView.findViewById(R.id.txt_title);
            txtArtist = itemView.findViewById(R.id.txt_artist);
        }

        void onBind(final Audio audio) {
            txtTitle.setText(audio.getTitle());
            txtArtist.setText(audio.getArtist());

            if (audio.getAlbumId() != null) {
                if (audio.getAlbumArt() == null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (audio.getAlbumArt() == null) {
                                Uri albumUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
                                String[] albumColumns = {
                                        MediaStore.Audio.Albums._ID,
                                        MediaStore.Audio.AlbumColumns.ALBUM_ART
                                };
                                String albumConditions = MediaStore.Audio.Albums._ID + "= ?";
                                String albumArgs = audio.getAlbumId();


                                Cursor albumCursor = mContext.getContentResolver().query(albumUri, albumColumns, albumConditions, new String[] {albumArgs}, null);
                                if (albumCursor != null) {
                                    if (albumCursor.moveToNext()) {
                                        audio.setAlbumArt(albumCursor.getString(albumCursor.getColumnIndexOrThrow(MediaStore.Audio.AlbumColumns.ALBUM_ART)));
                                    }
                                    albumCursor.close();
                                }
                            }

                            if (audio.getAlbumArt() != null) {
                                ((Activity) mContext).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Picasso.with(mContext).load(Uri.fromFile(new File(audio.getAlbumArt()))).into(imgAlbumArt);
                                    }
                                });
                            }

                        }
                    }).start();
                } else {
                    Picasso.with(mContext).load(Uri.fromFile(new File(audio.getAlbumArt()))).into(imgAlbumArt);
                }
            }

            this.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    audioClickListener.onAudioClick(audio);
                }
            });
        }
    }
}
