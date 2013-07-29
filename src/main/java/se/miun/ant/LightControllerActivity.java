package se.miun.ant;

import android.os.Bundle;
import android.app.Activity;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dsi.ant.channel.AntChannel;
import com.dsi.ant.channel.AntCommandFailedException;
import com.dsi.ant.message.fromant.DataMessage;

import se.miun.ant.ChannelRetriever.ChannelRetrieveException;
import se.miun.ant.ChannelInitializer.ChannelInitializationException;

public class LightControllerActivity extends Activity implements ChannelDataListener {

    public static final String TAG = "ANTLightController";

    private static final int CHANNEL_ID = 1;

    // Used to get references to ANT channels from the ANT Radio Service.
    private ChannelRetriever channelRetriever;
    // Used to set channel parameters of an retrieved channel.
    private ChannelInitializer channelInitializer;

    private TextView reportedIntensityTextView;
    private SeekBar intensitySlider;

    // For now only a single channel is supported.
    private AntChannel channel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_control);

        getViewReferences();
        intensitySlider.setOnSeekBarChangeListener(getIntensitySliderChangeListener());

        initializeComponents();
    }

    private void getViewReferences() {
        reportedIntensityTextView = (TextView)findViewById(R.id.intensity_reported_view);
        intensitySlider = (SeekBar)findViewById(R.id.intensity_slider);
    }

    private void initializeComponents() {
        channelRetriever = new ChannelRetriever(this);
        channelInitializer = new ChannelInitializer();
    }

    public void onChannelRadioButtonClicked(View view) {
        // This method is called automatically when the user presses a channel radio button.
        switch(view.getId()) {
            case R.id.radio_open_channel : {
                openChannel();
                break;
            }
            case R.id.radio_close_channel : {
                closeChannel();
                break;
            }
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.light_control, menu);
        return true;
    }

    @Override
    public void onBroadcastData(final byte[] data) {
        // This method is called when the channel receives a broadcast data packet.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateIntensityView(data);
            }
        });
    }

    private void updateIntensityView(byte[] data) {
        int intensityValue = parseIntensityData(data);
        reportedIntensityTextView.setText(String.valueOf(intensityValue));
    }

    private byte parseIntensityData(byte[] data) {
        // data has a length of 8 and is the payload sent by the master at period.
        // For now return the first value in the array. TODO: implement the real protocol.
        return data[0];
    }

    private SeekBar.OnSeekBarChangeListener getIntensitySliderChangeListener() {
        return new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateIntensityBroadcastData((byte)progress);
            }

            // These methods are not used.
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        };
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





