package com.xayah.databackup.fragment.backup

import android.content.Intent
import android.view.View
import androidx.appcompat.widget.ListPopupWindow
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.xayah.databackup.App
import com.xayah.databackup.activity.list.AppListActivity
import com.xayah.databackup.activity.processing.ProcessingActivity
import com.xayah.databackup.data.AppInfoBackup
import com.xayah.databackup.data.AppInfoBaseNum
import com.xayah.databackup.data.MediaInfo
import com.xayah.databackup.util.*
import com.xayah.databackup.view.fastInitialize
import kotlinx.coroutines.launch

class BackupViewModel : ViewModel() {
    val _isInitialized by lazy {
        MutableLiveData(false)
    }
    private var isInitialized
        get() = _isInitialized.value!!
        set(value) = _isInitialized.postValue(value)

    // 是否第一次进入Fragment
    private val _isFirst by lazy {
        MutableLiveData(true)
    }
    private var isFirst
        get() = _isFirst.value!!
        set(value) = _isFirst.postValue(value)

    var lazyChipGroup = ObservableBoolean(true)
    var lazyList = ObservableBoolean(true)

    // 应用备份列表
    private val _appInfoBackupList by lazy {
        MutableLiveData(mutableListOf<AppInfoBackup>())
    }
    private var appInfoBackupList
        get() = _appInfoBackupList.value!!.filter { it.infoBase.app || it.infoBase.data }
            .toMutableList()
        set(value) = _appInfoBackupList.postValue(value)
    private val appInfoBackupListNum: LiveData<AppInfoBaseNum> =
        Transformations.map(_appInfoBackupList) { appInfoBackupList ->
            val appInfoBaseNum = AppInfoBaseNum(0, 0)
            for (i in appInfoBackupList) {
                if (i.infoBase.app) appInfoBaseNum.appNum++
                if (i.infoBase.data) appInfoBaseNum.dataNum++
            }
            appInfoBaseNum
        }
    val appNum: LiveData<String> =
        Transformations.map(appInfoBackupListNum) { num -> num.appNum.toString() }
    val dataNum: LiveData<String> =
        Transformations.map(appInfoBackupListNum) { num -> num.dataNum.toString() }

    // 媒体备份列表
    private val _mediaInfoBackupList by lazy {
        MutableLiveData(mutableListOf<MediaInfo>())
    }
    var mediaInfoBackupList
        get() = _mediaInfoBackupList.value!!
        set(value) = _mediaInfoBackupList.postValue(value)

    var backupUser = ObservableField("${GlobalString.user}0")
    var restoreUser = ObservableField("${GlobalString.user}0")

    var backupItselfEnable = ObservableBoolean(false)
    var backupIconEnable = ObservableBoolean(false)
    var onResume = {}

    init {
        onResume = {
            if (isFirst) {
                isFirst = false
            } else {
                isInitialized = false
                lazyChipGroup.set(true)
                lazyList.set(true)
            }
        }
    }

    fun onChangeBackupUser(v: View) {
        viewModelScope.launch {
            val context = v.context
            var items =
                if (Bashrc.listUsers().first) Bashrc.listUsers().second else mutableListOf(
                    "0"
                )
            // 加入备份目录用户集
            items.addAll(Command.listBackupUsers())
            // 去重排序
            items = items.toSortedSet().toMutableList()
            val choice = items.indexOf(App.globalContext.readBackupUser())

            ListPopupWindow(context).apply {
                fastInitialize(v, items.toTypedArray(), choice)
                setOnItemClickListener { _, _, position, _ ->
                    dismiss()
                    context.saveBackupUser(items[position])
                    backupUser.set("${GlobalString.user}${items[position]}")
                    isInitialized = false
                }
                show()
            }
        }
    }

    fun onChangeRestoreUser(v: View) {
        viewModelScope.launch {
            val context = v.context
            val items =
                if (Bashrc.listUsers().first) Bashrc.listUsers().second.toTypedArray() else arrayOf(
                    "0"
                )
            val choice = items.indexOf(App.globalContext.readRestoreUser())

            ListPopupWindow(context).apply {
                fastInitialize(v, items, choice)
                setOnItemClickListener { _, _, position, _ ->
                    dismiss()
                    context.saveRestoreUser(items[position])
                    restoreUser.set("${GlobalString.user}${items[position]}")
                }
                show()
            }
        }
    }

    fun onSelectAppBtnClick(v: View) {
        v.context.startActivity(Intent(v.context, AppListActivity::class.java).apply {
            putExtra("isRestore", false)
        })
    }

    fun onBackupAppBtnClick(v: View) {
        v.context.startActivity(Intent(v.context, ProcessingActivity::class.java).apply {
            putExtra("isRestore", false)
            putExtra("isMedia", false)
        })
    }

    fun onBackupMediaBtnClick(v: View) {
        v.context.startActivity(
            Intent(v.context, ProcessingActivity::class.java).apply {
                putExtra("isRestore", false)
                putExtra("isMedia", true)
            }
        )
    }

    private fun setUser() {
        backupUser.set("${GlobalString.user}${App.globalContext.readBackupUser()}")
        restoreUser.set("${GlobalString.user}${App.globalContext.readRestoreUser()}")
    }

    private fun setBackupItselfCard() {
        backupItselfEnable.set(App.globalContext.readIsBackupItself())
    }

    fun onBackupItselfEnableCheckedChanged(v: View, checked: Boolean) {
        backupItselfEnable.set(checked)
        App.globalContext.saveIsBackupItself(backupItselfEnable.get())
    }

    fun onBackupIconEnableCheckedChanged(v: View, checked: Boolean) {
        backupIconEnable.set(checked)
        App.globalContext.saveIsBackupIcon(backupIconEnable.get())
    }

    private fun setBackupIconCard() {
        backupIconEnable.set(App.globalContext.readIsBackupIcon())
    }

    private suspend fun loadAllList() {
        appInfoBackupList = Loader.loadAppInfoBackupList()
        mediaInfoBackupList = Loader.loadMediaInfoBackupList()
    }

    suspend fun refresh() {
        // 加载列表
        setUser()
        setBackupItselfCard()
        setBackupIconCard()
        loadAllList()
        lazyList.set(false)
        isInitialized = true
    }
}