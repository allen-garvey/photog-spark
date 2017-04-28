package controllers

import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.stream.createHTML
import spark.ModelAndView
import spark.Request
import spark.Response
import templates.Layout
import htmlTags.main
import kotlinx.html.body

/**
 * Created by allen on 4/11/17.
 */

object ErrorController{
    fun notFound(request: Request, response: Response): ModelAndView {
        response.status(404)
        return ModelAndView(null, "404.hbs")
    }

    fun notFoundPage(request: Request, response: Response): String {
        response.status(404)
        return Layout.default(mainContent = {
            h1{
                +"404"
            }
            div { +"Page not found" }
        })
    }
}