package br.com.danilooliveira.muzikplayer.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

import br.com.danilooliveira.muzikplayer.database.TrackDatabase;
import br.com.danilooliveira.muzikplayer.domain.Track;
import br.com.danilooliveira.muzikplayer.utils.AppPreferences;

/**
 * Criado por Danilo de Oliveira (danilo.desenvolvedor@outlook.com) em 02/09/2017.
 *
 * Obtém as faixas de música armazenadas no banco de dados.
 *
 * Se as faixas já tiverem sido obtidas através do sistema do Android,
 * serão obtidas as que estão no banco de dados local do aplicativo.
 * Caso contrário, todas as faixas no sistema do Android serão importadas.
 * @see TrackDatabase#getTracksFromAndroidDB()
 *
 * Ao final da tarefa, as faixas são retornadas através de um callback.
 * @see OnCompleteListener
 */
public class TrackFinderTask extends AsyncTask<Void, Integer, List<Track>> {
    private final OnCompleteListener listener;
    private final TrackDatabase database;
    private final AppPreferences appPreferences;

    private final boolean scanStorage;

    public TrackFinderTask(Context context, boolean forceScan, OnCompleteListener listener) {
        this.listener = listener;

        database = new TrackDatabase(context);
        appPreferences = AppPreferences.with(context);

        scanStorage = forceScan || !appPreferences.isStorageScanned();
    }

    @Override
    protected List<Track> doInBackground(Void... voids) {
        if (!scanStorage) {
            return database.getList();
        } else {
            List<Track> trackList = database.getTracksFromAndroidDB();

            appPreferences.setStorageScanned(!trackList.isEmpty());

            return trackList;
        }
    }

    @Override
    protected void onPostExecute(List<Track> tracks) {
        super.onPostExecute(tracks);
        listener.onComplete(tracks);
    }

    public interface OnCompleteListener {
        void onComplete(List<Track> trackList);
    }
}
