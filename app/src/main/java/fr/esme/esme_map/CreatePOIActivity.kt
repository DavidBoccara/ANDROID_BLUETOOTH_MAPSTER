package fr.esme.esme_map

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.model.LatLng
import fr.esme.esme_map.ui.main.CreatePOIFragment

class CreatePOIActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_poi_activity)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, CreatePOIFragment.newInstance())
                .commitNow()
        }
    }
}