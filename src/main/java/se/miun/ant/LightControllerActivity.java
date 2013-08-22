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
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(LightControllerActivity.this,
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
    public void onChannelSelected(ChannelWrapper channelWrapper, int lightItensity) {
        startChannelViewFragment(channelWrapper, lightItensity);
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

    private void startChannelViewFragment(ChannelWrapper wrapper, int lightIntensity) {
        ChannelViewFragment channelViewFragment = new ChannelViewFragment(wrapper, lightIntensity);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.channel_list_fragment_container, channelViewFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        transaction.addToBackStack(null);

        transaction.commit();
    }

    private void openSettings() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void refreshAntChannels() {
        channelSearcher.startChannelSearch();
    }
}

