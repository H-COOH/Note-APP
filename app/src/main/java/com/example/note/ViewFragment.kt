package com.example.note

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.note.EditActivity.Companion.now
import com.example.note.EditFragment.Companion.editText
import java.util.*

class ViewFragment:Fragment() {

    override fun onCreateView(
        inflater:LayoutInflater,container:ViewGroup?,
        savedInstanceState:Bundle?,
    ):View {
        val view=inflater.inflate(R.layout.fragment_view,container,false)!!
        val webView:WebView=view.findViewById(R.id.webview)
        webView.settings.javaScriptEnabled=true
        webView.loadUrl("file:///android_asset/show.html")
        webView.webViewClient=object:WebViewClient() {
            override fun onPageFinished(view:WebView,url:String) {
                webView.loadUrl("javascript:show('"+editText.text.toString()
                    .replace("\n","⨂")+"');")
            }
        }

        editText.addTextChangedListener(object:TextWatcher {
            override fun beforeTextChanged(p0:CharSequence?,p1:Int,p2:Int,p3:Int) {}
            override fun onTextChanged(p0:CharSequence?,p1:Int,p2:Int,p3:Int) {}
            override fun afterTextChanged(p0:Editable?) {
                now.note=editText.text.toString()
                now.time1=DateFormat.format("yyyy-MM-dd HH:mm:ss",Date()).toString()
                if (now.time0=="") {
                    now.time0=now.time1
                }
                MainActivity.databaseHandler.updateNote(now,now)
                webView.loadUrl("javascript:show('"+editText.text.toString()
                    .replace("\n","⨂")+"');")
            }
        })

        view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.info)
            .setOnClickListener {
                view_else(it.context)
            }
        return view
    }

    fun view_else(context:Context) {
        val builder:AlertDialog.Builder=AlertDialog.Builder(context)
        builder.setTitle("笔记属性")
        val info=TextView(context)
        info.text="创建时间：${now.time0}\n修改时间：${now.time1}\n编辑时长：${now.time}分钟"
        info.setTextSize(TypedValue.COMPLEX_UNIT_SP,15F)
        info.setPadding(40,20,0,0)
        builder.setView(info)
        builder.setPositiveButton("OK") {dialog,which-> dialog.cancel()}
        builder.show()
    }
}