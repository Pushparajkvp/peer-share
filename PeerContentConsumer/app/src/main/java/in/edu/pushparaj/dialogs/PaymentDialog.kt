package `in`.edu.pushparaj.dialogs

import android.app.Activity
import android.app.Dialog
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import org.ligi.pushparaj.R
import `in`.edu.pushparaj.activities.StarPage


class PaymentDialog(val context: Activity,val accountNumber:String,val ifscCode:String,val position:Int): Dialog(context), View.OnClickListener {

    var dialogs = Dialog(context)
    override fun onClick(v: View?) {
        val mainPage : StarPage = context as StarPage
        mainPage.notifyDataSetAt(position,2)
        dialogs.dismiss()
    }


     fun showDialog() {
         dialogs.requestWindowFeature(Window.FEATURE_NO_TITLE)
         dialogs.setCancelable(true)
         dialogs.setContentView(R.layout.activity_payment)
         dialogs.show()
         dialogs.findViewById<TextView>(R.id.accountNumber)?.text = accountNumber
         dialogs.findViewById<TextView>(R.id.ifscCode)?.text = ifscCode
         dialogs.findViewById<Button>(R.id.submitButton)?.setOnClickListener(this)
    }

}