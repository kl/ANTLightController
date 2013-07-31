package se.miun.ant;

import android.os.RemoteException;

import com.dsi.ant.channel.AntChannel;
import com.dsi.ant.channel.AntCommandFailedException;

public class ChannelInitializer {

    public class ChannelInitializationException extends Exception {
        public ChannelInitializationException(String message) { super(message); }
        public ChannelInitializationException(String message, Throwable cause) { super(message, cause); }
    }

    public void initializeChannel(AntChannel channel)
            throws ChannelInitializationException {

        try {
            channel.assign(DefaultChannelParameters.TYPE);
            channel.setChannelId(DefaultChannelParameters.getChannelId());
            channel.setPeriod(DefaultChannelParameters.PERIOD);
            channel.setRfFrequency(DefaultChannelParameters.FREQUENCY);

        } catch (RemoteException e) {
            throw new ChannelInitializationException(e.getMessage(), e);
        } catch (AntCommandFailedException e) {
            throw new ChannelInitializationException(e.getMessage(), e);
        }
    }
}
