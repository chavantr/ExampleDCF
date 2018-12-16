package com.mywings.dictionary


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.NativeAd
import com.facebook.ads.NativeAdListener
import com.mywings.dictionary.helper.Constants
import com.mywings.dictionary.helper.MyDatabase
import com.mywings.dictionary.uibinder.WordAdapter

import kotlinx.android.synthetic.main.fragment_recent_word.view.*


class RecentWordFragment : Fragment() {

    private lateinit var myDatabase: MyDatabase

    private lateinit var wordAdapter: WordAdapter

    private lateinit var lstWords: ArrayList<Any>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myDatabase = MyDatabase(context, Constants.NAME, null, Constants.VERSION)
        lstWords = myDatabase.recentWords
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view = inflater.inflate(R.layout.fragment_recent_word, container, false)
        view.lstRecent.layoutManager = LinearLayoutManager(context)

        wordAdapter = WordAdapter(lstWords)

        view.lstRecent.adapter = wordAdapter

        val nativeAd = NativeAd(context, "328019941145714_328030974477944")

        nativeAd.setAdListener(adListener)

        nativeAd.loadAd()

        return view
    }


    private val adListener = object : NativeAdListener {

        override fun onAdClicked(p0: Ad?) {

        }

        override fun onMediaDownloaded(p0: Ad?) {

        }

        override fun onError(p0: Ad?, p1: AdError?) {

        }

        override fun onAdLoaded(ad: Ad?) {

            if (null != lstWords && !lstWords!!.isEmpty()) {
                for (i in lstWords!!.indices) {
                    if (i % 5 == 0 && i != 0) {
                        lstWords!!.add(i, ad!!)
                    }
                }
                wordAdapter.notifyDataSetChanged()
            }
        }

        override fun onLoggingImpression(p0: Ad?) {

        }

    }


}
