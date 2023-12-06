package com.wolf8017.twochat.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.wolf8017.twochat.R
import com.wolf8017.twochat.databinding.ActivityMainBinding
import com.wolf8017.twochat.menu.CallsFragment
import com.wolf8017.twochat.menu.ChatsFragment
import com.wolf8017.twochat.menu.StatusFragment
import com.wolf8017.twochat.view.activities.contact.ContactsActivity
import com.wolf8017.twochat.view.activities.settings.SettingsActivity


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: SectionsPagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setupWithViewPager(binding.viewPager)
        setupTabLayout(binding.tabLayout, binding.viewPager)
        setSupportActionBar(binding.toolbar)

        //Change Icon on left bottom
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                // Implementation for onPageScrolled
            }

            override fun onPageSelected(position: Int) {
                changeTabIcon(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
                // Implementation for onPageScrollStateChanged
            }
        })
    }

    private fun setupWithViewPager(viewPager2: ViewPager2) {
        this.adapter = SectionsPagerAdapter(this)
        this.adapter.addFragment(ChatsFragment(), "Chats")
        this.adapter.addFragment(StatusFragment(), "Status")
        this.adapter.addFragment(CallsFragment(), "Calls")
        viewPager2.adapter = this.adapter
    }

    private fun setupTabLayout(tabLayout: TabLayout, viewPager2: ViewPager2) {
        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            tab.text = this.adapter.getTabTitle(position)
        }.attach()
    }

    class SectionsPagerAdapter(activity: FragmentActivity?) : FragmentStateAdapter(activity!!) {
        private val mFragmentList: MutableList<Fragment> = ArrayList()
        private val mFragmentTitleList: MutableList<String> = ArrayList()

        constructor(activity: FragmentActivity, dummy: Int) : this(activity)

        fun getTabTitle(position: Int): String {
            return mFragmentTitleList[position]
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getItemCount(): Int {
            return mFragmentList.size
        }

        override fun createFragment(position: Int): Fragment {
            return mFragmentList[position]
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        when (id) {
            R.id.menu_search -> Toast.makeText(this@MainActivity, "Action Search", Toast.LENGTH_LONG).show()
            R.id.action_new_group -> Toast.makeText(this@MainActivity, "Action New Group", Toast.LENGTH_LONG).show()
            R.id.action_broadcast -> Toast.makeText(this@MainActivity, "Action Broadcast", Toast.LENGTH_LONG).show()
            R.id.action_starred_message -> Toast.makeText(
                this@MainActivity,
                "Action Starred Message",
                Toast.LENGTH_LONG
            ).show()

            R.id.action_settings -> startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun changeTabIcon(index: Int) {
        binding.fabAction.hide()
        Handler(Looper.getMainLooper()).postDelayed({
            when (index) {
                0 -> {
                    binding.fabAction.setImageDrawable(getDrawable(R.drawable.baseline_chat_24))
                    binding.fabAction.setOnClickListener {
                        startActivity(Intent(this@MainActivity, ContactsActivity::class.java))
                    }
                }

                1 -> binding.fabAction.setImageDrawable(getDrawable(R.drawable.baseline_camera_alt_24))
                2 -> binding.fabAction.setImageDrawable(getDrawable(R.drawable.baseline_call_24))
            }
            binding.fabAction.show()
        }, 444) // Replace delayTimeInMillis with the desired delay in milliseconds
    }

}
