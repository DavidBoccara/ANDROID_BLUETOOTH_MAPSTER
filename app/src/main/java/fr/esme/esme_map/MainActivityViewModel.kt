package fr.esme.esme_map

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fr.esme.esme_map.dao.AppDatabase
import fr.esme.esme_map.interfaces.UserInterface
import fr.esme.esme_map.model.POI
import fr.esme.esme_map.model.Position
import fr.esme.esme_map.model.User

class MainActivityViewModel(private val appDatabase: AppDatabase) : ViewModel() {

    private val user = User("JP") //Me
    private val userInterface: UserInterface =
        UserImplementation(user, appDatabase) // HEAVY NETWORK TASK


    val poisLiveData: MutableLiveData<List<POI>> by lazy {
        MutableLiveData<List<POI>>()
    }

    val myPositionLiveData: MutableLiveData<Position> by lazy {
        MutableLiveData<Position>()
    }


    fun getPositionFromViewModel() {
        //Simulation GPS
        Thread(
            Runnable {
/*                Thread.sleep(300)
                myPositionLiveData.postValue(Position(44.2012986, 1.8576953))
                Thread.sleep(300)
                myPositionLiveData.postValue(Position(46.141736, 0.404965))
                Thread.sleep(300)
                myPositionLiveData.postValue(Position(46.4351729231305, 4.65821921825409))
                Thread.sleep(300)
                myPositionLiveData.postValue(Position(44.7679, -0.3125))
                Thread.sleep(3000)
                myPositionLiveData.postValue(Position(43.7056788063795, 4.66414824128151))
                Thread.sleep(3000)
                myPositionLiveData.postValue(Position(49.228611, 0.721667))
                Thread.sleep(3000)
                myPositionLiveData.postValue(Position(48.6359715815921, -1.51141405105591))
                Thread.sleep(3000)
                myPositionLiveData.postValue(Position(43.4501102, 6.3080895))
                Thread.sleep(3000)
                myPositionLiveData.postValue(Position(46.36354, 4.700442))
                Thread.sleep(3000)
                myPositionLiveData.postValue(Position(48.8736985969585, 2.32269108295441))
                Thread.sleep(3000)*/
                myPositionLiveData.postValue(Position(47.4698136, -0.5593384))
                Thread.sleep(3000)
            }
        ).start()
    }

    fun getPOIFromViewModel() { //TODO add filtre
        Thread(
            Runnable {
                poisLiveData.postValue(userInterface.getPOIs())//json
            }).start()
    }

    fun savePOI(poi: POI) {
        Thread(
            Runnable {
                appDatabase.poiDao().createPOI(poi)
            }).start()
    }

    fun getUsers(): List<User> {
        return userInterface.getUsers()
    }


}