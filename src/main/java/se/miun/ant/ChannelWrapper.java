package se.miun.ant;

import android.os.RemoteException;
import android.util.Log;

import com.dsi.ant.channel.AntChannel;
import com.dsi.ant.channel.AntCommandFailedException;
import com.dsi.ant.channel.IAntChannelEventHandler;
import com.dsi.ant.message.ChannelState;
import com.dsi.ant.message.fromant.BroadcastDataMessage;
import com.dsi.ant.message.fromant.ChannelStatusMessage;
import com.dsi.ant.message.fromant.MessageFromAntType;
import com.dsi.ant.message.ipc.AntMessageParcel;

public class ChannelWrapper implements IAntChannelEventHandler {

    public interface ChannelDataListener {
        public void onChannelWrapperDataReceived(byte[] data, ChannelWrapper channelWrapper);
    }

    public static final String TAG = "ANTLightController";

    private AntChannel antChannel;
    private ChannelDataListener listener;

    public AntChannel getAntChannel() { return antChannel; }
    public void setAntChannel(AntChannel antChannel) { this.antChannel = antChannel; }

    public ChannelWrapper(AntChannel antChannel) {
        this.antChannel = antChannel;
        try {
            antChannel.setChannelEventHandler(this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean isChannelAlive() {
        if (antChannel == null) return false;

        try {
            ChannelStatusMessage status = antChannel.requestChannelStatus();
            ChannelState state = status.getChannelState();

            return state != ChannelState.INVALID && state != ChannelState.UNASSIGNED;

        } catch (RemoteException e) {
            Log.e(TAG, "Error getting channel status message: " + e.getMessage());
            return false;
        } catch (AntCommandFailedException e) {
            Log.e(TAG, "Error getting channel status message: " + e.getMessage());
            return false;
        }
    }

    public void setChannelDataListener(ChannelDataListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceiveMessage(MessageFromAntType messageType, AntMessageParcel messageParcel) {
        if (listener == null) return;

        if (messageType == MessageFromAntType.BROADCAST_DATA) {
            BroadcastDataMessage message = new BroadcastDataMessage(messageParcel);
            byte[] messageData = message.getPayload();
            if (messageData != null) listener.onChannelWrapperDataReceived(messageData, this);
        }

    }

    @Override
    public void onChannelDeath() {

    }

    @Override
    public String toString() {
        return antChannel.toString();
    }
}
