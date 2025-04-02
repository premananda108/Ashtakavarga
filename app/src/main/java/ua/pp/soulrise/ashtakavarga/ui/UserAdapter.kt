package ua.pp.soulrise.ashtakavarga.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.Job
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ua.pp.soulrise.ashtakavarga.data.AppDatabase
import ua.pp.soulrise.ashtakavarga.data.UserEntity
import java.text.SimpleDateFormat
import java.util.Locale
import ua.pp.soulrise.ashtakavarga.databinding.UserItemBinding
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import android.os.Environment

class UserAdapter(
    private var users: List<UserEntity>,
    private val db: AppDatabase,
    private val refreshList: () -> Unit,
    private val onUserClick: (Long) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    class UserViewHolder(private val binding: UserItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val userIdTextView: TextView = binding.userIdTextView
        val nameTextView: TextView = binding.nameTextView
        val nameEditText: EditText = binding.nameEditText
        val dateOfBirthTextView: TextView = binding.dateOfBirthTextView
        val dateOfBirthEditText: EditText = binding.dateOfBirthEditText
        val timeOfBirthTextView: TextView = binding.timeOfBirthTextView
        val timeOfBirthEditText: EditText = binding.timeOfBirthEditText
        val birthPlaceTextView: TextView = binding.birthPlaceTextView
        val birthPlaceEditText: EditText = binding.birthPlaceEditText
        val editButton: Button = binding.editButton
        val deleteButton: Button = binding.deleteButton
        val saveButton: Button = binding.saveButton
        val cancelButton: Button = binding.cancelButton
        val exportButton: Button = binding.exportButton

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = UserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.userIdTextView.text = user.userId.toString()
        holder.nameTextView.text = user.name
        holder.dateOfBirthTextView.text = SimpleDateFormat("dd.MM.yyyy").format(user.dateOfBirth)
        holder.timeOfBirthTextView.text = if (user.timeOfBirth > 0) SimpleDateFormat("HH:mm").format(user.timeOfBirth) else ""
        holder.birthPlaceTextView.text = user.birthPlace
        
        holder.nameEditText.setText(user.name)
        holder.dateOfBirthEditText.setText(SimpleDateFormat("dd.MM.yyyy").format(user.dateOfBirth))
        holder.timeOfBirthEditText.setText(if (user.timeOfBirth > 0) SimpleDateFormat("HH:mm").format(user.timeOfBirth) else "")
        holder.birthPlaceEditText.setText(user.birthPlace)

        // Добавляем обработчик клика на весь элемент
        holder.itemView.setOnClickListener {
            user.userId?.let { userId -> 
                // Добавляем логирование для отладки
                android.util.Log.d("UserAdapter", "Clicked on user with ID: $userId")
                onUserClick(userId.toLong())
            }
        }
        
        holder.exportButton.setOnClickListener {
            user.userId?.let { userId ->
                val context = holder.itemView.context
                scope.launch {
                    try {
                        val jsonData = db.userDao().exportUserData(userId, db.astrologyDao())
                        val fileName = "user_${user.name}_${SimpleDateFormat("yyyyMMdd_HHmmss").format(System.currentTimeMillis())}.json"
                        val file = File(context.cacheDir, fileName)

                        FileOutputStream(file).use { output ->
                            output.write(jsonData.toByteArray())
                        }

                        // Используем FileProvider для безопасного предоставления доступа к файлу
                        val fileUri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "ua.pp.soulrise.ashtakavarga.fileprovider",
                            file
                        )

                        // Создаем Intent для отправки файла
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_STREAM, fileUri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        // Запускаем диалог выбора приложения для отправки
                        context.startActivity(Intent.createChooser(shareIntent, "Поделиться файлом"))

                        // Удаляем файл после отправки
                        //file.delete()
                    } catch (e: Exception) {
                        android.util.Log.e("UserAdapter", "Error exporting data", e)
                        launch(Dispatchers.Main) {
                            Toast.makeText(context, "Ошибка при экспорте данных: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        holder.editButton.setOnClickListener {
            holder.nameTextView.visibility = View.GONE
            holder.dateOfBirthTextView.visibility = View.GONE
            holder.timeOfBirthTextView.visibility = View.GONE
            holder.birthPlaceTextView.visibility = View.GONE
            holder.editButton.visibility = View.GONE
            holder.deleteButton.visibility = View.GONE

            holder.nameEditText.visibility = View.VISIBLE
            holder.dateOfBirthEditText.visibility = View.VISIBLE
            holder.timeOfBirthEditText.visibility = View.VISIBLE
            holder.birthPlaceEditText.visibility = View.VISIBLE
            holder.saveButton.visibility = View.VISIBLE
            holder.cancelButton.visibility = View.VISIBLE
        }

        holder.cancelButton.setOnClickListener {
            holder.nameTextView.visibility = View.VISIBLE
            holder.dateOfBirthTextView.visibility = View.VISIBLE
            holder.timeOfBirthTextView.visibility = View.VISIBLE
            holder.birthPlaceTextView.visibility = View.VISIBLE
            holder.editButton.visibility = View.VISIBLE
            holder.deleteButton.visibility = View.VISIBLE

            holder.nameEditText.visibility = View.GONE
            holder.dateOfBirthEditText.visibility = View.GONE
            holder.timeOfBirthEditText.visibility = View.GONE
            holder.birthPlaceEditText.visibility = View.GONE
            holder.saveButton.visibility = View.GONE
            holder.cancelButton.visibility = View.GONE

            holder.nameEditText.setText(user.name)
            holder.dateOfBirthEditText.setText(SimpleDateFormat("dd.MM.yyyy").format(user.dateOfBirth))
            holder.timeOfBirthEditText.setText(if (user.timeOfBirth > 0) SimpleDateFormat("HH:mm").format(user.timeOfBirth) else "")
            holder.birthPlaceEditText.setText(user.birthPlace)
        }


        holder.saveButton.setOnClickListener {
            val newName = holder.nameEditText.text.toString().trim()
            val newDateOfBirthString = holder.dateOfBirthEditText.text.toString()
            val newTimeOfBirth = holder.timeOfBirthEditText.text.toString()
            val newBirthPlace = holder.birthPlaceEditText.text.toString().trim()

            if (newName.isEmpty()) {
                android.widget.Toast.makeText(holder.itemView.context, "Введите имя", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            sdf.isLenient = false
            val newDateOfBirth = try {
                sdf.parse(newDateOfBirthString)
            } catch (e: Exception) {
                android.widget.Toast.makeText(holder.itemView.context, "Неверный формат даты. Используйте формат дд.мм.гггг", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newDateOfBirthTimestamp = newDateOfBirth.time
            
            // Парсим время рождения
            val timeSdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            timeSdf.isLenient = false
            val timeOfBirthMillis = try {
                val time = timeSdf.parse(newTimeOfBirth)
                if (time != null) time.time else 0L
            } catch (e: Exception) {
                android.widget.Toast.makeText(holder.itemView.context, "Неверный формат времени. Используйте формат ЧЧ:ММ", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val updatedUser = user.copy(
                name = newName,
                dateOfBirth = newDateOfBirthTimestamp,
                timeOfBirth = timeOfBirthMillis,
                birthPlace = newBirthPlace
            )

            CoroutineScope(Dispatchers.IO).launch {
                db.userDao().update(updatedUser)
                refreshList() // Refresh the list after update
            }

            holder.nameTextView.visibility = View.VISIBLE
            holder.dateOfBirthTextView.visibility = View.VISIBLE
            holder.timeOfBirthTextView.visibility = View.VISIBLE
            holder.birthPlaceTextView.visibility = View.VISIBLE
            holder.editButton.visibility = View.VISIBLE
            holder.deleteButton.visibility = View.VISIBLE

            holder.nameEditText.visibility = View.GONE
            holder.dateOfBirthEditText.visibility = View.GONE
            holder.timeOfBirthEditText.visibility = View.GONE
            holder.birthPlaceEditText.visibility = View.GONE
            holder.saveButton.visibility = View.GONE
            holder.cancelButton.visibility = View.GONE
        }

        holder.deleteButton.setOnClickListener {
            val builder = android.app.AlertDialog.Builder(holder.itemView.context, android.R.style.Theme_Material_Dialog_Alert)
            builder.setTitle("Подтверждение")
                .setMessage("Вы действительно хотите удалить?")
                .setPositiveButton("Да") { _, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        db.userDao().deleteUserWithTable(user)
                        refreshList() // Refresh the list after deletion
                    }
                }
                .setNegativeButton("Нет") { dialog, _ ->
                    dialog.dismiss()
                }
            val dialog = builder.create()
            dialog.setOnShowListener {
                val positiveButton = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
                val negativeButton = dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)
                positiveButton.textSize = 16f
                negativeButton.textSize = 16f
            }
            dialog.show()
        }
    }

    override fun getItemCount() = users.size

    @SuppressLint("NotifyDataSetChanged")
    fun setUsers(newUsers: List<UserEntity>) {
        users = newUsers
        notifyDataSetChanged()
    }

    private fun handleImport(context: android.content.Context) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/json"
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        (context as? android.app.Activity)?.startActivityForResult(
            Intent.createChooser(intent, "Выберите файл для импорта"),
            IMPORT_FILE_REQUEST_CODE
        )
    }

    fun handleImportResult(context: android.content.Context, uri: android.net.Uri) {
        scope.launch {
            try {
                val jsonData = context.contentResolver.openInputStream(uri)?.use { input ->
                    String(input.readBytes())
                } ?: throw IllegalArgumentException("Не удалось прочитать файл")

                db.userDao().importUserData(jsonData, db.astrologyDao())
                refreshList()
                Toast.makeText(context, "Данные успешно импортированы", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                android.util.Log.e("UserAdapter", "Error importing data", e)
                Toast.makeText(context, "Ошибка при импорте данных: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val IMPORT_FILE_REQUEST_CODE = 123
    }
}