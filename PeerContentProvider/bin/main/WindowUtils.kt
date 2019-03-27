
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import io.ipfs.kotlin.IPFS
import javafx.scene.layout.Pane
import okhttp3.OkHttpClient
import org.intellij.lang.annotations.JdkConstants
import org.json.JSONArray
import org.json.JSONObject
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.Panel
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.File
import java.io.StringWriter
import java.util.concurrent.TimeUnit
import javax.swing.*
import javax.swing.JDialog
import javax.swing.JProgressBar





object WindowUtils {
    val frame = MainScreen("Peer Content Provider")
    fun createAndShowGUI() {
        frame.isVisible = true
    }
    fun showProgressDialog(isVisible:Boolean,title:String,content:String){
        frame.showDialog(isVisible,title,content)
    }
}
class MainScreen(title: String) : JFrame(), ActionListener {

    val ipfs = IPFS( "http://127.0.0.1:5001/api/v0/",
        OkHttpClient.Builder().connectTimeout(600, TimeUnit.SECONDS).readTimeout(600,TimeUnit.SECONDS).writeTimeout(600,TimeUnit.SECONDS).build(),
        Moshi.Builder().build())

    val dlg = JDialog(this, "Fetching ledger data", true)
    val dpb = JProgressBar(0, 500)


    val springLayout = SpringLayout()
    val promptLable = JLabel("Enter the following detail")
    val fileDescriptionLabel = JLabel("File Description")
    val accountNumberLabel = JLabel("Account Number")
    val ifscLabel = JLabel("IFSC code")
    val priceLabel = JLabel("Price in rupees")
    val fileDescriptionTextArea = JTextField("",40)
    val accountNumberTextArea = JTextField("",40)
    val ifscTextArea = JTextField("",40)
    val priceTextArea = JTextField("",40)
    val openButton = JButton("Open")
    val panel = Panel()
    val dialogBoxLable = JLabel("")

    var selectedFile: File? = null

    init{
        createUI(title)
    }
    private fun createUI(title: String) {

        setDefaultLookAndFeelDecorated(true)
        setTitle(title)
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        setSize(600, 400)
        setLocationRelativeTo(null)


        openButton.addActionListener(this)
        openButton.preferredSize = Dimension(600,50)
        promptLable.size = Dimension(300,100)

        dpb.isIndeterminate = true
        dlg.add(BorderLayout.CENTER, dpb)
        dlg.add(BorderLayout.NORTH, dialogBoxLable)
        dlg.defaultCloseOperation = JDialog.DO_NOTHING_ON_CLOSE
        dlg.setSize(300, 75)
        dlg.setLocationRelativeTo(this)

        panel.layout = springLayout
        panel.setSize(600,400)
        panel.add(openButton)
        panel.add(fileDescriptionLabel)
        panel.add(accountNumberLabel)
        panel.add(ifscLabel)
        panel.add(priceLabel)
        panel.add(fileDescriptionTextArea)
        panel.add(accountNumberTextArea)
        panel.add(ifscTextArea)
        panel.add(priceTextArea)
        panel.add(promptLable)

        add(panel)

        springLayout.putConstraint(SpringLayout.NORTH,promptLable,10,SpringLayout.NORTH,panel)
        springLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER,promptLable,0,SpringLayout.HORIZONTAL_CENTER,panel)

        springLayout.putConstraint(SpringLayout.WEST, fileDescriptionLabel, 5, SpringLayout.WEST, panel)
        springLayout.putConstraint(SpringLayout.NORTH,fileDescriptionLabel,15,SpringLayout.SOUTH,promptLable)

        springLayout.putConstraint(SpringLayout.WEST, accountNumberLabel, 5, SpringLayout.WEST, panel)
        springLayout.putConstraint(SpringLayout.NORTH,accountNumberLabel,10,SpringLayout.SOUTH,fileDescriptionLabel)

        springLayout.putConstraint(SpringLayout.WEST, ifscLabel, 5, SpringLayout.WEST, panel)
        springLayout.putConstraint(SpringLayout.NORTH,ifscLabel,15,SpringLayout.SOUTH,accountNumberLabel)

        springLayout.putConstraint(SpringLayout.WEST, priceLabel, 5, SpringLayout.WEST, panel)
        springLayout.putConstraint(SpringLayout.NORTH,priceLabel,15,SpringLayout.SOUTH,ifscLabel)

        springLayout.putConstraint(SpringLayout.WEST, fileDescriptionTextArea, 20, SpringLayout.EAST, fileDescriptionLabel)
        springLayout.putConstraint(SpringLayout.NORTH,fileDescriptionTextArea,10,SpringLayout.SOUTH,promptLable)

        springLayout.putConstraint(SpringLayout.NORTH,accountNumberTextArea,5,SpringLayout.SOUTH,fileDescriptionLabel)
        springLayout.putConstraint(SpringLayout.WEST, accountNumberTextArea, 0, SpringLayout.WEST, fileDescriptionTextArea)

        springLayout.putConstraint(SpringLayout.NORTH,ifscTextArea,5,SpringLayout.SOUTH,accountNumberTextArea)
        springLayout.putConstraint(SpringLayout.WEST, ifscTextArea, 0, SpringLayout.WEST, fileDescriptionTextArea)

        springLayout.putConstraint(SpringLayout.NORTH,priceTextArea,5,SpringLayout.SOUTH,ifscTextArea)
        springLayout.putConstraint(SpringLayout.WEST, priceTextArea, 0, SpringLayout.WEST, fileDescriptionTextArea)

        springLayout.putConstraint(SpringLayout.SOUTH,openButton,5,SpringLayout.SOUTH,panel)
        springLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER,openButton,0,SpringLayout.HORIZONTAL_CENTER,panel)

        setFileDetailsVisibility(false)
    }
    fun showDialog(isVisible:Boolean,title:String,content:String){
        dlg.title = title
        dialogBoxLable.text=content
        dlg.isVisible = isVisible
    }

    fun setFileDetailsVisibility(isVisible:Boolean){
        fileDescriptionLabel.isVisible = isVisible
        accountNumberLabel.isVisible = isVisible
        ifscLabel.isVisible = isVisible
        priceLabel.isVisible = isVisible
        promptLable.isVisible = isVisible
        fileDescriptionTextArea.isVisible = isVisible
        fileDescriptionTextArea.text =""
        ifscTextArea.isVisible = isVisible
        ifscTextArea.text=""
        priceTextArea.isVisible = isVisible
        priceTextArea.text=""
        accountNumberTextArea.isVisible = isVisible
        accountNumberTextArea.text=""
    }


    override fun actionPerformed(e: ActionEvent?) {
        if(openButton.text.equals("Open")) {
            val fileChooser = JFileChooser()
            fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY
            val result = fileChooser.showOpenDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.selectedFile
                openButton.text = "Upload"
                setFileDetailsVisibility(true)
            }
        }else if(openButton.text.equals("Upload")) {
            if(selectedFile==null)
                return

            if(accountNumberTextArea.text.trim() == "" || ifscTextArea.text.trim() == "" || fileDescriptionTextArea.text.trim() == "" || priceTextArea.text.trim() == ""){
                JOptionPane.showMessageDialog(null, "Please fill all the fields", "InfoBox: Fields not filled" , JOptionPane.INFORMATION_MESSAGE);
                return
            }


            Thread{
                showDialog(true,"Publishing","Encrypting...")
            }.start()

            val encryptedFile = EncryptionUtils.Encrypt(selectedFile!!,"test")

            dialogBoxLable.text="Publishing..."

            val namedHash = ipfs.add.file(encryptedFile!!)
            var jsonArray = LedgerSingleton.LedgerData
            if (jsonArray == null)
                jsonArray = JSONArray()

            for (i in 0..(jsonArray.length() - 1)) {
                val item = jsonArray.getJSONObject(i)
                if(item.get("FileHash").equals(namedHash.Hash)){

                    selectedFile =null
                    openButton.text = "Open"
                    showDialog(false,"","")
                    setFileDetailsVisibility(false)
                    JOptionPane.showMessageDialog(null, "File already exists in the swarm", "InfoBox: File Already Exists" , JOptionPane.INFORMATION_MESSAGE);
                    return
                }
            }
            var jsonObject = JSONObject()
            jsonObject.put("FileName", selectedFile!!.name);
            jsonObject.put("FileDescription", fileDescriptionTextArea.text)
            jsonObject.put("AccountNumber", accountNumberTextArea.text)
            jsonObject.put("IFSC", ifscTextArea.text)
            jsonObject.put("FileHash", namedHash.Hash)
            jsonObject.put("FilePrice", priceTextArea.text)
            jsonObject.put("FileSize",selectedFile!!.length())
            jsonObject.put("FilePassword","test")
            jsonArray.put(jsonObject)
            println(jsonArray.toString())
            Thread{
                val namedLedgerHash = ipfs.add.string(jsonArray.toString())
                println(namedHash.Hash)
                println(ipfs.name.publish(namedLedgerHash.Hash))
                ipfs.pins.add(namedLedgerHash.Hash);
                LedgerSingleton.LedgerData = jsonArray
                selectedFile =null
                openButton.text = "Open"

                setFileDetailsVisibility(false)
                showDialog(false,"","")
            }.start()

        }

    }
}
