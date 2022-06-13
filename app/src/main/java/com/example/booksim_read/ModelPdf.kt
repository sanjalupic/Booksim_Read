package com.example.booksim_read

class ModelPdf {

    //variables - PdfAddActivity (setup data to upload)
    var uid:String = ""
    var id:String = ""
    var title:String = ""
    var description:String = ""
    var categoryId:String = ""
    var url:String = ""
    var timestamp:Long = 0
    var viewsCount:Long = 0
    var downloadsCount:Long = 0


    //constructor (required by firebase)
    constructor()

    //parametrized constructor
    constructor(
        uid: String,
        id: String,
        title: String,
        description: String,
        categoryId: String,
        url: String,
        timestamp: Long,
        viewsCount: Long,
        downloadsCount: Long
    ) {
        this.uid = uid
        this.id = id
        this.title = title
        this.description = description
        this.categoryId = categoryId
        this.url = url
        this.timestamp = timestamp
        this.viewsCount = viewsCount
        this.downloadsCount = downloadsCount
    }

}