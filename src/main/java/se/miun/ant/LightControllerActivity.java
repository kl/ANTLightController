package se.miun.ant;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.dsi.ant.channel.AntChannel;

public class LightControllerActivity extends ActionBarActivity
       implements
        ChannelListFragment.ChannelSelectedListener,
        ChannelSearcher.OnChannelSearchStatusListener {

    public static final String TAG = "ANTLightController";

    private static final String CHANNEL_LIST_FRAGMENT_TAG = "channel_list_fragment_tag";

    // Used to get references to ANT channels from the ANT Radio Service.
    private ChannelSearcher channelSearcher;

    private MenuItem refreshMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_control);

        GlobalState.setApplicationContext(this.getApplicationContext());
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        initializeUI();
        initializeComponents();
        //channelSearcher.startChannelSearch();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.light_control, menu);
        refreshMenuItem = menu.findItem(R.id.action_refresh);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onPause() {
        super.onPause();
        GlobalState.getInstance().closeChannels();
    }

    @Override
    public void onResume() {
        super.onResume();
        GlobalState.getInstance().openChannels();
    }

    private void initializeComponents() {
        channelSearcher = new ChannelSearcher(this, this);
    }

    private void initializeUI() {
        FragmentManager fm = getSupportFragmentManager();

        ChannelListFragment channelListFragment =
                (ChannelListFragment)fm.findFragmentByTag(CHANNEL_LIST_FRAGMENT_TAG);

        if (channelListFragment == null) {
            channelListFragment = new ChannelListFragment();

            FragmentTransaction transaction = fm.beginTransaction();

            transaction.add(R.id.channel_list_fragment_container,
                            channelListFragment,
                            CHANNEL_LIST_FRAGMENT_TAG);

            transaction.commit();
        }
    }

    @Override
    public void onChannelSearcherInitialized() {
        //channelSearcher.startChannelSearch();
    }

    @Override
    public void onChannelSearchStarted() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                ImageView refreshIcon = (ImageView)inflater.inflate(R.layout.refresh_action_view, null);

                Animation rotation = AnimationUtils.loadAnimation(LightControllerActivity.this,
                                                                  R.anim.refresh_rotation);

                rotation.setRepeatCount(Animation.INFINITE);
                refreshIcon.startAnimation(rotation);

                MenuItemCompat.setActionView(refreshMenuItem, refreshIcon);
            }
        });
    }

    @Override
    public void onChannelSearchFinished() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                MenuItemCompat.getActionView(refreshMenuItem).clearAnimation();
                MenuItemCompat.setActionView(refreshMenuItem, null);
            }
        });
    }

    @Override
    public void onNoChannelsAvailable() {
        final Context context = this;

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(context,
                               getString(R.string.no_ant_channels_available),
                               Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onChannelConnected(final AntChannel antChannel) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                FragmentManager fm = getSupportFragmentManager();
                ChannelListFragment channelListFragment =
                        (ChannelListFragment)fm.findFragmentByTag(CHANNEL_LIST_FRAGMENT_TAG);

                ChannelWrapper wrapper = new ChannelWrapper(antChannel);
                channelListFragment.addChannelWrapper(wrapper);
            }
        });
    }

    @Override
    public void onChannelSelected(ChannelWrapper channelWrapper) {
        startChannelViewFragment(channelWrapper);
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

    private void startChannelViewFragment(ChannelWrapper channelWrapper) {
        ChannelViewFragment channelViewFragment = new ChannelViewFragment(channelWrapper);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.channel_list_fragment_container, channelViewFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.addToBackStack(null);

        transaction.commit();
    }

    private void openSettings() {
        // TODO: implement this
        //Toast.makeText(this, "Settings pressed", Toast.LENGTH_LONG).show();
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void refreshAntChannels() {
        channelSearcher.startChannelSearch();
    }

    /*
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
    */


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

    /*
    private void updateIntensityBroadcastData(byte intensity) {
        if (channel == null) return;

        try {
            channel.setBroadcastData(makeBroadcastData(intensity));
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to set broadcast data: " + e.getMessage());
        }
    }
    */

}





