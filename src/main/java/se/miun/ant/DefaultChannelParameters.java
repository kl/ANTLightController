package se.miun.ant;

import com.dsi.ant.message.ChannelId;
import com.dsi.ant.message.ChannelType;

public class DefaultChannelParameters {

    public static final ChannelType TYPE = ChannelType.BIDIRECTIONAL_SLAVE;

    public static final int DEVICE_TYPE = 16;
    public static final int TRANSMISSION_TYPE = 1;
    public static final int PERIOD = 32768; // 1 Hz
    public static final int FREQUENCY = 77;

    public static ChannelId getChannelId(int deviceNumber) {
        return new ChannelId(deviceNumber, DEVICE_TYPE, TRANSMISSION_TYPE);
    }
}
