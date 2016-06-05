package com.naman14.timber.adapters;

import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.TextView;

import com.naman14.timber.R;

/**
 * Created by meister on 5/19/16.
 */
public class BluetoothDeviceAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<BluetoothDevice> mData;

    public BluetoothDeviceAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public void setData(List<BluetoothDevice> data) {
        mData = data;
    }

    public int getCount() {
        return (mData == null) ? 0 : mData.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView	=  mInflater.inflate(R.layout.item_bluetooth_device, null);
            holder = new ViewHolder();
            holder.nameTv = (TextView) convertView.findViewById(R.id.deviceName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CharSequence device	= mData.get(position).getName();

        holder.nameTv.setText(device);

        return convertView;
    }

    private static class ViewHolder {
        TextView nameTv;

    }
}