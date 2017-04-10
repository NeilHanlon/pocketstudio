package edu.wit.mobileapp.pocketstudio;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.XpPreferenceFragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.SeekBar;

import net.xpece.android.support.preference.ListPreference;
import net.xpece.android.support.preference.MultiSelectListPreference;
import net.xpece.android.support.preference.PreferenceDividerDecoration;
import net.xpece.android.support.preference.PreferenceScreenNavigationStrategy;
import net.xpece.android.support.preference.RingtonePreference;
import net.xpece.android.support.preference.SeekBarPreference;
import net.xpece.android.support.preference.SharedPreferencesCompat;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Neil on 4/8/2017.
 */

public class NotificationPrefFragment extends XpPreferenceFragment {

    @Override
    public void onCreatePreferences2(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
        bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        // Setup SeekBarPreference "info" text field.
        final SeekBarPreference volume2 = (SeekBarPreference) findPreference("notifications_new_message_volume2");
        volume2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                volume2.setInfo(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        PreferenceScreenNavigationStrategy.ReplaceFragment.onCreatePreferences(this, rootKey);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final RecyclerView listView = getListView();

        // We're using alternative divider.
        listView.addItemDecoration(new PreferenceDividerDecoration(getContext()).drawBottom(true).drawBetweenCategories(false));
        setDivider(null);

        getPreferenceScreen().setTitle(getActivity().getTitle());

        // We don't want this. The children are still focusable.
        listView.setFocusable(false);
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof SeekBarPreference) {
                SeekBarPreference pref = (SeekBarPreference) preference;
                int progress = (int) value;
                pref.setInfo(progress + "%");
            } else if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            } else if (preference instanceof MultiSelectListPreference) {
                String summary = stringValue.trim().substring(1, stringValue.length() - 1); // strip []
                preference.setSummary(summary);
            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        final String key = preference.getKey();
        if (preference instanceof MultiSelectListPreference) {
            Set<String> summary = SharedPreferencesCompat.getStringSet(
                    PreferenceManager.getDefaultSharedPreferences(preference.getContext()),
                    key,
                    new HashSet<String>());
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, summary);
        } else if (preference instanceof SeekBarPreference) {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, ((SeekBarPreference) preference).getValue());
        } else {
            String value = PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext())
                    .getString(key, "");
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, value);
        }
    }
}
