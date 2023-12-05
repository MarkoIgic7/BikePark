package com.example.mosis_projekat
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.example.mosis_projekat.Data.ParkingSpot
import com.example.mosis_projekat.Data.User

class LocationAdapter(
    private val context: Context,
    dataArrayList: ArrayList<ParkingSpot?>?,
    users: ArrayList<User>,
): BaseAdapter(), Filterable {
    val usrs = users
    private var filteredData: ArrayList<ParkingSpot?> = dataArrayList!!
    private var allSpots: ArrayList<ParkingSpot?> = dataArrayList!!

    override fun getCount(): Int {
        return filteredData.size
    }
    override fun getItem(p0: Int): ParkingSpot {
        return filteredData.get(p0)!!
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getFilter(): Filter {
        return filter
    }
    private val filter = object: Filter(){
        override fun performFiltering(p0: CharSequence?): FilterResults {
            val result= FilterResults()
            if(p0==null || p0.length==0){
                result.values=allSpots
                result.count=allSpots.size
                //Toast.makeText(context,"Nema rezultata pretrage",Toast.LENGTH_SHORT).show()
            }
            else{
                //Toast.makeText(context,"Ima filter",Toast.LENGTH_SHORT).show()
                val filetered= ArrayList<ParkingSpot>()
                for (ps  in allSpots) {
                    val user = usrs.find { u -> u.uid==ps!!.user }
                    if((user!!.name+" "+user.surname)!!.toLowerCase().contains(p0.toString().toLowerCase())){
                        filetered.add(ps!!)
                    }
                }
                result.values= filetered
                result.count=filetered.size
            }
            return result
        }

        override fun publishResults(p0: CharSequence?, p1: FilterResults) {
            filteredData= p1.values as ArrayList<ParkingSpot?>
            notifyDataSetChanged() // omogucava da se refreshuje prikaz
        }
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val listData = getItem(position)
        val user = usrs.find { it.uid==listData!!.user }

        if(view== null){
            view = LayoutInflater.from(context).inflate(R.layout.list_item_locations,parent,false)
        }

        val listRank = view!!.findViewById<TextView>(R.id.listItemLocations_ranking)
        val listImage = view!!.findViewById<ImageView>(R.id.listItemLocations_profilePic)
        val listName = view!!.findViewById<TextView>(R.id.listItemLocations_fullName)
        val listPoints = view!!.findViewById<TextView>(R.id.listItemLocations_capacity)

        listRank.text = (position+1).toString()
        Glide.with(context!!).load(listData!!.image).into(listImage)
        listName.text = user!!.name+" "+user!!.surname
        listPoints.text = listData.currentNum.toString()+"/"+listData.maxNum.toString()

        return view
    }






}