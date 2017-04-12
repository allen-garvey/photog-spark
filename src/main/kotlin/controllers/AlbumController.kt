package controllers

import models.Album
import spark.ModelAndView
import spark.Request
import spark.Response

/**
 * Created by allen on 4/11/17.
 */

object AlbumController{

    fun index(request: Request, response: Response): ModelAndView {
        return ModelAndView(hashMapOf(Pair("folders", SqliteController.selectAllFolders()), Pair("albums", SqliteController.selectAllAlbums())), "album_index.hbs")
    }

    fun show(request: Request, response: Response, albumIdParameterName: String): ModelAndView {
        val album: Album = SqliteController.selectAlbum(request.params(albumIdParameterName))
        return ModelAndView(hashMapOf(Pair("folders", SqliteController.selectAllFolders()), Pair("images", SqliteController.imagesForAlbum(request.params(albumIdParameterName))), Pair("album", album)), "album_show.hbs")
    }
}