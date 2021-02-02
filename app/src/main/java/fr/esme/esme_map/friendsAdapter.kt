package fr.esme.esme_map

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import fr.esme.esme_map.interfaces.UserClickInterface
import fr.esme.esme_map.model.User


class FriendsAdapter(
    private val context: Context,
    private val arrayList: java.util.ArrayList<User>
) : BaseAdapter() {

    private lateinit var image: ImageView
    private lateinit var name: TextView


    override fun getCount(): Int {
        return arrayList.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var convertView = convertView
        convertView = LayoutInflater.from(context).inflate(R.layout.user_item_view, parent, false)

        name = convertView.findViewById(R.id.userName)
        name.text = arrayList[position].username

        image = convertView.findViewById(R.id.userImage)
        Picasso.get().load(arrayList[position].imageUrl).into(image);

        image.setOnClickListener {
            (context as UserClickInterface).OnUserClick(arrayList[position])
        }

        return convertView
    }
}