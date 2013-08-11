package se.miun.ant;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

// TODO: change frequency to use a number picker. Not all numbers are allowed.

public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_restore_defaults) {
            restoreDefaults();
            return true;
        }
        return false;
    }

    //
    // Restores the default values to the default SharedPreference object and to the UI views.
    //
    private void restoreDefaults() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        clearSharedPreferences(sharedPrefs);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
        setDefaultViewValues();
    }

    //
    // Clears the current values of the SharedPreferences object. This needs to be done before
    // the default values can be reset.
    //
    private void clearSharedPreferences(SharedPreferences sharedPrefs) {
        Editor editor = sharedPrefs.edit();
        editor.clear();
        editor.commit();
    }

    //
    // This method sets the preference view values to the default values. This is needed because
    // PreferenceActivity does not automatically change the view values when they are restored
    // to the default SharedPreferences object.
    //
    private void setDefaultViewValues() {
        Resources res = getResources();

        EditTextPreference deviceType = (EditTextPreference)findPreference(
                res.getString(R.string.pref_device_type_key));

        EditTextPreference transType = (EditTextPreference)findPreference(
                res.getString(R.string.pref_transmission_type_key));

        EditTextPreference period = (EditTextPreference)findPreference(
                res.getString(R.string.pref_period_key));

        EditTextPreference frequency = (EditTextPreference)findPreference(
                res.getString(R.string.pref_frequency_key));

        deviceType.setText(res.getString(R.string.pref_device_type_default));
        transType.setText(res.getString(R.string.pref_transmission_type_default));
        period.setText(res.getString(R.string.pref_period_default));
        frequency.setText(res.getString(R.string.pref_frequency_default));
    }
}
