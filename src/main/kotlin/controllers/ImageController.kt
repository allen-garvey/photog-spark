package controllers

import models.Album
import models.Image
import spark.ModelAndView
import spark.Request
import spark.Response

/**
 * Created by allen on 4/12/17.
 */

object ImageController{
    fun show(request: Request, response: Response, imageIdParameterName: String): ModelAndView {
        val image: Image = SqliteController.selectImage(request.params(imageIdParameterName)) ?:  return ErrorController.notFound(request, response)

        return ModelAndView(hashMapOf(Pair("image", image), Pair("albums", SqliteController.albumsForImage(request.params(imageIdParameterName)))), "image_show.hbs")
    }

    fun showAlbumImage(request: Request, response: Response, albumIdParameterName: String, imageIdParameterName: String): ModelAndView {
        val image: Image = SqliteController.selectImage(request.params(imageIdParameterName)) ?:  return ErrorController.notFound(request, response)
        val album: Album = SqliteController.selectAlbum(request.params(albumIdParameterName)) ?: return ErrorController.notFound(request, response)
        val images: MutableList<Image> = SqliteController.imagesForAlbum(request.params(albumIdParameterName))

        val imageIndex: Int = images.indexOf(image)
        //make sure this image is part of the parent album
        if(imageIndex < 0){
            return ErrorController.notFound(request, response)
        }
        val previousImage: Image? = if(imageIndex > 0) images.get(imageIndex - 1) else null
        val nextImage: Image? = if(imageIndex < images.size - 1) images.get(imageIndex + 1) else null

        return ModelAndView(hashMapOf(Pair("image", image), Pair("images", images), Pair("parent_album", album), Pair("previous_image", previousImage), Pair("next_image", nextImage), Pair("albums", SqliteController.albumsForImage(request.params(imageIdParameterName)))), "image_show.hbs")
    }
}