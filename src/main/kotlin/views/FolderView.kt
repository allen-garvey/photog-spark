package views

import models.Folder
import views.BaseView.uriEncode

/**
 * Created by allen on 4/4/17.
 */
object FolderView {
    fun urlForFolder(folder: Folder): String{
        return "/folders/" + uriEncode(folder.uuid)
    }
}