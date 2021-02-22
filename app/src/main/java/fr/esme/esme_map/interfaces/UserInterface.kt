package fr.esme.esme_map.interfaces

import android.bluetooth.BluetoothDevice
import fr.esme.esme_map.model.POI
import fr.esme.esme_map.model.Position
import fr.esme.esme_map.model.Run
import fr.esme.esme_map.model.User

interface UserInterface {

    fun getPOIs() : List<POI>
    fun getRuns() : List<Run>
    fun getUsers() : List<User>
    fun getMyPosition() : Position
    //bluetooth
    //fun getIdlist() : List<BluetoothDevice>

}