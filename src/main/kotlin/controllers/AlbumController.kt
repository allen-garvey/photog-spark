package controllers

import models.Album
import spark.ModelAndView
import spark.Request
import spark.Response
import templates.AlbumTemplate

/**
 * Created by allen on 4/11/17.
 */

object AlbumController{

    fun index(request: Request, response: Response): String {
        return AlbumTemplate.index(SqliteController.selectAllFolders(), SqliteController.selectAllAlbums())
    }

    fun show(request: Request, response: Response, albumIdParameterName: String): String {
        val album: Album = SqliteController.selectAlbum(request.params(albumIdParameterName)) ?: return ErrorController.notFoundPage(request, response)
        return AlbumTemplate.show(SqliteController.selectAllFolders(), album, SqliteController.imagesForAlbum(request.params(albumIdParameterName)))
        //return ModelAndView(hashMapOf(Pair("folders", SqliteController.selectAllFolders()), Pair("images", SqliteController.imagesForAlbum(request.params(albumIdParameterName))), Pair("album", album)), "album_show.hbs")
    }
}