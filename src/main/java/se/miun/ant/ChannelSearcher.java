package se.miun.ant;

import android.app.Activity;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.dsi.ant.channel.AntChannel;
import com.dsi.ant.channel.AntCommandFailedException;
import com.dsi.ant.channel.IAntChannelEventHandler;
import com.dsi.ant.message.EventCode;
import com.dsi.ant.message.fromant.ChannelEventMessage;
import com.dsi.ant.message.fromant.MessageFromAntType;
import com.dsi.ant.message.ipc.AntMessageParcel;

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
    }

    public static final String TAG = "ANTLightController";

    private Activity activity;                      // Activity used for Toast.
    private OnChannelSearchStatusListener listener;    // Is notified when a channel has connected.
    private ChannelRetriever channelRetriever;      // Used to get channels from the ANT system.
    private ChannelInitializer channelInitializer;  // Used to set default channel parameters.

    // Set to true when a search is in progress, otherwise set to false.
    // A search is in progress from the moment a channel search is initialized until there
    // is a channel timeout (meaning no master channel to connect to) on one of the opened channels.
    private boolean searchInProgress;

    /**
     * Constructor.
     * @param activity the Activity that is needed for Toast notifications.
     */
    public ChannelSearcher(Activity activity) {
        this.activity      = activity;
        channelRetriever   = new ChannelRetriever(activity, this);
        channelInitializer = new ChannelInitializer();
        searchInProgress   = false;
    }

    /**
     * Constructor.
     * @param activity the Activity that is needed for Toast notifications.
     * @param listener this object will be notified when an ANT slave channel has been
     *                 successfully created, opened and connected.
     */
    public ChannelSearcher(Activity activity, OnChannelSearchStatusListener listener) {
        this(activity);
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
            listener.onChannelSearchStarted();
            searchInProgress = true;
            startChannelSearchThread();
        }
    }

    // Starts the thread that runs a channel search. The thread will retrieve, initialize and open
    // one ANT channel and then exit.
    private void startChannelSearchThread() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                AntChannel channel;

                try {
                    channel = channelRetriever.getChannel();
                    channelInitializer.initializeChannel(channel);
                    channel.setChannelEventHandler(new AntChannelEventHandler(channel));

                } catch (ChannelRetriever.ChannelRetrieveException e) {
                    logErrorAndNotify("Unable to retrieve channel: " + e.getMessage(), e);
                    return;
                } catch (ChannelInitializer.ChannelInitializationException e) {
                    logErrorAndNotify("Unable to initialize channel: " + e.getMessage(), e);
                    return;
                } catch (RemoteException e) {
                    logErrorAndNotify("Unable to initialize channel: " + e.getMessage(), e);
                    return;
                }

                tryOpenChannel(channel);
            }
        }).start();
    }

    private void tryOpenChannel(AntChannel channel) {
        try {
            channel.open();
        } catch (RemoteException e) {
            logErrorAndNotify("Unable to open channel: " + e.getMessage(), e);
        } catch (AntCommandFailedException e) {
            logErrorAndNotify("Unable to open channel: " + e.getMessage(), e);
        }
    }

    private void logErrorAndNotify(String message, Exception e) {
        Log.e(TAG, message);
        notifyUserChannelError(e);
    }

    private void notifyUserChannelError(final Exception e) {
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(activity,
                        "Error opening ANT channel: " + e.getMessage() + "\nPlease try again.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * This class is used to listen for data received on a newly opened channel in order
     * to know whether the channel connected successfully or not.
     */
    private class AntChannelEventHandler implements IAntChannelEventHandler {

        private AntChannel channel;

        /**
         * Constructor.
         * @param channel the ANT channel that this object will listen for messages from.
         */
        public AntChannelEventHandler(AntChannel channel) {
            this.channel = channel;
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
                    Log.e(TAG, "Unable to clear ANT channel event handler: " + e.getMessage());
                }

                listener.onChannelConnected(channel);
                startChannelSearchThread();
            } else if (messageType == MessageFromAntType.CHANNEL_EVENT) {

                ChannelEventMessage eventMessage = new ChannelEventMessage(messageParcel);

                if (eventMessage.getEventCode() == EventCode.RX_SEARCH_TIMEOUT) {
                    Log.i(TAG, "Channel timeout");

                    channel.release();
                    searchInProgress = false;
                    listener.onChannelSearchFinished();
                }
            }
        }

        // TODO: This is a debug implementation. Change later.
        @Override public void onChannelDeath() {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(activity,
                                   "onChannelDeath() called",
                                   Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
