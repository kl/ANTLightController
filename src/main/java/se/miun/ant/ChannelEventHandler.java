package se.miun.ant;

import com.dsi.ant.channel.IAntChannelEventHandler;
import com.dsi.ant.message.fromant.BroadcastDataMessage;
import com.dsi.ant.message.fromant.MessageFromAntType;
import com.dsi.ant.message.ipc.AntMessageParcel;

public class ChannelEventHandler implements IAntChannelEventHandler {

    private ChannelDataListener listener;

    @Override
    public void onReceiveMessage(MessageFromAntType messageType, AntMessageParcel messageParcel) {

        // For now just deal with broadcast packets.
        if (messageType == MessageFromAntType.BROADCAST_DATA) {
            BroadcastDataMessage message = new BroadcastDataMessage(messageParcel);
            listener.onBroadcastData(message.getPayload());
        }
    }

    @Override
    public void onChannelDeath() {} // TODO: implement this

    public void setChannelDataListener(ChannelDataListener listener) {
        this.listener = listener;
    }
}
