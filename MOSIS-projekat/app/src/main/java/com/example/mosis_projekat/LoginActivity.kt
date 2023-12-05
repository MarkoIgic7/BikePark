package com.example.mosis_projekat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var registerNow: TextView
    private lateinit var loginButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        registerNow = findViewById(R.id.textViewLogin_redirectRegister)
        loginButton = findViewById(R.id.buttonLogin_login)

        auth = FirebaseAuth.getInstance()

        if(auth.currentUser != null){
            val i: Intent = Intent(this,MainActivity::class.java)
            startActivity(i)
            finish()
        }


        registerNow.setOnClickListener({
            val intent: Intent = Intent(this,RegisterActivity::class.java)
            startActivity(intent)
            finish()
        })

        loginButton.setOnClickListener({
            performLogIn()
        })
    }

    private fun performLogIn() {
            val user: EditText = findViewById(R.id.editTextLogin_username)
            val pass: EditText = findViewById(R.id.editTextLogin_password)

            if(user.text.isEmpty() || pass.text.isEmpty())
            {
                Toast.makeText(this,"Morate uneti oba polja",Toast.LENGTH_SHORT).show()
                return
            }
            val userInput = user.text.toString()
            val passInput = pass.text.toString()

        auth.signInWithEmailAndPassword(userInput, passInput)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val intent: Intent = Intent(this,MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(baseContext, "Navlidan mail ili password", Toast.LENGTH_SHORT,).show()

                }
            }
    }
}