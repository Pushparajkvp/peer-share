package `in`.edu.pushparaj

import android.os.Environment
import java.io.File
import org.json.JSONArray
import org.json.JSONObject
import `in`.edu.pushparaj.model.FileStatusModel
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Context.MODE_WORLD_READABLE
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.PBEParameterSpec
import kotlin.collections.ArrayList


object EncryptionUtils {

    fun Decrypt(file:File,password: String,context:Context){

        val pbeKeySpec = PBEKeySpec(password.toCharArray())
        val secretKeyFactory = SecretKeyFactory
                .getInstance("PBEwithMD5ANDDES")
        val secretKey = secretKeyFactory.generateSecret(pbeKeySpec)

        val fis = FileInputStream(file)
        val salt = ByteArray(8)
        fis.read(salt)

        val pbeParameterSpec = PBEParameterSpec(salt, 100)

        val cipher = Cipher.getInstance("PBEwithMD5ANDDES")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, pbeParameterSpec)

        val fos = context.openFileOutput(file.name, MODE_PRIVATE);

        val `in` = ByteArray(64)
        var read: Int
        while (true) {
            read = fis.read(`in`)
            if(read!=-1){
                val output = cipher.update(`in`, 0, read)
                if (output != null)
                    fos.write(output)
            }else
                break

        }

        val output = cipher.doFinal()
        if (output != null)
            fos.write(output)

        fis.close()
        fos.flush()
        fos.close()
        val path =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
    }


    fun getListOfFiles() : ArrayList<FileStatusModel>{
        val localStatus = App.preferences.getString("filesStatus","")
        val localList = ArrayList<FileStatusModel>()

        var localArray: JSONArray = JSONArray()
        if(!localStatus.equals("")){
            localArray = JSONArray(localStatus)
        }
        for(i in 0..(localArray.length()-1)){
            val jsonObject = JSONObject(localArray[i].toString())
            localList.add(FileStatusModel(jsonObject.getString("fileHash"), jsonObject.getInt("fileStatus")))
        }
        return  localList
    }
    fun saveListOfFiles(filesList :ArrayList<FileStatusModel>){
        val arr : JSONArray = JSONArray()

        for (i in 0..filesList.size - 1){
            val jsonObject = JSONObject()
            jsonObject.put("fileHash",filesList[i].fileHash)
            jsonObject.put("fileStatus",filesList[i].status)
            arr.put(jsonObject)
        }
        val editor= App.preferences.edit()
        editor.putString("filesStatus",arr.toString())
        editor.commit()
    }
}