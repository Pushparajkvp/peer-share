package `in`.edu.pushparaj.activities

import `in`.edu.pushparaj.R
import android.Manifest
import android.app.ProgressDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.TextView
import com.squareup.moshi.Moshi
import io.ipfs.kotlin.IPFS
import okhttp3.OkHttpClient
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import java.io.File
import java.io.InputStream
import java.util.concurrent.TimeUnit

@RuntimePermissions
class PaymentActivity : AppCompatActivity() {

    val ipfs = IPFS( "http://127.0.0.1:5001/api/v0/",
            OkHttpClient.Builder().connectTimeout(600, TimeUnit.SECONDS).readTimeout(600, TimeUnit.SECONDS).writeTimeout(600, TimeUnit.SECONDS).build(),
            Moshi.Builder().build())

    var fileName:String = ""
    var fileHash:String = ""
    var accountNumber:String = ""
    var ifscCode:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        var bundle :Bundle ?=intent.extras
        fileHash = bundle!!.getString("fileHash")
        accountNumber = bundle.getString("accountNumber")
        ifscCode = bundle.getString("ifscCode")
        fileName = bundle.getString("fileName")

        findViewById<TextView>(R.id.accountNumber).text = accountNumber
        findViewById<TextView>(R.id.ifscCode).text = ifscCode

        val button = findViewById<Button>(R.id.submitButton)
        button.setOnClickListener{
            downloadFileWithPermissionCheck()
        }
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun downloadFile() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Downloading File..")
        progressDialog.setCancelable(false)
        progressDialog.show()
        Thread{

            val inputStream = ipfs.get.ipfs.callCmd("cat/$fileHash").byteStream()
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val externalFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),fileName)
            externalFolder.copyInputStreamToFile(inputStream)

            runOnUiThread{
                progressDialog.dismiss()
            }
        }.start()
    }
    fun File.copyInputStreamToFile(inputStream: InputStream) {
        inputStream.use { input ->
            this.outputStream().use { fileOut ->
                input.copyTo(fileOut)
            }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

}
