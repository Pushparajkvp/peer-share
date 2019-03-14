package `in`.edu.pushparaj

import android.app.IntentService
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import `in`.edu.pushparaj.activities.StarPage
import android.app.NotificationChannel
import android.os.Build
import org.ligi.pushparaj.R


class IPFSDaemonService : IntentService("IPFSDaemonService") {

    private var nManager: NotificationManager? = null
    private var daemon: Process? = null
    internal var NOTIFICATION_ID = 12345

    override fun onHandleIntent(intent: Intent) {
        val exitIntent = Intent(this, IPFSDaemonService::class.java)
        exitIntent.action = "STOP"
        val pendingExit = PendingIntent.getService(this, 0, exitIntent, 0)

        val targetIntent = Intent(this, StarPage::class.java)
        val pIntent = PendingIntent.getActivity(this, 0, targetIntent, 0)
        val builder = NotificationCompat.Builder(this,"Chanelid").setOngoing(true)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle("IPFS Daemon")
                .setContentText("The daemon is running")
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "exit", pendingExit)

        builder.setContentIntent(pIntent)
        nManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel("Chanelid", "channename", NotificationManager.IMPORTANCE_HIGH)
            channel.description = "description"
            nManager!!.createNotificationChannel(channel)
        }
        startForeground(NOTIFICATION_ID,builder.build())
        try {
            daemon = IPFSDaemon(baseContext).run("daemon")
            State.isDaemonRunning = true
            daemon!!.waitFor()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        daemon!!.destroy()
        super.onDestroy()
        State.isDaemonRunning = false
        nManager?.cancel(NOTIFICATION_ID)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val action = intent.action
        if (nManager != null && action != null && action == "STOP") {
            stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

}
