package com.example.mosis_projekat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.mosis_projekat.Data.User

class UserAdapter(context: Context, dataArrayList: ArrayList<User?>?): ArrayAdapter<User?>(context,R.layout.list_item,dataArrayList!!){

    // okida se kad se setuje Adapter
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val listData = getItem(position)

        if(view== null){
            view = LayoutInflater.from(context).inflate(R.layout.list_item,parent,false)
        }

        val listRank = view!!.findViewById<TextView>(R.id.listItem_ranking)
        val listName = view!!.findViewById<TextView>(R.id.listItem_fullName)
        val listPoints = view!!.findViewById<TextView>(R.id.listItem_points)

        val imgV: ImageView = view.findViewById<ImageView>(R.id.listItem_profilePic)
        listRank.text = (position+1).toString()
        Glide.with(context!!).load(listData!!.imgName).into(imgV)
        listName.text = listData!!.name + " " + listData!!.surname
        listPoints.text = listData.points.toString()

        return view
    }
}