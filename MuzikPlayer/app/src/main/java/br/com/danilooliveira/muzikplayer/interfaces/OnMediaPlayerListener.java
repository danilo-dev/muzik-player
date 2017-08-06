package br.com.danilooliveira.muzikplayer.interfaces;

import br.com.danilooliveira.muzikplayer.domain.Track;

/**
 * Criado por Danilo de Oliveira (danilo.desenvolvedor@outlook.com) em 06/08/2017.
 */
public interface OnMediaPlayerListener {
    void onTrackChanged(Track track);
    void onPauseTrack();
    void onPlayTrack();
}
