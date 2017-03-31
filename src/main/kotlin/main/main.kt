package main

/**
 * Created by allen on 7/28/16.
 */
import spark.Spark.*
import controllers.SqliteController
import spark.ModelAndView
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import spark.Filter
import views.AlbumView
import views.CustomHandlebarsTemplateEngine
import views.ImageView


fun main(args : Array<String>) {
    if(args.isNotEmpty()){
        SqliteController.databaseRoot = args[0]
    }

    port(3000)

    staticFiles.location("/public")

    //allow routes to match with trailing slash
    before(Filter({ req, res ->
        val path = req.pathInfo()
        if (!path.equals("/") && path.endsWith("/")){
            res.redirect(path.substring(0, path.length - 1))
        }
    }))

    //set response type to json for api routes
    after(Filter({req, res ->
        if(req.pathInfo().startsWith("/api")){
            res.type("application/json")
        }
    }))

    //gzip everything
    after(Filter({req, res ->
        res.header("Content-Encoding", "gzip")
    }))

    //used to parse and convert JSON
    val gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

    val templateEngine = CustomHandlebarsTemplateEngine()
    templateEngine.registerHelpers(ImageView())
    templateEngine.registerHelpers(AlbumView())


    get("/", { req, res -> ModelAndView(hashMapOf(Pair("albums", SqliteController.selectAllAlbums())), "album_index.hbs")  }, templateEngine)
    get("/albums/:id", { req, res -> ModelAndView(hashMapOf(Pair("images", SqliteController.imagesForAlbum(req.params(":id"))), Pair("album", SqliteController.selectAlbum(req.params(":id")))), "album_show.hbs")  }, templateEngine)

    get("/hello/:name", { req, res -> ModelAndView(hashMapOf(Pair("name", req.params(":name"))), "index.hbs")  }, templateEngine)

    get("/api/albums", { req, res -> SqliteController.selectAllAlbums() }, { gson.toJson(it) })
    get("/api/albums/:id/images", { req, res -> SqliteController.imagesForAlbum(req.params(":id")) }, { gson.toJson(it) })
}
