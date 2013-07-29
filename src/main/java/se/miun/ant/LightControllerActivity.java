package se.miun.ant;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.dsi.ant.channel.AntChannel;
import com.dsi.ant.channel.AntCommandFailedException;
import com.dsi.ant.message.fromant.DataMessage;

import se.miun.ant.ChannelInitializer.ChannelInitializationException;
import se.miun.ant.ChannelRetriever.ChannelRetrieveException;

public class LightControllerActivity extends ActionBarActivity implements ChannelDataListener {

    public static final String TAG = "ANTLightController";

    private static final int CHANNEL_ID = 1;

    // Used to get references to ANT channels from the ANT Radio Service.
    private ChannelRetriever channelRetriever;
    // Used to set channel parameters of an retrieved channel.
    private ChannelInitializer channelInitializer;

    // For now only a single channel is supported.
    private AntChannel channel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_control);

        getViewReferences();
        initializeComponents();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.light_control, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void getViewReferences() {

    }

    private void initializeComponents() {
        channelRetriever = new ChannelRetriever(this);
        channelInitializer = new ChannelInitializer();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items.
        switch (item.getItemId()) {
            case R.id.action_settings:
                openSettings();
                return true;
            case R.id.action_refresh:
                refreshAntChannels();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openSettings() {
        // TODO: implement this
        Toast.makeText(this, "Settings pressed", Toast.LENGTH_LONG).show();
    }

    private void refreshAntChannels() {
        // TODO: implement this
        Toast.makeText(this, "Refresh pressed", Toast.LENGTH_LONG).show();
    }

    private void openChannel() {
        try {
            channel = channelRetriever.getChannel();
            channelInitializer.initializeChannel(channel, getNewChannelId());
        } catch (ChannelRetrieveException e) {
            Log.e(TAG, "Unable to retrieve channel: " + e.getMessage());
            notifyUserChannelError(e);
            return;
        } catch (ChannelInitializationException e) {
            Log.e(TAG, "Unable to initialize channel: " + e.getMessage());
            notifyUserChannelError(e);
            return;
        }

        setChannelEventHandler(channel);
        tryOpenChannel(channel);
    }

    private void closeChannel() {
        if (channel != null) {
            try {
                channel.close();
            } catch (RemoteException e) {
                Log.e(TAG, "Channel close exception: " + e.getMessage());
            } catch (AntCommandFailedException e) {
                Log.e(TAG, "Channel close exception: " + e.getMessage());
            } finally {
                channel = null;
            }
        }
    }

    private int getNewChannelId() {
        // Each channel will need to have a unique channel id in order to differentiate them.
        // Since there is only one channel at the moment, return a constant value.
        return CHANNEL_ID;
    }

    private void setChannelEventHandler(AntChannel channel) {
        ChannelEventHandler handler = new ChannelEventHandler();

        try {
            channel.setChannelEventHandler(handler);
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to set channel event handler: " + e.getMessage());
            notifyUserChannelError(e);
            return;
        }

        handler.setChannelDataListener(this);
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

    @Override
    public void onBroadcastData(final byte[] data) {
        // This method is called when the channel receives a broadcast data packet.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //updateIntensityView(data);
            }
        });
    }

    /*
    private void updateIntensityView(byte[] data) {
        int intensityValue = parseIntensityData(data);
        reportedIntensityTextView.setText(String.valueOf(intensityValue));
    }
    */

    private byte parseIntensityData(byte[] data) {
        // data has a length of 8 and is the payload sent by the master at period.
        // For now return the first value in the array. TODO: implement the real protocol.
        return data[0];
    }

    private void updateIntensityBroadcastData(byte intensity) {
        if (channel == null) return;

        try {
            channel.setBroadcastData(makeBroadcastData(intensity));
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to set broadcast data: " + e.getMessage());
        }
    }

    private byte[] makeBroadcastData(byte intensity) {
        // TODO: implement the real protocol.

        // The payload is a byte array that is DataMessage.LENGTH_STANDARD_PAYLOAD (default 8) long.
        byte[] data = new byte[DataMessage.LENGTH_STANDARD_PAYLOAD];

        // Set the first byte to the intensity value.
        data[0] = intensity;

        // Set the remaining bytes to 0.
        for (int i = 1; i < DataMessage.LENGTH_STANDARD_PAYLOAD; i++) {
            data[i] = 0;
        }

        return data;
    }

    private void notifyUserChannelError(Exception e) {
        Toast.makeText(this,
                       "Error opening ANT channel: " + e.getMessage() + "\nPlease try again.",
                       Toast.LENGTH_LONG).show();
    }
}





