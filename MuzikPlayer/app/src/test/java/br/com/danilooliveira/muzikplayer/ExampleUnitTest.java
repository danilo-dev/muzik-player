package br.com.danilooliveira.muzikplayer;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    private List<String> historyList = new ArrayList<>();
    private List<String> trackList = new ArrayList<>();

    private int currentPosition = 0;

    @Test
    public void getCorrectTrack() throws Exception {
        populateTrackList();

        String track = getRandomTrack();
        historyList.add(track);
        onPlayTrack(track);

        onNextTrack();
        onNextTrack();
        onPreviousTrack();
        onPreviousTrack();
        onPreviousTrack();
        onPreviousTrack();
        onNextTrack();
        onNextTrack();
        onNextTrack();
        onNextTrack();
        onNextTrack();

        System.out.print(historyList.toString());
    }

    private void onPreviousTrack() {
        System.out.printf("PREVIOUS(position: %d)\n", currentPosition);
        String track;

        currentPosition--;

        if (currentPosition < 0) {
            currentPosition = 0;
            track = getRandomTrack();
            historyList.add(0, track);
            System.out.printf("Added to history: %d\n", currentPosition);
        } else {
            track = historyList.get(currentPosition);
            System.out.printf("From history: %d\n", currentPosition);
        }

        onPlayTrack(track);
    }

    private void onNextTrack() {
        System.out.printf("NEXT(position: %d)\n", currentPosition);
        String track;

        currentPosition++;

        if (currentPosition >= historyList.size()) {
            track = getRandomTrack();
            historyList.add(track);
            System.out.printf("Added to history: %d\n", currentPosition);
        } else {
            track = historyList.get(currentPosition);
            System.out.printf("From history: %d\n", currentPosition);
        }

        onPlayTrack(track);
    }

    private void onPlayTrack(String track) {
        System.out.printf("Playing: %s | Position: %d\n\n", track, currentPosition);
    }

    private String getRandomTrack() {
        String track = trackList.remove(trackList.size() - 1);
        System.out.printf("New item: %s\n", track);
        return track;
    }

    private void populateTrackList() {
        trackList.add("H");
        trackList.add("G");
        trackList.add("F");
        trackList.add("E");
        trackList.add("D");
        trackList.add("C");
        trackList.add("B");
        trackList.add("A");
    }
}