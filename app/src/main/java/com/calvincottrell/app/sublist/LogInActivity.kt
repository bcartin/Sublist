package com.calvincottrell.app.sublist

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.calvincottrell.app.sublist.Models.Location
import com.calvincottrell.app.sublist.Models.LocationsList
import com.calvincottrell.app.sublist.Models.Subcontractors
import com.calvincottrell.app.sublist.Models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_log_in.*
import kotlin.system.exitProcess

var locationsList = LocationsList()
var sublist = ArrayList<Subcontractors>()
var loggedUser = User()

class LogInActivity : AppCompatActivity() {

    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

//        FirebaseAuth.getInstance().signOut()
        checkIfUserIsLoggedIn()

        loginButton.setOnClickListener {
            val email = emailText.text.toString()
            val password = passwordText.text.toString()
            login(email, password)
        }

        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        forgotPasswordButton.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            val emailAddress = "user@example.com"

            auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Email to reset your password sent", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun login(email: String, password: String) {
        if (email == "" || password == "") {
            Toast.makeText(this, "Email or Password cannot be blank.", Toast.LENGTH_LONG).show()
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                loadUser {
                    if (loggedUser.allowedInSublist) {
                        startMainActivity()
                    }
                    else {
                        Toast.makeText(this, "You don't have access to the sublist yet", Toast.LENGTH_LONG).show()
                    }
                }

            }
            .addOnFailureListener {
                var errorMessage: String = ""
                if (it is FirebaseAuthInvalidUserException) {
                    errorMessage = "Invalid User"
                }
                else if (it is FirebaseAuthInvalidCredentialsException) {
                    errorMessage = "Incorrect Password"
                }
                else {
                    errorMessage = it.localizedMessage
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
    }

    private fun checkIfUserIsLoggedIn() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            loadUser {
                if (loggedUser.allowedInSublist) {
                    startMainActivity()
                }
                else {
                    Toast.makeText(this, "You don't have access to the sublist yet", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun loadUser(callback: () -> Unit) {
        db.collection("users").document(FirebaseAuth.getInstance().currentUser?.email.toString()).get()
            .addOnSuccessListener {
                loggedUser = it.toObject(User::class.java)!!
                callback()
            }

    }



}
