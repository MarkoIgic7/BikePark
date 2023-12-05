package com.example.mosis_projekat

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.mosis_projekat.ViewModel.UserViewModel
import com.example.mosis_projekat.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class ProfileFragment : Fragment() {

    lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private val userViewModel: UserViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        setHasOptionsMenu(true)

    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)


        databaseReference = FirebaseDatabase.getInstance("https://mosis-projekat-a9d1a-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
        val user=userViewModel._users.value!!.find { it.uid==FirebaseAuth.getInstance().currentUser!!.uid }
        val imgV: ImageView = view.findViewById<ImageView>(R.id.profileF_profilePic)
        Glide.with(context!!).load(user!!.imgName).into(imgV)
        view.findViewById<TextView>(R.id.profileF_mailTextView).text = user.email
        view.findViewById<TextView>(R.id.profileF_nameTextView).text = user.name +" "+ user.surname
        view.findViewById<TextView>(R.id.profileF_numberTextView).text = user.number
        view.findViewById<TextView>(R.id.profileF_pointsTextView).text = user.points.toString() + " poen(a)"

        view.findViewById<Button>(R.id.profileF_logoutBtn).setOnClickListener({
            auth.signOut()
            val i: Intent = Intent(context, LoginActivity::class.java)
            startActivity(i)
        })
        return view
    }
    override fun onPrepareOptionsMenu(menu: Menu) {
        val item = menu.findItem(R.id.nav_top_Add)
        if (item != null) {
            item.isVisible = false
        }
        activity!!.setTitle("Moj profil")
    }
}