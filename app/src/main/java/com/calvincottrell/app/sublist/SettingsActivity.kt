package com.calvincottrell.app.sublist

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.calvincottrell.app.sublist.Models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setSupportActionBar(findViewById(R.id.my_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val auth = FirebaseAuth.getInstance()
        nameTextView.text = auth.currentUser?.displayName.toString()

        logOutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LogInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        feedbackButton.setOnClickListener {
            val intent = Intent(this, FeedbackActivity::class.java)
            startActivity(intent)
        }

        saveChangesButton.setOnClickListener {
            updateUserInfo()
        }

        resetPasswordButton.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            val emailAddress = "user@example.com"

            auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Email to reset your password sent", Toast.LENGTH_LONG).show()
                    }
                }
        }

        loadUserInfo()
    }

    private fun loadUserInfo() {
        nameEditText.setText(loggedUser.name)
        phoneEditText.setText(loggedUser.number)
        companyEditText.setText(loggedUser.company)
    }

    private fun updateUserInfo() {
        val fbUser = FirebaseAuth.getInstance().currentUser
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(nameEditText.text.toString())
            .build()
        fbUser?.updateProfile(profileUpdates)
        val email = FirebaseAuth.getInstance().currentUser?.email!!
        val userDataMap = HashMap<String, Any>()
        userDataMap["name"] = nameEditText.text.toString()
        userDataMap["company"] = companyEditText.text.toString()
        userDataMap["number"] = phoneEditText.text.toString()
        val userRef = db.collection("users").document(email)
        userRef.update(userDataMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Changes Saved", Toast.LENGTH_LONG).show()
            }
    }
}
