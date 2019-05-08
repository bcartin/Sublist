package com.calvincottrell.app.sublist

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.widget.ImageView
import com.calvincottrell.app.sublist.Models.Contractor
import com.calvincottrell.app.sublist.Models.Review
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.review_row_item.view.*

class ProfileActivity : AppCompatActivity() {

    val reviewsAdapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setSupportActionBar(findViewById(R.id.profile_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val contractor = intent.extras.getSerializable("contractor") as? Contractor
        loadContractorInfo(contractor)
        loadReviews(contractor)

        reviewsRecyclerView.adapter = reviewsAdapter
        reviewsRecyclerView.layoutManager = LinearLayoutManager(this)
        reviewsRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        addReviewButton.setOnClickListener {
            val intent = Intent(this, ReviewActivity::class.java)
            intent.putExtra("contractor", contractor)
            startActivityForResult(intent, 1)
//            startActivity(intent)
        }

        requestQuoteButton.setOnClickListener {
            val intent = Intent(this, RequestActivity::class.java)
            intent.putExtra("contractor", contractor)
            startActivityForResult(intent, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                val contractor = data?.extras?.getSerializable("contractor") as? Contractor
                loadContractorInfo(contractor)
                loadReviews(contractor)
            }
        }
    }

    private fun loadContractorInfo(contractor: Contractor?) {
        profile_toolbar.title = contractor?.name
        nameTextView.text = contractor?.name
        locationTextView.text = "${contractor?.location} (50 Mile Radius)"
        phoneTextView.text = contractor?.number ?: "N/A"
        emailTextView.text = contractor?.email ?: "N/A"
        targetTextView.text = contractor?.features?.joinToString() ?: "N/A"

    }

    private fun loadReviews(contractor: Contractor?) {
        reviewsAdapter.clear()
        contractor?.reviews?.forEach {
            reviewsAdapter.add(ReviewListItem(it, this))
        }
    }


    class ReviewListItem(val review: Review, val context: Context): Item<ViewHolder>() {

        override fun getLayout(): Int {
            return R.layout.review_row_item
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.reviewrTextView.text = review.reviewer
            viewHolder.itemView.featureTextView.text = review.feature
            viewHolder.itemView.dateTextView.text = review.date
            viewHolder.itemView.commentTextView.text = review.details
            setStars(1, review.stars, viewHolder)
            setStars(2, review.workmanship, viewHolder)
            setStars(3, review.onBudget, viewHolder)
            setStars(4, review.onSchedule, viewHolder)
            setStars(5, review.communication, viewHolder)
        }

        fun setStars(category: Int, stars: Int, viewHolder: ViewHolder) {
            val emptyStarImageDrawable = context.getDrawable(R.drawable.empty_star)
            for (i in 1..5) {
                val id = context.resources.getIdentifier("star$category$i", "id", context.packageName)
                val imageView = viewHolder.itemView.findViewById<ImageView>(id)
                imageView.setImageDrawable(emptyStarImageDrawable)
            }
            val starImageDrawable = context.getDrawable(R.drawable.star)
            for (i in 1..stars) {
                val id = context.resources.getIdentifier("star$category$i", "id", context.packageName)
                val imageView = viewHolder.itemView.findViewById<ImageView>(id)
                imageView.setImageDrawable(starImageDrawable)
            }
        }

    }
}
