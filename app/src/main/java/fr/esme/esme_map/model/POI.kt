package fr.esme.esme_map.model
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class POI(

    @PrimaryKey val uid: Int,
    //@ColumnInfo(name = "owner") val owner: User,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "score") var score: Int,
    @ColumnInfo(name = "position") var position: Position,
    //@ColumnInfo(name = "category") var category: Category
)