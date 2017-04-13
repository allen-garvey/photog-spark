package controllers

import models.Folder
import spark.ModelAndView
import spark.Request
import spark.Response


/**
 * Created by allen on 4/13/17.
 */

object FolderController{
    fun show(request: Request, response: Response, folderIdParameterName: String): ModelAndView {
        val folder: Folder = SqliteController.selectFolder(request.params(folderIdParameterName)) ?: return ErrorController.notFound(request, response)
        return ModelAndView(hashMapOf(Pair("folders", SqliteController.selectAllFolders()), Pair("albums", SqliteController.albumsForFolder(request.params(folderIdParameterName)))), "album_index.hbs")
    }
}