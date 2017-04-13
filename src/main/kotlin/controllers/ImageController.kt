package controllers

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
}