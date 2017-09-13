package kr.co.tjeit.bluetoothplactice.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import kr.co.tjeit.bluetoothplactice.R;
import kr.co.tjeit.bluetoothplactice.data.BtDevice;

/**
 * Created by the on 2017-09-13.
 */

public class BtNewDeviceAdapter extends ArrayAdapter<BtDevice> {

    Context mContext;
    List<BtDevice> mList;
    LayoutInflater inf;

    public BtNewDeviceAdapter(Context context, List<BtDevice> list) {
        super(context, R.layout.btdevice_list_item, list);

        mContext = context;
        mList = list;
        inf = LayoutInflater.from(mContext);

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            row = inf.inflate(R.layout.btdevice_list_item, null);
        }

        BtDevice data = mList.get(position);
        TextView deviceNameTxt = (TextView) row.findViewById(R.id.deviceNameTxt);
        TextView deviceAddressTxt = (TextView) row.findViewById(R.id.deviceAddressTxt);

        deviceNameTxt.setText(data.getDeviceName());
        deviceAddressTxt.setText(data.getDeviceAddress());

        return row;
    }
}
