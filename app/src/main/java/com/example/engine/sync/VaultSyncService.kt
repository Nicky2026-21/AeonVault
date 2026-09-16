package com.example.engine.sync

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.data.local.AppDatabase

class VaultSyncService : Service() {
    private lateinit var syncManager: VaultSyncManager

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getDatabase(applicationContext)
        syncManager = VaultSyncManager(applicationContext, db.vaultDao())
        syncManager.startBackgroundSync()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        syncManager.triggerImmediateSync()
        return START_STICKY
    }

    override fun onDestroy() {
        syncManager.stopBackgroundSync()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
