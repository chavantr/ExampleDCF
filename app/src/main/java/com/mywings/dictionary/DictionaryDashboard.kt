package com.mywings.dictionary

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.mywings.dictionary.helper.Constants
import com.mywings.dictionary.helper.MyDatabase
import kotlinx.android.synthetic.main.activity_dictionary_dashboard.*

class DictionaryDashboard : AppCompatActivity() {

    private lateinit var myDatabase: MyDatabase

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {

                loadFragment(AllWordFragment())

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {

                loadFragment(RecentWordFragment())

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {

                loadFragment(DailyWordFragment())

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {

                loadFragment(SearchFragment())

                return@OnNavigationItemSelectedListener true

            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dictionary_dashboard)

        myDatabase = MyDatabase(this, Constants.NAME, null, Constants.VERSION)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        loadFragment(AllWordFragment())

    }


    private fun loadFragment(fragment: Fragment): Boolean {
        if (null != fragment) {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
            return true
        }
        return false
    }
}
