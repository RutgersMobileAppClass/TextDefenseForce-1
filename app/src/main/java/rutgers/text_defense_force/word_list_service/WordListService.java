package rutgers.text_defense_force.word_list_service;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import rutgers.text_defense_force.R;
import rutgers.text_defense_force.word_list_service.service_wrapper.ServiceWrapperListener;

public class WordListService extends Service {

    public static final String ACTION_RESPONSE_WORD =
            "WordListService.responseWord";
    public static final String ACTION_RESPONSE_SERVICE_EXCEPTION =
            "WordListService.responseServiceException";
    public static final String ACTION_REQUEST_WORD =
            "WordListService.requestWord";
    public static final String EXTRA_WORD =
            "WordListService.ExtraWord";

    protected final ConcurrentHashMap<Integer, Boolean> usedWords = new ConcurrentHashMap<>();

    protected final InitializationThread initializationThread = new InitializationThread();

    protected final ExecutorService wordObtainerExecutor = Executors.newFixedThreadPool(5);

    // -1 for error
    protected int wordListWordCount = 0;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Start the initialization thread
        initializationThread.start();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // If the initialization thread is still running, wait until it completes
        try {
            initializationThread.join();
        } catch (InterruptedException interruptedException) {
        }

        // Attempt to shut down all running word obtainer threads as well as any pending ones
        wordObtainerExecutor.shutdownNow();

    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null) return START_STICKY;
        if (intent.getAction() == null) return START_STICKY;

        // If someone requested a word:
        if (intent.getAction().equals(ACTION_REQUEST_WORD)) {

            // If there was an issue reading the word list, broadcast a service exception
            if (wordListWordCount == -1) {
                broadcastServiceException();

                // Otherwise, let the executor start a thread to obtain a word
            } else {

                // If we have used up all the words in the word list, clear the used words list
                if (usedWords.size() == wordListWordCount) usedWords.clear();

                // Start a new thread to obtain a word
                wordObtainerExecutor.execute(new WordObtainerThread());

            }
        }

        // Keep the service running until someone stops it
        return START_STICKY;

    }

    protected synchronized void broadcastWord(String word) {
        Intent intent = new Intent();
        intent.setAction(ACTION_RESPONSE_WORD);
        intent.putExtra(EXTRA_WORD, word);
        LocalBroadcastManager.getInstance(WordListService.this).sendBroadcast(intent);
    }

    protected synchronized void broadcastServiceException() {
        Intent intent = new Intent();
        intent.setAction(ACTION_RESPONSE_SERVICE_EXCEPTION);
        LocalBroadcastManager.getInstance(WordListService.this).sendBroadcast(intent);
    }

    protected class WordObtainerThread implements Runnable {

        @Override
        public void run() {

            // Verify the initialization thread is complete before continuing. If it is not, wait
            // until it is
            try {
                initializationThread.join();
            } catch (InterruptedException interruptedException) {
                return;
            }

            // If there was an exception retreiving the number of words in the word list, broadcast
            // an exception and return
            if (wordListWordCount == -1) {
                broadcastServiceException();
                return;
            }

            // Each line in the word list contains one word. So we must generate a random number
            // from 0 to (number of lines in the word list) and check if that line (aka word) has
            // already been used by the service. If it has we must repeat the process until we
            // obtain one that hasn't been used.
            Integer randomWordLine;
            Random random = new Random();
            while (true) {
                randomWordLine = random.nextInt(wordListWordCount);
                if (usedWords.putIfAbsent(randomWordLine, true) == null) break;
            }

            // Now we may open the word list, travel to the generated line number, grab the word
            // on that line, and broadcast it
            String obtainedWord = null;
            InputStream inputStream = getResources().openRawResource(R.raw.wordlist);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            int currentLineNumber = 0;
            try {
                while (currentLineNumber < randomWordLine) {
                    bufferedReader.readLine();
                    currentLineNumber = currentLineNumber + 1;
                }
                obtainedWord = bufferedReader.readLine();
            } catch (IOException ioException) {
                // If there was an issue reading the word list, broadcast an exception
                broadcastServiceException();
                return;
            } finally {
                try {
                    bufferedReader.close();
                } catch (IOException ioException) {
                }
            }
            broadcastWord(obtainedWord);
        }
    }

    protected class InitializationThread extends Thread {

        public void run() {

            // Open the word list with a buffered reader
            InputStream inputStream = getResources().openRawResource(R.raw.wordlist);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            wordListWordCount = 0;

            // Loop through all the lines in the file. Because each line represents a word, we may
            // count each individual line to represent one word
            try {
                while ((bufferedReader.readLine()) != null) wordListWordCount++;

                // If there was an exception reading the file, set the number of words to -1 so that
                // we can later identify that an exception occurred while getting the number of words
            } catch (IOException ioException) {
                wordListWordCount = -1;

                // Finally irregardless if an error occurred or not, close the buffered reader and
                // update the word count.
            } finally {
                try {
                    bufferedReader.close();
                } catch (IOException ioException) {
                }
            }
        }
    }
}