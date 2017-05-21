package controllers

import models.Folder
import spark.ModelAndView
import spark.Request
import spark.Response
import templates.AlbumTemplate
import templates.FolderTemplate
import templates.SharedTemplate
import views.FolderView
import views.Link


/**
 * Created by allen on 4/13/17.
 */

object FolderController{
    fun index(request: Request, response: Response): String{
        val folders: MutableList<Folder> = SqliteController.selectAllFolders()
        val folderLinks: List<Link> = folders.map { Link(it.name, FolderView.urlForFolder(it)) }

        return SharedTemplate.textListPage("Folders", folderLinks, "All folders")
    }

    fun show(request: Request, response: Response, folderIdParameterName: String): String {
        val folder: Folder = SqliteController.selectFolder(request.params(folderIdParameterName)) ?: return ErrorController.notFound(request, response)
        return AlbumTemplate.index(SqliteController.albumsForFolder(request.params(folderIdParameterName)), folder.name)
    }
}