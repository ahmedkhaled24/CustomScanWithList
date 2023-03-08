package com.example.customscanwithlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DataAdapter(var arr: MutableList<Long>): RecyclerView.Adapter<DataAdapter.DataViewHolder>() {

    class DataViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val barcodeTv : TextView = itemView.findViewById(R.id.barcodeTv)
        val countTv : TextView = itemView.findViewById(R.id.countTv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.scan_item, parent, false)
        return DataViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.barcodeTv.text = arr[position].toString()
    }

    override fun getItemCount(): Int {
        return arr.size
    }


}