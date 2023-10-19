package com.example.note

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.note.DatabaseHandler.Companion.toInt
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader

class MainActivity:AppCompatActivity() {

    var file=mutableMapOf<String,MutableList<String>>()

    companion object {
        var note=mutableListOf<NoteData>()
        lateinit var databaseHandler:DatabaseHandler
    }

    var cut=-1
    var input_res=""
    var path=mutableListOf("Root")
    var opath=""

    var main=mutableListOf<ListData>()
    lateinit var adapter:MyAdapter
    lateinit var toolbar:androidx.appcompat.widget.Toolbar

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar=findViewById(R.id.toolBar)
        setSupportActionBar(toolbar)
        databaseHandler=DatabaseHandler(this)
        load(this)

        adapter=MyAdapter(main,this)
        val list:ListView=findViewById(R.id.mainView)
        list.adapter=adapter
        registerForContextMenu(list)
        refresh()
    }

    fun input_get(context:Context,func:()->Unit) {
        input_res=""
        val builder:AlertDialog.Builder=AlertDialog.Builder(context)
        builder.setTitle("请输入名称：")
        val input=EditText(context)
        input.inputType=InputType.TYPE_CLASS_TEXT
        builder.setView(input)
        builder.setPositiveButton("OK") {dialog,which->
            input_res=input.text.toString()
            if (input_res!="") {
                func()
            }
        }
        builder.setNegativeButton("Cancel") {dialog,which-> dialog.cancel()}
        builder.show()
    }

    fun get_note(path:String,name:String):Int {
        for (i in 0 until note.size) {
            if (note[i].path==path&&note[i].name==name) {
                return i
            }
        }
        return -1
    }

    fun refresh() {
        main.clear()
        if (path.size>1) {
            main.add(ListData("上一级"))
        }
        opath=path.joinToString("/")+"/"
        for (i in file[opath]!!) {
            main.add(ListData(i,1))
        }
        for (i in note) {
            if (i.path!=opath) {
                continue
            }
            main.add(ListData(i.name,i.done.toInt()+2))
        }
        adapter.notifyDataSetChanged()
    }

    fun checkm(op:String,at:String):Boolean {
        for (i in 0 until file[op]!!.size) {
            if (at==file[op]!![i]) {
                return false
            }
        }
        return true
    }

    fun checkn(op:String,at:String):Boolean {
        for (i in note) {
            if (op==i.path&&at==i.name) {
                return false
            }
        }
        return true
    }

    fun able(op:String):Boolean {
        for (i in note) {
            if (i.path.indexOf(op)!=-1) {
                return false
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu:Menu):Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return true
    }

    override fun onOptionsItemSelected(item:MenuItem):Boolean {
        return when (item.itemId) {
            R.id.security-> {
                val intent=Intent(this,SetActivity::class.java)
                startActivity(intent,null)
                return true
            }
            R.id.paste_it-> {
                if (!checkn(opath,note[cut].name)) {
                    Toast.makeText(this,"已存在同名文件",Toast.LENGTH_SHORT).show()
                }
                else {
                    val old=note[cut]
                    val use=old.copy()
                    use.path=opath
                    note[cut]=use
                    databaseHandler.updateNote(old,use)
                    cut=-1
                    toolbar.menu.findItem(R.id.paste_it).isVisible=false
                    refresh()
                }
                return true
            }
            R.id.new_file-> {
                input_get(this) {
                    if (!checkn(opath,input_res)) {
                        Toast.makeText(this,"已存在同名文件",Toast.LENGTH_SHORT).show()
                    }
                    else {
                        val use=NoteData(opath,input_res)
                        note.add(use)
                        databaseHandler.insertNote(use)
                        refresh()
                    }
                }
                return true
            }
            R.id.new_fold-> {
                input_get(this) {
                    if (!checkm(opath,input_res)) {
                        Toast.makeText(this,"已存在同名文件夹",Toast.LENGTH_SHORT).show()
                    }
                    else {
                        file[opath]!!.add(input_res)
                        file[opath+input_res+"/"]=mutableListOf()
                        save(this)
                        refresh()
                    }
                }
                return true
            }
            else->super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateContextMenu(
        menu:ContextMenu?,v:View?,menuInfo:ContextMenu.ContextMenuInfo?,
    ) {
        super.onCreateContextMenu(menu,v,menuInfo)
        menuInflater.inflate(R.menu.menu_list,menu)
        val info:AdapterView.AdapterContextMenuInfo=menuInfo as AdapterView.AdapterContextMenuInfo
        val pos=info.position
        when (main[pos].mode) {
            1-> {
                if (able(opath+main[pos].name+"/")) {
                    menu!!.findItem(R.id.delete_0).isVisible=true
                }
                menu!!.findItem(R.id.rename_0).isVisible=true
            }
            2-> {
                menu!!.findItem(R.id.view_1).isVisible=true
                menu.findItem(R.id.edit_1).isVisible=true
                menu.findItem(R.id.rename_1).isVisible=true
                menu.findItem(R.id.cut_1).isVisible=true
                menu.findItem(R.id.archive_1).isVisible=true
                menu.findItem(R.id.delete_1).isVisible=true
            }
            3-> {
                menu!!.findItem(R.id.view_1).isVisible=true
                menu.findItem(R.id.cut_1).isVisible=true
                menu.findItem(R.id.delete_1).isVisible=true
            }
        }
    }

    override fun onContextItemSelected(item:MenuItem):Boolean {
        val at=main[(item.menuInfo as AdapterView.AdapterContextMenuInfo).position].name
        return when (item.itemId) {
            R.id.delete_0-> {
                val builder=AlertDialog.Builder(this)
                builder.setMessage("确定删除？").setCancelable(false)
                    .setPositiveButton("Yes") {dialog,id->
                        file[opath]!!.remove(at)
                        val tpath=opath+at+"/"
                        val tmp=file.toMutableMap()
                        for ((i,j) in file) {
                            if (i.indexOf(tpath)!=-1) {
                                tmp.remove(i)
                            }
                        }
                        file=tmp
                        save(this)
                        refresh()
                    }.setNegativeButton("No") {dialog,id->
                        dialog.dismiss()
                    }
                val alert=builder.create()
                alert.show()
                return true
            }
            R.id.rename_0-> {
                input_get(this) {
                    if (!checkm(opath,input_res)) {
                        Toast.makeText(this,"已存在同名文件夹",Toast.LENGTH_SHORT).show()
                    }
                    else {
                        file[opath]!!.remove(at)
                        file[opath]!!.add(input_res)
                        val npath=opath+input_res+"/"
                        val tpath=opath+at+"/"
                        val olen=tpath.length
                        val tmp=file.toMutableMap()
                        for ((i,j) in file) {
                            if (i.indexOf(tpath)!=-1) {
                                tmp[npath+i.substring(olen)]=j
                                tmp.remove(i)
                            }
                        }
                        file=tmp
                        save(this)
                        for (i in 0 until note.size) {
                            if (note[i].path.indexOf(tpath)!=-1) {
                                val fnote=note[i]
                                fnote.path=npath+note[i].path.substring(olen)
                                databaseHandler.updateNote(note[i],fnote)
                                note[i]=fnote
                            }
                        }
                        refresh()
                    }
                }
                return true
            }
            R.id.view_1-> {
                val intent=Intent(this,ViewActivity::class.java)
                intent.putExtra("pos",get_note(opath,at))
                startActivity(intent,null)
                return true
            }
            R.id.edit_1-> {
                val intent=Intent(this,EditActivity::class.java)
                intent.putExtra("pos",get_note(opath,at))
                startActivity(intent,null)
                return true
            }
            R.id.rename_1-> {
                input_get(this) {
                    if (!checkn(opath,input_res)) {
                        Toast.makeText(this,"已存在同名文件",Toast.LENGTH_SHORT).show()
                    }
                    else {
                        val tmp=get_note(opath,at)
                        val old=note[tmp]
                        val use=old.copy()
                        use.name=input_res
                        note[tmp]=use
                        databaseHandler.updateNote(old,use)
                        refresh()
                    }
                }
                return true
            }
            R.id.cut_1-> {
                cut=get_note(opath,at)
                toolbar.menu.findItem(R.id.paste_it).isVisible=true
                return true
            }
            R.id.archive_1-> {
                val builder=AlertDialog.Builder(this)
                builder.setMessage("确定归档？").setCancelable(false)
                    .setPositiveButton("Yes") {dialog,id->
                        val tmp=get_note(opath,at)
                        val old=note[tmp]
                        val use=old.copy()
                        use.done=true
                        note[tmp]=use
                        databaseHandler.updateNote(old,use)
                        refresh()
                    }.setNegativeButton("No") {dialog,id->
                        dialog.dismiss()
                    }
                val alert=builder.create()
                alert.show()
                return true
            }
            R.id.delete_1-> {
                val builder=AlertDialog.Builder(this)
                builder.setMessage("确定删除？").setCancelable(false)
                    .setPositiveButton("Yes") {dialog,id->
                        val tmp=get_note(opath,at)
                        val old=note[tmp]
                        note.removeAt(tmp)
                        databaseHandler.deleteNote(old)
                        if (cut==tmp) {
                            cut=-1
                            toolbar.menu.findItem(R.id.paste_it).isVisible=false
                        }
                        else if (cut>tmp) {
                            cut--
                        }
                        refresh()
                    }.setNegativeButton("No") {dialog,id->
                        dialog.dismiss()
                    }
                val alert=builder.create()
                alert.show()
                return true
            }
            else->super.onOptionsItemSelected(item)
        }
    }

    inner class MyAdapter(val main:MutableList<ListData>,val context:Context):BaseAdapter() {

        override fun getCount():Int {
            return main.size
        }

        override fun getItem(p0:Int):Any {
            return main[p0]
        }

        override fun getItemId(p0:Int):Long {
            return p0.toLong()
        }

        override fun getView(p0:Int,p1:View?,p2:ViewGroup?):View {
            val view:View=LayoutInflater.from(context).inflate(R.layout.note_list,p2,false)
            val task=getItem(p0) as ListData
            val text=view.findViewById(R.id.mainText) as TextView
            text.text=task.name
            when (task.mode) {
                0-> {
                    text.setTypeface(null,Typeface.ITALIC)
                    text.setOnClickListener {
                        path.removeLast()
                        if (path.size==1) {
                            toolbar.menu.findItem(R.id.security).isVisible=true
                        }
                        refresh()
                    }
                }
                1-> {
                    text.setTypeface(null,Typeface.BOLD)
                    text.setOnClickListener {
                        path.add(task.name)
                        toolbar.menu.findItem(R.id.security).isVisible=false
                        refresh()
                    }
                    text.setOnLongClickListener {
                        it.showContextMenu()
                    }
                }
                2-> {
                    text.setTextColor(Color.RED)
                }
                3-> {
                    text.setTextColor(Color.BLUE)
                }
            }
            if (task.mode==2||task.mode==3) {
                text.setOnClickListener {
                    val intent=Intent(it.context,ViewActivity::class.java)
                    intent.putExtra("pos",get_note(opath,task.name))
                    startActivity(intent,null)
                }
                text.setOnLongClickListener {
                    it.showContextMenu()
                }
            }
            return view
        }

    }

    fun load(context:Context) {
        databaseHandler=DatabaseHandler(context)
        val r=read(context)
        var u=""
        var v=""
        var w=mutableListOf<String>()
        for (i in r) {
            when (i) {
                ':'-> {
                    v=u
                    u=""
                }
                '|'-> {
                    w.add(u)
                    u=""
                }
                '#'-> {
                    file[v]=w
                    v=""
                    w=mutableListOf()
                }
                else-> {
                    u+=i
                }
            }
        }
        note=databaseHandler.tolistNote() as MutableList<NoteData>
    }

    fun save(context:Context) {
        var r=""
        for ((i,j) in file) {
            if (j.size==0) {
                r+=i+":#"
            }
            else {
                r+=i+":"+j.joinToString("|")+"|#"
            }
        }
        rite(context,r)
    }

    fun read(context:Context):String {
        val fileInputStream:FileInputStream
        try {
            fileInputStream=context.openFileInput("note.dat")
        }
        catch (e:Exception) {
            e.printStackTrace()
            return "Root/:#"
        }
        val bufferedReader=BufferedReader(InputStreamReader(fileInputStream))
        val stringBuilder:StringBuilder=StringBuilder()
        var text:String?=null
        while ({text=bufferedReader.readLine(); text}()!=null) {
            stringBuilder.append(text+"\n")
        }
        return stringBuilder.toString().dropLast(1)
    }

    fun rite(context:Context,data:String) {
        val fileOutputStream:FileOutputStream
        try {
            fileOutputStream=context.openFileOutput("note.dat",Context.MODE_PRIVATE)
            fileOutputStream.write(data.toByteArray())
        }
        catch (e:Exception) {
            e.printStackTrace()
        }
    }
}

data class NoteData(
    var path:String="",
    var name:String="",
    var done:Boolean=false,
    var time0:String="",
    var time1:String="",
    var time:Int=0,
    var note:String="",
)

class ListData(
    var name:String="",
    var mode:Int=0,
)