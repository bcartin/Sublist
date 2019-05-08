package com.calvincottrell.app.sublist

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import com.calvincottrell.app.sublist.Models.Contractor
import com.calvincottrell.app.sublist.Models.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_review.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class ReviewActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    val db = FirebaseFirestore.getInstance()
    var workmanshipStars = 0
    var scheduleStars = 0
    var budgetStars = 0
    var communicationStars = 0
    var totalStars = 0
    var topLevelContractor = Contractor()
    var services = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        setSupportActionBar(findViewById(R.id.review_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        serviceButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.aim_small, 0, 0, 0)
        val contractor = intent.extras.getSerializable("contractor") as? Contractor
        topLevelContractor = contractor!!
        review_toolbar.title = contractor.name

        if (!contractor.features.isNullOrEmpty()) {
            services = contractor.features
            var spinnerAdapter = ArrayAdapter(this, R.layout.spinner_layout, services)
            spinnerAdapter.setDropDownViewResource(R.layout.spinner_layout)

            with(serviceSpinner) {
                adapter = spinnerAdapter
                setSelection(0, false)
                onItemSelectedListener = this@ReviewActivity
                prompt = "Select your service"
                gravity = Gravity.CENTER
            }

            serviceButton.text = services[serviceSpinner.selectedItemPosition]

            serviceButton.setOnClickListener {
                serviceSpinner.performClick()
            }
        }
        serviceSpinner.onItemSelectedListener = this

        star11.setOnClickListener {
            workmanshipStars = 1
            setStars(1,1)
            calculateTotal()
        }
        star12.setOnClickListener {
            workmanshipStars = 2
            setStars(1,2)
            calculateTotal()
        }
        star13.setOnClickListener {
            workmanshipStars = 3
            setStars(1,3)
            calculateTotal()
        }
        star14.setOnClickListener {
            workmanshipStars = 4
            setStars(1,4)
            calculateTotal()
        }
        star15.setOnClickListener {
            workmanshipStars = 5
            setStars(1,5)
            calculateTotal()
        }

        star21.setOnClickListener {
            scheduleStars = 1
            setStars(2,1)
            calculateTotal()
        }
        star22.setOnClickListener {
            scheduleStars = 2
            setStars(2,2)
            calculateTotal()
        }
        star23.setOnClickListener {
            scheduleStars = 3
            setStars(2,3)
            calculateTotal()
        }
        star24.setOnClickListener {
            scheduleStars = 4
            setStars(2,4)
            calculateTotal()
        }
        star25.setOnClickListener {
            scheduleStars = 5
            setStars(2,5)
            calculateTotal()
        }

        star31.setOnClickListener {
            budgetStars = 1
            setStars(3,1)
            calculateTotal()
        }
        star32.setOnClickListener {
            budgetStars = 2
            setStars(3,2)
            calculateTotal()
        }
        star33.setOnClickListener {
            budgetStars = 3
            setStars(3,3)
            calculateTotal()
        }
        star34.setOnClickListener {
            budgetStars = 4
            setStars(3,4)
            calculateTotal()
        }
        star35.setOnClickListener {
            budgetStars = 5
            setStars(3,5)
            calculateTotal()
        }

        star41.setOnClickListener {
            communicationStars = 1
            setStars(4,1)
            calculateTotal()
        }
        star42.setOnClickListener {
            communicationStars = 2
            setStars(4,2)
            calculateTotal()
        }
        star43.setOnClickListener {
            communicationStars = 3
            setStars(4,3)
            calculateTotal()
        }
        star44.setOnClickListener {
            communicationStars = 4
            setStars(4,4)
            calculateTotal()
        }
        star45.setOnClickListener {
            communicationStars = 5
            setStars(4,5)
            calculateTotal()
        }

        addReviewButton.setOnClickListener {
            saveReview()
        }

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        println("Nothing")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        serviceButton.text = services[position]
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

    private fun setStars(category: Int, stars: Int) {
        val emptyStarImageDrawable = this.getDrawable(R.drawable.empty_star)
        for (i in 1..5) {
            val id = this.resources.getIdentifier("star$category$i", "id", this.packageName)
            val imageView = findViewById<ImageView>(id)
            imageView.setImageDrawable(emptyStarImageDrawable)
        }
        val starImageDrawable = this.getDrawable(R.drawable.star)
        for (i in 1..stars) {
            val id = this.resources.getIdentifier("star$category$i", "id", this.packageName)
            val imageView = findViewById<ImageView>(id)
            imageView.setImageDrawable(starImageDrawable)
        }
    }

    private fun calculateTotal() {
        var calculatedTotalStars: Double = 0.0
        calculatedTotalStars = ((workmanshipStars.toDouble() + scheduleStars.toDouble() + budgetStars.toDouble() + communicationStars.toDouble()) / 20.0) * 5
        totalStars = calculatedTotalStars.roundToInt()
        setStars(0, totalStars)
    }

    private fun calculateOverall(): Int {
        var starSum: Double = 0.0
        var count = topLevelContractor.reviews.count().toDouble()
        topLevelContractor.reviews.forEach {
            starSum += it.stars.toDouble()
        }
        val overallStars = (starSum / (5 * count)) * 5
        return overallStars.roundToInt()
    }

    private fun saveReview() {
        val name = FirebaseAuth.getInstance().currentUser?.displayName ?: "Anonymous"
        val formatter = DateTimeFormatter.ISO_DATE
        val currentDate = formatter.format(LocalDate.now())
        val review = Review(name, totalStars, workmanshipStars, budgetStars, scheduleStars, communicationStars, currentDate, serviceButton.text.toString(), addressEditText.text.toString(), detailsEditText.text.toString())
        val contractorRef = db.collection("subcontractors").document(topLevelContractor.id)
        contractorRef.update("reviews", FieldValue.arrayUnion(review)).addOnSuccessListener {
            topLevelContractor.reviews.add(review)
            Toast.makeText(this, "Review Added", Toast.LENGTH_LONG).show()
            val overallStars = calculateOverall()
            topLevelContractor.stars = overallStars
            contractorRef.update("stars", overallStars)
        }

    }
}
