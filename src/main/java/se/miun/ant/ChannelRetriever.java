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

    public ChannelRetriever(Context context, OnChannelProviderAvailableListener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
        AntService.bindService(context, this);
    }

    public AntChannel getChannel() throws ChannelRetrieveException {
        if (channelProvider == null) {
            throw new ChannelRetrieveException("ANT Channel Provider has not been initialized.");
        }

        try {
            return channelProvider.acquireChannel(context, PredefinedNetwork.PUBLIC);
        } catch (ChannelNotAvailableException e) {
            throw new ChannelRetrieveException(e.getMessage(), e);
        } catch (RemoteException e) {
            throw new ChannelRetrieveException(e.getMessage(), e);
        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder binder) {
        antService = new AntService(binder);

        try {
            channelProvider = antService.getChannelProvider();
            listener.onChannelProviderAvailable();
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {}
}

