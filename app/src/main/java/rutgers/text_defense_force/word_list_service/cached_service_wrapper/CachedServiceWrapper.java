package rutgers.text_defense_force.word_list_service.cached_service_wrapper;
import android.app.Activity;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import rutgers.text_defense_force.word_list_service.service_wrapper.ServiceWrapper;
import rutgers.text_defense_force.word_list_service.service_wrapper.ServiceWrapperListener;

public class CachedServiceWrapper implements ServiceWrapperListener {

    protected final CachedServiceWrapperListener cachedServiceWrapperListener;
    protected final ServiceWrapper serviceWrapper;
    protected final int cacheSize;
    protected final ConcurrentLinkedQueue<String> cachedWords = new ConcurrentLinkedQueue<String>();
    protected final AtomicInteger userRequestsPending = new AtomicInteger(0);

    public CachedServiceWrapper(Activity associatedActivity,
                                CachedServiceWrapperListener cachedServiceWrapperListener,
                                int cacheSize) {

        if (cachedServiceWrapperListener == null) throw new NullPointerException();
        if (cacheSize < 1) throw new IllegalArgumentException();
        this.cachedServiceWrapperListener = cachedServiceWrapperListener;
        this.cacheSize = cacheSize;
        serviceWrapper = new ServiceWrapper(associatedActivity, this);
        startWrapper();
    }

    protected synchronized void startWrapper() {
        serviceWrapper.startWrapper();

        userRequestsPending.set(0);

        // To deal with if the cache has to many entries:
        int truncateCache = cachedWords.size() - cacheSize;
        for (int i = truncateCache; i > cacheSize; i--)
            cachedWords.poll();

        // To deal with if the cache has to little entries:
        int requestsNeededToFillCache = cacheSize - cachedWords.size();
        for (int j = 0; j < requestsNeededToFillCache; j++)
            serviceWrapper.requestWord();
    }

    public synchronized void stopWrapper() {
        serviceWrapper.stopWrapper();
        userRequestsPending.set(0);
    }

    public synchronized String requestWord() {

        // If the wrapper isn't running, start it
        if (!serviceWrapper.isWrapperRunning()) {
            startWrapper();
        }

        // Attempt to get a word from the cache
        String word = cachedWords.poll();

        // If we don't have a word on tap, increment the number of user requests pending
        if (word ==  null) userRequestsPending.incrementAndGet();

        // Make a request for another word (in either case if a word was in the cache or not, this
        // will need to be called)
        serviceWrapper.requestWord();

        // Returned the word (if any) that was obtained from the cache. This may be null
        return word;

    }

    public void wordObtained(String word) {

        // Because multiple threads may be calling this, we want to convert them to synchronous
        // calls
        synchronized (userRequestsPending) {

            // If there are unfulfilled user requests:
            if (userRequestsPending.get() != 0) {

                // Decrement the number of pending user requests and serve the word to the user
                userRequestsPending.decrementAndGet();
                cachedServiceWrapperListener.wordObtained(word);
                return;

            }

        }

        // Otherwise add the word to the cache
        cachedWords.offer(word);

    }

    public void serviceFailure() {
        cachedServiceWrapperListener.serviceFailure();
    }
}