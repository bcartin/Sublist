package com.calvincottrell.app.sublist

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import com.calvincottrell.app.sublist.Models.Contractor
import com.calvincottrell.app.sublist.Models.Request
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_request.*

class RequestActivity : AppCompatActivity() {

    var topLevelContractor = Contractor()
    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request)

        setSupportActionBar(findViewById(R.id.request_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val contractor = intent.extras.getSerializable("contractor") as? Contractor
        loadContractorInfo(contractor)
        topLevelContractor = contractor!!

        requestButton.setOnClickListener {
            submitRequest()
        }
    }

    private fun loadContractorInfo(contractor: Contractor?) {
        request_toolbar.title = contractor?.name
        nameTextView.text = contractor?.name
        locationTextView.text = "${contractor?.location} (50 Mile Radius)"
        phoneTextView.text = contractor?.number ?: "N/A"
        emailTextView.text = contractor?.email ?: "N/A"
        targetTextView.text = contractor?.features?.joinToString() ?: "N/A"

        requestTextView.text = "Request A Quote From ${contractor?.name}"

    }

    override fun onBackPressed() {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("contractor", topLevelContractor)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("contractor", topLevelContractor)
            setResult(Activity.RESULT_OK, intent)
            finish()
            return true
        }
        else {
            return super.onOptionsItemSelected(item)
        }
    }

    private fun submitRequest() {
        val name = FirebaseAuth.getInstance().currentUser?.displayName ?: "Anonymous"
        val email = FirebaseAuth.getInstance().currentUser?.email ?: "N/A"
        val request = Request(addressEditText.text.toString(), topLevelContractor.email, topLevelContractor.name, topLevelContractor.number, email, name, "Phone Number", detailsEditText.text.toString())
        val requestRef = db.collection("requests")
        requestRef.add(request)
            .addOnSuccessListener {
            Toast.makeText(this, "Request Submitted", Toast.LENGTH_LONG).show()
        }
    }
}
