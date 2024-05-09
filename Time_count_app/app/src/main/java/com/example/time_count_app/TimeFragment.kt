package com.example.time_count_app

import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.time_count_app.databinding.FragmentTimeBinding
import com.example.time_count_app.databinding.SaveDialogBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
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
            v.onPause.observe(viewLifecycleOwner){bool ->
               if (!bool && v.isScale.value == true){
                   startBt.text = "Pause"
               }
                else startBt.text = "Start"
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
}

fun ConvertTime(time: Int): String {
    val min = time / 60
    val sec = time % 60
    return String.format("%02d:%02d", min, sec)
}
