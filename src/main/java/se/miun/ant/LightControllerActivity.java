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

/**
 * This is the main Android activity class for the application. It extends ActionBarActivity
 * in order to provide an action bar. When the user clicks an action bar button it is handled
 * in this class.
 *
 * This class is also responsible for handling fragment transaction. There is one main fragment
 * UI container (see res/layout/activity_lightcontrol.xml) and fragments are added dynamically
 * to the container from this class. An example of a fragment transaction is when the user clicks
 * the "details" button of a channel. Then a new ChannelViewFragment is created and added to the
 * container.
 *
 * When the user clicks the "refresh" action bar button in order to initiate a channel search,
 * this class sends a message to the ChannelSearcher object that tells it to start a channel search.
 * As channels are created and connected by the ChannelSearcher, they are posted back to this class,
 * which in turn forwards the channels to the ChannelListFragment object, which puts the channels
 * in a list which is shown to the user.
 */
public class LightControllerActivity extends ActionBarActivity
                                     implements ChannelListFragment.ChannelSelectedListener,
                                                ChannelSearcher.OnChannelSearchStatusListener {



    // Used as an identifier for the channel list fragment.
    private static final String CHANNEL_LIST_FRAGMENT_TAG = "channel_list_fragment_tag";

    // Used to get references to ANT channels from the ANT Radio Service.
    private ChannelSearcher channelSearcher;

    // The refresh action bar item. We need the reference in order to start/stop the refresh animation.
    private MenuItem refreshMenuItem;

    /**
     * Sets the layout for the activity and does state initialization.
     * @param savedInstanceState the saved instance state or null if none.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_control);

        // The GlobalState class must have a reference to the application context.
        GlobalState.setApplicationContext(this.getApplicationContext());
        // Sets default preference (which are accessible form the SharedPreference object).
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        initializeUI();
        initializeComponents();
    }

    //
    // When this method is called (from onCreate) no fragment has been added to the fragment container.
    // So here we get a reference to and add the ChannelListFragment object to the container,
    // either by creating a new object or getting an already created one. See the Android fragment
    // documentation for more information of when a fragment will be created/reused:
    // http://developer.android.com/guide/components/fragments.html
    //
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

    private void initializeComponents() {
        channelSearcher = new ChannelSearcher(this, this);
    }

    /**
     * Inflate the menu items for use in the action bar.
     * @param menu the Menu object to inflate the menu into.
     * @return true if the menu should be displayed
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.light_control, menu);
        refreshMenuItem = menu.findItem(R.id.action_refresh);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * When onPause is called (meaning this activity is no longer active) we close all channels
     * in order to save battery.
     */
    @Override
    public void onPause() {
        super.onPause();
        GlobalState.getInstance().closeChannels();
    }

    /**
     * When onResume is called (meaning this activity is now the active activity) when open all the
     * channels that were close in onPause.
     */
    @Override
    public void onResume() {
        super.onResume();
        GlobalState.getInstance().openChannels();
    }

    /**
     * This method is called when the ChannelSearcher has completed its initialization.
     */
    @Override
    public void onChannelSearcherInitialized() {
        // Do nothing for now.
    }

    /**
     * When the ChannelSearcher has successfully started a channel search it will notify
     * LightControllerActivity through this method. Here we start the refresh icon animation
     * to let the user know that a search is in progress.
     */
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

                // Exchange the current static refresh icon for the animated icon.
                MenuItemCompat.setActionView(refreshMenuItem, refreshIcon);
            }
        });
    }

    /**
     * When a channel search has finished (either successfully or through an error) this method is called.
     * Here we stop and clear the refresh icon animation.
     */
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

    /**
     * This method is called when the channel searcher attempted to start a search but all
     * ANT channels were already in use.
     */
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

    /**
     * When a channel is successfully created and matched with an ANT master channel, the channel
     * searcher calls this method. When we get the channel we need get a hold of the
     * ChannelListFragment object, so we can send the channel (as a ChannelWrapper object) to it.
     * @param antChannel the connected ANT channel.
     */
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

    /**
     * This method is called by the ChannelListFragment when the user clicks the "details" button
     * for a channel in the channel list. When this happens we start a new ChannelViewFragment
     * that displays the details screen for the channel.
     * @param channelWrapper the ChannelWrapper object for the channel that was selected.
     * @param lightItensity the current light intensity value of the channel.
     */
    @Override
    public void onChannelSelected(ChannelWrapper channelWrapper, int lightItensity) {
        startChannelViewFragment(channelWrapper, lightItensity);
    }

    /**
     * onOptionsItemSelected is called when the user clicks a button in the action bar.
     * When this happens we check the ID of the MenuItem and take the appropriate action.
     * @param item the MenuItem that was clicked.
     * @return true if this (overridden) method handled the method, otherwise return super.
     */
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

    //
    // Creates a new ChannelViewFragment and adds it to the fragment UI container.
    // This fragment will replace the current fragment that occupies the fragment UI container
    // (which will be the ChannelListFragment).
    //
    private void startChannelViewFragment(ChannelWrapper wrapper, int lightIntensity) {
        ChannelViewFragment channelViewFragment = new ChannelViewFragment(wrapper, lightIntensity);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.channel_list_fragment_container, channelViewFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        transaction.addToBackStack(null);

        transaction.commit();
    }

    //
    // Starts the settings activity. This method is called when the user clicks the "settings"
    // icon in the action bar.
    //
    private void openSettings() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    //
    // Tells the ChannelSearcher to start a new channel search. This method is called when the
    // user clicks the "refresh" icon in the action bar.
    //
    private void refreshAntChannels() {
        channelSearcher.startChannelSearch();
    }
}
