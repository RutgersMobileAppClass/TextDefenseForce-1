package rutgers.text_defense_force.word_list_service.service_wrapper;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import java.util.concurrent.atomic.AtomicBoolean;
import rutgers.text_defense_force.word_list_service.WordListService;

public class ServiceWrapper extends BroadcastReceiver {
    protected final Activity associatedActivity;
    protected final ServiceWrapperListener serviceWrapperListener;
    protected final AtomicBoolean wrapperRunning = new AtomicBoolean(false);

    public ServiceWrapper(Activity associatedActivity, ServiceWrapperListener
            serviceWrapperListener) throws RuntimeException {

        if ((associatedActivity == null) || (serviceWrapperListener == null))
            throw new NullPointerException();

        this.associatedActivity = associatedActivity;
        this.serviceWrapperListener = serviceWrapperListener;

    }

    public synchronized void startWrapper() {
        if (!wrapperRunning.compareAndSet(false, true)) return;
        startWordListService();
        registerAsListener();
    }

    public synchronized void stopWrapper() {
        if (!wrapperRunning.compareAndSet(true, false)) return;
        stopWordListService();
        unregisterAsListener();
    }

    public synchronized boolean isWrapperRunning() {
        return wrapperRunning.get();
    }

    public synchronized void requestWord() throws IllegalStateException {
        if (!wrapperRunning.get()) throw new IllegalStateException("The wrapper isn't running");
        Intent intent = new Intent(associatedActivity, WordListService.class);
        intent.setAction(WordListService.ACTION_REQUEST_WORD);
        associatedActivity.startService(intent);
    }

    protected void startWordListService() {
        Intent intent = new Intent(associatedActivity, WordListService.class);
        associatedActivity.startService(intent);
    }

    protected void stopWordListService() {
        Intent intent = new Intent(associatedActivity, WordListService.class);
        associatedActivity.stopService(intent);
    }

    protected void registerAsListener() {
        LocalBroadcastManager.getInstance(associatedActivity).registerReceiver(this,
                new IntentFilter(WordListService.ACTION_RESPONSE_WORD));
        LocalBroadcastManager.getInstance(associatedActivity).registerReceiver(this,
                new IntentFilter(WordListService.ACTION_RESPONSE_SERVICE_EXCEPTION));
    }

    protected void unregisterAsListener() {
        LocalBroadcastManager.getInstance(associatedActivity).unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        switch (intent.getAction()) {

            case WordListService.ACTION_RESPONSE_WORD:

                String word = intent.getStringExtra(WordListService.EXTRA_WORD);
                serviceWrapperListener.wordObtained(word);
                break;

            case WordListService.ACTION_RESPONSE_SERVICE_EXCEPTION:

                serviceWrapperListener.serviceFailure();
                break;

        }
    }
}