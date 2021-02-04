package fr.esme.esme_map

import android.bluetooth.BluetoothDevice
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LeDeviceListAdapter(
    private val context: Context
) : BaseAdapter() {
    private val mLeDevices: ArrayList<BluetoothDevice>

    init {
        mLeDevices = ArrayList<BluetoothDevice>()
    }

    fun addDevice(device: BluetoothDevice) {
        if (!mLeDevices.contains(device)) {
            mLeDevices.add(device)

        }
    }

    fun getDevice(position: Int): BluetoothDevice? {
        return mLeDevices[position]
    }

    fun clear() {
        mLeDevices.clear()
    }

    override fun getCount(): Int {
        return mLeDevices.size
    }

    override fun getItem(i: Int): Any {
        return mLeDevices[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(i: Int, convertView: View?, parent: ViewGroup): View? {
        var convertView: View? = convertView
        // General ListView optimization code.
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.user_item_view, parent,false)
        }

        var deviceName: TextView = convertView!!.findViewById(R.id.userName) as TextView

        val device = mLeDevices[i]
        if (deviceName != null && deviceName.text.length > 0)
            deviceName.text = device.address
        else
            deviceName.text = "error"

        return convertView
    }
}