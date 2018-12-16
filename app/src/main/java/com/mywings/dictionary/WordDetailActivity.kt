package com.mywings.dictionary

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.mywings.dictionary.helper.Constants
import com.mywings.dictionary.helper.MyDatabase
import kotlinx.android.synthetic.main.activity_word_detail.*
import kotlinx.android.synthetic.main.content_word_detail.*

class WordDetailActivity : AppCompatActivity() {


    private lateinit var myDatabase: MyDatabase

    private var adView: AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_detail)
        setSupportActionBar(toolbar)

        myDatabase = MyDatabase(this, Constants.NAME, null, Constants.VERSION)


        adView = AdView(this, "328019941145714_328033167811058", AdSize.BANNER_HEIGHT_50)

        banner_container.addView(adView)

        adView!!.loadAd()

        var wordDetail = myDatabase.getWordDetail(intent.extras.getInt("id"))

        var recent = myDatabase.saveRecent(intent.extras.getInt("id"))

        title = intent.extras.getString("word")

        var strType = ""


        when (wordDetail.wordType) {
            "n" -> strType = "Noun"
            "a" -> strType = "Adjective"
            "adv" -> strType = "Adverb"
            "vt" -> strType = "Transitive Verb"
            "" -> strType = "Abbreviation"
        }

        lblWordDetails.text = "Meaning :  ${wordDetail.meaning} \n\n\n\nType : $strType"

        fab.setOnClickListener {

            val favorite = myDatabase.saveFavorite(intent.extras.getInt("id"))

            when (favorite) {

                100 -> Toast.makeText(this, "Already favorite.", Toast.LENGTH_LONG).show()

                1 -> Toast.makeText(this, "Added in favorite list.", Toast.LENGTH_LONG).show()

                0 -> Toast.makeText(this, "Error occurred.", Toast.LENGTH_LONG).show()
            }

        }

    }
}
