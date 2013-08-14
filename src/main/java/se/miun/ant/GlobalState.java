package se.miun.ant;


import android.content.Context;
import android.widget.Toast;

public class GlobalState {

    public static final String LOG_TAG = "ANTLightController";

    public static void setApplicationContext(Context context) {
        if (applicationContext == null) applicationContext = context;
    }

    private static Context applicationContext;

    private static GlobalState instance;

    private ChannelRetriever channelRetriever;
    private ChannelList channelList;

    public ChannelRetriever getChannelRetriever() {
        return channelRetriever;
    }

    public ChannelList getChannelList() {
        return channelList;
    }

    public static GlobalState getInstance() {
        if (instance == null) instance = new GlobalState();
        return instance;
    }

    private GlobalState() {
        channelRetriever = new ChannelRetriever(applicationContext);
        channelList = new ChannelList(applicationContext);
    }

    public void closeChannels() {
        channelList.closeChannels();
    }

    public void openChannels() {
        channelList.openChannels();
    }

    /**
     * For temporary debugging only! Should not be depended on.
     * @param text the text to show as a Toast.
     */
    public static void DEBUG(String text) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_LONG).show();
    }
}
