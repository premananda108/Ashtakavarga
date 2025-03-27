package ua.pp.soulrise.ashtakavarga.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ua.pp.soulrise.ashtakavarga.data.AppDatabase
import ua.pp.soulrise.ashtakavarga.data.UserEntity
import java.text.SimpleDateFormat
import java.util.Locale
import ua.pp.soulrise.ashtakavarga.R

class UserActivity : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var editTextDateOfBirth: EditText
    private lateinit var buttonAddUser: Button
    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        editTextName = findViewById(R.id.editTextName)
        editTextDateOfBirth = findViewById(R.id.editTextDateOfBirth)
        buttonAddUser = findViewById(R.id.buttonAddUser)
        usersRecyclerView = findViewById(R.id.usersRecyclerView)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "ashtakavarga-database"
        ).build()

        userAdapter = UserAdapter(emptyList(), db, 
            { loadUsers() }, // Refresh list after changes
            { userId -> // Обработчик клика по пользователю
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("user_id", userId)
                startActivity(intent)
            }
        )
        usersRecyclerView.adapter = userAdapter
        usersRecyclerView.layoutManager = LinearLayoutManager(this)

        buttonAddUser.setOnClickListener {
            addUser()
        }

        loadUsers() // Load users on activity start
    }

    private fun addUser() {
        val name = editTextName.text.toString()
        val dateOfBirthString = editTextDateOfBirth.text.toString()

        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val date = sdf.parse(dateOfBirthString)

        if (name.isNotEmpty() && date != null) {
            val dateOfBirthTimestamp = date.time
            val user = UserEntity(name = name, dateOfBirth = dateOfBirthTimestamp)

            CoroutineScope(Dispatchers.IO).launch {
                db.userDao().insert(user)
                loadUsers() // Refresh list after adding user
                runOnUiThread {
                    editTextName.text.clear()
                    editTextDateOfBirth.text.clear()
                }
            }
        }
    }

    private fun loadUsers() {
        CoroutineScope(Dispatchers.IO).launch {
            val users = db.userDao().getAllUsers()
            runOnUiThread {
                userAdapter.setUsers(users)
            }
        }
    }
}