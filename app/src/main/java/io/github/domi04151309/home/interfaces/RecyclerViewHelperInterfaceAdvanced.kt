package io.github.domi04151309.home.interfaces

import android.view.View
import androidx.recyclerview.widget.RecyclerView

interface RecyclerViewHelperInterfaceAdvanced {
    fun onItemClicked(view: View, position: Int)
    fun onItemHandleTouched(viewHolder: RecyclerView.ViewHolder)
}