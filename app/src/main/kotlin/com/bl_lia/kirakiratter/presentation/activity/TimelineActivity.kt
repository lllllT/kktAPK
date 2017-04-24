package com.bl_lia.kirakiratter.presentation.activity

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.FragmentManager
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import com.bl_lia.kirakiratter.R
import com.bl_lia.kirakiratter.presentation.adapter.timeline.TimelineFragmentPagerAdapter
import com.bl_lia.kirakiratter.presentation.adapter.timeline.TimelineSpinnerAdapter
import com.bl_lia.kirakiratter.presentation.fragment.NavigationDrawerFragment
import com.bl_lia.kirakiratter.presentation.fragment.TimelineFragment
import kotlinx.android.synthetic.main.activity_timeline.*

class TimelineActivity : AppCompatActivity() {

    companion object {
        private const val FRAGMENT_TAG_TIMELINE = "FRAGMENT_TAG_TIMELINE"
        private const val FRAGMENT_TAG_NOTIFICATION = "FRAGMENT_TAG_NOTIFICATION"
    }

    val timelines: List<String> = listOf("home", "local")

    val spinnerAdapter: TimelineSpinnerAdapter by lazy {
        TimelineSpinnerAdapter(this, android.R.layout.simple_spinner_dropdown_item, timelines)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline)

        if (savedInstanceState == null) {
            view_pager.adapter = TimelineFragmentPagerAdapter(supportFragmentManager)

            supportFragmentManager.beginTransaction().also { transaction ->
                val fragment = NavigationDrawerFragment.newInstance()
                transaction.add(R.id.left_drawer, fragment)
            }.commit()
        }
    }

    override fun onStart() {
        super.onStart()

        button_katsu.setOnClickListener {
            val intent = Intent(this, KatsuActivity::class.java)
            startActivity(intent)
        }

        button_menu.setOnClickListener {
            if (!layout_drawer.isDrawerOpen(left_drawer)) {
                layout_drawer.openDrawer(left_drawer)
            }
        }

        spinner_timeline.adapter = spinnerAdapter
        spinner_timeline.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        spinner_timeline.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_home_black_24px, null)
                        view_pager.currentItem = 0
                    }
                    1 -> {
                        spinner_timeline.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_group_black_24px, null)
                        view_pager.currentItem = 1
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                val position = spinner_timeline.selectedItemPosition
                view_pager.currentItem = position
            }
        }

        button_account.setOnClickListener {
            val intent = Intent(this, AccountActivity::class.java)
            startActivity(intent)
        }
        button_notification.setOnClickListener { view_pager.currentItem = 2 }
        button_search.setOnClickListener { showNothing() }
    }

    private val FragmentManager.timeline: TimelineFragment?
        get() = findFragmentByTag(FRAGMENT_TAG_TIMELINE) as? TimelineFragment

    private fun showNothing() {
        Snackbar.make(layout_drawer, "まだないよ！", Snackbar.LENGTH_SHORT).show()
    }
}