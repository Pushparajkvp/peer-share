package `in`.edu.pushparaj.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.squareup.moshi.Moshi
import io.ipfs.kotlin.IPFS
import io.ipfs.kotlin.model.VersionInfo
import okhttp3.OkHttpClient
import `in`.edu.pushparaj.EncryptionUtils
import `in`.edu.pushparaj.IPFSDaemon
import `in`.edu.pushparaj.IPFSDaemonService
import `in`.edu.pushparaj.State
import org.json.JSONArray
import org.json.JSONObject
import org.ligi.pushparaj.*
import `in`.edu.pushparaj.adapters.FileListAdapter
import `in`.edu.pushparaj.model.FileModel
import `in`.edu.pushparaj.model.FileStatusModel
import org.ligi.pushparaj.activities.onRequestPermissionsResult
import org.ligi.pushparaj.activities.testWithPermissionCheck

import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import java.util.concurrent.TimeUnit

@RuntimePermissions
class StarPage : AppCompatActivity(){



    private val ipfsDaemon = IPFSDaemon(this)

    val ipfs = IPFS( "http://127.0.0.1:5001/api/v0/",
            OkHttpClient.Builder().connectTimeout(600, TimeUnit.SECONDS).readTimeout(600, TimeUnit.SECONDS).writeTimeout(600, TimeUnit.SECONDS).build(),
            Moshi.Builder().build())

    val filesData: ArrayList<FileModel> = ArrayList()
    var filesAdapter: FileListAdapter? = null
    var recyclerView : RecyclerView? = null
    var dialog : AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_star_page)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView!!.layoutManager = LinearLayoutManager(this)

        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setView(R.layout.progress_dialog)
        dialog = builder.create()


        if(!ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE).equals(PackageManager.PERMISSION_GRANTED)
           || !ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE).equals(PackageManager.PERMISSION_GRANTED)){
            testWithPermissionCheck()
            return
        }


        if(!ipfsDaemon.isReady()){
            ipfsDaemon.download(this, runInit = true) {
                ipfsDaemon.getVersionFile().writeText(assets.open("version").reader().readText())
                startDaemon()
            }
        }else {
            startDaemon()

        }


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun test(){
        if(!ipfsDaemon.isReady()){
            ipfsDaemon.download(this, runInit = true) {
                ipfsDaemon.getVersionFile().writeText(assets.open("version").reader().readText())
                startDaemon()
            }
        }else {
            startDaemon()

        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.sync_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.sync -> {
                syncLedger()
            }
        }
        return super.onOptionsItemSelected(item)

    }
    fun startDaemon(){
        if(!State.isDaemonRunning){
            startService(Intent(this, IPFSDaemonService::class.java))
            showProgressDialog("Starting Daemon")

            Thread(Runnable {
                var version: VersionInfo? = null
                while (version == null) {
                    try {
                        version = ipfs.info.version()
                        version?.let { ipfsDaemon.getVersionFile().writeText(it.Version) }
                    } catch (ignored: Exception) {
                    }
                }

                runOnUiThread{
                    hideProgressDialog()
                    syncLedger()
                }
            }).start()
        }
    }
    fun syncLedger(){
        showProgressDialog("Fetching ledger")
        Thread{
            val ipfsPath : String? = ipfs.name.resolve("QmTaS8rizdm9g6EkdsyzNUQRpSVNcsDJHGJ3XS4u72yhDf")
            if(ipfsPath!=null){
                val hash:String = ipfsPath.split("/")[2]
                val jsonLedger = JSONArray(ipfs.get.cat(hash))
                Log.i("pushpa",jsonLedger.toString())
                filesData.clear()

                for (i in 0..(jsonLedger.length() - 1)) {
                    val jsonObject = JSONObject(jsonLedger[i].toString())

                    filesData.add(FileModel(jsonObject.getString("FileDescription"),
                            jsonObject.getString("FilePrice"),
                            jsonObject.getString("FileName"),
                            jsonObject.getString("FileHash"),
                            jsonObject.getString("AccountNumber"),
                            jsonObject.getString("IFSC"),
                            jsonObject.getDouble("FileSize"), "test", 1))


                }
                val localFileStatus = EncryptionUtils.getListOfFiles()
                for(i in 0..filesData.size-1){
                    val fileStatus: FileStatusModel? = localFileStatus.find { s-> s.fileHash.equals(filesData[i].fileHash) }
                    if(fileStatus==null){
                        localFileStatus.add(FileStatusModel(filesData[i].fileHash, 1))
                    }else{
                        filesData[i].status = fileStatus.status
                    }
                }
                EncryptionUtils.saveListOfFiles(localFileStatus)
            }
            runOnUiThread{
                hideProgressDialog()
                refreshUI()
            }
        }.start()
    }
    fun refreshUI(){
        if (filesAdapter == null) {
            filesAdapter = FileListAdapter(filesData, this)
            recyclerView!!.adapter = filesAdapter
        } else {
            filesAdapter!!.notifyDataSetChanged()
        }
    }
    fun notifyDataSetAt(position:Int,status:Int){
        filesAdapter!!.changeStatus(position,status)
    }
    fun showProgressDialog(text:String){
        dialog!!.show()
        dialog!!.findViewById<TextView>(R.id.progressText)!!.text = text;
    }
    fun hideProgressDialog(){
        dialog!!.dismiss()
    }
}
