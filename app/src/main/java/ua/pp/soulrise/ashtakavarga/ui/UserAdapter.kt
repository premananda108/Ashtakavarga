package ua.pp.soulrise.ashtakavarga.ui

import android.annotation.SuppressLint
import android.text.InputType
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
import ua.pp.soulrise.ashtakavarga.R

class UserAdapter(
    private var users: List<UserEntity>,
    private val db: AppDatabase,
    private val refreshList: () -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userIdTextView: TextView = itemView.findViewById(R.id.userIdTextView)
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val nameEditText: EditText = itemView.findViewById(R.id.nameEditText)
        val dateOfBirthTextView: TextView = itemView.findViewById(R.id.dateOfBirthTextView)
        val dateOfBirthEditText: EditText = itemView.findViewById(R.id.dateOfBirthEditText)
        val editButton: Button = itemView.findViewById(R.id.editButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        val saveButton: Button = itemView.findViewById(R.id.saveButton)
        val cancelButton: Button = itemView.findViewById(R.id.cancelButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        return UserViewHolder(itemView)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.userIdTextView.text = user.userId.toString()
        holder.nameTextView.text = user.name
        holder.dateOfBirthTextView.text = SimpleDateFormat("dd.MM.yyyy").format(user.dateOfBirth)
        holder.nameEditText.setText(user.name)
        holder.dateOfBirthEditText.setText(SimpleDateFormat("dd.MM.yyyy").format(user.dateOfBirth))

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
            val newName = holder.nameEditText.text.toString()
            val newDateOfBirthString = holder.dateOfBirthEditText.text.toString()

            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val newDateOfBirth = sdf.parse(newDateOfBirthString)

            if (newName.isNotEmpty() && newDateOfBirth != null) {
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
        }

        holder.deleteButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                db.userDao().delete(user)
                refreshList() // Refresh the list after deletion
            }
        }
    }

    override fun getItemCount() = users.size

    @SuppressLint("NotifyDataSetChanged")
    fun setUsers(newUsers: List<UserEntity>) {
        users = newUsers
        notifyDataSetChanged()
    }
}