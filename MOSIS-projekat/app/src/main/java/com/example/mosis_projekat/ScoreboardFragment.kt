package com.example.mosis_projekat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.mosis_projekat.Data.User
import com.example.mosis_projekat.ViewModel.UserViewModel

class ScoreboardFragment : Fragment() {
    private lateinit var userAdapter: UserAdapter
    private lateinit var user: User
    var userArrayList= ArrayList<User?>()
    private val userViewModel: UserViewModel by activityViewModels()


    // 1. onCreate()
    // 2. onCreateView()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_scoreboard, container, false)
        activity!!.setTitle("Rang lista korisnika")
        userViewModel._users.observe(viewLifecycleOwner, Observer {
            if(userViewModel._users.value != null){
                userViewModel._users.value!!.forEach{
                    userArrayList.add(it)
                }
            }
        })

        userAdapter = UserAdapter(context!!,userArrayList)
        view.findViewById<ListView>(R.id.scoreboardF_listView).adapter = userAdapter

        return view
    }

}