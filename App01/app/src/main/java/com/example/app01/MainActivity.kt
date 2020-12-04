package com.example.app01

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.app01.DB.retrofitAPI
import com.example.app01.databinding.ActivityMainBinding
import com.example.app01.dto.branch.Branch
import com.example.app01.dto.User
import com.example.app01.dto.worker.Work
import com.example.app01.dto.worker.Worker
import com.example.app01.dto.workerdetail.WorkerDetail
import com.example.app01.dto.worker.WorkerInfo
import com.example.app01.dto.workerview.WorkerView
import com.prolificinteractive.materialcalendarview.CalendarDay
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {
    private lateinit var mRetrofit: Retrofit
    private lateinit var mRetrofitAPI: retrofitAPI
    private lateinit var binding : ActivityMainBinding
    var mBackWait:Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initServer()
        binding  = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val navController = this.findNavController(R.id.myNavHostFragment)

    }

    override fun onBackPressed() {
        alertToast("Press back button one more if you want to terminate app.")
        if(System.currentTimeMillis() - mBackWait >=2000 ) {
            mBackWait = System.currentTimeMillis()
        } else {
            finish()
        }
    }

    fun getCurrentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)
    fun getCurrentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1
    fun getCurrentDay(): Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    fun getCurrentHour(): Int = Calendar.getInstance().get(Calendar.HOUR)
    fun getCurrentMinute(): Int = Calendar.getInstance().get(Calendar.MINUTE)
    // Server Default Setting
    fun initServer() {
        mRetrofit = Retrofit.Builder().baseUrl("http://222.106.121.250:8080")
            .addConverterFactory(GsonConverterFactory.create()).build()
        mRetrofitAPI = mRetrofit.create(retrofitAPI::class.java)
    }
    fun getRetrofitAPI() : retrofitAPI {
        return mRetrofitAPI
    }
    fun alertToast(massege : String) {
        Toast.makeText(applicationContext, massege, Toast.LENGTH_SHORT).show()
    }
    fun getBranchesByIdBoss(id_boss : Int) {
        var getBranches = Thread(Runnable {
            dataObject.listBranch = (mRetrofitAPI.searchBranchByIdBoss(id_boss).execute().body() as ArrayList<Branch>?)!!
        })
        getBranches.start()
        getBranches.join()
        if (dataObject.listBranch.size == 0) {
            var newBranch : Branch = Branch()
            newBranch.id_boss = dataObject.selectUser.id
            newBranch.title = "New Branch"
            createBranch(newBranch)
        }
        dataObject.selectBranch = dataObject.listBranch[0]
    }
    // Create Branch
    fun createBranch(newBranch : Branch) : Boolean {
        var success = false
        var duplication = false
        var createBranch = Thread(Runnable {
            success = mRetrofitAPI.createBranch(newBranch).execute().body()!!
        })
        if (dataObject.listBranch.size != 0) {
            dataObject.listBranch.forEach {
                if (it.title == newBranch.title) duplication = true
            }
            if (duplication) {
                alertToast("Title of Branch is duplicated. Please check again.")
                return false
            } else {
                createBranch.start()
                createBranch.join()
                var id_b = 0
                var thread = Thread(Runnable {
                    id_b = mRetrofitAPI.getIdBranch(newBranch.title, newBranch.id_boss).execute().body()!!
                })
                thread.start()
                thread.join()
                newBranch.id = id_b
                var newWorkerInfo = WorkerInfo()
                newWorkerInfo.id_worker = dataObject.selectUser.id
                createWorker(newWorkerInfo, id_b)
                dataObject.listBranch.add(newBranch)
                return true
            }
        } else {
            dataObject.listBranch.add(newBranch)
            createBranch.start()
            createBranch.join()
        }
        return success
    }
    fun modifyBranch(newBranch : Branch) : Boolean {
        var success = false
        var modifyBranch = Thread(Runnable {
            success = mRetrofitAPI.modifyBranch(newBranch).execute().body()!!
        })
        modifyBranch.start()
        modifyBranch.join()
        return success
    }
    fun deleteBranch(target : Branch) : Boolean {
        var success = false
        var deleteBranch = Thread(Runnable {
            success = mRetrofitAPI.deleteBranch(target.id).execute().body()!!
        })
        deleteBranch.start()
        deleteBranch.join()
        return success
    }
    fun getWorkerViewesByIdBranch(id_branch : Int) {
        var list = ArrayList<WorkerView>()
        var getViews = Thread (Runnable {
            list = (mRetrofitAPI.getWorkerViewByIdBranch(id_branch).execute().body() as ArrayList<WorkerView>?)!!
        })
        getViews.start()
        getViews.join()
        if (list.size == 0) {
            var newWorkerInfo = WorkerInfo()
            newWorkerInfo.id_worker = dataObject.selectUser.id
            createWorker(newWorkerInfo, id_branch)
            var getViews2 = Thread (Runnable {
                list = (mRetrofitAPI.getWorkerViewByIdBranch(id_branch).execute().body() as ArrayList<WorkerView>?)!!
            })
            getViews2.start()
            getViews2.join()
        }
        dataObject.listWorkerView = list
    }
    fun createWorker(newWorkerInfo: WorkerInfo, id_branch: Int) : Boolean {
        var success = false
        var id = id_branch
        if (id == 0) {
            var th = Thread(Runnable {
                id = mRetrofitAPI.getIdBranch(dataObject.selectBranch.title, dataObject.selectUser.id).execute().body()!!
            })
            th.start()
            th.join()
            dataObject.selectBranch.id = id
        }
        var thread = Thread(Runnable {
            success = mRetrofitAPI.createWorkerInfo(newWorkerInfo, id).execute().body()!!
        })
        thread.start()
        thread.join()
        var detail = WorkerDetail()
        detail.id_workerInfo = getWorkerInfo(newWorkerInfo.id_worker, id).id
        createWorkerDetail(detail)
        var newWorkerView = WorkerView()
        newWorkerView.id = newWorkerInfo.id_worker
        newWorkerView.wage = newWorkerInfo.payment
        newWorkerView.name = dataObject.selectUser.name
        newWorkerView.age = dataObject.selectUser.age
        if (dataObject.listWorkerView.size != 1) {
            dataObject.listWorkerView.add(newWorkerView)
        }
        return success
    }
    fun searchUserByAccount(account : String) : User {
        var result = User()
        var search = Thread(Runnable {
            result = mRetrofitAPI.searchUserByAccount(account).execute().body()!!
        })
        search.start()
        search.join()
        return result
    }
    fun getAccountById(id_worker : Int) : String {
        var result = ""
        var thread = Thread(Runnable {
            result = mRetrofitAPI.getAccountById(id_worker).execute().body()!!
        })
        return result
    }
    fun getWorkerInfo(id_worker: Int, id_branch: Int) : WorkerInfo {
        var result = WorkerInfo()
        var thread = Thread(Runnable {
            result = mRetrofitAPI.searchWorkerInfoByIdWorker(id_worker, id_branch).execute().body()!!
        })
        thread.start()
        thread.join()
        return result
    }
    fun modifyWorkerInfo(workerInfo: WorkerInfo) : Boolean {
        var result = false
        var thread = Thread(Runnable {
            result = mRetrofitAPI.modifyWorkerInfo(workerInfo).execute().body()!!
        })
        thread.start()
        thread.join()
        var target = getWorksByIdWorkerInfo(workerInfo.id)
        target.forEach { calendarDay, work ->
            work.payment = workerInfo.payment
            modifyWork(work)
        }
        return result
    }
    fun deleteWorkerInfo(id_worker: Int, id_branch: Int) : Boolean {
        var result = false
        var thread = Thread(Runnable {
            result = mRetrofitAPI.deleteWorkerInfo(id_worker, id_branch).execute().body()!!
        })
        thread.start()
        thread.join()
        return result
    }
    fun createWork(workerInfo : WorkerInfo, dates : List<CalendarDay>) : Boolean {
        var result = false
        for (date in dates) {
            var work = Work()
            work.setFromWorkerInfo(workerInfo)
            var mon = ""
            var da = ""
            if (date.month < 10) mon = "0" + date.month else mon = date.month.toString()
            if (date.day < 10) da = "0" + date.day else da = date.day.toString()
            work.dateWork = date.year.toString() + "-" + mon + "-" + da
            work.calculate()
            var thread = Thread(Runnable {
                result = mRetrofitAPI.createWorkByIdWorkerInfo(work).execute().body()!!
            })
            thread.start()
            thread.join()
        }
        return result
    }
    fun deleteWorkByIdWorkInfo(id_workerInfo : Int) : Boolean {
        var result = false
        var thread = Thread (Runnable {
            result = mRetrofitAPI.deleteWorkByIdWorkerInfo(id_workerInfo).execute().body()!!
        })
        thread.start()
        thread.join()
        return result
    }
    fun getWorksByIdWorkerInfo(id_workerInfo: Int) : HashMap<CalendarDay, Work> {
        var result = LinkedHashMap<CalendarDay, Work>()
        var temp = listOf<Work>()
        var thread = Thread (Runnable {
            temp = mRetrofitAPI.getWorkByIdWorkerInfo(id_workerInfo).execute().body()!!
        })
        thread.start()
        thread.join()
        temp.forEach {
            var cut = it.dateWork.split("-")
            var calendarDay = CalendarDay.from(cut[0].toInt(), cut[1].toInt(), cut[2].toInt())
            result[calendarDay] = it
        }
        return result
    }
    fun modifyWork(work : Work) : Boolean {
        var result = false
        var thread = Thread(Runnable {
            result = mRetrofitAPI.modifyWork(work).execute().body()!!
        })
        thread.start()
        thread.join()
        return result
    }
    // Have to be used when listWorkerview is existed
    fun getWorkersByIdBranch(id_branch: Int) : ArrayList<Worker> {
        var list = listOf<WorkerInfo>()
        var list2 = ArrayList<Worker>()
        var thread = Thread(Runnable {
            list = mRetrofitAPI.getWorkersByIdBranch(id_branch).execute().body()!!
        })
        thread.start()
        thread.join()
        for (workerInfo in list) {
            for (it in dataObject.listWorkerView) {
                if (it.id == workerInfo.id_worker) {
                    var newWorker = Worker()
                    newWorker.setWorker(workerInfo, it)
                    list2.add(newWorker)
                    break
                }
            }
        }
        for (worker in list2) {
            worker.infowork = getWorksByIdWorkerInfo(worker.id_workerInfo)
            worker.datesWork = worker.infowork.keys.toMutableList()
        }
        return list2
    }
    fun createWorkerDetail(item : WorkerDetail) {
        var thread = Thread(Runnable {
            mRetrofitAPI.createWorkerDetail(item).execute().body()!!
        })
        thread.start()
        thread.join()
    }
    fun modifyWorkerDetail(item : WorkerDetail) {
        var thread = Thread(Runnable {
            mRetrofitAPI.modifyWorkerDetail(item).execute().body()!!
        })
        thread.start()
        thread.join()
    }
    fun getWorkerDetailByIdWorkerInfo(id_workerInfo: Int) : WorkerDetail {
        var detail = WorkerDetail()
        var thread = Thread(Runnable {
            detail = mRetrofitAPI.getWorkerDetailByIdWorkerInfo(id_workerInfo).execute().body()!!
        })
        thread.start()
        thread.join()
        return detail
    }
    // After Setting selectBranch
    fun getWorkersAndDetails() {
        getWorkerViewesByIdBranch(dataObject.selectBranch.id)
        dataObject.listWorker = getWorkersByIdBranch(dataObject.selectBranch.id)
        for (i : Int in 0..dataObject.listWorker.size-1) {
            dataObject.listWorkerDetail.add(getWorkerDetailByIdWorkerInfo(dataObject.listWorker[i].id_workerInfo))
            dataObject.listWorker[i].infowork = getWorksByIdWorkerInfo(dataObject.listWorker[i].id_workerInfo)
        }
    }
    fun getWorkerInfoByIdWorker(id_worker: Int) : ArrayList<WorkerInfo> {
        var result = ArrayList<WorkerInfo>()
        var thread = Thread(Runnable {
            result = (mRetrofitAPI.getWorkerInfoByIdWorker(id_worker).execute().body() as ArrayList<WorkerInfo>?)!!
        })
        thread.start()
        thread.join()
        return result
    }
    fun getBranchByIdBranch(id_branch : Int) : Branch {
        var result = Branch()
        var thread = Thread(Runnable {
            result = mRetrofitAPI.getBranchByIdBranch(id_branch).execute().body()!!
        })
        thread.start()
        thread.join()
        return result
    }
    fun getBranches(position : Int) {
        dataObject.selectBranch = dataObject.listBranch[position]
        dataObject.selectWorkerInfo = dataObject.listWorkerInfo[position]
        var options = getWorkersByIdBranch(dataObject.selectBranch.id)
        options.forEach {
            if (it.id_workerInfo == dataObject.selectWorkerInfo.id) {
                dataObject.selectWorker = it
            }
        }
        dataObject.selectWorker.infowork = getWorksByIdWorkerInfo(dataObject.selectWorkerInfo.id)
        dataObject.selectWorker.datesWork = dataObject.selectWorker.infowork.keys.toMutableList()
    }


}