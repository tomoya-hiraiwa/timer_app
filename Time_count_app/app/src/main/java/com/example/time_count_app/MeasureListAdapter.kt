package com.example.time_count_app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.time_count_app.databinding.DataItemBinding

class MeasureListAdapter(private val dataList: MutableList<TimeData>): RecyclerView.Adapter<MeasureListAdapter.MeasureViewHolder>() {
    inner class MeasureViewHolder(private val b: DataItemBinding): RecyclerView.ViewHolder(b.root){
        fun bindData(data: TimeData){
            b.apply {
                number.text = data.no.toString()
                time.text = ConvertTime(data.time)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeasureViewHolder {
        return MeasureViewHolder(DataItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: MeasureViewHolder, position: Int) {
        holder.bindData(data = dataList[position])
    }
}