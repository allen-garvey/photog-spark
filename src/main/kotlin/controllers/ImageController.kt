package controllers

import models.Album
import models.Image
import spark.ModelAndView
import spark.Request
import spark.Response
import templates.ImageTemplate

/**
 * Created by allen on 4/12/17.
 */

object ImageController{
    fun show(request: Request, response: Response, imageIdParameterName: String): String {
        val image: Image = SqliteController.selectImage(request.params(imageIdParameterName)) ?:  return ErrorController.notFound(request, response)

        return ImageTemplate.show(image, SqliteController.albumsForImage(request.params(imageIdParameterName)))
    }

    fun showAlbumImage(request: Request, response: Response, albumIdParameterName: String, imageIdParameterName: String): String {
        val image: Image = SqliteController.selectImage(request.params(imageIdParameterName)) ?:  return ErrorController.notFound(request, response)
        val album: Album = SqliteController.selectAlbum(request.params(albumIdParameterName)) ?: return ErrorController.notFound(request, response)
        val images: MutableList<Image> = SqliteController.imagesForAlbum(request.params(albumIdParameterName))
        val albums: MutableList<Album> = SqliteController.albumsForImage(request.params(imageIdParameterName))

        val imageIndex: Int = images.indexOf(image)
        //make sure this image is part of the parent album
        if(imageIndex < 0){
            return ErrorController.notFound(request, response)
        }
        val previousImage: Image? = if(imageIndex > 0) images.get(imageIndex - 1) else null
        val nextImage: Image? = if(imageIndex < images.size - 1) images.get(imageIndex + 1) else null

        return ImageTemplate.showAlbumImage(image, albums, album, images, previousImage, nextImage)
    }
}