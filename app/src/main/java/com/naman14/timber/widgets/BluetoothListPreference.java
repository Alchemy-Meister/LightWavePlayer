package com.naman14.timber.widgets;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.naman14.timber.R;
import com.naman14.timber.adapters.BluetoothDeviceAdapter;
import static com.naman14.timber.MusicPlayer.mService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

/**
 * Created by meister on 5/19/16.
 */
public class BluetoothListPreference extends DialogPreference {

    private MaterialProgressBar progressBar;
    private boolean hideProgressBar = false;

    private List<BluetoothDevice> deviceList = new ArrayList<>();
    private BluetoothDeviceAdapter mAdapter;
    private ListView deviceListView;
    private AsyncTask<Void, Void, Boolean> connectionTask;

    public BluetoothListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        mAdapter = new BluetoothDeviceAdapter(context);

        setPersistent(false);
        setDialogLayoutResource(R.layout.dialog_bluetooth_devices);
    }

    public BluetoothListPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        builder.setPositiveButton(null, null);

        if(hideProgressBar) {
            progressBar.setVisibility(View.GONE);
            progressBar.invalidate();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.invalidate();
        }

        deviceListView.setAdapter(mAdapter);

        super.onPrepareDialogBuilder(builder);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        progressBar = (MaterialProgressBar) view.findViewById(R.id.search);
        deviceListView = (ListView) view.findViewById(R.id.deviceList);

        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BluetoothDevice device = deviceList.get(position);

                if(mService != null ) {
                    try {
                        if (!mService.isDeviceConnected(device.getAddress())) {
                            if(connectionTask != null) {
                                connectionTask.cancel(true);
                            }
                            connectionTask = new AsyncTask<Void, Void, Boolean>() {
                                @Override
                                protected Boolean doInBackground(Void... params) {
                                    try {
                                       return mService.connectToDevice(device, true, null);
                                    } catch(RemoteException e) {
                                        return false;
                                    }
                                }

                                @Override
                                protected void onPostExecute(Boolean aBoolean) {
                                    super.onPostExecute(aBoolean);
                                    if(aBoolean) {
                                        Toast.makeText(getContext(), getContext().getResources().getString(R.string.connection_successful),
                                                Toast.LENGTH_SHORT).show();
                                        BluetoothListPreference.this.setSummary(getContext().getResources().getString(R.string.connected_to, device.getName()));
                                        BluetoothListPreference.this.getDialog().dismiss();
                                    } else {
                                        Toast.makeText(getContext(),getContext().getResources().getString(R.string.connection_failed),
                                                Toast.LENGTH_SHORT).show();
                                        BluetoothListPreference.this.setSummary(getContext().getResources().getString(R.string.no_receiver_connected));
                                    }
                                }
                            };
                            connectionTask.execute();
                        } else {
                            Log.d("Preferences", "Already connected");
                        }
                    } catch(RemoteException ignored) {}
                }
            }
        });
    }

    public void showProgressBar() {
        hideProgressBar = false;
        if(progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.invalidate();
        }
    }

    public void hideProgressBar() {
        hideProgressBar = true;
        if(progressBar != null) {
            progressBar.setVisibility(View.GONE);
            progressBar.invalidate();
        }
    }

    public void emptyDeviceList() {
        deviceList = new ArrayList<>();
        updateListView();
    }

    public void updateDeviceList() {
        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bta.getBondedDevices();
        for (BluetoothDevice dev : pairedDevices) {
            if(!deviceList.contains(dev)) {
                deviceList.add(dev);
            }
        }
    }

    public void updateListView() {
        mAdapter.setData(deviceList);
        if(deviceListView != null) {
            deviceListView.setAdapter(mAdapter);
        }
    }

    public List<BluetoothDevice> getDeviceList() {
        return this.deviceList;
    }
}
