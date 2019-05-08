package com.calvincottrell.app.sublist

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.calvincottrell.app.sublist.Models.Feedback
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_feedback.*

class FeedbackActivity : AppCompatActivity() {

    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        setSupportActionBar(findViewById(R.id.feedback_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        submitFeedbackButton.setOnClickListener {
            val fb = Feedback(nameEditText.text.toString(), detailsEditText.text.toString())
            db.collection("feedback").add(fb)
                .addOnSuccessListener {
                    Toast.makeText(this, "Feedback Submitted", Toast.LENGTH_LONG).show()
                }
        }
    }


}
