package br.com.danilooliveira.muzikplayer.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import br.com.danilooliveira.muzikplayer.R;
import br.com.danilooliveira.muzikplayer.domain.Track;

/**
 * Criado por Danilo de Oliveira (danilo.desenvolvedor@outlook.com) em 13/08/2017.
 */
public class TrackInfoFragment extends Fragment {
    private static final Uri.Builder uriBuilder = new Uri.Builder().scheme("file");
    private ImageView imgAlbumArt;
    private TextView txtTitle, txtArtist;

    private Context mContext;
    private Track track;

    public static TrackInfoFragment newInstance(Track track) {
        TrackInfoFragment fragment = new TrackInfoFragment();
        fragment.track = track;
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_track_info, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgAlbumArt = view.findViewById(R.id.img_album_art);
        txtTitle = view.findViewById(R.id.txt_title);
        txtArtist = view.findViewById(R.id.txt_artist);

        txtTitle.setSelected(true);
        txtArtist.setSelected(true);

        refresh(track);
    }

    public void refresh(Track track) {
        if (track != null) {
            if (track.getAlbumArt() != null) {
                Picasso.with(mContext)
                        .load(uriBuilder.path(track.getAlbumArt()).build())
                        .into(imgAlbumArt);
            } else {
                imgAlbumArt.setImageResource(R.drawable.ic_placeholder_album_large);
            }

            txtTitle.setText(track.getTitle());
            txtArtist.setText(track.getArtist());
        }
    }
}
