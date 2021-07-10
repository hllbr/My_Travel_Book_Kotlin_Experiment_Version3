package com.hllbr.mytravelbookkotlinexper3

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var placesArray = ArrayList<Place>()

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.add_place,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_place_option){
            val intent = Intent(applicationContext,MapsActivity::class.java)
            intent.putExtra("infos","new")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            val database = openOrCreateDatabase("PLACES", Context.MODE_PRIVATE,null)
            val cursor = database.rawQuery("SELECT * FROM places",null)
            val addressIndex = cursor.getColumnIndex("address")
            val latıtudeIndex = cursor.getColumnIndex("latitude")
            val longitutdeIndex = cursor.getColumnIndex("longitude")
            while (cursor.moveToNext()){
                val addressFromDatabase = cursor.getString(addressIndex)
                val latitudeFromDatabase = cursor.getDouble(latıtudeIndex)
                val longitudeFromDatabase = cursor.getDouble(longitutdeIndex)
                val myPlace = Place(addressFromDatabase,latitudeFromDatabase,longitudeFromDatabase)
                println(myPlace.address)
                println(myPlace.latitude)
                println(myPlace.longitude)
                placesArray.add(myPlace)
            }
            cursor.close()
        }catch (e: Exception){
            e.printStackTrace()
        }
        val customAdapter = CustomAdapter(placesArray,this)
        listView.adapter = customAdapter
        listView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(this@MainActivity,MapsActivity::class.java)
            intent.putExtra("infos","old")
            intent.putExtra("selectedPlace",placesArray.get(position))
            startActivity(intent)
        }

    }
}