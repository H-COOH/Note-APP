package com.example.note

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ViewActivity:AppCompatActivity() {

    lateinit var now:NoteData

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)
        now=MainActivity.note[intent.getIntExtra("pos",-1)]

        val webview:WebView=findViewById(R.id.webView)
        webview.settings.javaScriptEnabled=true
        webview.loadUrl("file:///android_asset/show.html")
        webview.webViewClient=object:WebViewClient() {
            override fun onPageFinished(view:WebView,url:String) {
                webview.loadUrl("javascript:show('"+now.note.replace("\n","⨂")+"');")
            }
        }

        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.info).setOnClickListener {
            view_else(it.context)
        }
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.back).setOnClickListener {
            finish()
        }
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