
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.PBEParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.io.ByteArrayOutputStream




object EncryptionUtils {

    fun Encrypt(file:File,password:String) : File{
        val inFile = FileInputStream(file)

        val outFile = FileOutputStream(File(stripExtension(file.name)+".des"))

        val pbeKeySpec = PBEKeySpec(password.toCharArray())
        val secretKeyFactory = SecretKeyFactory
            .getInstance("PBEwithMD5ANDDES")
        val secretKey = secretKeyFactory.generateSecret(pbeKeySpec)

        val salt = byteArrayOf(10,20,30,40,50,60,70,80)

        val pbeParameterSpec = PBEParameterSpec(salt, 100)
        val cipher = Cipher.getInstance("PBEwithMD5ANDDES")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, pbeParameterSpec)
        outFile.write(salt)

        val input = ByteArray(64)
        var bytesRead: Int

        while(true) {
            bytesRead = inFile.read(input)
            if(bytesRead!=-1) {
                val output = cipher.update(input, 0, bytesRead)
                if (output != null)
                    outFile.write(output)
            }else
                break
        }

        val output = cipher.doFinal()
        if (output != null)
            outFile.write(output)

        inFile.close()
        outFile.flush()
        outFile.close()
        return File(stripExtension(file.name)+".des")
    }

    fun Decrypt(file:File,password: String){

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

        val fos = FileOutputStream(File("test.json"))

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
    }
    fun stripExtension(str: String?): String? {
        // Handle null case specially.

        if (str == null) return null

        // Get position of last '.'.

        val pos = str.lastIndexOf(".")

        // If there wasn't any '.' just return the string as is.

        return if (pos == -1) str else str.substring(0, pos)

        // Otherwise return the string, up to the dot.

    }
}