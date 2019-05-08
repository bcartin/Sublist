package com.calvincottrell.app.sublist

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import com.calvincottrell.app.sublist.Models.Contractor
import com.calvincottrell.app.sublist.Models.Location
import com.calvincottrell.app.sublist.Models.LocationsList
import com.calvincottrell.app.sublist.Models.Subcontractors
import com.google.firebase.firestore.FirebaseFirestore
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.list_footer.view.*
import kotlinx.android.synthetic.main.list_header.view.*
import kotlinx.android.synthetic.main.list_row_item.view.*
import kotlinx.android.synthetic.main.location_list_item.view.*
import kotlin.math.min

class MainActivity : AppCompatActivity() {

    val db = FirebaseFirestore.getInstance()
    val adapter = GroupAdapter<ViewHolder>()
    val locationsAdapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        println("666666666")

        setSupportActionBar(findViewById(R.id.my_toolbar))

        sublistRecyclerView.visibility = View.VISIBLE
        sublistRecyclerView.layoutManager = LinearLayoutManager(this)
        sublistRecyclerView.adapter = adapter
        sublistRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        val locationListLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        locationsRecyclerView.layoutManager = locationListLayoutManager
        locationsRecyclerView.adapter = locationsAdapter
        locationsRecyclerView.setBackgroundColor(Color.parseColor("#F1EDED"))


        loadingPanel.visibility = View.VISIBLE
        if (locationsList.locations.count() == 0) {
            downloadLocations {
                loadLocationsList()
            }
        }
        else {
            loadLocationsList()
        }

    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(p0: String?): Boolean {
                val filter = p0 ?: ""
                loadList(filter)
                return false
            }

            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

        })



        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when (item?.itemId) {
        R.id.settings -> {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            true
        }
        else -> {
            super .onOptionsItemSelected(item)
        }
    }

    private fun downloadContractors(location: String) {
        sublist.clear()
        var roofingContractor = Subcontractors()
        val contractorList = ArrayList<Contractor>()
        db.collection("subcontractors").whereEqualTo("location", location)
            .get()
            .addOnSuccessListener {
                for (document in it) {
                    val item = document.toObject(Contractor::class.java)
                    item.id = document.id
                    contractorList.add(item)
                }
                val groupedList = contractorList.groupBy { it.type }
                groupedList.forEach {
                    val sortedContractors = it.value.sortedWith(compareBy({-it.stars}, {it.isFavorite}, {-it.numberOfReviews}))
                    val sub = Subcontractors(it.key.capitalize(), ArrayList(sortedContractors),true)
                    if (it.key != "roofer") {
                        sublist.add(sub)
                    }
                    else {
                        roofingContractor = sub
                    }
                }
                sublist.add(0, roofingContractor)
                loadList("")
            }
            .addOnFailureListener {
                println(it.localizedMessage)

            }
    }

    private fun loadList(filter: String) {
        adapter.clear()
        sublist.forEach {
            val sortedContractors = it.contractors.sortedWith(compareBy({-it.stars}, {it.isFavorite}, {-it.numberOfReviews}))
            val filteredContractors = sortedContractors.filter { s -> s.name.contains(filter, ignoreCase = true) }
            if (filteredContractors.isNotEmpty()) {
                adapter.add(HeaderItem(it.name))
                if (it.isCollapsed) {
                    for (i in 0 until min(filteredContractors.count(), 3)) {
                        val contractor = filteredContractors[i]
                        adapter.add(ListItem(i+1, contractor, this, {
                            val intent = Intent(this, ProfileActivity::class.java)
                            intent.putExtra("contractor", contractor)
                            startActivity(intent)
                        }, {
                            val intent = Intent(this, RequestActivity::class.java)
                            intent.putExtra("contractor", contractor)
                            startActivity(intent)
                        }))
                    }
                } else {
                    filteredContractors.forEachIndexed { index, contractor ->
                        adapter.add(ListItem(index+1, contractor, this, {
                            val intent = Intent(this, ProfileActivity::class.java)
                            intent.putExtra("contractor", contractor)
                            startActivity(intent)
                        }, {
                            val intent = Intent(this, RequestActivity::class.java)
                            intent.putExtra("contractor", contractor)
                            startActivity(intent)
                        }))
                    }
                }
                if (filteredContractors.count() > 3) {
                    adapter.add(FooterItem(it, this) { expand: Boolean ->
                        it.isCollapsed = expand
                        adapter.clear()
                        loadList(filter)
                    })
                }
            }
        }
        loadingPanel.visibility = View.INVISIBLE
    }

    private fun downloadLocations(callback: () -> Unit) {
        db.collection(getString(R.string.sublist)).get()
            .addOnSuccessListener {
                it.forEach {
                    locationsList = it.toObject(LocationsList::class.java)
                    callback()
                }
            }
    }

    private fun loadLocationsList() {
        locationsList.locations.forEach {
            locationsAdapter.add(LocationItem(false, it) {name: String ->
                loadingPanel.visibility = View.VISIBLE
                adapter.clear()
                downloadContractors(name)
            })
        }
        if (sublist.count() > 0) {
            loadList("")
        }
        else {
            val location = locationsList.locations[0].name
            downloadContractors(location)
        }

    }




}

class ListItem(val index: Int, val contractor: Contractor?, val context: Context, private val onProfileClicked: (String) -> Unit, private val onRequestClicked: (String) -> Unit): Item<ViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.list_row_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.nameTextView.text = "${index}. ${contractor?.name}"
        viewHolder.itemView.reviewsTextView.setTextColor(Color.parseColor("#acc6f6"))
        viewHolder.itemView.reviewsTextView.text = "( ${contractor?.numberOfReviews} Reviews )"
        val stars = contractor?.stars ?: 0
        setStars(stars, viewHolder)

        viewHolder.itemView.viewProfileButton.setOnClickListener {
            onProfileClicked("")
        }

        viewHolder.itemView.requestQuoteButton.setOnClickListener {
            onRequestClicked("")
        }
    }

    fun setStars(stars: Int, viewHolder: ViewHolder) {
        val emptyStarImageDrawable = context.getDrawable(R.drawable.empty_star)
        for (i in 1..5) {
            val id = context.resources.getIdentifier("star$i", "id", context.packageName)
            val imageView = viewHolder.itemView.findViewById<ImageView>(id)
            imageView.setImageDrawable(emptyStarImageDrawable)
        }
        val starImageDrawable = context.getDrawable(R.drawable.star)
        for (i in 1..stars) {
            val id = context.resources.getIdentifier("star$i", "id", context.packageName)
            val imageView = viewHolder.itemView.findViewById<ImageView>(id)
            imageView.setImageDrawable(starImageDrawable)
        }
    }

}

class HeaderItem(val name: String): Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.list_header
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.headerTitleTextView.text = name

    }
}

class FooterItem(val group: Subcontractors, val context: Context, private val onExpandClicked: (Boolean) -> Unit): Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.list_footer
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.setBackgroundColor(Color.parseColor("#DFE0E1"))
        if (group.isCollapsed) {
            viewHolder.itemView.expandTextView.text = "View All ${group.name}"
            viewHolder.itemView.expandImageView.setImageDrawable(context.getDrawable(R.drawable.sortdown))
        }
        else {
            viewHolder.itemView.expandTextView.text = "Collapse ${group.name}"
            viewHolder.itemView.expandImageView.setImageDrawable(context.getDrawable(R.drawable.sortup))
        }

        viewHolder.itemView.expandTextView.setOnClickListener {
            var expand = false
            if (group.isCollapsed) {
                expand = true
            }
            onExpandClicked(!expand)
        }

    }
}

class LocationItem(val isSelected: Boolean, val location: Location, private val onLocationClicked: (String) -> Unit): Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.location_list_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.locationButton.text = location.name
        val background = viewHolder.itemView.locationButton.background as GradientDrawable
        background.color = ColorStateList.valueOf(Color.parseColor(location.color))

        viewHolder.itemView.locationButton.setOnClickListener {
            onLocationClicked(location.name)
        }


    }
}



