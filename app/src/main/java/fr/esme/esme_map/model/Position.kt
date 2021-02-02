package fr.esme.esme_map.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Position(
    @PrimaryKey val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double
)