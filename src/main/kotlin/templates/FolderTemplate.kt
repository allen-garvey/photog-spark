package templates

/**
 * Created by allen on 5/18/17.
 */

import kotlinx.html.*
import models.Folder
import views.FolderView

object FolderTemplate {
    fun index(folders: MutableList<Folder>): String{
        return Layout.mainLayout(folders, {
            ul(""){
                folders.forEach {
                    li {
                        a(FolderView.urlForFolder(it)){
                            +it.name
                        }
                    }
                }
            }
        }, subTitle = "All folders")
    }
}