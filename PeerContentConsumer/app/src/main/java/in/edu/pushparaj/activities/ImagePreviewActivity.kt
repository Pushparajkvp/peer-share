package `in`.edu.pushparaj.activities

import `in`.edu.pushparaj.R
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.os.Environment
import android.view.WindowManager
import android.widget.ImageView
import it.sephiroth.android.library.imagezoom.ImageViewTouch
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase


class ImagePreviewActivity : AppCompatActivity() {

    private var fileName:String = ""
    private var imageView: ImageViewTouch? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        var bundle :Bundle ?=intent.extras
        fileName = bundle!!.getString("fileName")

        var path = getFilesDir().absolutePath+"/"+fileName;

        imageView = findViewById(R.id.imageView)
        imageView!!.setImageBitmap(BitmapFactory.decodeFile(path))
        imageView!!.displayType = ImageViewTouchBase.DisplayType.FIT_IF_BIGGER


    }
}
