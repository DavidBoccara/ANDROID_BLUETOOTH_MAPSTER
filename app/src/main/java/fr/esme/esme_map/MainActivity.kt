package fr.esme.esme_map


import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.room.Room
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import fr.esme.esme_map.dao.AppDatabase
import fr.esme.esme_map.interfaces.UserClickInterface
import fr.esme.esme_map.model.POI
import fr.esme.esme_map.model.Position
import fr.esme.esme_map.model.User
import java.io.IOException
import java.lang.Exception
import java.util.*
import androidx.lifecycle.MutableLiveData
import com.google.gson.reflect.TypeToken
import java.lang.NullPointerException

var devices = java.util.ArrayList<BluetoothDevice>()
var devicesMap = HashMap<String, BluetoothDevice>()
var mArrayAdapter: ArrayAdapter<String>? = null
val uuid: UUID = UUID.fromString("8989063a-c9af-463a-b3f1-f21d9b2b827b")
var message = "toto"
var statei = 0

var myPosition : Position? = null
var myPOIs : List<POI>? = null
val getUserPositionLiveData: MutableLiveData<Position> by lazy {
    MutableLiveData<Position>()
}
val getUserpoisLiveData: MutableLiveData<List<POI>> by lazy {
    MutableLiveData<List<POI>>()
}



class MainActivity : AppCompatActivity(), OnMapReadyCallback, UserClickInterface {
    private val TAG = MainActivity::class.qualifiedName
    private lateinit var mMap: GoogleMap
    private lateinit var viewModel: MainActivityViewModel
    private var isFriendShow = true
    private val POI_ACTIVITY = 1
    private val USER_ACTIVITY = 2
    private lateinit var fusedLocationClient : FusedLocationProviderClient

    private var textView: String? = null


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        viewModel.getPOIFromViewModel()
        viewModel.getPOIFromViewModel()
        viewModel.getPositionFromViewModel()

        mMap.setOnMapClickListener {

            val intent = Intent(this, CreatePOIActivity::class.java).apply {
                putExtra("LATLNG", it)
            }

            startActivityForResult(intent, POI_ACTIVITY)


        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == POI_ACTIVITY) {
            var t = data?.getStringExtra("poi")
            var poi = Gson().fromJson<POI>(t, POI::class.java)
            viewModel.savePOI(poi)
            showPOI(Gson().fromJson<POI>(t, POI::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        setContentView(R.layout.activity_main)
        mArrayAdapter = ArrayAdapter(this, R.layout.dialog_select_device)

        //button
        findViewById<FloatingActionButton>(R.id.showFriendsButton).setOnClickListener {view ->
            if (BluetoothAdapter.getDefaultAdapter() == null) {
                Snackbar.make(view, "Bluetooth is disabled", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()}
            else{
                manageUserVisibility()
            }

        }
        if (statei == 0){
            BluetoothServerController(this).start()
            statei = 1
        }

        //MAP
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //BaseData
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        ).build()



        viewModel = MainActivityViewModel(db)

        viewModel.poisLiveData.observe(this, { listPOIs ->
            showPOIs(listPOIs)
            myPOIs = listPOIs



        })

        viewModel.myPositionLiveData.observe(this, { position ->
            showMyPosition(position)
            myPosition = position
        })




        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            this.requestPermissions(
                arrayOf<String>(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 1
            )
        }

        val locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        var locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    showMyPosition(Position(location.latitude, location.longitude))
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        //getUser
        getUserPositionLiveData.observe(this,{poi->
            showPOI(POI(1,"copain",12,poi!!))
        })
        getUserpoisLiveData.observe(this,{pois ->
            showPOIs(pois)
        })
    }

    //TODO show POI
    fun showPOIs(POIs: List<POI>) {
        POIs?.forEach {
            val poiPos = LatLng(it.position.latitude, it.position.longitude)
            mMap.addMarker(MarkerOptions().position(poiPos).title(it.name))
        }
    }

    fun showPOI(poi: POI) {
        mMap.addMarker(
            MarkerOptions().position(
                LatLng(
                    poi.position.latitude,
                    poi.position.longitude
                )
            ).title(poi.name)
        )
    }

    var test = false
    //TODO show MyPosition
    fun showMyPosition(position: Position) {
        val myPos = LatLng(position.latitude, position.longitude)


        val circleOptions = CircleOptions()
        circleOptions.center(myPos)
        circleOptions.radius(80.0)
        circleOptions.strokeColor(Color.WHITE)
        circleOptions.fillColor(Color.BLACK)
        circleOptions.strokeWidth(6f)

        if(this::myPositionCircle.isInitialized) {
            myPositionCircle.remove()
        }
        myPositionCircle =  mMap.addCircle(circleOptions)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPos, 14f))
    }

    lateinit var myPositionCircle : Circle

    //TODO show Travel

    //TODO show USer


    fun manageUserVisibility() {
        devicesMap = HashMap()
        devices = ArrayList()
        mArrayAdapter!!.clear()

        message = "dd"

        for (device in BluetoothAdapter.getDefaultAdapter().bondedDevices) {
            devicesMap.put(device.address, device)
            devices.add(device)
            // Add the name and address to an array adapter to show in a ListView
            mArrayAdapter!!.add((if (device.name != null) device.name else "Unknown") + "\n" + device.address + "\nPared")
            Log.d(TAG, mArrayAdapter.toString())

        }

        // Start discovery process


        val dialog = SelectDeviceDialog()
        dialog.show(supportFragmentManager, "select_device")


        if (isFriendShow) {
            isFriendShow = false
            findViewById<ListView>(R.id.friendsListRecyclerview).visibility = View.INVISIBLE
        } else {
            isFriendShow = true


            var friends = viewModel.getUsers()

            val adapter = FriendsAdapter(this, ArrayList(friends))
            findViewById<ListView>(R.id.friendsListRecyclerview).adapter = adapter
            findViewById<ListView>(R.id.friendsListRecyclerview).visibility = View.VISIBLE
            //BluetoothServerController(this).start()
        }


    }



    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")

    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }


    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")

    }


    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")

    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()

        mMap.clear()
    }

    override fun OnUserClick(user: User) {

        Log.d("ADAPTER", "test")

        val intent = Intent(this, UserActivity::class.java).apply {
            putExtra("USER", Gson().toJson(user))
        }

        startActivityForResult(intent, USER_ACTIVITY)



    }
}
class SelectDeviceDialog: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(this.activity)
        builder.setTitle("Send message to")
        builder.setAdapter(mArrayAdapter) { _, which: Int ->
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
            BluetoothClient(devices[which]).start()

        }

        return builder.create()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
    }
}
class BluetoothClient(device: BluetoothDevice): Thread() {

    private val socket = device.createRfcommSocketToServiceRecord(uuid)

    override fun run() {

        Log.i("client", "Connecting")
        Log.i("client", "send to :"+devices.toString())
        if(!this.socket.isConnected){
            this.socket.connect()
        }

        val mmBuffer: ByteArray = Gson().toJson(myPOIs).toByteArray()
        Log.i("client", "Sending")
        val outputStream = this.socket.outputStream
        val inputStream = this.socket.inputStream

        try {

            outputStream.write(mmBuffer)
            outputStream.flush()
            Log.i("client", "Sent")
            statei=0
        } catch(e: Exception) {
            Log.e("client", "Cannot send", e)
        }
    }

}

class BluetoothServerController(activity: MainActivity) : Thread() {
    private var cancelled: Boolean
    private val serverSocket: BluetoothServerSocket?
    private val activity = activity

    init {
        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        if (btAdapter != null) {
            this.serverSocket = btAdapter.listenUsingRfcommWithServiceRecord("test", uuid)
            this.cancelled = false
        } else {
            this.serverSocket = null
            this.cancelled = true
        }

    }

    override fun run() {
        var socket: BluetoothSocket

        while(true) {
            if (this.cancelled) {
                break
            }

            try {
                socket = serverSocket!!.accept()
            } catch(e: IOException) {
                break
            }

            if (!this.cancelled && socket != null) {
                Log.i("server", "Connecting")
                BluetoothServer(this.activity, socket).start()
            }
        }
    }

    fun cancel() {
        this.cancelled = true
        this.serverSocket!!.close()
    }
}



class BluetoothServer(private val activity: MainActivity, private val socket: BluetoothSocket): Thread() {
    private val inputStream = this.socket.inputStream
    private val outputStream = this.socket.outputStream

    override fun run() {
        try {
            Thread.sleep(1000)

            val available = inputStream.available()
            val bytes = ByteArray(available)
            Log.i("server", "Reading")
            inputStream.read(bytes, 0, available)
            val text = String(bytes)
            Log.i("server", "Message received")

            if (text.isNotEmpty()){
                activity.runOnUiThread {
                    getUserpoisLiveData.value = Gson().fromJson<List<POI>>(text, object : TypeToken<List<POI>>() {}.type)
                }
                Log.i("server", "OK")

            }
        } catch (e: Exception) {
            Log.e("client", "Cannot read data", e)
        }
        /**finally {
        inputStream.close()
        outputStream.close()
        socket.close()
        }**/
    }
}