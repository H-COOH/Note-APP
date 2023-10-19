package com.example.note

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.note.EditActivity.Companion.now

class EditFragment:Fragment() {

    companion object {
        lateinit var editText:EditText
    }

    override fun onCreateView(
        inflater:LayoutInflater,container:ViewGroup?,
        savedInstanceState:Bundle?,
    ):View {
        val view=inflater.inflate(R.layout.fragment_edit,container,false)!!
        editText=view.findViewById(R.id.editText)
        editText.setText(now.note)

        view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.back)
            .setOnClickListener {
                requireActivity().finish()
            }
        return view
    }

}