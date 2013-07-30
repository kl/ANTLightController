package se.miun.ant;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.dsi.ant.channel.AntChannel;
import com.dsi.ant.channel.AntCommandFailedException;
import com.dsi.ant.message.fromant.DataMessage;

public class LightControllerActivity extends ActionBarActivity
       implements ChannelDataListener,
                  ChannelListFragment.OnChannelSelectedListener,
                  ChannelSearcher.OnChannelConnectedListener {

    public static final String TAG = "ANTLightController";

    // Used to get references to ANT channels from the ANT Radio Service.
    private ChannelRetriever channelRetriever;
    private ChannelSearcher channelSearcher;

    // For now only a single channel is supported.
    private AntChannel channel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_control);

        initializeComponents();
        //channelSearcher.startChannelSearch();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.light_control, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void initializeComponents() {
        channelSearcher = new ChannelSearcher(this);
        channelSearcher.addOnChannelConnectedListener(this);
    }

    @Override
    public void onChannelSearcherInitialized() {
        channelSearcher.startChannelSearch();
    }

    @Override
    public void onChannelConnected(final ChannelWrapper channelWrapper) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = getSupportFragmentManager();
                ChannelListFragment channelListFragment =
                        (ChannelListFragment)fm.findFragmentById(R.id.channel_list_fragment);

                channelListFragment.addChannelWrapper(channelWrapper);
            }
        });
    }

    @Override
    public void onChannelSelected(ChannelWrapper channelWrapper) {
        // TODO: initiate the edit fragment and pass in the channel wrapper to it
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
        channelSearcher.startChannelSearch();
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


}





