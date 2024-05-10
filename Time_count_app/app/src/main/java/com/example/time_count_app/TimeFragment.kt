package com.example.time_count_app

import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.time_count_app.databinding.FragmentTimeBinding
import com.example.time_count_app.databinding.SaveDialogBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class TimeFragment : Fragment() {
    private lateinit var b: FragmentTimeBinding
    private lateinit var v: TimeViewModel
    private lateinit var database: TimeDatabase
    private lateinit var pref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        b = FragmentTimeBinding.inflate(inflater)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        v = ViewModelProvider(requireActivity())[TimeViewModel::class.java]
        pref = requireContext().getSharedPreferences("pref", Context.MODE_PRIVATE)
        database = TimeDatabase(requireContext())
        b.apply {
            timeList.layoutManager = LinearLayoutManager(requireContext())
            startBt.setOnClickListener {
                v.measureTime()
            }

            startBt.setOnClickListener {
                if (v.onPause.value == false && v.isScale.value == true) {
                    v.pauseTime()
                } else {
                    v.measureTime()
                }
            }

            stopBt.setOnClickListener {
                println("stop clicked")
                v.stopTime()
            }
            saveBt.setOnClickListener {
                saveDialog()
            }

            v.isScale.observe(viewLifecycleOwner) { bool ->
                if (bool) {
                    stopBt.visibility = View.VISIBLE
                    saveBt.visibility = View.GONE
                    startBt.text = "Pause"
                } else {
                    stopBt.visibility = View.GONE
                    saveBt.visibility = View.VISIBLE
                    startBt.text = "Start"
                }
            }
            v.onPause.observe(viewLifecycleOwner) { bool ->
                if (!bool && v.isScale.value == true) {
                    startBt.text = "Pause"
                } else startBt.text = "Start"
            }

            v.time.observe(viewLifecycleOwner) { tc ->
                time.text = ConvertTime(tc)
                timer.now = tc.toFloat()
                timer.invalidate()
            }

            v.timeList.observe(viewLifecycleOwner) { data ->
                saveBt.isEnabled = data.isNotEmpty()
                val adapter = MeasureListAdapter(data)
                timeList.adapter = adapter
                val listHelper = ItemTouchHelper(ListTouchHelper())
                listHelper.attachToRecyclerView(timeList)
            }
            v.timeCount.observe(viewLifecycleOwner) { count ->
                step.text = "Step${count}"
            }

        }
    }

    private fun saveDialog() {
        Dialog(requireContext()).apply {
            val db = SaveDialogBinding.inflate(layoutInflater)
            setContentView(db.root)
            db.apply {
                nameEdit.doAfterTextChanged {
                    saveBt.isEnabled = !nameEdit.text.isNullOrEmpty()
                }
                saveBt.setOnClickListener {
                    val name = nameEdit.text.toString()
                    lifecycleScope.launch {
                        saveData(name)
                    }
                    cancel()
                }
                setCancelable(true)
            }
        }.show()
    }

    private suspend fun saveData(name: String) {
        val db = database.writableDatabase
        val number = pref.getInt("number", 0)
        pref.edit().apply {
            putInt("number", number + 1)
                .commit()
        }
        val parentValues = ContentValues().apply {
            put("id", number)
            put("name", name)
        }
        withContext(Dispatchers.IO) {
            db.insert(TimeDatabase.parent, null, parentValues)
            for (i in v.timeList.value?.indices ?: return@withContext) {
                val data = v.timeList.value!![i]
                val con = ContentValues().apply {
                    put("parent", number)
                    put("number", data.no)
                    put("time", data.time)
                }
                db.insert(TimeDatabase.child, null, con)
            }
        }
        v.initTime()
    }

    inner class ListTouchHelper() : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.ACTION_STATE_IDLE,
        ItemTouchHelper.RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            v.removeList(viewHolder.adapterPosition)
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            val backGround = ColorDrawable(ContextCompat.getColor(requireContext(), R.color.second))
            val itemView = viewHolder.itemView
            val drawable =
                ContextCompat.getDrawable(requireContext(), R.drawable.baseline_delete_outline_24)
            val iconMargin = (itemView.height - drawable!!.intrinsicHeight) / 2
            val iconTop = itemView.top + (itemView.height - drawable.intrinsicHeight) / 2
            drawable.setBounds(
                itemView.left + iconMargin,
                itemView.top + iconMargin,
                itemView.left + drawable.intrinsicWidth,
                iconTop + drawable.intrinsicHeight
            )
            backGround.setBounds(
                itemView.left + 5,
                itemView.top + 5,
                itemView.left + dX.toInt(),
                itemView.bottom - 5
            )
            if (dX == 0f) {
                backGround.setBounds(0, 0, 0, 0)
            }
            backGround.draw(c)
            drawable.draw(c)
        }
    }
}

fun ConvertTime(time: Int): String {
    val min = time / 60
    val sec = time % 60
    return String.format("%02d:%02d", min, sec)
}
