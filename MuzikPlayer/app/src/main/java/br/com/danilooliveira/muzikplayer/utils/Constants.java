package br.com.danilooliveira.muzikplayer.utils;

/**
 * Criado por Danilo de Oliveira (danilo.desenvolvedor@outlook.com) em 06/08/2017.
 */
public interface Constants {
    String ACTION_TRACK_CHANGED = "br.com.danilooliveira.muzikplayer.intent.ACTION_TRACK_CHANGED";
    String ACTION_PAUSE = "br.com.danilooliveira.muzikplayer.intent.ACTION_PAUSE";
    String ACTION_PLAY = "br.com.danilooliveira.muzikplayer.intent.ACTION_PLAY";

    String BUNDLE_TRACK = "track";

    int REQUEST_PERMISSION_STORAGE = 1;

    int TYPE_NO_REPEAT = 0;
    int TYPE_REPEAT_CURRENT = 1;
    int TYPE_REPEAT_ALL = 2;
}
