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

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.*;

public class ChannelWrapper implements IAntChannelEventHandler {

    public interface ChannelDataListener {
        public void onChannelDataReceived(byte[] data, ChannelWrapper channelWrapper);
        public void onChannelConnectionClosed();
    }

    public class ChannelDataSendException extends Exception {

        public ChannelDataSendException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static final String TAG = "ANTLightController";

    private static final int RX_FAILS_ALLOWED_IN_ROW = 5;

    private int rx_fails = 0;

    private AntChannel antChannel;
    private List<ChannelDataListener> listeners;

    public ChannelWrapper(AntChannel antChannel) {
        listeners = new ArrayList<ChannelDataListener>();
        this.antChannel = antChannel;
        try {
            antChannel.setChannelEventHandler(this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void sendAcknowledgedData(byte[] data) throws ChannelDataSendException {
        checkNotNull(data, "data must not be null");

        try {
            antChannel.startSendAcknowledgedData(data);
        } catch (RemoteException e) {
            throwChannelDataSendException(e);
        } catch (AntCommandFailedException e) {
            throwChannelDataSendException(e);
        }
    }

    public void setBroadcastData(byte[] data) throws ChannelDataSendException {
        checkNotNull(data, "data must not be null");

        try {
            antChannel.setBroadcastData(data);
        } catch (RemoteException e) {
            throwChannelDataSendException(e);
        }
    }

    private void throwChannelDataSendException(Throwable cause) throws ChannelDataSendException {
        throw new ChannelDataSendException(
                "Error sending data over the ANT channel: " + cause.getMessage(), cause);
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

        antChannel.release();
        antChannel = null;
    }

    public void addChannelDataListener(ChannelDataListener listener) {
        listeners.add(listener);
    }

    public void removeChannelDataListener(ChannelDataListener listener) {
        listeners.remove(listener);
    }

    @Override // TODO: cleanup
    public void onReceiveMessage(MessageFromAntType messageType, AntMessageParcel messageParcel) {
        if (listeners.isEmpty()) return;

        if (messageType == MessageFromAntType.BROADCAST_DATA) {
            rx_fails = 0;
            BroadcastDataMessage message = new BroadcastDataMessage(messageParcel);
            byte[] broadcastMessageData = message.getPayload();
            if (broadcastMessageData != null) notifyBroadcastData(broadcastMessageData);
        }

        if (messageType == MessageFromAntType.CHANNEL_EVENT) {
            ChannelEventMessage eventMessage = new ChannelEventMessage(messageParcel);
            EventCode code = eventMessage.getEventCode();

            if (code == EventCode.CHANNEL_CLOSED ) {
                notifyOnChannelConnectionClosed();
            } else if (code == EventCode.RX_FAIL) {
                Log.i(TAG, "RX_FAIL");
                rx_fails += 1;
                if (rx_fails >= RX_FAILS_ALLOWED_IN_ROW) notifyOnChannelConnectionClosed();
            }
        }
    }

    private void notifyBroadcastData(byte[] broadcastMessageData) {
        for (ChannelDataListener listener : listeners) {
            listener.onChannelDataReceived(broadcastMessageData, this);
        }
    }

    private void notifyOnChannelConnectionClosed() {
        for (ChannelDataListener listener : listeners) {
            listener.onChannelConnectionClosed();
        }
    }

    @Override
    public void onChannelDeath() {
        Log.i(GlobalState.LOG_TAG, "onChannelDeath called from ChannelWrapper#" + hashCode());
    }
}











