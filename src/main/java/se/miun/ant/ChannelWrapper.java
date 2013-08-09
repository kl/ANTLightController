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

    private static final int RX_FAILS_ALLOWED_IN_ROW = 1;

    private int rx_fails = 0;

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

    public void setBroadcastData(byte[] data) {
        if (antChannel == null) return;

        try {
            antChannel.setBroadcastData(data);
        } catch (RemoteException e) {
            Log.e(TAG, "Error setting broadcast data: " + e.getMessage());
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

    public void closeChannel() {
        // TODO: trying to close the channel causes error message 0x15 (invalid channel state) why?
        /*
        try {
            antChannel.close();
        } catch (RemoteException e) {
            Log.e(GlobalState.LOG_TAG, "Could not close channel: " + e.getMessage());
        } catch (AntCommandFailedException e) {
            Log.e(GlobalState.LOG_TAG, "Could not close channel: " + e.getMessage());
        }
        */

        try {
            antChannel.clearChannelEventHandler();
        } catch (RemoteException e) {
            Log.e(GlobalState.LOG_TAG, "Could not clear channel event handler: " + e.getMessage());
        }
    }

    public void openChannel() {
        /*
        try {
            antChannel.open();
        } catch (RemoteException e) {
            Log.e(GlobalState.LOG_TAG, "Could not open channel: " + e.getMessage());
        } catch (AntCommandFailedException e) {
            Log.e(GlobalState.LOG_TAG, "Could not open channel: " + e.getMessage());
        }
        */

        try {
            antChannel.setChannelEventHandler(this);
        } catch (RemoteException e) {
            Log.e(GlobalState.LOG_TAG, "Could not set channel event handler: " + e.getMessage());
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
            rx_fails = 0;
            BroadcastDataMessage message = new BroadcastDataMessage(messageParcel);
            byte[] messageData = message.getPayload();
            if (messageData != null) listener.onChannelDataReceived(messageData, this);
        }

        if (messageType == MessageFromAntType.CHANNEL_EVENT) {
            ChannelEventMessage eventMessage = new ChannelEventMessage(messageParcel);
            EventCode code = eventMessage.getEventCode();

            if (code == EventCode.CHANNEL_CLOSED ) {
                listener.onChannelConnectionClosed();
            } else if (code == EventCode.RX_FAIL) {
                Log.i(TAG, "RX_FAIL");
                rx_fails += 1;
                if (rx_fails >= RX_FAILS_ALLOWED_IN_ROW) listener.onChannelConnectionClosed();
            }
        }
    }

    @Override
    public void onChannelDeath() {
        Log.i(GlobalState.LOG_TAG, "onChannelDeath called from ChannelWrapper#" + hashCode());
    }
}











