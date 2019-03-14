package `in`.edu.pushparaj.model
/*
* 1. pay
* 2. download
* 3. decrypt
* 4. open
* */
class FileModel(val fileDescription:String,val filePrice:String,val fileName:String,val fileHash:String,val accoutNumber:String,val ifscCode:String,val fileSize:Double,val password:String,var status:Int)

class FileStatusModel(val fileHash:String,var status:Int)