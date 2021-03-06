package com.example.myapp.mynotes

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.myapp.mynotes.adapter.NotesAdapter
import com.example.myapp.NotesApp
import com.example.myapp.R
import com.example.myapp.mynotes.clicklinstener.itemClickListener
import com.example.myapp.data.local.db.Notes
import com.example.myapp.data.local.pref.PrefConstant
import com.example.myapp.utils.AppConstant
import com.example.myapp.utils.WorkManager.MyWorker
import com.example.myapp.addnotes.AddNotesActivity
import com.example.myapp.blog.BlogActivity
import com.example.myapp.detail.DetailActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.concurrent.TimeUnit

//import kotlinx.android.synthetic.main.activity_my_notes.*

class MyNotesActivity :AppCompatActivity(){
    lateinit var fullName: String
    lateinit var fabAddNotes: FloatingActionButton
    lateinit var sharedPreferences: SharedPreferences
    lateinit var recyclerView: RecyclerView
    var notesList = ArrayList<Notes>()
    var TAG = "MyNotesActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_notes)
        bindViews()
        setupSharedPreference()
        getIntentData()
        getDataFromDataBase()
        setupToolBarText()
        setupRecyclerView()
        setupWorkManager()

        var launchSomeActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val title=data?.getStringExtra(AppConstant.TITLE)
                val description=data?.getStringExtra(AppConstant.DESCRIPTION)
                val imagePath=data?.getStringArrayExtra(AppConstant.imagePath)

                val notesApp=application as NotesApp
                val notesDae=notesApp.getNotesDb().notesDao()
                val notes= Notes(title = title!!, description = description!!,imagePath = imagePath.toString(),isTaskCompleted = false)
                if (title.isNotEmpty() && description.isNotEmpty()) {
                   notesList.add(notes)
                } else {
                    Toast.makeText(
                        this@MyNotesActivity,
                        "Title and description can't be empty",
                        Toast.LENGTH_SHORT
                    ).show()
                }


                //notesList.add(notes)
                notesDae.insert(notes)
                //getNotesToDb(notes)
                recyclerView.adapter?.notifyItemChanged(notesList.size-1)
            }
        }
        fabAddNotes.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                val intent = Intent(this@MyNotesActivity, AddNotesActivity::class.java)
                    launchSomeActivity.launch(intent)

            }

        })
    }

    private fun setupWorkManager() {
        val constaint=Constraints.Builder()
                //.setRequiresCharging(true)
                //.setRequiresBatteryNotLow(true)
                .build()
        val request=PeriodicWorkRequest.Builder(MyWorker::class.java,1,TimeUnit.MINUTES)
                .setConstraints(constaint)
                .build()
        WorkManager.getInstance().enqueue(request)

    }

//    private fun setupDialogBox() {
//        val view = LayoutInflater.from(this@MyNotesActivity)
//            .inflate(R.layout.add_notes_dialog_layout, null)
//
//        val EditTextTitle = view.findViewById<EditText>(R.id.EditTextTitle)
//        val EditTextDescription = view.findViewById<EditText>(R.id.EditTextDescription)
//        val submitButton = view.findViewById<Button>(R.id.submitButton)
//        val dialog = AlertDialog.Builder(this)
//            .setView(view)
//            .setCancelable(false)
//            .create()
//        submitButton.setOnClickListener(object : View.OnClickListener {
//            override fun onClick(p0: View?) {
//                val title = EditTextTitle.text.toString()
//                val description = EditTextDescription.text.toString()
//                val notes = Notes(title = title, description = description)
//                if (title.isNotEmpty() && description.isNotEmpty()) {
//                    notesList.add(notes)
//                } else {
//                    Toast.makeText(
//                        this@MyNotesActivity,
//                        "Title and description can't be empty",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//                getNotesToDb(notes)
//                dialog.hide()
//            }
//        })
//        dialog.show()
//
//    }

//    private fun getNotesToDb(notes: Notes) {
//        val notesApp = applicationContext as NotesApp
//        val notesDae = notesApp.getNotesDb().notesDao()
//        notesDae.insert(notes)
//    }

    private fun setupRecyclerView() {
        val ItemClickListener = object : itemClickListener {
            override fun onClick(notes: Notes) {
                val intent = Intent(this@MyNotesActivity, DetailActivity::class.java)
                intent.putExtra(AppConstant.TITLE, notes.title)
                intent.putExtra(AppConstant.DESCRIPTION, notes.description)
                startActivity(intent)
            }

            override fun onUpdate(notes: Notes) {
                Log.d(TAG, notes.isTaskCompleted.toString())

                val notesApp = applicationContext as NotesApp
                val notesDae = notesApp.getNotesDb().notesDao()
                notesDae.update(notes)
            }
        }
        val notesAdapter = NotesAdapter(notesList, ItemClickListener)
        val linearLayoutManager = LinearLayoutManager(this@MyNotesActivity)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = notesAdapter

    }

    private fun setupToolBarText() {
//        val loginIntent=intent
//        var fullName=""
//        if(loginIntent.hasExtra(AppConstant.Full_Name)){
//            fullName=loginIntent.getStringExtra(AppConstant.Full_Name).toString()
//            if (TextUtils.isEmpty(fullName)){
//                fullName=sharedPreferences.getString(PrefConstant.full_name,"")!!
//            }
//        }
//        if (!TextUtils.isEmpty(fullName)) {
            if (supportActionBar != null) {
                supportActionBar?.title = fullName
            }
  //      }
    }

    private fun getDataFromDataBase() {
        val notesApp = applicationContext as NotesApp
        val notesDae=notesApp.getNotesDb().notesDao()
        notesList.addAll(notesDae.getAll())
    }

    private fun getIntentData() {
        fullName = sharedPreferences.getString(PrefConstant.full_name, "").toString()
    }

    private fun setupSharedPreference() {
        sharedPreferences = getSharedPreferences(PrefConstant.Shared_Preference_Notes, MODE_PRIVATE)
    }

    private fun bindViews() {
        fabAddNotes = findViewById(R.id.fabAddNotes)
        recyclerView = findViewById(R.id.recyclerviewNotes)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater=menuInflater
        inflater.inflate(R.menu.menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId==R.id.blog){
            Log.d(TAG,"Click successfull")
            val intent=Intent(this, BlogActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}