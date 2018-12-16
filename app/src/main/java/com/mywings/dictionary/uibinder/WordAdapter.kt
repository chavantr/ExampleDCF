package com.mywings.dictionary.uibinder

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.facebook.ads.Ad
import com.facebook.ads.AdChoicesView
import com.facebook.ads.NativeAd
import com.mywings.dictionary.R
import com.mywings.dictionary.WordDetailActivity
import com.mywings.dictionary.model.Word
import kotlinx.android.synthetic.main.item_native_ad.view.*
import kotlinx.android.synthetic.main.layout_word_row.view.*

class WordAdapter(words: ArrayList<Any>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var lstWords: ArrayList<Any> = words

    override fun onCreateViewHolder(parent: ViewGroup, itemType: Int): RecyclerView.ViewHolder {
        return when (itemType) {
            WORD -> WordViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_word_row, parent, false))
            NATIVE -> NativeAdViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_native_ad,
                    parent,
                    false
                )
            )
            else -> WordViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_word_row, parent, false))
        }
    }

    override fun getItemCount(): Int = lstWords.size

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val itemType = getItemViewType(position)
        if (itemType == WORD) {
            val wordViewHolder = viewHolder as WordViewHolder
            val word = lstWords[position] as Word
            wordViewHolder.lblWord.text = word.word
            wordViewHolder.cvContainer.setOnClickListener {
                val intent = Intent(it.context, WordDetailActivity::class.java)
                intent.putExtra("id", word.id)
                intent.putExtra("word", word.word)
                it.context.startActivity(intent)
            }
        } else if (itemType == NATIVE) {
            val nativeAdViewHolder = viewHolder as NativeAdViewHolder
            var nativeAd = lstWords.get(position) as NativeAd
            nativeAdViewHolder.tvAdTitle.text = nativeAd.advertiserName
            nativeAdViewHolder.tvAdBody.text = nativeAd.adBodyText
            nativeAdViewHolder.btnCTA.text = nativeAd.adCallToAction
            nativeAdViewHolder.sponsorLabel.text = nativeAd.sponsoredTranslation
            nativeAdViewHolder.adChoicesContainer.removeAllViews()
            val adChoicesView = AdChoicesView(nativeAdViewHolder.containder.context, nativeAd, true)
            nativeAdViewHolder.adChoicesContainer.addView(adChoicesView)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = lstWords[position]
        return when (item) {
            is Word -> WORD
            is Ad -> NATIVE
            else -> -1
        }
    }

    companion object {
        const val WORD = 0
        const val NATIVE = 1
    }

    inner class WordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val lblWord = view.lblWord
        val cvContainer = view.cvContainer
    }


    inner class NativeAdViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val containder = view
        val adIconView = view.adIconView
        val tvAdTitle = view.tvAdTitle
        val tvAdBody = view.tvAdBody
        val btnCTA = view.btnCTA
        val adChoicesContainer = view.adChoicesContainer
        val mediaView = view.mediaView
        val sponsorLabel = view.sponsored_label
    }
}