package views

import models.Folder
import views.BaseView.baseUrl
import views.BaseView.uriEncode

/**
 * Created by allen on 4/4/17.
 */
object FolderView {
    fun indexUrl(): String{
        return baseUrl() + "folders/"
    }


    fun urlForFolder(folder: Folder): String{
        return  indexUrl() + uriEncode(folder.uuid)
    }
}