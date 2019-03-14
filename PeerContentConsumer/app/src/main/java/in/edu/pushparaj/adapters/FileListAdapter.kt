package `in`.edu.pushparaj.adapters

import android.app.Activity
import android.content.Intent
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.squareup.moshi.Moshi
import io.ipfs.kotlin.IPFS
import okhttp3.OkHttpClient
import `in`.edu.pushparaj.EncryptionUtils
import org.ligi.pushparaj.R
import `in`.edu.pushparaj.activities.FilePreviewActivity
import `in`.edu.pushparaj.dialogs.PaymentDialog
import `in`.edu.pushparaj.model.FileModel
import java.io.File
import java.io.InputStream
import java.util.concurrent.TimeUnit

class FileListAdapter(val items : ArrayList<FileModel>, val context: Activity) : RecyclerView.Adapter<ViewHolder>() {

    val ipfs = IPFS( "http://127.0.0.1:5001/api/v0/",
            OkHttpClient.Builder().connectTimeout(600, TimeUnit.SECONDS).readTimeout(600, TimeUnit.SECONDS).writeTimeout(600, TimeUnit.SECONDS).build(),
            Moshi.Builder().build())

    var dialog : AlertDialog? = null
    init {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)
        builder.setView(R.layout.progress_dialog)
        dialog = builder.create()

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.file_list_row, parent, false))
    }
    fun notify( positon:Int){
        notifyItemChanged(positon)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rowView = holder.holderView
        val fileModel = items[position]

        rowView.setOnClickListener{
            when(fileModel.status){

                1->{
                    PaymentDialog(context, fileModel.accoutNumber, fileModel.ifscCode, position).showDialog()
                }
                2->{
                    downloadFile(fileModel.fileHash,fileModel.fileName,position)
                }
                3->{
                    decryptFile(fileModel.fileName,fileModel.password,position)
                }
                4->{
                    val intent = Intent(context, FilePreviewActivity::class.java)
                    intent.putExtra("fileName", fileModel.fileName)
                    context.startActivity(intent)
                }

            }

            notify(position)
        }

        val fileNameTextView = rowView!!.findViewById<TextView>(R.id.fileName)
        val fileDescriptionTextView = rowView.findViewById<TextView>(R.id.fileDescription)
        val fileSizeTextView = rowView.findViewById<TextView>(R.id.fileSize)
        val filePriceTextView = rowView.findViewById<TextView>(R.id.filePrice)
        val fileStatusImage = rowView.findViewById<ImageView>(R.id.actionIcon)

        fileStatusImage.setImageResource(getImageSource(fileModel.status))
        fileNameTextView.text = fileModel.fileName
        fileDescriptionTextView.text = fileModel.fileDescription
        fileSizeTextView.text =  fileModel.fileSize.toString()+" bytes"
        filePriceTextView.text = "Rs "+fileModel.filePrice.toString()
    }

    private fun getImageSource(status: Int): Int {
        when(status){
            1-> return R.drawable.dollar
            2-> return R.drawable.download
            3-> return R.drawable.lock
            4-> return R.drawable.open
        }
        return 0
    }

    fun decryptFile(fileName: String,password:String,position: Int){
        showProgressDialog("Decrypting")
        Thread{
            EncryptionUtils.Decrypt(File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),fileName),password);
            context.runOnUiThread{
                changeStatus(position,4)
                hideProgressDialog()
            }
        }.start()

    }
    fun downloadFile(fileHash:String,fileName:String,position: Int) {
        showProgressDialog("Downloading")
        Thread{
            val inputStream = ipfs.get.ipfs.callCmd("cat/$fileHash").byteStream()
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val externalFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),fileName)
            externalFolder.copyInputStreamToFile(inputStream)
            context.runOnUiThread{
                changeStatus(position,3)
                hideProgressDialog()
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

    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        return items.size
    }
    fun isFileAvailable(fileName:String) : Boolean{
        var path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath+"/"+fileName;
        return File(path).exists()
    }

    fun changeStatus(position: Int, status: Int) {
        items[position].status = status
        val localFileStatus = EncryptionUtils.getListOfFiles()
        val fileStatus = localFileStatus.find { s->s.fileHash.equals(items[position].fileHash)}
        if(fileStatus !=null)
            fileStatus.status = items[position].status
        EncryptionUtils.saveListOfFiles(localFileStatus)

        notifyItemChanged(position)
    }
    fun showProgressDialog(text:String){
        dialog!!.show()
        dialog!!.findViewById<TextView>(R.id.progressText)!!.text = text;
    }

    fun hideProgressDialog(){
        dialog!!.dismiss()
    }

}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    val holderView = view
}