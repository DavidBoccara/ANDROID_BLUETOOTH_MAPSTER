package fr.esme.esme_map.ui.main

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import fr.esme.esme_map.R
import fr.esme.esme_map.model.Category
import fr.esme.esme_map.model.POI
import fr.esme.esme_map.model.Position
import fr.esme.esme_map.model.User
import java.util.*

class CreatePOIFragment : Fragment() {

    companion object {
        fun newInstance() = CreatePOIFragment()
    }

    private lateinit var viewModel: CreatePOIViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.create_poi_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val poi: LatLng? = activity?.intent?.getParcelableExtra<LatLng>("LATLNG")
        viewModel = ViewModelProvider(this).get(CreatePOIViewModel::class.java)
        viewModel.latlng = poi!!

        showPOI(poi)

        val editiText = activity?.findViewById<EditText>(R.id.POINameEditText)

        activity?.findViewById<Button>(R.id.validateButton)?.setOnClickListener {


            //TODO REcuperer depuis le formulaire

            var poi: POI = POI(
                Any().hashCode(),
                //User("JP"),
                editiText?.text.toString(),
                5,
                Position(viewModel.latlng.latitude, viewModel.latlng.longitude),
               // Category("Culture", Color())
            )



            var string = Gson().toJson(poi)

            val intent = Intent().putExtra("poi", string)
            activity?.setResult(1, intent)

            //Close activity
            activity?.finish()

        }

    }

    fun showPOI(poi: LatLng) {

        activity?.findViewById<TextView>(R.id.message)?.text = poi.toString()

    }


}