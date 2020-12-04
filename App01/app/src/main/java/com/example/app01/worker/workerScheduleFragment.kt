package com.example.app01.worker

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.app01.MainActivity
import com.example.app01.R
import com.example.app01.dataObject
import com.example.app01.databinding.DialogBranchSelectionBinding
import com.example.app01.databinding.DialogDateModificationBinding
import com.example.app01.databinding.FragmentWorkerScheduleBinding
import com.example.app01.dto.branch.Branch
import com.example.app01.dto.branch.branchAdapter
import com.example.app01.dto.worker.Work
import com.example.app01.dto.worker.Worker
import com.example.app01.dto.worker.WorkerInfo
import com.example.app01.dto.workerdetail.WorkerDetail
import com.example.app01.dto.workerdetail.WorkerDetailAdapter
import com.example.app01.dto.workerview.WorkerView
import com.example.app01.selector.SelectGreenDecorator
import com.example.app01.selector.SelectRedDecorator
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class workerScheduleFragment : Fragment() {
    private lateinit var binding: FragmentWorkerScheduleBinding
    private lateinit var mBranchAdapter: branchAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_worker_schedule, container, false
        )
        binding.layoutMyPage.setOnClickListener {
            it.findNavController()
                .navigate(R.id.action_workerScheduleFragment_to_workerMypageFragment)
        }
        binding.CvAttendence.setTopbarVisible(false)
        binding.CvAttendence.isPagingEnabled = false

        (activity as MainActivity).getBranches(0)
        binding.currentBranch = dataObject.selectBranch.title
        defaultSetting(dataObject.selectWorkerInfo, dataObject.selectWorker)

        // Dialog for each date available
        binding.CvSchedule.setOnDateLongClickListener { widget, date ->
            if (dataObject.selectWorker.datesWork.contains(date)) {
                val dialog = AlertDialog.Builder(requireContext()).create()
                val bindingDialog: DialogDateModificationBinding = DataBindingUtil.inflate(
                    inflater,
                    R.layout.dialog_date_modification,
                    container,
                    false
                )
                dialog.setView(bindingDialog.root)
                dialog.show()
                // Setting Default
                bindingDialog.date =
                    date.month.toString() + "월 " + date.day.toString() + "일 " + date.year.toString()
                bindingDialog.timeStart = dataObject.selectWorker.infowork[date]!!.timeStart
                bindingDialog.timeEnd = dataObject.selectWorker.infowork[date]!!.timeEnd
                bindingDialog.payment =
                    dataObject.selectWorker.infowork[date]!!.calculate().toString() + "원"
                bindingDialog.workhour = dataObject.selectWorker.infowork[date]!!.hoursOfWork
            }
        }

        // Branch Selection
        binding.selectionBranch.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext()).create()
            val bindingDialog: DialogBranchSelectionBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.dialog_branch_selection,
                container,
                false
            )
            bindingDialog.dialogBranchSelection.setBackgroundResource(R.drawable.back_03)
            bindingDialog.staticText.setTextColor(Color.BLACK)
            mBranchAdapter = branchAdapter()
            val listView = bindingDialog.RvBranches
            listView.adapter = mBranchAdapter
            for (item: Branch in dataObject.listBranch) {
                mBranchAdapter.addItem(item)
            }
            dialog.setView(bindingDialog.root)
            dialog.show()
            listView.onItemClickListener = object : AdapterView.OnItemClickListener {
                override fun onItemClick(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    (activity as MainActivity).getBranches(position)
                    binding.currentBranch = dataObject.selectBranch.title
                    defaultSetting(dataObject.selectWorkerInfo, dataObject.selectWorker)
                    dialog.cancel()
                }
            }
        }
        return binding.root
    }

    // Marking current selection dates on Calender
    fun paintCalanderSchedule(dates: List<CalendarDay>) {
        binding.CvSchedule.clearSelection()
        if (dates.size != 0) {
            binding.CvSchedule.selectRange(dates[0], dates[dates.size - 1])
        }
    }

    fun defaultSetting(item: WorkerInfo, worker: Worker) {
        var timeTotal = getTimetotalByWeight(worker)
        var resultTotal = calculate(timeTotal, item.payment)
        binding.timeNormal = timeTotal[0].toString() + "h " + timeTotal[1].toString() + "m"
        binding.timeNight = timeTotal[2].toString() + "h " + timeTotal[3].toString() + "m"
        binding.timeFull = timeTotal[4].toString() + "h " + timeTotal[5].toString() + "m"
        binding.timeTotal = (timeTotal[0] + timeTotal[2] + timeTotal[4]).toString() + "h " + (timeTotal[1] + timeTotal[3] + timeTotal[5]).toString() + "m"
        binding.wageNormal = resultTotal[0].toString() + "원"
        binding.wageNight = resultTotal[1].toString() + "원"
        binding.wageFull = resultTotal[2].toString() + "원"
        binding.wageTotal = (resultTotal[0] + resultTotal[2] + resultTotal[1]).toString() + "원"
        binding.wage = item.payment.toString()
        paintCalander((activity as MainActivity), worker.infowork)
        paintCalanderSchedule(worker.datesWork)
    }


    fun setVariables(worker: Worker, item: WorkerView){
        var timeTotal = getTimetotalByWeight(worker)
        var resultTotal = calculate(timeTotal, item.wage)
        binding.timeNormal = timeTotal[0].toString() + "h " + timeTotal[1].toString() + "m"
        binding.timeNight = timeTotal[2].toString() + "h " + timeTotal[3].toString() + "m"
        binding.timeFull = timeTotal[4].toString() + "h " + timeTotal[5].toString() + "m"
        binding.timeTotal = (timeTotal[0] + timeTotal[2] + timeTotal[4]).toString() + "h " + (timeTotal[1] + timeTotal[3] + timeTotal[5]).toString() + "m"
        binding.wageNormal = resultTotal[0].toString() + "원"
        binding.wageNight = resultTotal[1].toString() + "원"
        binding.wageFull = resultTotal[2].toString() + "원"
        binding.wageTotal = (resultTotal[0] + resultTotal[2] + resultTotal[1]).toString() + "원"
        binding.wage = item.wage.toString()
    }

    fun paintDateAttend(activity: MainActivity, date : CalendarDay) {
        binding.CvAttendence.addDecorators(
            SelectGreenDecorator(
                activity,
                date
            )
        )
    }
    fun paintDateAbsence(activity: MainActivity, date : CalendarDay) {
        binding.CvAttendence.addDecorators(
            SelectRedDecorator(
                activity,
                date
            )
        )
    }
    // Marking current selection dates on Calender
    fun paintCalander(activity: MainActivity, works: HashMap<CalendarDay, Work>) {
        binding.CvAttendence.clearSelection()
        works.forEach { calendarDay, work ->
            if (work.attendence == 0) {
                paintDateAttend(activity, calendarDay)
            } else {
                paintDateAbsence(activity, calendarDay)
            }
        }
    }

    fun getTimetotalByWeight(worker : Worker) : ArrayList<Int> {
        var cal = Calendar.getInstance()
        var attend = 0
        var absence = 0
        var listTimeTotal = ArrayList<ArrayList<Int>>()
        var hourTotalNormal = 0
        var minTotalNormal= 0
        var hourTotalNight = 0
        var minTotalNight = 0
        for (i in 0..worker.infowork.size - 1) {
            if (worker.infowork[worker.datesWork[i]]!!.attendence == 0) {
                attend += 1
                var calendarDay = worker.datesWork[i]
                var work = worker.infowork[calendarDay]
                cal.set(calendarDay.year, calendarDay.month, calendarDay.day)
                var k = work!!.getTime()
                hourTotalNormal += k[0]
                minTotalNormal += k[1]
                hourTotalNight += k[2]
                minTotalNight += k[3]
            } else {
                absence += 1
            }
            // 4 == End day of week : Sunday
            if (cal.get(Calendar.DAY_OF_WEEK) == 4 || i == worker.infowork.size - 1) {
                if (minTotalNormal > 60) {
                    hourTotalNormal += minTotalNormal / 60
                    minTotalNormal %= 60
                }
                if (minTotalNight > 60) {
                    hourTotalNight += minTotalNight / 60
                    minTotalNight %= 60
                }
                listTimeTotal.add(arrayListOf(hourTotalNormal, minTotalNormal, hourTotalNight, minTotalNight))
                hourTotalNormal = 0
                minTotalNormal= 0
                hourTotalNight = 0
                minTotalNight = 0
            }
        }
        var result = arrayListOf(0, 0, 0, 0, 0, 0)
        for (i in 0..listTimeTotal.size - 1) {
            result[0] += listTimeTotal[i][0]
            result[1] += listTimeTotal[i][1]
            result[2] += listTimeTotal[i][2]
            result[3] += listTimeTotal[i][3]
            if (listTimeTotal[i][0] >= 15) {
                result[4] += listTimeTotal[i][0] - 15
                result[5] += listTimeTotal[i][1]
            } else {
                result[4] += 0
                result[5] += 0
            }
        }
        binding.attendence = attend.toString()
        binding.absence = absence.toString()
        return result
    }

    fun calculate(it: ArrayList<Int>, wage: Int): ArrayList<Int> {
        var wageNormal = 0
        var wageNight = 0
        var wageFulltime = 0
        wageNormal += (it[0] * 60 + it[1]) * wage / 60
        wageNight += (it[2] * 60 + it[3]) * wage / 60
        wageFulltime += (it[4] * 60 + it[5]) * wage / 60
        return arrayListOf(wageNormal, wageNight, wageFulltime)
    }
}