package controllers

import spark.ModelAndView
import spark.Request
import spark.Response

/**
 * Created by allen on 4/11/17.
 */

object ErrorController{
    fun notFound(request: Request, response: Response): ModelAndView {
        response.status(404)
        return ModelAndView(null, "404.hbs")
    }
}