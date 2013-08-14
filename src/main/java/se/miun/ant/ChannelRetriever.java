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

public class ChannelRetriever implements ServiceConnection {

    interface OnChannelProviderAvailableListener {
        public void onChannelProviderAvailable();
    }

    public static final String TAG = "ANTLightController";

    public class ChannelRetrieveException extends Exception {
        public ChannelRetrieveException(String message) { super(message); }
        public ChannelRetrieveException(String message, Throwable cause) { super(message, cause); }
    }

    private Context context;
    private AntService antService;
    private AntChannelProvider channelProvider;
    private OnChannelProviderAvailableListener listener;

    public ChannelRetriever(Context context) {
        this.context = context.getApplicationContext();
        AntService.bindService(context, this);
    }

    public void setOnChannelProviderAvailableListener(OnChannelProviderAvailableListener listener) {
        this.listener = listener;
        notifyListenerIfProviderAvailable();
    }

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

    public int getNumberOfChannelsAvailable() throws ChannelRetrieveException {
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

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder binder) {
        antService = new AntService(binder);

        try {
            channelProvider = antService.getChannelProvider();
            notifyListenerIfProviderAvailable();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
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

