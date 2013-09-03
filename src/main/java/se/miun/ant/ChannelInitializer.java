package se.miun.ant;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.RemoteException;
import android.preference.PreferenceManager;

import com.dsi.ant.channel.AntChannel;
import com.dsi.ant.channel.AntCommandFailedException;
import com.dsi.ant.message.ChannelId;
import com.dsi.ant.message.ChannelType;

/**
 * ChannelInitializer takes an non-initialized ("empty") AntChannel object and sets its channel parameters.
 * The channel parameters are fetched from the SharedPreference object for the application. This is the
 * object that is used when the user saves preference values in the SettingsActivity class.
 * For the default parameters, see the file res/xml/preferences.xml
 */
public class ChannelInitializer {

    /** Thrown when there is an error initializing a channel */
    public class ChannelInitializationException extends Exception {
        public ChannelInitializationException(String message) { super(message); }
        public ChannelInitializationException(String message, Throwable cause) { super(message, cause); }
    }

    /** The type of the ANT channel. This should be set to bidirectional slave. */
    public static final ChannelType TYPE = ChannelType.BIDIRECTIONAL_SLAVE;

    // The context needed to access the SharedPreferences.
    private Context context;

    /**
     * Constructor.
     * @param context the Context needed to access the SharedPreferences.
     */
    public ChannelInitializer(Context context) {
        this.context = context;
    }

    /**
     * Initializes a channel with the channel parameters specified in SharedPreferences.
     * @param channel the AntChannel to initialize.
     * @throws ChannelInitializationException if an error occurs.
     */
    public void initializeChannel(AntChannel channel) throws ChannelInitializationException {
        try {
            setChannelParameters(channel);
        } catch (RemoteException e) {
            throw new ChannelInitializationException(e.getMessage(), e);
        } catch (AntCommandFailedException e) {
            throw new ChannelInitializationException(e.getMessage(), e);
        }
    }

    //
    // Sets channel parameters for a channel.
    //
    private void setChannelParameters(AntChannel channel) throws RemoteException, AntCommandFailedException {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Resources resources = context.getResources();

        int deviceType = getDeviceTypeFromPreferences(sharedPrefs, resources);
        int transType  = getTransmissionTypeFromPreferences(sharedPrefs, resources);
        int frequency  = getFrequencyFromPreferences(sharedPrefs, resources);
        int period     = getPeriodFromPreferences(sharedPrefs, resources);

        channel.assign(TYPE);
        channel.setChannelId(getChannelId(deviceType, transType));
        channel.setRfFrequency(frequency);
        channel.setPeriod(period);
    }

    //
    // The following methods return specific channel parameter values by accessing
    // the SharedPreferences object.
    //

    private int getDeviceTypeFromPreferences(SharedPreferences sharedPrefs, Resources res) {
        String deviceType = sharedPrefs.getString(res.getString(R.string.pref_device_type_key), null);
        return Integer.valueOf(deviceType);
    }

    private int getTransmissionTypeFromPreferences(SharedPreferences sharedPrefs, Resources res) {
        String transType = sharedPrefs.getString(res.getString(R.string.pref_transmission_type_key), null);
        return Integer.valueOf(transType);
    }

    private int getFrequencyFromPreferences(SharedPreferences sharedPrefs, Resources res) {
        String frequency = sharedPrefs.getString(res.getString(R.string.pref_frequency_key), null);
        return Integer.valueOf(frequency);
    }

    private int getPeriodFromPreferences(SharedPreferences sharedPrefs, Resources res) {
        String period = sharedPrefs.getString(res.getString(R.string.pref_period_key), null);
        return Integer.valueOf(period);
    }

    //
    // Note: the slave sets id to zero to signify a wildcard value.
    //
    private ChannelId getChannelId(int deviceType, int transmissionType) {
        return new ChannelId(0, deviceType, transmissionType);
    }
}
