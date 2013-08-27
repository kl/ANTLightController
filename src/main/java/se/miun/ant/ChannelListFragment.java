package se.miun.ant;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

/**
 * The ChannelListFragment class extends ListFragment in order to display the list of channels
 * that is shown to the user. Extending ListFragment means that this class has a built in
 * ListView (the user interface part of a "list"). For the ListView to display anything to the user,
 * it needs to have a ListAdapter set. A ListAdapter is a class that knows about some data which
 * should be displayed on the ListView, and for each item in the data set it creates one "row" in the list.
 *
 * In this implementation the ListAdapter resides in a separate class, ChannelList. ChannelList
 * is also responsible for maintaining the data set (the ANT channels) that will be displayed
 * on the ListView. So in order for ChannelListFragment to get a hold of the ListAdapter needed
 * by the ListView, it gets a reference to the ChannelList class and then asks for its ListAdapter.
 *
 * Once this is done, ChannelListFragment listens for events from the ChannelList class such as
 * when the user has clicked a channel in the channel list, or when new channel data has arrived
 * on an ANT channel.
 */
public class ChannelListFragment extends ListFragment implements ChannelList.ChannelListener {

    public interface ChannelSelectedListener {
        /**
         * Called when a channel has been selected (the user clicked the channel's "details" button).
         * @param channelWrapper the ChannelWrapper object for the selected channel.
         * @param lightIntensity the current light intensity value of the selected channel.
         */
        public void onChannelSelected(ChannelWrapper channelWrapper, int lightIntensity);
    }

    // The ListView that we get by extending ListFragment.
    private ListView listView;

    private ChannelList channelList;

    // The listener which will be notified when a channel is selected.
    private ChannelSelectedListener channelListener;

    /**
     * Called when the fragment is created and does initial setup.
     * This will generally only happen once during the application lifetime.
     * (i.e. this method will not be called on an orientation change for example).
     * @param savedInstanceState the saved instance state or null.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        channelList = GlobalState.getInstance().getChannelList();
        channelList.setChannelListener(this);

        setEmptyText(getString(R.string.channel_list_empty_text));
        listView = getListView();
        listView.setItemsCanFocus(false);
        setListAdapter(channelList.getChannelAdapter());
    }

    /**
     * Called when the fragment is attached to its parent activity (LightControllerActivity).
     * @param activity the parent activity (will be cast to ChannelSelectedListener).
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            channelListener = (ChannelSelectedListener)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    " must implement OnChannelSelectedListener");
        }
    }

    /**
     * Called when the fragment goes from an inactive to active state. Here we tell the ChannelList
     * to validate its channels because at this point channels may have been closed by the user
     * (in the ChannelViewFragment class) and should therefore not be displayed in the list any more.
     */
    @Override
    public void onResume() {
        super.onResume();
        channelList.validateChannels();
    }

    /**
     * Called by the ChannelList when the user has selected a channel. Delegate the channel to
     * the channelListener object.
     * @param wrapper the ChannelWrapper object of the selected channel.
     * @param lightIntensity the current light intensity value of the channel.
     */
    @Override
    public void onChannelSelected(ChannelWrapper wrapper, int lightIntensity) {
        channelListener.onChannelSelected(wrapper, lightIntensity);
    }

    /**
     * This method is called when an ANT channel receives new light intensity data and the
     * light intensity number for that channel in the channel list needs to be updated. For simplicity
     * the ChannelList will pass in the current intensity values for all channels in an array, and
     * it is up to this method to make sure that the current values or correctly shown in the ListView.
     *
     * Another way to accomplish this is to call the notifyDataSetChanged method on the ListAdapter,
     * which will then automatically update the ListView. The reason this is not used is that it will
     * cause a re-rendering of the ListView which in turn causes the SeekBars to loose focus, which would
     * make them unusable because this method is called quite often.
     * @param intensityValues the current light intensity values for all channels.
     */
    @Override
    public void onLightIntensityDataUpdated(final int[] intensityValues) {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                int firstVisible = listView.getFirstVisiblePosition();

                for (int childIndex = 0; childIndex < listView.getChildCount(); childIndex++) {
                    int intensityIndex = firstVisible + childIndex;

                    // This can happen if a channel is added to the list view after the start of
                    // this method but before the start of the for loop.
                    if (intensityIndex >= intensityValues.length) {
                        return;
                    }

                    View row = listView.getChildAt(childIndex);
                    TextView intensityView = (TextView)row.findViewById(R.id.channel_intensity_view);
                    int lightIntensity = intensityValues[intensityIndex];
                    intensityView.setText(String.valueOf(lightIntensity));
                }
            }
        });
    }

    /**
     * Adds a ChannelWrapper object to the ChannelList.
     * @param channelWrapper the ChannelWrapper to add to the ChannelList.
     */
    public void addChannelWrapper(ChannelWrapper channelWrapper) {
        channelList.addChannelWrapper(channelWrapper);
    }
}
