package com.example.note

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.note.MainActivity.Companion.databaseHandler
import com.example.note.MainActivity.Companion.note
import com.google.android.material.tabs.TabLayout

class EditActivity:AppCompatActivity() {

    companion object {
        lateinit var now:NoteData
    }

    lateinit var handler:Handler

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        now=note[intent.getIntExtra("pos",-1)]

        val pager:ViewPager=findViewById(R.id.view_pager)
        val tab:TabLayout=findViewById(R.id.tabs)
        val adapter=TabAdapter(supportFragmentManager)
        pager.adapter=adapter
        tab.setupWithViewPager(pager)
        now.time--
        handler=Handler(Looper.getMainLooper())
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateTextTask)
    }

    override fun onResume() {
        super.onResume()
        handler.post(updateTextTask)
    }

    val updateTextTask=object:Runnable {
        override fun run() {
            now.time++
            databaseHandler.updateNote(now,now)
            handler.postDelayed(this,60000)
        }
    }
}

class TabAdapter(supportFragmentManager:FragmentManager):
    FragmentStatePagerAdapter(supportFragmentManager) {

    private val mFragmentList=arrayListOf(EditFragment(),ViewFragment())
    private val mFragmentTitleList=arrayListOf("源码","预览")

    override fun getCount():Int {
        return 2
    }

    override fun getItem(position:Int):Fragment {
        return mFragmentList[position]
    }

    override fun getPageTitle(position:Int):CharSequence {
        return mFragmentTitleList[position]
    }
}
