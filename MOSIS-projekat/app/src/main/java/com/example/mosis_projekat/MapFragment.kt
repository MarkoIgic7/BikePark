package com.example.mosis_projekat

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.example.mosis_projekat.Data.ParkingSpot
import com.example.mosis_projekat.ViewModel.ParkingSpotViewModel
import com.example.mosis_projekat.ViewModel.UserViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.StorageReference
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class MapFragment : Fragment() {
    lateinit var map: MapView
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var context: Context

    private lateinit var locationManager: LocationManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val userViewModel: UserViewModel by activityViewModels()
    private val spotsViewModel: ParkingSpotViewModel by activityViewModels()

    private val auth = FirebaseAuth.getInstance()


    private var longitude: Double= 0.0
    private var latitude:Double  = 0.0


    private lateinit var img:ImageView
    private lateinit var addSpot_author: TextView
    private lateinit var addSpot_maxNum: EditText
    private lateinit var addSpot_date: TextView
    private lateinit var addSpot_btn: Button
    private lateinit var addSpot_cancel:Button

    private lateinit var fabButton: FloatingActionButton
    private lateinit var fabButtonFilter: FloatingActionButton
    private lateinit var showAll: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationManager = getContext()!!.getSystemService(LOCATION_SERVICE) as LocationManager
        userViewModel.getAllUsers()
        spotsViewModel.getAllParkingSpots()
        setHasOptionsMenu(true)


    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       val view = inflater.inflate(R.layout.fragment_map, container, false)
        activity!!.setTitle("Bike Park Nis")
        fabButton = view.findViewById(R.id.fab_radius)
        fabButtonFilter = view.findViewById(R.id.fab_filter)
        showAll = view.findViewById(R.id.showAllBtn)
        showAll.setOnClickListener({
            drawAllPins(spotsViewModel._spotsObject)
        })
        return view
    }

    private fun  showRadiusDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setView(R.layout.radius_dialog)
        val dialog = builder.create()
        dialog.show()
        dialog.setCancelable(true)

        val cncl: Button = dialog.findViewById(R.id.radius_cnclBtn)
        val okBtn: Button = dialog.findViewById(R.id.radius_OkButton)
        val radiusValue: EditText = dialog.findViewById(R.id.radius_radiusInput)

        cncl.setOnClickListener({
            dialog.dismiss()
        })
        okBtn.setOnClickListener({
            if(radiusValue.text.toString().isEmpty()){
                Toast.makeText(context,"Morate popuniti polje",Toast.LENGTH_SHORT).show()
            }
            else{
                dialog.dismiss()
                deleteAllPins()
                drawRadiusPins(radiusValue.text.toString().toDouble())
            }
        })


    }

    private fun drawRadiusPins(d: Double) {
        val result = floatArrayOf(1F)
        spotsViewModel._spotsObject.forEach{
            //Toast.makeText(context,this.latitude.toString()+"/"+this.longitude.toString(),Toast.LENGTH_SHORT).show()
            Location.distanceBetween(this.latitude,this.longitude, it.latitude!!.toDouble(),it.longitude.toDouble(),result)
            if(result[0]<d){
                val pinLocation  = GeoPoint(it.latitude!!.toDouble(), it.longitude.toDouble())
                val pinMarker = Marker(map)
                pinMarker.position = pinLocation
                pinMarker.setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_BOTTOM)
                pinMarker.title = "Stajaliste"
                pinMarker.snippet = it.currentNum.toString()+"/"+it.maxNum.toString()
                pinMarker.icon = resources.getDrawable(R.drawable.twotone_location_on_24_green)
                pinMarker.setOnMarkerClickListener { marker, mapView->
                    val result2 = floatArrayOf(1.0F)
                    Location.distanceBetween(this.latitude,this.longitude,it.latitude.toDouble(),it.longitude.toDouble(),result2)
                    if(result2[0]<25){
                        showCustomDialog(it.maxNum, it.currentNum, it.image!!, it.id)
                    }
                    else{
                        Toast.makeText(context,"Morate biti u radiusu od 25m da bi interagovali",Toast.LENGTH_SHORT).show()
                        Toast.makeText(context,"Ima jos "+ (it.maxNum-it.currentNum).toString()+" slobodnih mesta",Toast.LENGTH_LONG).show()
                    }
                    //Toast.makeText(context,"Marko pokusao pin",Toast.LENGTH_SHORT).show()
                    //showCustomDialog(it.maxNum,it.currentNum, it.image!!)
                    true
                }
                map.overlays.add(pinMarker)
            }
        }

    }

    private fun deleteAllPins() {
        map.overlays.forEach{

            if(it is Marker)
                map.overlays.remove(it)
        }
    }

    private fun drawAllPins(spots: ArrayList<ParkingSpot>) {
        spots.forEach{
            val pinLocation  = GeoPoint(it.latitude!!.toDouble(), it.longitude.toDouble())
            val pinMarker = Marker(map)
            pinMarker.position = pinLocation
            pinMarker.setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_BOTTOM)
            pinMarker.icon = resources.getDrawable(R.drawable.twotone_location_on_24_green)
            pinMarker.title = "Stajaliste"
            pinMarker.snippet = it.currentNum.toString()+"/"+it.maxNum.toString()
            pinMarker.setOnMarkerClickListener { marker, mapView->
                val result2 = floatArrayOf(1.0F)
                Location.distanceBetween(this.latitude,this.longitude,it.latitude.toDouble(),it.longitude.toDouble(),result2)
                if(result2[0]<25){
                    showCustomDialog(it.maxNum,it.currentNum, it.image!!,it.id)
                }
                else{
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle("Morate biti u odgovarajucem radiusu (25m)!")
                    builder.setMessage("Ukupno mesta : "+ it.maxNum.toString()+"\nSlobodno mesta : " +(it.maxNum-it.currentNum).toString()+"\nStajaliste kreireano : "+ it.date)
                    builder.show()
                    //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))
                    //Toast.makeText(context,"Morate biti u radiusu od 25m da bi interagovali",Toast.LENGTH_SHORT).show()
                    //Toast.makeText(context,"Ima jos "+ (it.maxNum-it.currentNum).toString()+" slobodnih mesta",Toast.LENGTH_LONG).show()
                }
                //Toast.makeText(context,"Marko pokusao pin",Toast.LENGTH_SHORT).show()
                //showCustomDialog(it.maxNum,it.currentNum, it.image!!)
                true
            }
            map.overlays.add(pinMarker)
        }

    }

    private fun showCustomDialog(maxNum: Int, currNum: Int, image: String, spotId: String?) {
        val builder = AlertDialog.Builder(context)
        builder.setView(R.layout.reserve_dialog)
        val dialog = builder.create()
        dialog.show()
        dialog.setCancelable(true)

        var plusClicked: Boolean = false
        val imageSpot: ImageView = dialog.findViewById(R.id.reserve_image)
        val imgPlus = dialog.findViewById<ImageView>(R.id.reserve_plus)
        val imgMinus = dialog.findViewById<ImageView>(R.id.reserve_minus)
        val btnCancel: Button = dialog.findViewById(R.id.reserve_cancelBtn)
        val maxlbl: TextView = dialog.findViewById(R.id.reserve_maxLabel)
        val curr: TextView = dialog.findViewById(R.id.reserve_current)
        curr.text = (maxNum - currNum).toString()
        Glide.with(context!!).load(image).into(imageSpot)

        maxlbl.text = "Maximalni broj mesta : " + maxNum.toString()
        val user = userViewModel._users.value!!.find { it.uid == auth.currentUser!!.uid }

        if(user!!.mySpots.contains(spotId)){
            imgPlus.setImageResource(R.drawable.baseline_exposure_plus_1_24_grey)
        }
        else{
            imgMinus.setImageResource(R.drawable.baseline_exposure_neg_1_24_grey)
        }
        var spot = spotsViewModel._spots.value!!.find {it.id == spotId}
        imgPlus.setOnClickListener({
            if(!(user!!.mySpots.contains(spotId))){
                user.mySpots.add(spotId.toString())
                user.points+=2
                spot!!.currentNum++
                spotsViewModel.updateSpot(spot)
                userViewModel.updateUser(user)
                Toast.makeText(context,"Uspesno ste zauzeli mesto",Toast.LENGTH_SHORT).show()
                imgPlus.setImageResource(R.drawable.baseline_exposure_plus_1_24_grey)
                imgMinus.setImageResource(R.drawable.baseline_exposure_neg_1_24_green)
                curr.text = (spot!!.maxNum-spot!!.currentNum).toString()
            }
            else{
                Toast.makeText(context,"Vec ste zauzeli mesto na ovom stajalistu",Toast.LENGTH_SHORT).show()
            }
        })
        imgMinus.setOnClickListener({
            if(user!!.mySpots.contains(spotId)){
                user.mySpots.remove(spotId)
                user.points+=1
                userViewModel.updateUser(user)
                spot!!.currentNum--
                spotsViewModel.updateSpot(spot)
                Toast.makeText(context,"Uspesno ste otkazali mesto",Toast.LENGTH_SHORT).show()
                imgMinus.setImageResource(R.drawable.baseline_exposure_neg_1_24_grey)
                imgPlus.setImageResource(R.drawable.baseline_exposure_plus_1_24_green)
                curr.text = (spot!!.maxNum-spot!!.currentNum).toString()
            }
            else{
                Toast.makeText(context,"Nemate prethodno zauzeto mesto na ovom stajalistu",Toast.LENGTH_SHORT).show()
            }
        })
        btnCancel.setOnClickListener({
            dialog.dismiss()
        })
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }


    @SuppressLint("ServiceCast")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var ctx: Context? = activity?.applicationContext
        org.osmdroid.config.Configuration.getInstance().load(ctx,PreferenceManager.getDefaultSharedPreferences((ctx)))
        map = requireView().findViewById<MapView>(R.id.map)
        map.setMultiTouchControls(true)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))

        }
        if(ActivityCompat.checkSelfPermission(requireActivity(),android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireActivity(),android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }


        setMyLocationOverlay()
        map.controller.setZoom(15.0)
        val startPointNis = GeoPoint(43.3209,21.8958)
        map.controller.setCenter(startPointNis)

        //CancellationToke - ako hocu da Task bude cancelable
        fusedLocationClient.getCurrentLocation(LocationRequest.QUALITY_HIGH_ACCURACY, object : CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
            override fun isCancellationRequested() = false
        })
            .addOnSuccessListener { location: Location? ->
                if (location == null)
                    Toast.makeText(context, "Cannot get location.", Toast.LENGTH_SHORT).show()
                else {
                    //Toast.makeText(context, "Evo ti lokacija", Toast.LENGTH_SHORT).show()
                    this.latitude = location.latitude
                    this.longitude = location.longitude
                    map.controller.setZoom(17.0)
                    val startPointProba = GeoPoint(this.latitude,this.longitude)
                    map.controller.animateTo(startPointProba)
                }

            }
        spotsViewModel._spots.observe(viewLifecycleOwner, Observer {
            if(spotsViewModel._spots.value != null){
                drawAllPins(spotsViewModel._spots.value!!)
            }
        })

        fabButton.setOnClickListener({
            //Toast.makeText(context,this.latitude.toString()+"/"+this.longitude.toString(),Toast.LENGTH_SHORT).show()
            showRadiusDialog()
        })
        fabButtonFilter.setOnClickListener({
            showFilterDialog()
        })



    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showFilterDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setView(R.layout.filter_dialog)
        val dialog = builder.create()
        dialog.show()
        dialog.setCancelable(true)

        val cncl: Button = dialog.findViewById(R.id.filter_cancel)
        val okBtn: Button = dialog.findViewById(R.id.filter_ok)
        val author: EditText = dialog.findViewById(R.id.filter_author)
        val dateFrom: Button = dialog.findViewById(R.id.filter_dateFromButton)
        val dateTo: Button  = dialog.findViewById(R.id.filter_dateToButton)
        val filterNum: EditText = dialog.findViewById(R.id.filter_number)

        val calendar = Calendar.getInstance()
        val datePickFrom = DatePickerDialog.OnDateSetListener{dialog,year,month,dayOfMonth ->
            calendar.set(Calendar.YEAR,year)
            calendar.set(Calendar.MONTH,month)
            calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            val format = "dd-MM-yyyy"
            val sdf = SimpleDateFormat(format)
            dateFrom.text = (sdf.format(calendar.time))
        }
        //val calendarTo = Calendar.getInstance()
        val datePickTo = DatePickerDialog.OnDateSetListener{dialog,year,month,dayOfMonth ->
            calendar.set(Calendar.YEAR,year)
            calendar.set(Calendar.MONTH,month)
            calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            val format = "dd-MM-yyyy"
            val sdf = SimpleDateFormat(format)
            dateTo.text = (sdf.format(calendar.time))
        }


        dateFrom.setOnClickListener {
            DatePickerDialog(context,datePickFrom,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
        dateTo.setOnClickListener({
            DatePickerDialog(context,datePickTo,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show()
        })

        cncl.setOnClickListener({
            dialog.dismiss()
        })
        okBtn.setOnClickListener {
            var listaObjekata: ArrayList<ParkingSpot> = ArrayList<ParkingSpot>()
            spotsViewModel._spotsObject.forEach { ps ->
                val user = userViewModel._users.value!!.find { it.uid == ps.user }
                var numValue = 0
                var nameValue = ""
                var date2: Date
                var date1: Date

                val sdf1 = SimpleDateFormat("dd-MM-yyyy")
                val spotDate: Date = sdf1.parse(ps.date)

                //Datum od provera - ogranicenja
                if(dateFrom.text.equals("Datum od")){
                    date1 = sdf1.parse("01-01-2010")
                }
                else{
                    date1 = sdf1.parse(dateFrom.text.toString())
                }
                // Datum do provera - ogranicenja
                if(dateTo.text.equals("Datum do")){

                    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                    val formattedString: String = LocalDate.now().plusDays(1).format(formatter)
                    date2 = sdf1.parse(formattedString)
                }
                else{
                    date2 = sdf1.parse(dateTo.text.toString())
                }
                // broje mesta provera - ogranicenja
                if(filterNum.text.isEmpty()){
                    numValue = 0
                }
                else{
                    numValue = filterNum.text.toString().toInt()
                }
                // ime proverae - ogranicenja
                if(author.text.isEmpty()){
                    nameValue =""
                }
                else
                {
                    nameValue = author.text.toString()                }
                if ((ps.maxNum - ps.currentNum) >= numValue && user!!.email!!.contains(nameValue) && spotDate.after(date1) && spotDate.before(date2)) {
                    listaObjekata.add(ps)
                }
                //Toast.makeText(context,date2.toString(),Toast.LENGTH_SHORT).show()
            }
            deleteAllPins()
            drawAllPins(listaObjekata)
            dialog.dismiss()
        }
    }

    private fun setMyLocationOverlay() {
        var myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context),map)
        myLocationOverlay.enableMyLocation()
        map.overlays.add(myLocationOverlay)
    }
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ){
            isGranted: Boolean ->
            if(isGranted){
                setMyLocationOverlay()
                //Toast.makeText(context,"TRUE ODOBRENJE",Toast.LENGTH_LONG).show()
            }
        }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            //return
        }
        fusedLocationClient.getCurrentLocation(LocationRequest.QUALITY_HIGH_ACCURACY, object : CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
            override fun isCancellationRequested() = false
        })
            .addOnSuccessListener { location: Location? ->
                if (location == null)
                    Toast.makeText(context, "Cannot get location.", Toast.LENGTH_SHORT).show()
                else {
                    //Toast.makeText(context, "Evo ti lokacija", Toast.LENGTH_SHORT).show()
                    this.latitude = location.latitude
                    this.longitude = location.longitude
                    map.controller.setZoom(17.0)
                    val startPointProba = GeoPoint(this.latitude,this.longitude)
                    map.controller.animateTo(startPointProba)
                }

            }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1 && resultCode==RESULT_OK)
        {
            Toast.makeText(context,"Upaljena lokacija",Toast.LENGTH_SHORT).show()
            activity!!.finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_nav,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_top_Add -> {
                if(ActivityCompat.checkSelfPermission(requireActivity(),android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(context,"Morate omoguciti koriscenje kamere u podesavanjima",Toast.LENGTH_SHORT).show()
                }
                else {


                    //Toast.makeText(context, "Dodavanje novog pina", Toast.LENGTH_SHORT).show()

                    val bundle: Bundle = Bundle()
                    bundle.putString("latitude", this.latitude.toString())
                    bundle.putString("longitude", this.longitude.toString())
                    setFragmentResult("Lokacija", bundle)

                    val fragmentTransaction = parentFragmentManager.beginTransaction()

                    fragmentTransaction.replace(R.id.fragment_container_view, AddSpotFragment())
                    fragmentTransaction.addToBackStack("PROBANEKA")
                    fragmentTransaction.commit()
                }

            }


        }
        return super.onOptionsItemSelected(item)
    }


}


