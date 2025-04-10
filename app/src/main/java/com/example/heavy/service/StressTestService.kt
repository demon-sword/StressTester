package com.example.heavy.service

import android.R
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlin.math.sqrt


class StressTestService : Service() {
    private val cpuThreads: MutableList<Thread> = ArrayList()
    private val memoryList: MutableList<ByteArray> = ArrayList()

    private var cpuPercent = 70
    private var ramPercent = 50


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Stress Test Running")
            .setContentText("Stressing CPU and RAM")
            .setSmallIcon(R.drawable.stat_notify_sync)
            .build()

        startForeground(NOTIF_ID, notification)

        if (intent != null) {
            cpuPercent = intent.getIntExtra(EXTRA_CPU_PERCENT, 70)
            ramPercent = intent.getIntExtra(EXTRA_RAM_PERCENT, 50)
        }

//        startCpuStress(cpuPercent)
//        allocateMemory(ramPercent)

        return START_STICKY
    }

    private fun startCpuStress(percent: Int) {
        val availableCores = Runtime.getRuntime().availableProcessors()
        for (i in 0 until availableCores) {
            val thread = Thread {
                try {
                    while (!Thread.currentThread().isInterrupted) {
                        val busyTime = percent.toLong()
                        val idleTime = (100 - percent).toLong()
                        val startTime = System.currentTimeMillis()
                        while ((System.currentTimeMillis() - startTime) < busyTime) {
                            sqrt(Math.random())
                        }
                        Thread.sleep(idleTime)
                    }
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }
            thread.start()
            cpuThreads.add(thread)
        }
    }

    private fun allocateMemory(percent: Int) {
        val memInfo = ActivityManager.MemoryInfo()
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager?
        if (activityManager != null) {
            activityManager.getMemoryInfo(memInfo)
            val targetBytes = (memInfo.totalMem * (percent / 100.0)).toLong()
            try {
                while (allocatedMemory < targetBytes) {
                    memoryList.add(ByteArray(1024 * 1024)) // Add 1MB at a time
                }
            } catch (e: OutOfMemoryError) {
                Log.e("StressService", "Ran out of memory during allocation", e)
            }
        }
    }

    private val allocatedMemory: Long
        get() {
            var total: Long = 0
            for (arr in memoryList) {
                total += arr.size.toLong()
            }
            return total
        }

    override fun onDestroy() {
        for (thread in cpuThreads) {
            thread.interrupt()
        }
        cpuThreads.clear()
        memoryList.clear()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Stress Test",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager: NotificationManager = getSystemService(NotificationManager::class.java)
            if (manager != null) {
                manager.createNotificationChannel(channel)
            }
        }
    }

    companion object {
        private const val CHANNEL_ID = "StressTestChannel"
        private const val NOTIF_ID = 1

        const val EXTRA_CPU_PERCENT: String = "cpu_percent"
        const val EXTRA_RAM_PERCENT: String = "ram_percent"
    }
}
