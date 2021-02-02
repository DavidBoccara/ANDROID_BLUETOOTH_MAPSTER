package fr.esme.esme_map.dao

import androidx.room.TypeConverter
import com.google.gson.Gson
import fr.esme.esme_map.model.Position


public object Converters {
    @TypeConverter
    @JvmStatic
    public fun fromString(value: String?): Position {
        return Gson().fromJson(value, Position::class.java)
    }

    @TypeConverter
    @JvmStatic
    public fun fromArrayLisr(position: Position): String {
        val gson = Gson()
        return gson.toJson(position)
    }
}

