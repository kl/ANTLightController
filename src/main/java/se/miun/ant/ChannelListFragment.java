package se.miun.ant;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChannelListFragment extends ListFragment {


    public interface OnChannelSelectedListener {
        public void onChannelSelected(int listItemPosition);
    }


    private class ListItemDataListener implements ChannelWrapper.ChannelDataListener {

        private ChannelWrapper channelWrapper;
        private byte[] lastReceivedData;

        public ListItemDataListener(ChannelWrapper channelWrapper) {
            this.channelWrapper = channelWrapper;
            this.channelWrapper.setChannelDataListener(this);
        }

        @Override
        public void onChannelWrapperDataReceived(byte[] data, ChannelWrapper channelWrapper) {
            if (!Arrays.equals(data, lastReceivedData)) {
                lastReceivedData = data;
                notifyNewDataReceived();
            }
        }

        @Override
        public String toString() {
            return String.valueOf(lastReceivedData[0]);
        }

        private void notifyNewDataReceived() {
            ChannelListFragment.this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    channelsAdapter.notifyDataSetChanged();
                }
            });
        }
    }


    public static final String TAG = "ANTLightController";

    private List<ListItemDataListener> itemListeners;

    private ArrayAdapter<ListItemDataListener> channelsAdapter;

    private OnChannelSelectedListener channelSelectedListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        itemListeners = new ArrayList<ListItemDataListener>();

        channelsAdapter = new ArrayAdapter<ListItemDataListener>(getActivity(),
                                                                 android.R.layout.simple_list_item_1,
                                                                 itemListeners);
        setListAdapter(channelsAdapter);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            channelSelectedListener = (OnChannelSelectedListener)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    " must implement OnChannelSelectedListener");
        }
    }

    @Override
    public void onListItemClick(ListView listView, View clickedView, int position, long id) {
        channelSelectedListener.onChannelSelected(position);
    }

    public void addChannelWrapper(ChannelWrapper channelWrapper) {
        if (channelWrapper.isChannelAlive()) {

            itemListeners.add(new ListItemDataListener(channelWrapper));

        }
    }
}





