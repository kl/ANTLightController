package se.miun.ant;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * This class is used to keep references to certain objects that need to be created once for the
 * process lifetime of the application, and not destroyed and recreated during every activity
 * lifecycle. For more information on this, see the following document:
 * http://developer.android.com/guide/components/activities.html
 */
public class GlobalState {

    /** The log tag string used for logging. */
    public static final String LOG_TAG = "ANTLightController";

    /**
     * Stores a reference to the application context. This method needs to be called at the start
     * of onCreate() in LightControllerActivity. Note that there is a difference between the
     * application context (it has process level lifetime) and a "normal" context (which has
     * activity level lifetime).
     * @param context the context to get the application context from.
     */
    public static void setApplicationContext(Context context) {
        if (applicationContext == null) applicationContext = context.getApplicationContext();
    }

    private static Context applicationContext;

    // Singleton instance.
    private static GlobalState instance;

    // These objects must only be created once for the app process.
    private ChannelRetriever channelRetriever;
    private ChannelList channelList;

    /**
     * Returns the singleton instance of this class.
     * @return the singleton instance of this class.
     */
    public static GlobalState getInstance() {
        if (instance == null) instance = new GlobalState();
        return instance;
    }

    /**
     * Returns the ChannelRetriever instance.
     * @return the ChannelRetriever instance.
     */
    public ChannelRetriever getChannelRetriever() {
        return channelRetriever;
    }

    /**
     * Returns the ChannelList instance.
     * @return the ChannelList instance.
     */
    public ChannelList getChannelList() {
        return channelList;
    }

    //
    // Private constructor. Only called when creating the singleton instance the first time.
    //
    private GlobalState() {
        channelRetriever = new ChannelRetriever(applicationContext);
        channelList = new ChannelList(applicationContext);
    }

    //
    // Sends the closeChannels() message to the channelList.
    //
    public void closeChannels() {
        channelList.closeChannels();
    }

    //
    // Sends the openChannels() message to the channelList.
    //
    public void openChannels() {
        channelList.openChannels();
    }

    /**
     * For temporary debugging only! Should not be depended on.
     * @param text the text to show as a Toast.
     */
    public static void DEBUG(final String text) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(applicationContext, text, Toast.LENGTH_LONG).show();
            }
        });
    }
}
