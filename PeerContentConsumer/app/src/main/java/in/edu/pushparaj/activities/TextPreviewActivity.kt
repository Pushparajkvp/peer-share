package `in`.edu.pushparaj.activities

import `in`.edu.pushparaj.R
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.annotation.RequiresApi
import android.view.WindowManager
import android.webkit.WebView
import android.widget.Toast
import java.nio.file.Files
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException


class TextPreviewActivity : AppCompatActivity() {

    private var fileName:String = ""
    private var webView: WebView? = null
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_preview)

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        var bundle :Bundle ?=intent.extras
        fileName = bundle!!.getString("fileName")

        var path = getFilesDir().absolutePath+"/"+fileName;
        val file = File(path)
        val text = StringBuilder("<html><body>")

        try {
            val br = BufferedReader(FileReader(file))
            var line: String?
            while (true) {
                line = br.readLine()
                if(line!=null){
                    text.append(line)
                    text.append('\n')
                }else
                    break

            }

            br.close()
        } catch (e: IOException) {
            Toast.makeText(this,"IOException",Toast.LENGTH_LONG).show()
        }
        text.append("</body></html>")
        webView = findViewById(R.id.webView)
        webView!!.loadData(text.toString(), "text/html", "UTF-8");
    }
}
