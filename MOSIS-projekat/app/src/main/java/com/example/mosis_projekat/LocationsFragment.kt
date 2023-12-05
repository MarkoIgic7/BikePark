package com.example.mosis_projekat

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.mosis_projekat.Data.ParkingSpot
import com.example.mosis_projekat.ViewModel.ParkingSpotViewModel
import com.example.mosis_projekat.ViewModel.UserViewModel


class LocationsFragment : Fragment() {
    private lateinit var locationsAdapter: LocationAdapter
    private lateinit var loc: ParkingSpot
    var locationsArrayList = ArrayList<ParkingSpot?>()
    private val parkingSpotViewModel: ParkingSpotViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()



    private lateinit var searchView: SearchView
    private lateinit var usersForSearch: ArrayList<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userViewModel.getAllUsers()

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_locations, container, false)
        activity!!.setTitle("Lista parking stajalista")
        searchView = view.findViewById(R.id.locationsF_search)



        parkingSpotViewModel._spots.observe(viewLifecycleOwner, Observer {
            if(parkingSpotViewModel._spots.value!=null){
                parkingSpotViewModel._spots.value!!.forEach{
                    locationsArrayList.add(it)
                }
            }
        })

        locationsAdapter = LocationAdapter(context!!,locationsArrayList,userViewModel._usersObject)
        view.findViewById<ListView>(R.id.locationsF_listView).adapter = locationsAdapter

        view.findViewById<ListView>(R.id.locationsF_listView).setOnItemClickListener{parent, view, position, id ->
            val builder = AlertDialog.Builder(context)
            builder.setView(R.layout.location_image_dialog)
            val dialog = builder.create()
            dialog.show()
            dialog.setCancelable(true)
            val listData = parent.getItemAtPosition(position) as ParkingSpot
            Glide.with(context!!).load(listData!!.image).into(dialog.findViewById<ImageView>(R.id.locationImage))
        }


        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                locationsAdapter.filter.filter(p0)
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                locationsAdapter.filter.filter(p0)
                return false
            }

        })
         return view
    }


}