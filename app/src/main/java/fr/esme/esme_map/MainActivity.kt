package fr.esme.esme_map

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.observe
import androidx.room.Room
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import fr.esme.esme_map.dao.AppDatabase
import fr.esme.esme_map.interfaces.UserClickInterface
import fr.esme.esme_map.model.POI
import fr.esme.esme_map.model.Position

class MainActivity : AppCompatActivity(), OnMapReadyCallback, UserClickInterface {

    private val TAG = MainActivity::class.qualifiedName
    private lateinit var mMap: GoogleMap
    private lateinit var viewModel: MainActivityViewModel
    private var isFriendShow = true

    val REQUEST_ENABLE_BT = 3

    private val POI_ACTIVITY = 1
    private val USER_ACTIVITY = 2
    private val BLUETOOTH = 3
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var leDeviceListAdapter: LeDeviceListAdapter


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
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

        if (requestCode == BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) {
                val bluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner
                var mScanning = false
                val handler = Handler()
                val leScanCallback: ScanCallback = object : ScanCallback() {
                    override fun onScanResult(callbackType: Int, result: ScanResult) {
                        super.onScanResult(callbackType, result)


                        Log.d(TAG, "scan sucess" + result.device)
                        leDeviceListAdapter?.addDevice(result.device)
                        leDeviceListAdapter?.notifyDataSetChanged()


                    }
                }
// Stops scanning after 10 seconds.
                val SCAN_PERIOD: Long = 10000

                if (!mScanning) { // Stops scanning after a pre-defined scan period.
                    handler.postDelayed({
                        mScanning = false
                        bluetoothLeScanner.stopScan(leScanCallback)
                    }, SCAN_PERIOD)
                    mScanning = true
                    bluetoothLeScanner.startScan(leScanCallback)
                    Log.d(TAG, "scan")
                } else {
                    mScanning = false
                    bluetoothLeScanner.stopScan(leScanCallback)
                    Log.d(TAG, "No scan")
                }

            }
        } else {
            // User did not enable Bluetooth or an error occurred
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        setContentView(R.layout.activity_main)

        //button
        findViewById<FloatingActionButton>(R.id.showFriendsButton).setOnClickListener {
            manageUserVisibility()
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

        viewModel.poisLiveData.observe(this) { listPOIs ->
            showPOIs(listPOIs)
        }

        viewModel.myPositionLiveData.observe(this) { position ->
            showMyPosition(position)
        }


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
                for (location in locationResult.locations) {
                    showMyPosition(Position(location.latitude, location.longitude))
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        //bluetooth

        leDeviceListAdapter = LeDeviceListAdapter(applicationContext)
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
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

    //TODO show MyPosition
    fun showMyPosition(position: Position) {
        val myPos = LatLng(position.latitude, position.longitude)


        val circleOptions = CircleOptions()
        circleOptions.center(myPos)
        circleOptions.radius(80.0)
        circleOptions.strokeColor(Color.WHITE)
        circleOptions.fillColor(Color.BLACK)
        circleOptions.strokeWidth(6f)

        if (this::myPositionCircle.isInitialized) {
            myPositionCircle.remove()
        }
        myPositionCircle = mMap.addCircle(circleOptions)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPos, 14f))
    }

    lateinit var myPositionCircle: Circle

    //TODO show Travel

    //TODO show USer
    fun manageUserVisibility() {

        if (isFriendShow) {
            isFriendShow = false
            findViewById<ListView>(R.id.friendsListRecyclerview).visibility = View.INVISIBLE
        } else {
            isFriendShow = true
            //bluetooth
            val bluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner
            var mScanning = false
            val handler = Handler()
            val leScanCallback: ScanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    super.onScanResult(callbackType, result)


                    Log.d(TAG, "scan sucess" + result.device)
                    leDeviceListAdapter?.addDevice(result.device)
                    leDeviceListAdapter?.notifyDataSetChanged()


                }
            }
// Stops scanning after 10 seconds.
            val SCAN_PERIOD: Long = 10000

            if (!mScanning) { // Stops scanning after a pre-defined scan period.
                handler.postDelayed({
                    mScanning = false
                    bluetoothLeScanner.stopScan(leScanCallback)
                }, SCAN_PERIOD)
                mScanning = true
                bluetoothLeScanner.startScan(leScanCallback)
                Log.d(TAG, "scan")
            } else {
                mScanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
                Log.d(TAG, "No scan")
            }

        }
        //set device visible
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)


        //print

        //var friends = viewModel.getUsers()
        //var friends = viewModel.getItem()
        //val adapter = FriendsAdapter(this, ArrayList(friends))
        findViewById<ListView>(R.id.friendsListRecyclerview).adapter =
            leDeviceListAdapter//TODO put bluetooth adapter
        Log.d(TAG, "ca marche ?")

        findViewById<ListView>(R.id.friendsListRecyclerview).visibility = View.VISIBLE

    }


    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")

    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    var myPosition: Location? = null

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

    override fun OnUserClick(LeDeviceListAdapter: BluetoothDevice) {
        /**
        val intent = Intent(this, UserActivity::class.java).apply {
        putExtra("USER", Gson().toJson(LeDeviceListAdapter))
        }

        startActivityForResult(intent, USER_ACTIVITY)**/
        Log.d(TAG, "INUSERCLICK")
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                    gatt.close()
                }
            } else {
                Log.w(
                    "BluetoothGattCallback",
                    "Error $status encountered for $deviceAddress! Disconnecting..."
                )
                gatt.close()

            }
        }
    }
}