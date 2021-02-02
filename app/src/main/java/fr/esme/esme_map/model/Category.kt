package fr.esme.esme_map.model

import android.graphics.Color
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Category(
    @PrimaryKey val name: String,
    @ColumnInfo(name = "color") var color: Int
)