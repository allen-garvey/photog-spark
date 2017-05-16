package controllers

import models.Album
import spark.ModelAndView
import spark.Request
import spark.Response
import templates.AlbumTemplate
import templates.SharedTemplate
import views.AlbumView

/**
 * Created by allen on 4/11/17.
 */

object AlbumController{

    fun index(request: Request, response: Response): String {
        return AlbumTemplate.index(SqliteController.selectAllFolders(), SqliteController.selectAllAlbums())
    }

    fun show(request: Request, response: Response, albumIdParameterName: String): String {
        val album: Album = SqliteController.selectAlbum(request.params(albumIdParameterName)) ?: return ErrorController.notFound(request, response)

        return SharedTemplate.imageListPage(SqliteController.selectAllFolders(), album.name, SqliteController.imagesForAlbum(request.params(albumIdParameterName)), { AlbumView.urlForAlbumImage(album, it) })
    }
}