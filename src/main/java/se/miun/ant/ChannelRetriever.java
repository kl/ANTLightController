package se.miun.ant;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.dsi.ant.AntService;
import com.dsi.ant.channel.AntChannel;
import com.dsi.ant.channel.AntChannelProvider;
import com.dsi.ant.channel.ChannelNotAvailableException;
import com.dsi.ant.channel.ChannelNotAvailableReason;
import com.dsi.ant.channel.PredefinedNetwork;

/**
 * The ChannelRetriever class is responsible for connecting to the ANT service and it provides
 * uninitialized (no channel parameters set) channels to its clients via the getChannel() method.
 *
 * The instance of this class lives inside of GlobalState. This is because we only want to connect
 * to the ANT service once for the duration of the application process. We connect to the ANT
 * service in the constructor of this class.
 */
public class ChannelRetriever implements ServiceConnection {

    interface OnChannelProviderAvailableListener {
        /**
         * This method is called when the ANT service has been successfully connected to
         * and the AntChannelProvider object has been initialized (which means its now OK to call
         * getChannel()).
         * */
        public void onChannelProviderAvailable();
    }

    /** Thrown when there is an error retrieving a channel */
    public class ChannelRetrieveException extends Exception {
        public ChannelRetrieveException(String message) { super(message); }
        public ChannelRetrieveException(String message, Throwable cause) { super(message, cause); }
    }

    private Context context;
    private AntService antService;
    private AntChannelProvider channelProvider;
    private OnChannelProviderAvailableListener listener;

    /**
     * Constructor.
     * @param context the context required to bind to the ANT service.
     */
    public ChannelRetriever(Context context) {
        this.context = context.getApplicationContext();
        AntService.bindService(context, this);
    }

    /**
     * Sets the listener that will listen for the OnChannelProviderAvailable event.
     * @param listener the event listener.
     */
    public void setOnChannelProviderAvailableListener(OnChannelProviderAvailableListener listener) {
        this.listener = listener;
        notifyListenerIfProviderAvailable();
    }

    /**
     * Retrieves a new AntChannel object form the AntChannelProvider.
     * @return an uninitialized AntChannel.
     * @throws ChannelRetrieveException if an AntChannel could not be retrieved.
     */
    public AntChannel getChannel() throws ChannelRetrieveException {
        checkChannelProviderInitialized();

        try {
            return channelProvider.acquireChannel(context, PredefinedNetwork.ANT_PLUS);
        } catch (ChannelNotAvailableException e) {
            throw new ChannelRetrieveException(e.getMessage(), e);
        } catch (RemoteException e) {
            throw new ChannelRetrieveException(e.getMessage(), e);
        }
    }

    /**
     * Returns the number of ANT channels currently available on the ANT device.
     * @return the number of ANT channels currently available on the ANT device.
     * @throws ChannelRetrieveException if an error occurred.
     */
    public int getNumberOfChannelsAvailable() throws ChannelRetrieveException {
        // Note: the implementation of this method is somewhat convoluted because the
        // getNumChannelsAvailable() method in AntChannelProvider is stupid and returns
        // 0 both when there are no more channels available and when an error occurred.

        checkChannelProviderInitialized();

        try {
            int availableChannels = channelProvider.getNumChannelsAvailable();

            if (availableChannels == 0) {
                return returnZeroAvailableChannelsUnlessException();
            } else {
                return availableChannels;
            }
        } catch (RemoteException e) {
            throw new ChannelRetrieveException(e.getMessage(), e);
        }
    }

    private int returnZeroAvailableChannelsUnlessException() throws ChannelRetrieveException {
        Exception exception = getChannelOpenException();

        if (isNoMoreChannelsAvailableException(exception)) {
            return 0;
        } else {
            throw new ChannelRetrieveException(exception.getMessage(), exception);
        }
    }

    private Exception getChannelOpenException() {
        AntChannel channel = null;

        try {
            channel = getChannel();
            return null;
        } catch (ChannelRetrieveException e) {
            return (Exception)e.getCause();
        } finally {
            if (channel != null) channel.release();
        }
    }

    private boolean isNoMoreChannelsAvailableException(Exception exception) {

        try {
            ChannelNotAvailableException nae = (ChannelNotAvailableException)exception;
            return nae.reasonCode == ChannelNotAvailableReason.ALL_CHANNELS_IN_USE;
        } catch (ClassCastException e) {
            return false;
        }
    }

    /**
     * This method will be called when we have successfully bound with the ANT service.
     * The passed in IBinder object is used to construct the AntService object.
     * @param componentName the component name.
     * @param binder the binder.
     */
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder binder) {
        antService = new AntService(binder);

        try {
            channelProvider = antService.getChannelProvider();
            notifyListenerIfProviderAvailable();
        } catch (RemoteException e) {
            Log.e(GlobalState.LOG_TAG, e.getMessage());
        }
    }

    @Override // TODO: does this need to be implemented?
    public void onServiceDisconnected(ComponentName componentName) {}

    private void notifyListenerIfProviderAvailable() {
        if (listener != null && channelProvider != null) {
            listener.onChannelProviderAvailable();
        }
    }

    private void checkChannelProviderInitialized() throws ChannelRetrieveException {
        if (channelProvider == null) {
            throw new ChannelRetrieveException("ANT Channel Provider has not been initialized.");
        }
    }
}
