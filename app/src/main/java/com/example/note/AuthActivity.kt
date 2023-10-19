package com.example.note

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class AuthActivity:AppCompatActivity() {
    private lateinit var executor:Executor
    private lateinit var biometricPrompt:BiometricPrompt
    private lateinit var promptInfo:BiometricPrompt.PromptInfo
    private var init=true

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        fun next() {
            val intent=Intent(applicationContext,MainActivity::class.java)
            startActivity(intent,null)
        }

        executor=ContextCompat.getMainExecutor(this)
        biometricPrompt=
            BiometricPrompt(this,executor,object:BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode:Int,
                    errString:CharSequence,
                ) {
                    super.onAuthenticationError(errorCode,errString)
                    Toast.makeText(applicationContext,"错误:$errString",Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(
                    result:BiometricPrompt.AuthenticationResult,
                ) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(applicationContext,"验证成功",Toast.LENGTH_SHORT).show()
                    next()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext,"验证失败",Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo=BiometricPrompt.PromptInfo.Builder().setTitle("身份验证").setNegativeButtonText("取消")
            .build()

        val setting=getSharedPreferences("Note_Setting",Context.MODE_PRIVATE)
        val need_auth=setting.getBoolean("Need_Auth",false)
        val password=setting.getString("Password","")
        val biology=setting.getBoolean("Biology",false)

        if (!need_auth) {
            next()
        }

        if (!biology) {
            findViewById<TextView>(R.id.textView).visibility=View.GONE
            findViewById<Button>(R.id.auth2).visibility=View.GONE
        }

        findViewById<Button>(R.id.auth1).setOnClickListener {
            if (findViewById<EditText>(R.id.password).text.toString()==password) {
                next()
            }
            else {
                Toast.makeText(this,"密码错误",Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.auth2).setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }
    }

    override fun onStart() {
        super.onStart()
        if (init) {
            init=false
        }
        else {
            finish()
        }
    }

}