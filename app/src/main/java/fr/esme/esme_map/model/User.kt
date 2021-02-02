package fr.esme.esme_map.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class User(
    @PrimaryKey var username: String){
    @ColumnInfo(name = "imageUrl") var imageUrl: String = "https://vignette.wikia.nocookie.net/heros/images/f/f7/Ace_Infobox.png/revision/latest?cb=20200621201542&path-prefix=fr"

}
