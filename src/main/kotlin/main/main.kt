package main

/**
 * Created by allen on 7/28/16.
 */
import spark.Spark.*
import com.google.gson.Gson
import controllers.SqliteController
import spark.ModelAndView
import spark.template.handlebars.HandlebarsTemplateEngine
import java.util.*
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder



fun main(args : Array<String>) {
    port(3000)

    staticFiles.location("/public")

    //allow routes to match with trailing slash
    before({ req, res ->
        val path = req.pathInfo()
        if (path.endsWith("/")){
            res.redirect(path.substring(0, path.length - 1))
        }
    })

    //gzip everything
    after({req, res ->
        res.header("Content-Encoding", "gzip")
    })

    //used to parse and convert JSON
    val gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()
    val templateEngine = HandlebarsTemplateEngine()

//    SqliteController.selectAllAlbums()

    get("/", { req, res -> ModelAndView(hashMapOf(Pair("name", "Test")), "index.hbs")  }, templateEngine)
    get("/hello/:name", { req, res -> ModelAndView(hashMapOf(Pair("name", req.params(":name"))), "index.hbs")  }, templateEngine)

    get("/api/albums", { req, res -> SqliteController.selectAllAlbums() }, { gson.toJson(it) })
    get("/api/albums/:id/images", { req, res -> SqliteController.imagesForAlbum(req.params(":id")) }, { gson.toJson(it) })
}
