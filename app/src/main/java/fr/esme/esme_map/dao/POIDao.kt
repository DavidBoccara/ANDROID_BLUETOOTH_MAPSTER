package fr.esme.esme_map.dao

import androidx.room.*
import fr.esme.esme_map.model.POI

@Dao
interface POIDao {

    @Insert
    fun createPOI(poi: POI)

    @Delete
    fun deletePOI(poi: POI)

    @Update
    fun updatePOI(poi: POI)


    @Query("SELECT * FROM poi")
    fun getPOIs() : List<POI>

}