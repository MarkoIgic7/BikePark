package com.example.mosis_projekat.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mosis_projekat.Data.ParkingSpot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson

class ParkingSpotViewModel: ViewModel() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    val _spots: MutableLiveData<ArrayList<ParkingSpot>> = MutableLiveData<ArrayList<ParkingSpot>>()
    var _spotsObject  = ArrayList<ParkingSpot>()


    fun addParkingSpot(ps: ParkingSpot)
    {
        _spots.value?.add(ps)
        databaseReference.child(ps.id!!).setValue(ps).addOnCompleteListener {  }
    }
    fun getAllParkingSpots()
    {
        val spotsList: ArrayList<ParkingSpot> = ArrayList<ParkingSpot>()
        databaseReference.get().addOnSuccessListener {
            if (it.getValue() != null) {
                val q: Map<String, Object> = it.getValue() as HashMap<String, Object>
                q.forEach { (key, value) ->
                    run {

                        val gson = Gson()
                        val json = gson.toJson(value)
                        val ps = gson.fromJson(json, ParkingSpot::class.java)
                        spotsList.add(ps)
                    }
                }
            } else {
            }
            spotsList.sortBy { spot: ParkingSpot -> spot.id  }
            _spotsObject = spotsList
            _spots.value = spotsList
            _spots.value!!.sortByDescending { it.date }
        }
    }
    fun updateSpot(spot: ParkingSpot){

        val liveDB =FirebaseDatabase.getInstance("https://mosis-projekat-a9d1a-default-rtdb.europe-west1.firebasedatabase.app/").getReference("ParkingSpots/").child(spot.id!!)
        _spots.value!!.removeIf { it.id==spot.id }
        _spots.value!!.add(spot)
        //_spots.value!!.sortByDescending { it.points }
        val update= mapOf<String,Any>("currentNum" to spot.currentNum)
        liveDB.updateChildren(update).addOnSuccessListener {

        }
    }
    init {
        databaseReference = FirebaseDatabase.getInstance("https://mosis-projekat-a9d1a-default-rtdb.europe-west1.firebasedatabase.app/").getReference("ParkingSpots")
        storageReference = FirebaseStorage.getInstance().reference
    }
}