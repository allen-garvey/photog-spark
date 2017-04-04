package views

import models.Folder

/**
 * Created by allen on 4/4/17.
 */
class FolderView: BaseView() {
    fun urlForFolder(folder: Folder): String{
        return "/folders/" + uriEncode(folder.uuid)
    }
}