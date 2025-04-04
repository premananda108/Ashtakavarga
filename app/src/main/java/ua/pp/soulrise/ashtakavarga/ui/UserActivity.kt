package ua.pp.soulrise.ashtakavarga.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
    companion object {
        private const val PICK_FILE_REQUEST_CODE = 1
    }

    private lateinit var editTextName: EditText
    private lateinit var editTextDateOfBirth: EditText
    private lateinit var editTextTimeOfBirth: EditText
    private lateinit var editTextBirthPlace: EditText
    private lateinit var buttonAddUser: Button
    private lateinit var importButton: Button
    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        setSupportActionBar(findViewById(R.id.toolbar))

        editTextName = findViewById(R.id.editTextName)
        editTextDateOfBirth = findViewById(R.id.editTextDateOfBirth)
        editTextTimeOfBirth = findViewById(R.id.editTextTimeOfBirth)
        editTextBirthPlace = findViewById(R.id.editTextBirthPlace)
        buttonAddUser = findViewById(R.id.buttonAddUser)
        importButton = findViewById(R.id.importButton)
        usersRecyclerView = findViewById(R.id.usersRecyclerView)

        // Добавляем TextWatcher для замены слешей на точки
        editTextDateOfBirth.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "astrology_database_room"
        ).fallbackToDestructiveMigration()
        .build()

        userAdapter = UserAdapter(emptyList(), db, 
            { loadUsers() }, // Refresh list after changes
            { userId -> // Обработчик клика по пользователю
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("user_id", userId)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish() // Закрываем UserActivity после перехода
            }
        )
        usersRecyclerView.adapter = userAdapter
        usersRecyclerView.layoutManager = LinearLayoutManager(this)

        buttonAddUser.setOnClickListener {
            addUser()
        }

        importButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
            }
            startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
        }

        loadUsers() // Load users on activity start
    }

    private fun addUser() {
        val name = editTextName.text.toString().trim()
        val dateOfBirthString = editTextDateOfBirth.text.toString()

        if (name.isEmpty()) {
            android.widget.Toast.makeText(this, "Введите имя", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        sdf.isLenient = false
        val date = try {
            sdf.parse(dateOfBirthString)
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, "Неверный формат даты. Используйте формат дд.мм.гггг", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        if (date != null) {
            val dateOfBirthTimestamp = date.time

            val timeOfBirthString = editTextTimeOfBirth.text.toString()
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val timeOfBirth = try {
                timeFormat.parse(timeOfBirthString)?.time ?: 0
            } catch (e: Exception) {
                android.widget.Toast.makeText(this, "Неверный формат времени. Используйте формат ЧЧ:ММ", android.widget.Toast.LENGTH_SHORT).show()
                return
            }

            val birthPlace = editTextBirthPlace.text.toString().trim()
            if (birthPlace.length > 256) {
                android.widget.Toast.makeText(this, "Место рождения не должно превышать 256 символов", android.widget.Toast.LENGTH_SHORT).show()
                return
            }

            val user = UserEntity(
                name = name,
                dateOfBirth = dateOfBirthTimestamp,
                timeOfBirth = timeOfBirth,
                birthPlace = birthPlace
            )

            CoroutineScope(Dispatchers.IO).launch {
                db.userDao().insert(user)
                loadUsers() // Refresh list after adding user
                runOnUiThread {
                    editTextName.text.clear()
                    editTextDateOfBirth.text.clear()
                    editTextTimeOfBirth.text.clear()
                    editTextBirthPlace.text.clear()
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_help -> {
                startActivity(Intent(this, HelpActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                try {
                    val jsonData = contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() }
                    if (jsonData != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                db.userDao().importUserData(jsonData, db.astrologyDao())
                                loadUsers() // Обновляем список после импорта
                                runOnUiThread {
                                    android.widget.Toast.makeText(this@UserActivity, "Импорт успешно завершен", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                runOnUiThread {
                                    android.widget.Toast.makeText(this@UserActivity, "Ошибка при импорте: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.widget.Toast.makeText(this, "Ошибка при чтении файла: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}