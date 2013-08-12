package se.miun.ant;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.dsi.ant.channel.AntChannel;
import com.dsi.ant.channel.AntCommandFailedException;
import com.dsi.ant.channel.IAntChannelEventHandler;
import com.dsi.ant.message.fromant.MessageFromAntType;
import com.dsi.ant.message.ipc.AntMessageParcel;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * ChannelSearcher is used to open ANT channels that will try to connect to an available
 * master channel. If the connection is made, the AntChannel object is passed to an observer.
 * A search can be initiated by calling startChannelSearch() on an instance of this class.
 * The search will continue to create and connect (sequentially) new ANT channels until
 * a channel times out and then it will stop.
 */
public class ChannelSearcher implements ChannelRetriever.OnChannelProviderAvailableListener {

    /**
     * The OnChannelSearchStatusListener must be implemented by a client (client to this class)
     * that wishes to be notified when an ANT channel has been successfully created, opened and
     * connected.
     */
    public interface OnChannelSearchStatusListener {
        /**
         * Called when the channel has completed its initialization and it is safe to call
         * startChannelSearch().
         */
        public void onChannelSearcherInitialized();

        /**
         * Called when an ANT channel has been successfully created, opened and connected.
         * @param antChannel the connected ANT channel.
         */
        public void onChannelConnected(AntChannel antChannel);

        /**
         * Called when a channel search has been started.
         */
        public void onChannelSearchStarted();

        /**
         * Called when an ongoing channel search has finished.
         */
        public void onChannelSearchFinished();
        /**
         * Called when there are no free channels available to open.
         */
        public void onNoChannelsAvailable();
    }

    private static final int CHANNEL_TIMER_TIMEOUT_MS = 1000 * 15;

    private Context context;                        // Context used for Toast.
    private OnChannelSearchStatusListener listener; // Is notified when a channel has connected.
    private ChannelRetriever channelRetriever;      // Used to get channels from the ANT system.
    private ChannelInitializer channelInitializer;  // Used to set default channel parameters.
    private Handler uiThreadHandler;                // Used to post runnables on the UI thread.

    private List<AntChannelEventHandler> channelEventHandlers;

    // Set to true when a search is in progress, otherwise set to false.
    // A search is in progress from the moment a channel search is initialized until there
    // is a channel timeout (meaning no master channel to connect to) on one of the opened channels.
    private boolean searchInProgress;

    /**
     * Constructor.
     * @param context the Activity that is needed for Toast notifications.
     */
    public ChannelSearcher(Context context) {
        this.context = context;
        uiThreadHandler = new Handler(Looper.getMainLooper());

        channelRetriever = GlobalState.getInstance().getChannelRetriever();
        channelRetriever.setOnChannelProviderAvailableListener(this);

        channelInitializer = new ChannelInitializer(context);

        channelEventHandlers = new ArrayList<AntChannelEventHandler>();

        searchInProgress = false;
    }

    /**
     * Constructor.
     * @param context the Activity that is needed for Toast notifications.
     * @param listener this object will be notified when an ANT slave channel has been
     *                 successfully created, opened and connected.
     */
    public ChannelSearcher(Context context, OnChannelSearchStatusListener listener) {
        this(context);
        setOnChannelConnectedListener(listener);
    }

    /**
     * This callback is called when the ANT Radio Service has finished initializing and
     * the channel provider can be used to obtain ANT channels from the service.
     */
    @Override
    public void onChannelProviderAvailable() {
        if (listener != null) listener.onChannelSearcherInitialized();
    }

    /**
     * Sets the OnChannelSearchStatusListener that is notified when a channel has connected.
     * @param listener the listener that will be notified.
     */
    public void setOnChannelConnectedListener(OnChannelSearchStatusListener listener) {
        this.listener = listener;
    }

    /**
     * This method requests that a channel search should be started. If a channel search is
     * already in progress and this method is called it will not do anything.
     */
    public void startChannelSearch() {
        if (!searchInProgress) {
            searchInProgress = true;
            listener.onChannelSearchStarted();
            startChannelSearchThread();
        }
    }

    //
    // Starts the thread that runs a channel search. The thread will retrieve, initialize and open
    // all available ANT channels and then exit.
    //
    private void startChannelSearchThread() {
        new Thread(new Runnable() {

            @Override
            public void run() {

                int channelsAvailable;

                try {
                    channelsAvailable = channelRetriever.getNumberOfChannelsAvailable();
                    Log.i(GlobalState.LOG_TAG, "Number of channels available: " + channelsAvailable);
                } catch (ChannelRetriever.ChannelRetrieveException e) {
                    stopChannelSearch();
                    logErrorAndNotifyUser("Unable to retrieve channel: " + e.getMessage(), e);
                    return;
                }

                if (channelsAvailable == 0) {
                    stopChannelSearch();
                    listener.onNoChannelsAvailable();
                    return;
                }

                for (int i = 0; i < channelsAvailable; i++) {

                    try {
                        AntChannel channel = channelRetriever.getChannel();
                        channelInitializer.initializeChannel(channel);
                        channel.open();
                        channelEventHandlers.add(new AntChannelEventHandler(channel));

                    } catch (ChannelRetriever.ChannelRetrieveException e) {
                        logErrorAndNotifyUser("Unable to retrieve channel: " + e.getMessage(), e);
                        cleanupAndNotifySearchFinished();
                        return;
                    } catch (ChannelInitializer.ChannelInitializationException e) {
                        logErrorAndNotifyUser("Unable to initialize channel: " + e.getMessage(), e);
                        cleanupAndNotifySearchFinished();
                        return;
                    } catch (RemoteException e) {
                        logErrorAndNotifyUser("Unable to initialize channel: " + e.getMessage(), e);
                        cleanupAndNotifySearchFinished();
                        return;
                    } catch (AntCommandFailedException e) {
                        logErrorAndNotifyUser("Unable to open channel: " + e.getMessage(), e);
                        cleanupAndNotifySearchFinished();
                        return;
                    }
                }
                startChannelTimeoutTimer();
            }
        }).start();
    }

    // Note two different threads are calling two methods:
    // 1) The thread created by startChannelSearchThread()
    // 2) The thread created by the TimerTask in the AntChannelEventHandler.
    // However, these threads do not run at the same time, so the synchronized is a precaution only.

    private synchronized void stopChannelSearch() {
        searchInProgress = false;
        listener.onChannelSearchFinished();
    }

    private synchronized void cleanupChannelHandlers() {
        for (AntChannelEventHandler handler : channelEventHandlers) {
            if (!handler.isChannelConnected()) {
                releaseChannel(handler.channel);
            }
        }
        channelEventHandlers.clear();
    }

    private synchronized void releaseChannel(AntChannel channel) {
        if (channel != null) {
            channel.release();
        }
    }

    private void logErrorAndNotifyUser(String message, Exception e) {
        Log.e(GlobalState.LOG_TAG, message);
        notifyUserChannelError(e);
    }

    private void notifyUserChannelError(final Exception e) {
        uiThreadHandler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(context,
                        "Error opening ANT channel: " + e.getMessage() + "\nPlease try again.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startChannelTimeoutTimer() {
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                cleanupAndNotifySearchFinished();
            }

        }, CHANNEL_TIMER_TIMEOUT_MS);
    }

    private void cleanupAndNotifySearchFinished() {
        cleanupChannelHandlers();
        stopChannelSearch();
    }


    /**
     * This class is used to listen for data received on a newly opened channel in order
     * to know whether the channel connected successfully or not.
     */
    private class AntChannelEventHandler implements IAntChannelEventHandler {

        private AntChannel channel;
        private boolean isChannelConnected;

        public boolean isChannelConnected() {
            return isChannelConnected;
        }

        /**
         * Constructor.
         * @param channel the ANT channel that this object will listen for messages from.
         */
        public AntChannelEventHandler(AntChannel channel) {
            this.channel = channel;
            isChannelConnected = false;

            try {
                channel.setChannelEventHandler(this);
            } catch (RemoteException e) {
                Log.e(GlobalState.LOG_TAG, "Unable to set ANT Channel event handler: " + e.getMessage());
            }
        }

        /**
         * Checks if a message is received and if it is a broadcast data message. If so, the
         * channel has successfully connected, and a new channel search is started.
         * @param messageType the ANT message type.
         * @param messageParcel used to construct an object that can read the message contents.
         */
        // TODO: cleanup
        @Override
        public void onReceiveMessage(MessageFromAntType messageType, AntMessageParcel messageParcel) {

            if (messageType == MessageFromAntType.BROADCAST_DATA) {

                try {
                    channel.clearChannelEventHandler();
                } catch (RemoteException e) {
                    Log.e(GlobalState.LOG_TAG, "Unable to clear ANT channel event handler: " + e.getMessage());
                }

                isChannelConnected = true;
                listener.onChannelConnected(channel);
            }
        }

        // TODO: This is a debug implementation. Change later.
        @Override public void onChannelDeath() {
            uiThreadHandler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(context,
                            "onChannelDeath() called",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
