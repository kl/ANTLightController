package se.miun.ant;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;

/**
 * It is necessary to use a ListAdapter in order to get something to display on an Android ListView.
 * This class implements a ListAdapter which extends ArrayAdapter. Each element in the backing array
 * (actually a list) will be rendered on the ListView in the ChannelListFragment class.
 */
public class ChannelAdapter extends ArrayAdapter<ListItemState> {

    // The list of ListItemState objects. This is the list that is used when rendering the UI list.
    private List<ListItemState> itemStates;
    // A LayoutInflater object is needed to create View objects from an XML file.
    private LayoutInflater inflater;

    /**
     * Constructor.
     * @param context the context needed by the ArrayAdapter.
     * @param itemStates the list of ListItemState objects used to create views from.
     */
    public ChannelAdapter(Context context, List<ListItemState> itemStates) {
        super(context, R.layout.channel_list_view, itemStates);
        this.itemStates = itemStates;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * This method is overridden to always return false in order to disable the views in the UI
     * list from being selectable.
     * @param position the position in the list that the user clicked.
     * @return always false.
     */
    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    /**
     * Constructs a View object for the given position in the list.
     * See http://developer.android.com/reference/android/widget/Adapter.html for more info on
     * how this method works.
     * @param position the position in the list to create the View for.
     * @param convertView a cached View object used to speed up view creation (may be null).
     * @param parent The parent that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) convertView = inflater.inflate(R.layout.channel_list_view, null);

        try {
            ListItemState stateAtPosition = itemStates.get(position);
            return setViewState(convertView, stateAtPosition);
        } catch (IndexOutOfBoundsException e) {
            // This can happen if a ListItemState gets removed from itemStates after
            // the start of this method but before the start of the try block.
            return convertView;
        }
    }

    //
    // Sets the "state" of the channel list view object (see layout/channel_list_view.xml).
    // This method gets references to each of the child-views of channel list view (for example the
    // light intensity SeekBar view) and sets their values to reflect the values in the
    // ListItemState object. The ViewHolder class is used to avoid excessive calls to findViewById,
    // which can be expensive.
    //
    private View setViewState(View channelView, ListItemState stateAtPosition) {

        TextView intensityView;
        SeekBar intensityBar;
        ImageButton openButton;

        ViewHolder holder = (ViewHolder)channelView.getTag();

        if (holder != null) {
            intensityView = holder.intensityView;
            intensityBar = holder.intensityBar;
            openButton = holder.openButton;
        } else {
            intensityView = (TextView)channelView.findViewById(R.id.channel_intensity_view);
            openButton = (ImageButton)channelView.findViewById(R.id.channel_open_imagebutton);
            intensityBar = (SeekBar)channelView.findViewById(R.id.channel_intensity_seekbar);
            channelView.setTag(new ViewHolder(intensityView, intensityBar, openButton));
        }

        stateAtPosition.setIntensityBar(intensityBar);
        stateAtPosition.setOpenButton(openButton);

        intensityBar.setProgress(stateAtPosition.lightIntensity);
        intensityView.setText(String.valueOf(stateAtPosition.lightIntensity));

        return channelView;
    }

    private class ViewHolder {
        protected TextView intensityView;
        protected SeekBar intensityBar;
        protected ImageButton openButton;

        public ViewHolder(TextView intensityView, SeekBar intensityBar, ImageButton openButton) {
            this.intensityView = intensityView;
            this.intensityBar = intensityBar;
            this.openButton = openButton;
        }
    }
}
