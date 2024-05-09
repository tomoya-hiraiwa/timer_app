package com.example.time_count_app

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.time_count_app.databinding.FragmentHistoryBinding
import com.example.time_count_app.databinding.HistoryItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Time


class HistoryFragment : Fragment() {
    private lateinit var b: FragmentHistoryBinding
    private lateinit var database: TimeDatabase
    private lateinit var pref: SharedPreferences
    private lateinit var v: HistoryVIewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        b = FragmentHistoryBinding.inflate(inflater)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = TimeDatabase(requireContext())
        v = ViewModelProvider(requireActivity())[HistoryVIewModel::class.java]
        pref = requireContext().getSharedPreferences("pref", Context.MODE_PRIVATE)
        b.apply {
            hisList.layoutManager = LinearLayoutManager(requireContext())
            floatingActionButton.setOnClickListener {
                deleteDialog()
            }
            v.parentData.observe(viewLifecycleOwner) { data ->
                val adapter = HistoryListAdapter(data, database, requireContext())
                hisList.adapter = adapter
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            v.getParentData(database)
        }
    }

    private fun deleteDialog(){
        AlertDialog.Builder(requireContext()).apply {
            setTitle("確認")
            setMessage("全てのデータを削除します")
            setNegativeButton("キャンセル"){_,_->}
            setPositiveButton("実行"){_,_->
                lifecycleScope.launch {
                    v.deleteData(database)
                }
            }
        }.show()
    }
}

class HistoryListAdapter(
    private val dataList: MutableList<ParentData>,
    private val database: TimeDatabase,
    private val context: Context
) : RecyclerView.Adapter<HistoryListAdapter.HistoryViewHolder>() {
    inner class HistoryViewHolder(private val b: HistoryItemBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bindData(data: ParentData) {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                var childList = mutableListOf<TimeData>()
                val db = database.readableDatabase
                val cur = db.query(
                    TimeDatabase.child, arrayOf("number", "time"), "parent = ?",
                    arrayOf(data.id.toString()), null, null, null
                )
                while (cur.moveToNext()) {
                    val number = cur.getInt(cur.getColumnIndexOrThrow("number"))
                    val time = cur.getInt(cur.getColumnIndexOrThrow("time"))
                    childList.add(TimeData(number, time))
                }
                withContext(Dispatchers.Main) {
                    for (i in childList.indices) {
                        val textView = TextView(context).apply {
                            textSize = 20f
                            setPadding(5, 10, 5, 10)
                            val timeText = ConvertTime(childList[i].time)
                            text = "${childList[i].no}. ${timeText}"
                        }
                        b.frame.addView(textView)
                    }
                }
            }
            b.apply {
                name.text = data.name
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder(
            HistoryItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bindData(data = dataList[position])
    }
}

data class ParentData(val id: Int, val name: String)