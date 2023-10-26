package com.anatoliyvinokurov.findthecat

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlin.random.Random

const val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

class GameActivity : AppCompatActivity() {
    private lateinit var catImageView: ImageView
    private lateinit var dogImageView: ImageView

    private var interstitialAd: InterstitialAd? = null
    private var adIsLoading: Boolean = false

    private val PREFS_NAME = "MyPrefs"
    private val DOG_IMAGES_INDEX_KEY = "dog_images_index"
    private val DOG_COORDINATES_INDEX_KEY = "dog_coordinates_index"
    private val CAT_IMAGES_INDEX_KEY = "cat_images_index"
    private val adWatchLimit = 5
    private var adWatchCount = 0
    var dogImagesIndex = 0
    var dogCoordinatesIndex = 0
    var catImagesIndex = 0

    val dogImages = arrayOf(R.drawable.dog1, R.drawable.dog2, R.drawable.dog3, R.drawable.dog4, R.drawable.dog5, R.drawable.dog6, R.drawable.dog7, R.drawable.dog8, R.drawable.dog9, R.drawable.dog10, R.drawable.dog11, R.drawable.dog12, R.drawable.dog13, R.drawable.dog14, R.drawable.dog15)
    val catImages = arrayOf(R.drawable.cat1, R.drawable.cat2, R.drawable.cat3, R.drawable.cat4, R.drawable.cat5, R.drawable.cat6, R.drawable.cat7, R.drawable.cat8, R.drawable.cat9, R.drawable.cat10, R.drawable.cat11, R.drawable.cat12, R.drawable.cat13, R.drawable.cat14, R.drawable.cat15)
    private lateinit var sharedPreferences: SharedPreferences

    // Получаем экранную метрику
    val displayMetrics = Resources.getSystem().displayMetrics
    // Ширина экрана в пикселях
    val screenWidth = displayMetrics.widthPixels
    // Высота экрана в пикселях
    val screenHeight = displayMetrics.heightPixels

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        catImageView = findViewById(R.id.catImageView)
        dogImageView = findViewById(R.id.dogImageView)

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        loadSavedValues()
        initializeAds()

        //костыль исправ баг
        handleDogClick()


        dogImageView.setOnClickListener {
            handleDogClick()
        }
    }

    private fun loadSavedValues() {
        dogImagesIndex = sharedPreferences.getInt(DOG_IMAGES_INDEX_KEY, 0)
        dogCoordinatesIndex = sharedPreferences.getInt(DOG_COORDINATES_INDEX_KEY, 0)
        catImagesIndex = sharedPreferences.getInt(CAT_IMAGES_INDEX_KEY, 0)

        dogImageView.setImageResource(dogImages[dogImagesIndex])
        val newLeft = Random.nextInt(20, screenWidth - 100)
        val newTop = Random.nextInt(20, screenHeight - 100)
        val params = dogImageView.layoutParams as RelativeLayout.LayoutParams
        params.leftMargin = newLeft
        params.topMargin = newTop
        dogImageView.layoutParams = params
        catImageView.setImageResource(catImages[catImagesIndex])
    }

    private fun initializeAds() {
        MobileAds.initialize(this) {}
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder().setTestDeviceIds(listOf("ABCDEF012345")).build()
        )
    }

    private fun handleDogClick() {
        dogImageView.setImageResource(dogImages[dogImagesIndex])
        if (dogImagesIndex + 1 < dogImages.size) {
            dogImagesIndex++
        }else{
            dogImagesIndex = Random.nextInt(0, dogImages.size)
        }
        val params = dogImageView.layoutParams as RelativeLayout.LayoutParams
        val newLeft = Random.nextInt(20, screenWidth - 100)
        val newTop = Random.nextInt(20, screenHeight - 100)
        params.leftMargin = newLeft
        params.topMargin = newTop
        dogImageView.layoutParams = params

        catImageView.setImageResource(catImages[catImagesIndex])
        if (catImagesIndex + 1 < catImages.size){
            catImagesIndex++
        }else{
            catImagesIndex = Random.nextInt(0, catImages.size)
        }

        if (adWatchCount >= adWatchLimit) {
            showInterstitial()
            adWatchCount = 0
        } else {
            adWatchCount++
        }

        saveValues()
    }

    private fun saveValues() {
        sharedPreferences.edit {
            putInt(DOG_IMAGES_INDEX_KEY, dogImagesIndex)
            putInt(DOG_COORDINATES_INDEX_KEY, dogCoordinatesIndex)
            putInt(CAT_IMAGES_INDEX_KEY, catImagesIndex)
        }
    }

    private fun loadAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                    adIsLoading = false
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    adIsLoading = false
                }
            }
        )
    }

    private fun showInterstitial() {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                }

                override fun onAdShowedFullScreenContent() {
                    // Called when ad is dismissed.
                }
            }
            interstitialAd?.show(this)
        } else {
            startGame()
        }
    }

    private fun startGame() {
        if (!adIsLoading && interstitialAd == null) {
            adIsLoading = true
            loadAd()
        }
    }
}
