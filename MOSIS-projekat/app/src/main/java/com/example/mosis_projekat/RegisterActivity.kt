package com.example.mosis_projekat

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.example.mosis_projekat.Data.User
import com.example.mosis_projekat.ViewModel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference

    private lateinit var  redirectToLogin: TextView
    private lateinit var  choosePhoto: ImageView
    private lateinit var  takeAPhoto: ImageView
    private lateinit var  registerBtn: Button

    private lateinit var profilePicUri: Uri
    lateinit var currentPhotoPath: String
    private lateinit var progressDialog: ProgressDialog



    private val userViewModel: UserViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        redirectToLogin = findViewById(R.id.textViewRegister_redirectLogin)
        choosePhoto = findViewById<ImageView>(R.id.imageViewRegister_profilePic)
        takeAPhoto = findViewById(R.id.imageViewRegister_profilePicCamera)
        registerBtn = findViewById(R.id.buttonRegister_signup)

        auth = Firebase.auth
        databaseReference = FirebaseDatabase.getInstance("https://mosis-projekat-a9d1a-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
        storageReference = FirebaseStorage.getInstance().reference


        redirectToLogin.setOnClickListener({
            val intent: Intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        })

        registerBtn.setOnClickListener({
            performRegister() // registracija - FirebaseAUTH
        })

        choosePhoto.setOnClickListener({
            choosePhotoFromGallery()

        })

        takeAPhoto.setOnClickListener({
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 107)
            }
        })

    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            107 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent()
                } else {
                        Toast.makeText(this,"Bicete uskraceni mogucnosti za dodavanje novih Parking stajalista",Toast.LENGTH_SHORT).show()
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun uploadImage() {
        var profilePicPath: String = auth.currentUser?.uid.toString() //+ "URI-"+ profilePicUri.toString().replace("/","")
        storageReference.child("Users/"+profilePicPath).putFile(profilePicUri).addOnCompleteListener{
            if(it.isSuccessful){
                FirebaseStorage.getInstance().reference.child("Users/"+profilePicPath).downloadUrl.addOnCompleteListener {

                val imgName = it.result.toString()
                val uid = auth.currentUser?.uid

                val email  = findViewById<EditText>(R.id.editTextRegister_username).text.toString()
                val pass  = findViewById<EditText>(R.id.editTextRegister_password).text.toString()
                val firstName = findViewById<EditText>(R.id.editTextRegister_name).text.toString()
                val lastName = findViewById<EditText>(R.id.editTextRegister_surname).text.toString()
                val number = findViewById<EditText>(R.id.editTextRegister_number).text.toString()



                val user = User(uid,email,pass,firstName,lastName,number,0,imgName, arrayListOf("inicijalizacija da ne bih posle ispitovao da li postoji"))
                userViewModel.addUser(user)

                if(progressDialog.isShowing)
                    progressDialog.dismiss()
                val intent: Intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
                }
            }
        }
    }


    companion object{
        val GALLERY_PICK = 100
        val CAMERA_PICK = 101
    }

    private fun choosePhotoFromGallery()  {
        val galleryIntent: Intent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, GALLERY_PICK)
    }

    private fun performRegister() {
        val email: EditText = findViewById(R.id.editTextRegister_username)
        val pass: EditText = findViewById(R.id.editTextRegister_password)
        val pass2: EditText = findViewById(R.id.editTextRegister_password2)
        val name: EditText = findViewById(R.id.editTextRegister_name)
        val surname: EditText = findViewById(R.id.editTextRegister_surname)
        val number: EditText = findViewById(R.id.editTextRegister_number)

        if(!email.text.isEmpty() && !pass.text.isEmpty() && !pass2.text.isEmpty() && !name.text.isEmpty() && !surname.text.isEmpty()
            && !number.text.isEmpty() && null != choosePhoto.drawable)
        {
            if(pass.text.toString() == pass2.text.toString())
            {
                val emailInput = email.text.toString()
                val passInput = pass.text.toString()

                auth.createUserWithEmailAndPassword(emailInput, passInput)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            uploadImage()
                            progressDialog = ProgressDialog(this)
                            progressDialog.setMessage("Kreiranje korisnika")
                            progressDialog.setCancelable(false)
                            progressDialog.show()
                        } else {
                            Toast.makeText(baseContext, "Greska prilikom kreiranja naloga", Toast.LENGTH_SHORT,).show()
                        }
                    }
            }
            else
            {
                Toast.makeText(this,"Passwordi nisu istu",Toast.LENGTH_SHORT).show()
            }
        }
        else
        {
            Toast.makeText(baseContext,"Morate pupniti sva polja",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_PICK && resultCode == RESULT_OK) {
            var f:File=File(currentPhotoPath)
            choosePhoto.setImageURI(Uri.fromFile(f))
            Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
                val f = File(currentPhotoPath)
                mediaScanIntent.data = Uri.fromFile(f)
                sendBroadcast(mediaScanIntent)
            }
            profilePicUri= Uri.fromFile(f)
        }
        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK)
        {
            profilePicUri=data?.data!!
            choosePhoto.setImageURI(profilePicUri)
        }
    }
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, CAMERA_PICK)
                }
            }
        }
    }
    private fun createImageFile(): File {
        val storageDir: File? = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("bikeParkProfilePic",".jpg",storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }
}