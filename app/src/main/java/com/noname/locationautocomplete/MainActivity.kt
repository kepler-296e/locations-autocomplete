package com.noname.locationautocomplete

import android.os.Bundle
import android.view.Menu
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.InputStreamReader

data class Location(val name: String, val code: String)

class MainActivity : AppCompatActivity() {

    companion object {
        const val STATES = 0
        const val CITIES = 1
    }

    private lateinit var locationsAdapter: ArrayAdapter<String>
    private lateinit var states: Array<Location>
    private lateinit var cities: Array<Location>
    private var showingCities = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // get states and cities from JSON
        states = scrapJson(R.raw.states_us, Array<Location>::class.java)
        cities = scrapJson(R.raw.cities_us, Array<Location>::class.java)
        // fill list view with states
        showStates()
    }

    private fun <T> scrapJson(jsonId: Int, dataClass: Class<T>): T {
        val inputStream = resources.openRawResource(jsonId)
        return Gson().fromJson(BufferedReader(InputStreamReader(inputStream)), dataClass)
    }

    private fun setLocationsAdapter(locationNames: ArrayList<String>, adapterType: Int) {
        locationsAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, locationNames)
        // setting location list view
        lview_locations.adapter = locationsAdapter
        lview_locations.setOnItemClickListener { _, _, position, _ ->
            val selectedLocationName = lview_locations.getItemAtPosition(position) as String
            when (adapterType) {
                STATES -> showSelectedStateCities(selectedLocationName)
                CITIES -> showToast("You select \'$selectedLocationName\'")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showSelectedStateCities(selectedLocationName: String) {
        // set toolbar title and display back arrow
        title = selectedLocationName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // fill locations recycler view with selected state localities
        val selectedState = states.filter { it.name == selectedLocationName }
        val selectedStateCities = cities.filter { it.code == selectedState[0].code }
        // create and fill selected state city names
        val selectedStateCityNames = arrayListOf<String>()
        for (city in selectedStateCities) selectedStateCityNames.add(city.name)
        // fill locations list view with selected state cities
        setLocationsAdapter(selectedStateCityNames, CITIES)
        // if cities are showing when pressed back, list view back to states
        showingCities = true
    }

    private fun showStates() {
        // set default toolbar title and hide back arrow
        title = "Select US state"
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        // fill list view with state names
        val stateNames = arrayListOf<String>()
        for (state in states) stateNames.add(state.name)
        setLocationsAdapter(stateNames, STATES)
        // if cities are showing when pressed back, list view back to states
        showingCities = false
    }

    override fun onBackPressed() {
        if (showingCities) showStates()
        else super.onBackPressed()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchView = menu?.findItem(R.id.item_search)?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                if (::locationsAdapter.isInitialized) locationsAdapter.filter.filter(newText?.trim())
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }
}