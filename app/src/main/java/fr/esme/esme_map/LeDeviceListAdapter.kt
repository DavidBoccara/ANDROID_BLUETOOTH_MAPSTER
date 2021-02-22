package fr.esme.esme_map

import android.bluetooth.*
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast
import java.util.*
import kotlin.collections.ArrayList


class LeDeviceListAdapter(
    private val context: Context
) : BaseAdapter() {
    private val mLeDevices: ArrayList<BluetoothDevice>

    //(context as MainActivity).
    var bluetoothGatt: BluetoothGatt? = null

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
        if (convertView == null) {
            convertView =
                LayoutInflater.from(context).inflate(R.layout.user_item_view, parent, false)
        }

        var deviceName: TextView = convertView!!.findViewById(R.id.userName) as TextView
        var deviceName1: TextView = convertView!!.findViewById(R.id.userName1) as TextView

        val device = mLeDevices[i]
        if (deviceName != null && deviceName.text.length > 0) {
            deviceName.text = device.address
            deviceName1.text = device.name
        } else
            deviceName.text = "error"

        deviceName.setOnClickListener {
            bluetoothGatt = mLeDevices[i].connectGatt(context, false, gattCallback)
            Toast.makeText(context, mLeDevices[i].address, Toast.LENGTH_SHORT).show()
            Log.w(TAG, mLeDevices[i].address.toString())
        }
        return convertView
    }


    //private val TAG = BluetoothLeService::class.java.simpleName
    private  val STATE_DISCONNECTED = 0
    private  val STATE_CONNECTING = 1
    private val STATE_CONNECTED = 2
     val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
   val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
   val ACTION_GATT_SERVICES_DISCOVERED =
        "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
    val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
    val EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"
    //val UUID_HEART_RATE_MEASUREMENT = UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT)
    private var connectionState = STATE_DISCONNECTED

    // Various callback methods defined by the BLE API.
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            val intentAction: String
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    intentAction = ACTION_GATT_CONNECTED
                    connectionState = STATE_CONNECTED
                    broadcastUpdate(intentAction)
                    Log.i(TAG, "Connected to GATT server.")
                    Log.i(
                        TAG, "Attempting to start service discovery: " +
                                bluetoothGatt?.discoverServices()
                    )
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    intentAction = ACTION_GATT_DISCONNECTED
                    connectionState = STATE_DISCONNECTED
                    Log.i(TAG, "Disconnected from GATT server.")
                    broadcastUpdate(intentAction)
                }
            }
        }

        // New services discovered
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
                else -> Log.w(TAG, "onServicesDiscovered received: $status")

            }
        }

        // Result of a characteristic read operation

    }
    val UUID_HEART_RATE_MEASUREMENT = UUID.fromString("5a842ce6-d122-4915-9d94-bfc001b049c2")
    private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic) {
        val intent = Intent(action)

        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        when (characteristic.uuid) {
            UUID_HEART_RATE_MEASUREMENT -> {
                val flag = characteristic.properties
                val format = when (flag and 0x01) {
                    0x01 -> {
                        Log.d(TAG, "Heart rate format UINT16.")
                        BluetoothGattCharacteristic.FORMAT_UINT16
                    }
                    else -> {
                        Log.d(TAG, "Heart rate format UINT8.")
                        BluetoothGattCharacteristic.FORMAT_UINT8
                    }
                }
                val heartRate = characteristic.getIntValue(format, 1)
                Log.d(TAG, String.format("Received heart rate: %d", heartRate))
                intent.putExtra(NfcAdapter.EXTRA_DATA, (heartRate).toString())
            }
            else -> {
                // For all other profiles, writes the data formatted in HEX.
                val data: ByteArray? = characteristic.value
                if (data?.isNotEmpty() == true) {
                    val hexString: String = data.joinToString(separator = " ") {
                        String.format("%02X", it)
                    }
                    Log.d(TAG, String.format("%02X"))
                    intent.putExtra(NfcAdapter.EXTRA_DATA, "$data\n$hexString")
                }
            }

        }
        Log.w(TAG, "broadcastUpdate()")

        val data = characteristic.value

        Log.v(TAG, "data.length: " + data!!.size)

        if (data != null && data.size > 0) {
            val stringBuilder = StringBuilder(data.size)
            for (byteChar in data) {
                stringBuilder.append(String.format("%02X ", byteChar))
                Log.v(TAG, String.format("%02X ", byteChar))
            }
            intent.putExtra(EXTRA_DATA, """${String(data)}$stringBuilder""".trimIndent())
        }
        Log.w(TAG, "broadcast sent !!!")
    }
    fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
            }
        }
    }
    fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
    }
    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        Log.w(TAG, "send broadcast !")
    }



    fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    // Handles various events fired by the Service.
// ACTION_GATT_CONNECTED: connected to a GATT server.
// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
// ACTION_DATA_AVAILABLE: received data from the device. This can be a
// result of read or notification operations.
    /**private val gattUpdateReceiver = object : BroadcastReceiver() {

        //private lateinit var bluetoothLeService: BluetoothLeService

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when (action) {
                ACTION_GATT_CONNECTED -> {
                    connected = true
                    updateConnectionState(R.string.connected)
                    (context as? Activity)?.invalidateOptionsMenu()
                }
                ACTION_GATT_DISCONNECTED -> {
                    connected = false
                    updateConnectionState(R.string.disconnected)
                    (context as? Activity)?.invalidateOptionsMenu()
                    clearUI()
                }
                ACTION_GATT_SERVICES_DISCOVERED -> {
                    // Show all the supported services and characteristics on the
                    // user interface.
                    displayGattServices(bluetoothLeService.getSupportedGattServices())
                }
                ACTION_DATA_AVAILABLE -> {
                    displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA))
                }
            }
        }
    }**/


}


