package com.example.androidudemy.countryanthem

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.appbar.MaterialToolbar
import java.io.File
import java.io.FileOutputStream


lateinit var searchName: AutoCompleteTextView
lateinit var searchB: Button
lateinit var imageButton: ImageButton
lateinit var countryName: TextView
lateinit var anthemName: TextView
lateinit var anthemDescription: TextView
lateinit var mediaPlayer: MediaPlayer
lateinit var toolbar: MaterialToolbar

class MainActivity : AppCompatActivity() {
    private var randIndex:Int=-1
    private val REQUEST_STORAGE_PERMISSION = 1
    private var currentMediaPlayer: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!hasStoragePermission()) {
            requestStoragePermission()
        }
        toolbar = findViewById(R.id.materialToolbar)
        toolbar.overflowIcon =
            AppCompatResources.getDrawable(this, R.drawable.baseline_more_vert_24)

        searchName = findViewById(R.id.searchText)
        searchB = findViewById(R.id.searchB)
        imageButton = findViewById(R.id.imageButton)
        countryName = findViewById(R.id.countryName)
        anthemName = findViewById(R.id.anthemName)
        anthemDescription = findViewById(R.id.anthemD)

        val countryList = resources.getStringArray(R.array.countries)
        val anthemList = resources.getStringArray(R.array.anthems)
        val details = resources.getStringArray(R.array.description)
        val countryImageMap = mapOf(
            "india" to R.drawable.india,
            "usa" to R.drawable.usa,
            "france" to R.drawable.france,
            "united kingdom" to R.drawable.uk,
            "canada" to R.drawable.canada,
            "japan" to R.drawable.japan,
            "brazil" to R.drawable.brazil,
            "south africa" to R.drawable.southafrica,
            "russia" to R.drawable.russia,
            "australia" to R.drawable.australia
        )
        val anthemMap = mapOf(
            "india" to R.raw.indian,
            "usa" to R.raw.usa,
            "france" to R.raw.france,
            "united kingdom" to R.raw.uk,
            "canada" to R.raw.canada,
            "japan" to R.raw.japan,
            "brazil" to R.raw.brazil,
            "south africa" to R.raw.southafrica,
            "russia" to R.raw.russia,
            "australia" to R.raw.australia
        )
        val namesAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, countryList)
        searchName.setAdapter(namesAdapter)

        var matchFound = false
        var searchWord:String?=null
        searchB.setOnClickListener {
            val keyboardController =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            keyboardController.hideSoftInputFromWindow(searchB.windowToken, 0)
            searchWord = searchName.text.toString().trim()
            matchFound=false
            if (searchWord!!.isNotBlank()) {
                for (i in countryList.indices) {
                    if (countryList[i].equals(searchWord, ignoreCase = true)) {
                        countryName.text = countryList[i]
                        anthemName.text = anthemList[i]
                        anthemDescription.text = details[i]

                        var imageRes = countryImageMap[searchWord!!.lowercase()]
                        if (imageRes != null) {
                            imageButton.setImageResource(imageRes)
                        }
                        matchFound = true
                        break
                    }
                }
                if (matchFound) {
                    if(currentMediaPlayer != null) {
                        currentMediaPlayer?.stop()
                        currentMediaPlayer?.release()
                    }
                    mediaActions(anthemMap, searchWord!!)
                }
                if (!matchFound) {
                    countryName.text = "No Matches Found"
                    anthemName.text = "No Matches Found"
                    anthemDescription.text = "Try to check the spelling..."
                    imageButton.setImageResource(R.drawable.ic_launcher_foreground)
                }
            } else {
                countryName.text = "NA"
                anthemName.text = "NA"
                anthemDescription.text = "!!Please Enter a country name or anthem name!!"
            }
        }


        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.location ->{
                    if(matchFound)
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/place/$searchWord")))
                    else
                        Toast.makeText(applicationContext,"NO MATCHES FOUND..!",Toast.LENGTH_LONG).show()
                }
                R.id.like -> feedbackDialogBox()
                R.id.share -> {
                    if(matchFound){
                        val screenshot = captureScreenShot(window.decorView.rootView)
                        val screenshotFile = saveScreenShotToFile(screenshot)
                        shareScreenShot(screenshotFile)
                    }
                    else
                        Toast.makeText(applicationContext,"Search For a Country First",Toast.LENGTH_LONG).show()
                }

                R.id.supriseMe -> {
                    randIndex = (0 until countryList.size).random()
                    searchWord = countryList[randIndex]
                    countryName.text = countryList[randIndex]
                    anthemName.text = anthemList[randIndex]
                    anthemDescription.text = details[randIndex]
                    searchName.setText(searchWord)

                    var imageRes = countryImageMap[searchWord!!.lowercase()]
                    if (imageRes != null) {
                        imageButton.setImageResource(imageRes)
                    }
                    matchFound = true
                    if(currentMediaPlayer != null) {
                        currentMediaPlayer?.stop()
                        currentMediaPlayer?.release()
                    }
                    mediaActions(anthemMap,searchWord!!)
                }
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun feedbackDialogBox() {
        var alertD = AlertDialog.Builder(this@MainActivity)

        alertD.setTitle("🥹")
            .setMessage("Like the app?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                if (toolbar.menu.findItem(R.id.like) != null)
                    toolbar.menu.findItem(R.id.like).isEnabled = false
                positiveResponse()
            }
            .setNegativeButton("No") { dialogInterface, i ->
                dialogInterface.cancel()
            }
        alertD.create().show()
    }

    private fun positiveResponse() {
        var alertD = AlertDialog.Builder(this@MainActivity)

        alertD.setMessage("Thanks for the feedback...!").setCancelable(false)
            .setPositiveButton("OK") { _, _ -> }
        alertD.create().show()
    }

    private fun captureScreenShot(view: View): Bitmap {
        val screenshot = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(screenshot)
        view.draw(canvas)
        return screenshot
    }

    private fun saveScreenShotToFile(bitmap: Bitmap): File {
        val screenshotsDir = getExternalFilesDir(null)
        val screenshotFile = File(screenshotsDir, "screenshot.png")

        try {
            FileOutputStream(screenshotFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return screenshotFile
    }

    private fun shareScreenShot(screenshotFile: File) {
        val uri = FileProvider.getUriForFile(this, "${packageName}.provider", screenshotFile)
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/png"
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(shareIntent, "Share Screenshot"))
    }

    private fun hasStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_STORAGE_PERMISSION
        )
    }



    private fun mediaActions(anthemMap: Map<String, Int>, searchWord: String) {

        val anthemRes = anthemMap[searchWord!!.lowercase()]
        mediaPlayer = anthemRes?.let { path -> MediaPlayer.create(this, path) }!!
        currentMediaPlayer = mediaPlayer
        imageButton.setOnClickListener {
            if (currentMediaPlayer != null && (!currentMediaPlayer!!.isPlaying)) {
                currentMediaPlayer?.start()
                Toast.makeText(applicationContext, "Anthem Started", Toast.LENGTH_SHORT).show()
            } else {
                currentMediaPlayer?.pause()
                Toast.makeText(applicationContext, "Anthem Paused", Toast.LENGTH_SHORT).show()
            }

        }

        imageButton.setOnLongClickListener {
            Toast.makeText(applicationContext, "Reset Applied", Toast.LENGTH_SHORT).show()
            currentMediaPlayer?.stop()
            currentMediaPlayer?.prepare()
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

}



