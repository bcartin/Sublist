package com.calvincottrell.app.sublist

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.widget.Toast
import com.calvincottrell.app.sublist.Models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        returnButton.setOnClickListener {
            finish()
        }

        registerButton.setOnClickListener {
            if (formIsValid()) {
                registerUser()
            }
            else {
                Toast.makeText(this, "Required Information Missing", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun formIsValid(): Boolean {
        var isValid = false
        if (nameText.text.isNotBlank() || emailText.text.isNotBlank() || passwordText.text.isNotBlank()) {
            isValid = true
        }
        return isValid
    }

    private fun registerUser() {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailText.text.toString(), passwordText.text.toString())
            .addOnSuccessListener {
                createNewUser()
            }
            .addOnFailureListener {
                showAlert("Error", it.localizedMessage)
            }
    }

    private fun showAlert(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setNeutralButton("OK") {_,_ ->
//            finish()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun createNewUser() {
        val fbUser = FirebaseAuth.getInstance().currentUser
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(nameText.text.toString())
            .build()
        fbUser?.updateProfile(profileUpdates)
        val user = User(emailText.text.toString(), nameText.text.toString(), phoneEditText.text.toString(), companyEditText.text.toString(), false)
        val userRef = db.collection("users").document(emailText.text.toString())
        userRef.set(user)
            .addOnSuccessListener {
                showAlert("Registration Complete", "Your request to join ${companyEditText.text.toString()}'s Sublist has been submitted. You will be notified when you are granted access.")
            }
    }
}
