/*
 * Copyright (C) 2015 Naman Dwivedi
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package com.naman14.timber.fragments;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.view.View;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.prefs.ATECheckBoxPreference;
import com.afollestad.appthemeengine.prefs.ATEColorPreference;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.naman14.timber.MusicService;
import com.naman14.timber.R;
import com.naman14.timber.activities.SettingsActivity;
import com.naman14.timber.permissions.Nammu;
import com.naman14.timber.permissions.RuntimePermissionsHelper;
import com.naman14.timber.utils.Constants;
import com.naman14.timber.utils.NavigationUtils;
import com.naman14.timber.utils.PreferencesUtility;
import com.naman14.timber.utils.TimberUtils;
import com.naman14.timber.widgets.BluetoothListPreference;

import static com.naman14.timber.MusicPlayer.mService;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String NOW_PLAYING_SELECTOR = "now_playing_selector";
    private static final String KEY_ABOUT = "preference_about";
    private static final String KEY_SOURCE = "preference_source";
    private static final String KEY_THEME = "theme_preference";
    private static final String TOGGLE_ANIMATIONS = "toggle_animations";
    private static final String TOGGLE_SYSTEM_ANIMATIONS = "toggle_system_animations";
    private static final String KEY_START_PAGE = "start_page_preference";
    private static final String KEY_ENABLE_BLUETOOTH = "enable_bluetooth";
    private static final String KEY_BLUETOOTH_DEVICE = "bluetooth_mac";
    private static final int REQUEST_AUDIO_RECORD = 123;
    Preference nowPlayingSelector;
    SwitchPreference toggleAnimations;
    ListPreference themePreference, startPagePreference;
    CheckBoxPreference bluetoothPreference;
    BluetoothListPreference bluetoothDevicePreference;
    PreferencesUtility mPreferences;
    private String mAteKey;

    private IntentFilter iFilter = new IntentFilter();
    private BroadcastReceiver mReceiver;

    private AsyncTask<Void, Void, Integer> updateTask;
    private boolean enableDevice = false;
    private boolean alreadyAskedForPermission = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        iFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        iFilter.addAction(MusicService.CONNECTION_INTERRUPTED);
        iFilter.addAction(MusicService.CONNECTION_REESTABLISHED);

        mReceiver = new BroadcastReceiver() {
            public void onReceive (Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    switch(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
                        case BluetoothAdapter.STATE_ON:
                            bluetoothPreference.setChecked(true);
                            if(RuntimePermissionsHelper.hasPermissions(getActivity(), Manifest.permission.RECORD_AUDIO)) {
                                bluetoothDevicePreference.setEnabled(true);
                            }
                            preferenceUpdateTask();
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            if(mService != null) {
                                try {
                                    mService.enableReconnect(false);
                                } catch(RemoteException ignored) {}
                            }
                            bluetoothPreference.setChecked(false);
                            bluetoothDevicePreference.setEnabled(false);
                            bluetoothDevicePreference.showProgressBar();
                            bluetoothDevicePreference.emptyDeviceList();
                            bluetoothDevicePreference.setSummary(
                                    getActivity().getResources().getString(R.string.no_receiver_connected));
                    }
                } else if(MusicService.CONNECTION_INTERRUPTED.equals(action)) {
                    bluetoothDevicePreference.setSummary(
                            getActivity().getResources().getString(R.string.no_receiver_connected));
                } else if(MusicService.CONNECTION_REESTABLISHED.equals(action)) {
                    updateDeviceSummaryName();
                }
            }

        };

        mPreferences = PreferencesUtility.getInstance(getActivity());
        nowPlayingSelector = findPreference(NOW_PLAYING_SELECTOR);
//        themePreference = (ListPreference) findPreference(KEY_THEME);
        startPagePreference = (ListPreference) findPreference(KEY_START_PAGE);
        bluetoothPreference = (CheckBoxPreference) findPreference(KEY_ENABLE_BLUETOOTH);
        bluetoothDevicePreference = (BluetoothListPreference) findPreference(KEY_BLUETOOTH_DEVICE);

        nowPlayingSelector.setIntent(NavigationUtils.getNavigateToStyleSelectorIntent(getActivity(),
                Constants.SETTINGS_STYLE_SELECTOR_NOWPLAYING));

        PreferencesUtility.getInstance(getActivity()).setOnSharedPreferenceChangeListener(this);
        setPreferenceClickListeners();

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
    }

    private void setPreferenceClickListeners() {

//        themePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                Intent i = getActivity().getBaseContext().getPackageManager().getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
//                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(i);
//                return true;
//            }
//        });

        startPagePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                switch ((String) newValue) {
                    case "last_opened":
                        mPreferences.setLastOpenedAsStartPagePreference(true);
                        break;
                    case "songs":
                        mPreferences.setLastOpenedAsStartPagePreference(false);
                        mPreferences.setStartPageIndex(0);
                        break;
                    case "albums":
                        mPreferences.setLastOpenedAsStartPagePreference(false);
                        mPreferences.setStartPageIndex(1);
                        break;
                    case "artists":
                        mPreferences.setLastOpenedAsStartPagePreference(false);
                        mPreferences.setStartPageIndex(2);
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        invalidateSettings();
        ATE.apply(view, mAteKey);
    }

    public void invalidateSettings() {
        mAteKey = ((SettingsActivity) getActivity()).getATEKey();

        ATEColorPreference primaryColorPref = (ATEColorPreference) findPreference("primary_color");
        primaryColorPref.setColor(Config.primaryColor(getActivity(), mAteKey), Color.BLACK);
        primaryColorPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new ColorChooserDialog.Builder((SettingsActivity) getActivity(), R.string.primary_color)
                        .preselect(Config.primaryColor(getActivity(), mAteKey))
                        .show();
                return true;
            }
        });

        ATEColorPreference accentColorPref = (ATEColorPreference) findPreference("accent_color");
        accentColorPref.setColor(Config.accentColor(getActivity(), mAteKey), Color.BLACK);
        accentColorPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new ColorChooserDialog.Builder((SettingsActivity) getActivity(), R.string.accent_color)
                        .preselect(Config.accentColor(getActivity(), mAteKey))
                        .show();
                return true;
            }
        });


        findPreference("dark_theme").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // Marks both theme configs as changed so MainActivity restarts itself on return
                Config.markChanged(getActivity(), "light_theme");
                Config.markChanged(getActivity(), "dark_theme");
                // The dark_theme preference value gets saved by Android in the default PreferenceManager.
                // It's used in getATEKey() of both the Activities.
                getActivity().recreate();
                return true;
            }
        });

        final ATECheckBoxPreference statusBarPref = (ATECheckBoxPreference) findPreference("colored_status_bar");
        final ATECheckBoxPreference navBarPref = (ATECheckBoxPreference) findPreference("colored_nav_bar");

        statusBarPref.setChecked(Config.coloredStatusBar(getActivity(), mAteKey));
        statusBarPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ATE.config(getActivity(), mAteKey)
                        .coloredStatusBar((Boolean) newValue)
                        .apply(getActivity());
                return true;
            }
        });


        navBarPref.setChecked(Config.coloredNavigationBar(getActivity(), mAteKey));
        navBarPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ATE.config(getActivity(), mAteKey)
                        .coloredNavigationBar((Boolean) newValue)
                        .apply(getActivity());
                return true;
            }
        });

        if(isBluetoothEnabled()) {
            updateDeviceSummaryName();
            preferenceUpdateTask();
        }

        bluetoothPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if((boolean) newValue) {
                    if(RuntimePermissionsHelper.hasPermissions(getActivity(), Manifest.permission.RECORD_AUDIO)) {
                        bluetoothDevicePreference.setEnabled(true);
                    }
                } else {
                    bluetoothDevicePreference.setEnabled(false);
                    bluetoothDevicePreference.setSummary(
                            getActivity().getResources().getString(R.string.no_receiver_connected));

                    if(mService != null) {
                        try {
                            mService.enableReconnect(false);
                        } catch(RemoteException ignored) {}
                    }
                }
                setBluetoothStatus((boolean) newValue);
                return true;
            }
        });

        if(TimberUtils.isMarshmallow()) {
            if(!Nammu.checkPermission(Manifest.permission.RECORD_AUDIO)) {
                if(!alreadyAskedForPermission) {
                    requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO_RECORD);
                }
            }

        }
    }

    private boolean isBluetoothEnabled() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter.isEnabled();
    }

    private void setBluetoothStatus(boolean enabled) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if(!isEnabled && enabled) {
            bluetoothAdapter.enable();
        } else if(isEnabled && !enabled) {
            bluetoothAdapter.disable();
        }
    }

    private void updateBluetoothPreferences() {
        if (!isBluetoothEnabled()) {
            bluetoothPreference.setChecked(false);
            bluetoothDevicePreference.setEnabled(false);
            bluetoothDevicePreference.showProgressBar();
            bluetoothDevicePreference.emptyDeviceList();
        } else if(isBluetoothEnabled()) {
            bluetoothPreference.setChecked(true);
            if(enableDevice) {
                bluetoothDevicePreference.setEnabled(true);
                preferenceUpdateTask();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        this.getActivity().registerReceiver(mReceiver, iFilter);
        updateDeviceSummaryName();
        if(RuntimePermissionsHelper.hasPermissions(this.getActivity(), Manifest.permission.RECORD_AUDIO)) {
            enableDevice = true;
        }
        updateBluetoothPreferences();

    }

    @Override
    public void onPause() {
        super.onPause();
        this.getActivity().unregisterReceiver(mReceiver);
    }

    private void updateDeviceSummaryName() {
        if(mService != null) {
            String deviceName = null;
            try {
                deviceName = mService.getConnectedDeviceName();
            } catch(RemoteException ignored) {}
            if(bluetoothDevicePreference != null) {
                if(deviceName != null) {
                    bluetoothDevicePreference.setSummary(
                            getActivity().getResources().getString(R.string.connected_to, deviceName));
                } else {
                    bluetoothDevicePreference.setSummary(
                            getActivity().getResources().getString(R.string.no_receiver_connected));
                }
            }
        }
    }

    private void preferenceUpdateTask() {
        if(updateTask != null) {
            updateTask.cancel(true);
            bluetoothDevicePreference.showProgressBar();
        }
        updateTask = new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                do {
                    bluetoothDevicePreference.updateDeviceList();
                } while(bluetoothDevicePreference.getDeviceList().size() == 0);
                return 1;
            }

            @Override
            protected void onPostExecute(Integer integer) {
                bluetoothDevicePreference.hideProgressBar();
                bluetoothDevicePreference.updateListView();
            }
        };
        updateTask.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_AUDIO_RECORD:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    enableDevice = false;
                    bluetoothDevicePreference.setEnabled(false);
                    RuntimePermissionsHelper.showMessageOKCancel(getResources().getString(
                            R.string.record_audio_permission_message,
                            getResources().getString(R.string.app_name)), SettingsFragment.this.getActivity());
                    alreadyAskedForPermission = false;

                } else if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    alreadyAskedForPermission = false;
                    enableDevice = true;
                    bluetoothDevicePreference.setEnabled(true);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
