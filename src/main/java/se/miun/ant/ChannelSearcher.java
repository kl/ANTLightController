package se.miun.ant;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.dsi.ant.channel.AntChannel;
import com.dsi.ant.channel.AntCommandFailedException;
import com.dsi.ant.channel.IAntChannelEventHandler;
import com.dsi.ant.message.fromant.MessageFromAntType;
import com.dsi.ant.message.ipc.AntMessageParcel;


public class ChannelSearcher implements ChannelRetriever.OnChannelProviderAvailableListener {

    public interface OnChannelConnectedListener {
        public void onChannelSearcherInitialized();
        public void onChannelConnected(AntChannel antChannel);
    }

    private class EventHandler implements IAntChannelEventHandler {

        private AntChannel channel;

        public EventHandler(AntChannel channel) {
            this.channel = channel;
        }

        @Override
        public void onReceiveMessage(MessageFromAntType messageType, AntMessageParcel messageParcel) {

            if (messageType == MessageFromAntType.BROADCAST_DATA) {
                try {
                    channel.clearChannelEventHandler();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                listener.onChannelConnected(channel);

                startChannelSearch();
            }

        }

        @Override
        public void onChannelDeath() {}
    }

    public static final String TAG = "ANTLightController";

    private static int currentChannelId = 1;

    private Context context;
    private OnChannelConnectedListener listener;
    private ChannelRetriever channelRetriever;
    private ChannelInitializer channelInitializer;

    public ChannelSearcher(Context context) {
        this.context       = context;
        channelRetriever   = new ChannelRetriever(context, this);
        channelInitializer = new ChannelInitializer();
    }

    @Override
    public void onChannelProviderAvailable() {
        if (listener != null) listener.onChannelSearcherInitialized();
    }

    public void startChannelSearch() {

        AntChannel channel = null;

        try {
            channel = channelRetriever.getChannel();
            channelInitializer.initializeChannel(channel, getNewChannelId());
        } catch (ChannelRetriever.ChannelRetrieveException e) {
            Log.e(TAG, "Unable to retrieve channel: " + e.getMessage());
            notifyUserChannelError(e);
            return;
        } catch (ChannelInitializer.ChannelInitializationException e) {
            Log.e(TAG, "Unable to initialize channel: " + e.getMessage());
            notifyUserChannelError(e);
            return;
        }

        try {
            channel.setChannelEventHandler(new EventHandler(channel));
        } catch (RemoteException e) {
            e.printStackTrace();
        }


        tryOpenChannel(channel);
    }

    public void addOnChannelConnectedListener(OnChannelConnectedListener listener) {
        this.listener = listener;
    }

    private int getNewChannelId() {
        int id = currentChannelId;
        currentChannelId += 1;
        return id;
    }

    private void tryOpenChannel(AntChannel channel) {
        try {
            channel.open();
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to open channel: " + e.getMessage());
            notifyUserChannelError(e);
        } catch (AntCommandFailedException e) {
            Log.e(TAG, "Unable to open channel: " + e.getMessage());
            notifyUserChannelError(e);
        }
    }

    private void notifyUserChannelError(Exception e) {
        Toast.makeText(context,
                       "Error opening ANT channel: " + e.getMessage() + "\nPlease try again.",
                       Toast.LENGTH_LONG).show();
    }
}
