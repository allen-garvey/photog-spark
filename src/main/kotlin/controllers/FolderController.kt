package controllers

import models.Folder
import spark.ModelAndView
import spark.Request
import spark.Response
import templates.AlbumTemplate
import templates.FolderTemplate


/**
 * Created by allen on 4/13/17.
 */

object FolderController{
    fun index(request: Request, response: Response): String{
        return FolderTemplate.index(SqliteController.selectAllFolders())
    }

    fun show(request: Request, response: Response, folderIdParameterName: String): String {
        val folder: Folder = SqliteController.selectFolder(request.params(folderIdParameterName)) ?: return ErrorController.notFound(request, response)
        return AlbumTemplate.index(SqliteController.albumsForFolder(request.params(folderIdParameterName)), folder.name)
    }
}