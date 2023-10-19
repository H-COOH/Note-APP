package com.example.note

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SetActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set)

        val switch1:Switch=findViewById(R.id.switch1)
        val password2:EditText=findViewById(R.id.password2)
        val switch3:Switch=findViewById(R.id.switch3)

        val setting=getSharedPreferences("Note_Setting",Context.MODE_PRIVATE)
        var need_auth=setting.getBoolean("Need_Auth",false)
        var password=setting.getString("Password","")
        var biology=setting.getBoolean("Biology",false)

        fun show() {
            if (need_auth) {
                password2.visibility=View.VISIBLE
                switch3.visibility=View.VISIBLE
                findViewById<TextView>(R.id.textView2).visibility=View.VISIBLE
                findViewById<TextView>(R.id.textView3).visibility=View.VISIBLE
            }
            else {
                password2.visibility=View.GONE
                switch3.visibility=View.GONE
                findViewById<TextView>(R.id.textView2).visibility=View.GONE
                findViewById<TextView>(R.id.textView3).visibility=View.GONE
            }
        }

        switch1.isChecked=need_auth
        password2.setText(password)
        switch3.isChecked=biology
        show()

        switch1.setOnClickListener {
            need_auth=switch1.isChecked
            with(setting.edit()) {
                putBoolean("Need_Auth",need_auth)
                apply()
            }
            show()
        }
        password2.addTextChangedListener(object:TextWatcher {
            override fun beforeTextChanged(p0:CharSequence?,p1:Int,p2:Int,p3:Int) {}
            override fun onTextChanged(p0:CharSequence?,p1:Int,p2:Int,p3:Int) {}
            override fun afterTextChanged(p0:Editable?) {
                password=password2.text.toString()
                with(setting.edit()) {
                    putString("Password",password)
                    apply()
                }
            }
        })
        switch3.setOnClickListener {
            biology=switch3.isChecked
            with(setting.edit()) {
                putBoolean("Biology",biology)
                apply()
            }
        }
    }
}