
import com.squareup.moshi.Moshi
import io.ipfs.kotlin.IPFS
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import java.awt.EventQueue
import java.io.File
import java.util.concurrent.TimeUnit

val ipfs = IPFS( "http://127.0.0.1:5001/api/v0/",OkHttpClient.Builder().connectTimeout(600,TimeUnit.SECONDS).readTimeout(600,TimeUnit.SECONDS).writeTimeout(600,TimeUnit.SECONDS).build(),Moshi.Builder().build())

fun main(args : Array<String>) {

    WindowUtils.createAndShowGUI()
    Thread{
        WindowUtils.showProgressDialog(true,"Fetching Ledger Data","Fetching Ledger...")
    }.start()

    Thread{
        val ipfsPath : String? = ipfs.name.resolve("QmTaS8rizdm9g6EkdsyzNUQRpSVNcsDJHGJ3XS4u72yhDf")
        if(ipfsPath!=null){
            val hash:String = ipfsPath.split("/")[2]
            LedgerSingleton.LedgerData = JSONArray(ipfs.get.cat(hash))
            println(LedgerSingleton.LedgerData.toString())
        }
        WindowUtils.showProgressDialog(false,"","")
    }.start()
}

object LedgerSingleton{
    var LedgerData: JSONArray? = null
}
