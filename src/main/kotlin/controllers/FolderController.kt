package controllers

import models.Folder
import spark.ModelAndView
import spark.Request
import spark.Response
import templates.AlbumTemplate


/**
 * Created by allen on 4/13/17.
 */

object FolderController{
    fun show(request: Request, response: Response, folderIdParameterName: String): String {
        val folder: Folder = SqliteController.selectFolder(request.params(folderIdParameterName)) ?: return ErrorController.notFound(request, response)
        return AlbumTemplate.index(SqliteController.selectAllFolders(), SqliteController.albumsForFolder(request.params(folderIdParameterName)), folder.name)
    }
}