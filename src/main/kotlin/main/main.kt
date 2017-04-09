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
import views.*


fun main(args : Array<String>) {
    var portNum: Int = 3000

    if(args.isNotEmpty()){
        try{
            val userPortNum: Int = Integer.parseInt(args[0])
            if(userPortNum in 1..65534){
                portNum = userPortNum
            }
        }
        catch (e: NumberFormatException){
            //don't do anything, since we will use default port
        }
    }

    if(args.size >= 2){
        SqliteController.databaseRoot = args[1]
    }

    port(portNum)

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
    templateEngine.registerHelpers(SiteView())
    templateEngine.registerHelpers(FolderView())


    get("/", { req, res -> ModelAndView(hashMapOf(Pair("folders", SqliteController.selectAllFolders()), Pair("albums", SqliteController.selectAllAlbums())), "album_index.hbs")  }, templateEngine)
    get("/folders/:uuid", { req, res -> ModelAndView(hashMapOf(Pair("folders", SqliteController.selectAllFolders()), Pair("albums", SqliteController.albumsForFolder(req.params(":uuid")))), "album_index.hbs")  }, templateEngine)
    get("/albums", { req, res -> ModelAndView(hashMapOf(Pair("folders", SqliteController.selectAllFolders()), Pair("albums", SqliteController.selectAllAlbums())), "album_index.hbs")  }, templateEngine)
    get("/albums/:id", { req, res -> ModelAndView(hashMapOf(Pair("folders", SqliteController.selectAllFolders()), Pair("images", SqliteController.imagesForAlbum(req.params(":id"))), Pair("album", SqliteController.selectAlbum(req.params(":id")))), "album_show.hbs")  }, templateEngine)
    get("/images/:id", { req, res -> ModelAndView(hashMapOf(Pair("image", SqliteController.selectImage(req.params(":id"))), Pair("albums", SqliteController.albumsForImage(req.params(":id")))), "image_show.hbs")  }, templateEngine)


    get("/api/albums", { req, res -> SqliteController.selectAllAlbums() }, { gson.toJson(it) })
    get("/api/folders", { req, res -> SqliteController.selectAllFolders() }, { gson.toJson(it) })
    get("/api/albums/:id/images", { req, res -> SqliteController.imagesForAlbum(req.params(":id")) }, { gson.toJson(it) })
}
