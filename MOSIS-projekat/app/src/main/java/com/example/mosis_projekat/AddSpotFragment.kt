package com.example.mosis_projekat

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import com.example.mosis_projekat.Data.ParkingSpot
import com.example.mosis_projekat.ViewModel.ParkingSpotViewModel
import com.example.mosis_projekat.ViewModel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class AddSpotFragment : Fragment() {

    private lateinit var view: View
    private lateinit var addBtn: Button
    private lateinit var cancelBtn: Button
    private lateinit var img: ImageView
    private lateinit var date: TextView
    private lateinit var user: TextView
    private lateinit var maxNum: EditText


    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference

    private lateinit var latitude: String
    private lateinit var longitude: String

    private lateinit var progressDialog: ProgressDialog






    private val userViewModel: UserViewModel by activityViewModels()
    private val parkingViewModel: ParkingSpotViewModel by activityViewModels()


    lateinit var currentPhotoPath: String
    private lateinit var parkingSpotPicUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener("Lokacija"){ requestKey, bundle ->
            this.latitude = bundle.getString("latitude").toString()
            this.longitude = bundle.getString("longitude").toString()

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.fragment_add_spot, container, false)
        activity!!.setTitle("Dodavanje novog stajalista")
        addBtn = view.findViewById(R.id.addSpot_addButton)
        cancelBtn = view.findViewById(R.id.addSpot_cancelBtn)
        img = view.findViewById(R.id.addSpot_image)
        date = view.findViewById(R.id.addSpot_time)
        user = view.findViewById(R.id.addSpot_author)
        maxNum = view.findViewById(R.id.addSpot_maxNumber)

        storageReference = FirebaseStorage.getInstance().reference

        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val formattedDateTime = currentDateTime.format(formatter)
        date.text = formattedDateTime.toString()


        val curr_user = userViewModel._users.value!!.find {it.uid == FirebaseAuth.getInstance().currentUser!!.uid}
        user.text = curr_user!!.email


        databaseReference = FirebaseDatabase.getInstance("https://mosis-projekat-a9d1a-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")


        cancelBtn.setOnClickListener({
            parentFragmentManager.popBackStack()
        })
        img.setOnClickListener({
            dispatchTakePictureIntent()
        })
        addBtn.setOnClickListener({
            if(!maxNum.text.isEmpty() && img.drawable != null)
            {
                progressDialog = ProgressDialog(context)
                progressDialog.setMessage("Dodavanje stajalista")
                progressDialog.setCancelable(false)
                progressDialog.show()
                uploadParkingSpot()
            }
            else{
                Toast.makeText(context,"Dodajte sliku i popunite polje",Toast.LENGTH_SHORT).show()
            }

        })

        return view
    }

    private fun uploadParkingSpot() {
        var profilePicPath: String = UUID.randomUUID().toString() //+ "URI-"+ profilePicUri.toString().replace("/","")
        storageReference.child("ParkingSpots/"+profilePicPath).putFile(parkingSpotPicUri).addOnCompleteListener{
            if(it.isSuccessful){
                FirebaseStorage.getInstance().reference.child("ParkingSpots/"+profilePicPath).downloadUrl.addOnCompleteListener {

                    val imgName = it.result.toString()

                    val maxNumValue = maxNum.text.toString().toInt()
                    val pSpot = ParkingSpot(profilePicPath,FirebaseAuth.getInstance().currentUser!!.uid,maxNumValue,0,date.text.toString(),this.latitude,this.longitude,imgName)

                    var user = userViewModel._users.value!!.find {it.uid == FirebaseAuth.getInstance().currentUser!!.uid}
                    user!!.points+=15
                    userViewModel.updateUser(user!!)
                    parkingViewModel.addParkingSpot(pSpot)
                    progressDialog.dismiss()
                    Toast.makeText(context,"Dodato stajaliste",Toast.LENGTH_LONG).show()
                    parentFragmentManager.popBackStack()

                }
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(context!!.packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        context!!,
                        "com.example.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, 101)
                }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //Toast.makeText(context, "on activity result", Toast.LENGTH_SHORT).show()
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RegisterActivity.CAMERA_PICK && resultCode == AppCompatActivity.RESULT_OK) {
            var f: File = File(currentPhotoPath)
            img.setImageURI(Uri.fromFile(f))
            img.background = null
            Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
                val f = File(currentPhotoPath)
                mediaScanIntent.data = Uri.fromFile(f)
                context!!.sendBroadcast(mediaScanIntent) // da obavesti sistem da je novi fajl kreiran
            }
            parkingSpotPicUri = Uri.fromFile(f)
        }
    }
    private fun createImageFile(): File {
        // Create an image file name
        //val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "parkingSpot", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }


}