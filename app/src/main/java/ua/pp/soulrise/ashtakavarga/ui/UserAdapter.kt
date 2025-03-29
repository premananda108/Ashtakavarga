package ua.pp.soulrise.ashtakavarga.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ua.pp.soulrise.ashtakavarga.data.AppDatabase
import ua.pp.soulrise.ashtakavarga.data.UserEntity
import java.text.SimpleDateFormat
import java.util.Locale
import ua.pp.soulrise.ashtakavarga.databinding.UserItemBinding

class UserAdapter(
    private var users: List<UserEntity>,
    private val db: AppDatabase,
    private val refreshList: () -> Unit,
    private val onUserClick: (Long) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(private val binding: UserItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val userIdTextView: TextView = binding.userIdTextView
        val nameTextView: TextView = binding.nameTextView
        val nameEditText: EditText = binding.nameEditText
        val dateOfBirthTextView: TextView = binding.dateOfBirthTextView
        val dateOfBirthEditText: EditText = binding.dateOfBirthEditText
        val editButton: Button = binding.editButton
        val deleteButton: Button = binding.deleteButton
        val saveButton: Button = binding.saveButton
        val cancelButton: Button = binding.cancelButton
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
        holder.nameEditText.setText(user.name)
        holder.dateOfBirthEditText.setText(SimpleDateFormat("dd.MM.yyyy").format(user.dateOfBirth))

        // Добавляем обработчик клика на весь элемент
        holder.itemView.setOnClickListener {
            user.userId?.let { userId -> onUserClick(userId.toLong()) }
        }

        holder.editButton.setOnClickListener {
            holder.nameTextView.visibility = View.GONE
            holder.dateOfBirthTextView.visibility = View.GONE
            holder.editButton.visibility = View.GONE
            holder.deleteButton.visibility = View.GONE

            holder.nameEditText.visibility = View.VISIBLE
            holder.dateOfBirthEditText.visibility = View.VISIBLE
            holder.saveButton.visibility = View.VISIBLE
            holder.cancelButton.visibility = View.VISIBLE
        }

        holder.cancelButton.setOnClickListener {
            holder.nameTextView.visibility = View.VISIBLE
            holder.dateOfBirthTextView.visibility = View.VISIBLE
            holder.editButton.visibility = View.VISIBLE
            holder.deleteButton.visibility = View.VISIBLE

            holder.nameEditText.visibility = View.GONE
            holder.dateOfBirthEditText.visibility = View.GONE
            holder.saveButton.visibility = View.GONE
            holder.cancelButton.visibility = View.GONE

            holder.nameEditText.setText(user.name)
            holder.dateOfBirthEditText.setText(SimpleDateFormat("dd.MM.yyyy").format(user.dateOfBirth))
        }


        holder.saveButton.setOnClickListener {
            val newName = holder.nameEditText.text.toString().trim()
            val newDateOfBirthString = holder.dateOfBirthEditText.text.toString()

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
            val updatedUser = user.copy(name = newName, dateOfBirth = newDateOfBirthTimestamp)

            CoroutineScope(Dispatchers.IO).launch {
                db.userDao().update(updatedUser)
                refreshList() // Refresh the list after update
            }

            holder.nameTextView.visibility = View.VISIBLE
            holder.dateOfBirthTextView.visibility = View.VISIBLE
            holder.editButton.visibility = View.VISIBLE
            holder.deleteButton.visibility = View.VISIBLE

            holder.nameEditText.visibility = View.GONE
            holder.dateOfBirthEditText.visibility = View.GONE
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
}