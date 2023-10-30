package com.wolf8017.twochat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.wolf8017.twochat.databinding.ActivityMainBinding
import com.wolf8017.twochat.menu.CallsFragment
import com.wolf8017.twochat.menu.ChatsFragment
import com.wolf8017.twochat.menu.StatusFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: SectionsPagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setupWithViewPager(binding.viewPager)
        setupTabLayout(binding.tabLayout, binding.viewPager)

    }

    private fun setupWithViewPager(viewPager2: ViewPager2) {
        this.adapter = SectionsPagerAdapter(this)
        this.adapter.addFragment(ChatsFragment(), "Chats")
        this.adapter.addFragment(StatusFragment(), "Status")
        this.adapter.addFragment(CallsFragment(), "Calls")
        viewPager2.adapter  = this.adapter
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
}
