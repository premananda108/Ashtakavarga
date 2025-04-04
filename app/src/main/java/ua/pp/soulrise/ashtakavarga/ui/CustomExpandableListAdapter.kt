package ua.pp.soulrise.ashtakavarga.ui

import android.content.Context
import android.text.Spanned
import android.text.method.LinkMovementMethod // Import needed
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat // Use ContextCompat for color retrieval
import ua.pp.soulrise.ashtakavarga.R

class CustomExpandableListAdapter(
    private val context: Context,
    private val groups: List<String>,
    private val children: List<List<Spanned>>
) : BaseExpandableListAdapter() {

    // --- ADD THIS CLASS DEFINITION ---
    // ViewHolder for child items
    private class ChildViewHolder(val textView: TextView)
    // ---------------------------------

    // --- AND ADD THIS CLASS DEFINITION ---
    // ViewHolder for group items
    private class GroupViewHolder(val textView: TextView)
    // ----------------------------------

    override fun getChild(groupPosition: Int, childPosition: Int): Spanned {
        return children[groupPosition][childPosition]
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val view: View
        val viewHolder: ChildViewHolder // Now this reference is valid

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
            // Ensure this ID matches your TextView in list_item.xml
            viewHolder = ChildViewHolder(view.findViewById(android.R.id.text2)) // Now this is valid
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ChildViewHolder // Now this is valid
        }

        val textView = viewHolder.textView
        textView.text = getChild(groupPosition, childPosition)
        textView.setLinkTextColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark))
        textView.setTextColor(ContextCompat.getColor(context, R.color.brown)) // Or your desired color

        // *** Crucial Lines ***
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.linksClickable = true // Ensure links are clickable

        return view
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return children[groupPosition].size
    }

    override fun getGroup(groupPosition: Int): String {
        return groups[groupPosition]
    }

    override fun getGroupCount(): Int {
        return groups.size
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val view: View
        val viewHolder: GroupViewHolder // Now this reference is valid

        if (convertView == null) {
            view = LayoutInflater.from(context)
                .inflate(android.R.layout.simple_expandable_list_item_1, parent, false)
            viewHolder = GroupViewHolder(view.findViewById(android.R.id.text1)) // Now this is valid
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as GroupViewHolder // Now this is valid
        }

        val textView = viewHolder.textView
        textView.text = getGroup(groupPosition)
        textView.setTextColor(ContextCompat.getColor(context, R.color.brown))
        return view
    }

    override fun hasStableIds(): Boolean {
        // Set to false if data can change dynamically in ways that affect IDs
        return true
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        // Return true if you want the child item itself to be selectable
        return true
    }
}