package com.mywings.dictionary


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mywings.dictionary.helper.Constants
import com.mywings.dictionary.helper.MyDatabase
import com.mywings.dictionary.model.Word
import com.mywings.dictionary.uibinder.WordAdapter
import kotlinx.android.synthetic.main.fragment_search.view.*


class SearchFragment : Fragment() {

    private lateinit var myDatabase: MyDatabase

    private lateinit var wordAdapter: WordAdapter

    private lateinit var lstWords: ArrayList<Any>

    private lateinit var lstWord: ArrayList<Any>

    private lateinit var lstViewWord: RecyclerView

    private var strPrevious: String? = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        myDatabase = MyDatabase(context, Constants.NAME, null, Constants.VERSION)
        lstWords = myDatabase.words
        lstWord = ArrayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view = inflater.inflate(R.layout.fragment_search, container, false)

        lstViewWord = view.lstSearch

        lstViewWord.layoutManager = LinearLayoutManager(context)

        wordAdapter = WordAdapter(lstWords)

        lstViewWord.adapter = wordAdapter

        view.txtSearch.addTextChangedListener(textWatcher);

        return view
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            if (editable.toString() != strPrevious) {
                if (editable.toString().isNotEmpty()) {
                    lstWord.clear()
                    for (word in lstWords) {
                        if ((word as Word).word.toLowerCase().startsWith(editable.toString().toLowerCase())) {
                            lstWord.add(word)
                        }
                    }
                    wordAdapter = WordAdapter(lstWord)
                    lstViewWord.adapter = wordAdapter
                } else {
                    wordAdapter = WordAdapter(lstWords)
                    lstViewWord.adapter = wordAdapter
                }
                strPrevious = editable.toString()
            }
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

    }


}
