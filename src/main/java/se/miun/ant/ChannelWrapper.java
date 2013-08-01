package se.miun.ant;

import android.os.RemoteException;
import android.util.Log;

import com.dsi.ant.channel.AntChannel;
import com.dsi.ant.channel.AntCommandFailedException;
import com.dsi.ant.channel.IAntChannelEventHandler;
import com.dsi.ant.message.ChannelState;
import com.dsi.ant.message.EventCode;
import com.dsi.ant.message.fromant.BroadcastDataMessage;
import com.dsi.ant.message.fromant.ChannelEventMessage;
import com.dsi.ant.message.fromant.ChannelStatusMessage;
import com.dsi.ant.message.fromant.MessageFromAntType;
import com.dsi.ant.message.ipc.AntMessageParcel;

public class ChannelWrapper implements IAntChannelEventHandler {

    public interface ChannelDataListener {
        public void onChannelDataReceived(byte[] data, ChannelWrapper channelWrapper);
        public void onChannelConnectionClosed();
    }

    public static final String TAG = "ANTLightController";

    private AntChannel antChannel;
    private ChannelDataListener listener;

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

    public void releaseChannel() {
        if (antChannel == null) return;

        try {
            antChannel.close();
        } catch (RemoteException e) {
            Log.e(TAG, "Error closing ANT channel: " + e.getMessage());
        } catch (AntCommandFailedException e) {
            Log.e(TAG, "Error closing ANT channel: " + e.getMessage());
        }

        antChannel.release();
        antChannel = null;
    }

    public void setChannelDataListener(ChannelDataListener listener) {
        this.listener = listener;
    }

    @Override // TODO: cleanup
    public void onReceiveMessage(MessageFromAntType messageType, AntMessageParcel messageParcel) {
        if (listener == null) return;

        if (messageType == MessageFromAntType.BROADCAST_DATA) {
            BroadcastDataMessage message = new BroadcastDataMessage(messageParcel);
            byte[] messageData = message.getPayload();
            if (messageData != null) listener.onChannelDataReceived(messageData, this);
        }

        if (messageType == MessageFromAntType.CHANNEL_EVENT) {
            ChannelEventMessage eventMessage = new ChannelEventMessage(messageParcel);
            EventCode code = eventMessage.getEventCode();

            if (code == EventCode.CHANNEL_CLOSED || code == EventCode.RX_FAIL) {
                listener.onChannelConnectionClosed();
            }
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
