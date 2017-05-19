package main

/**
 * Created by allen on 7/28/16.
 */
import spark.Spark.*
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import controllers.*
import spark.Filter
import views.AlbumView
import views.FolderView
import views.PersonView


fun main(args: Array<String>) {
    var portNum: Int = 3000

    if (args.isNotEmpty()) {
        try {
            val userPortNum: Int = Integer.parseInt(args[0])
            if (userPortNum in 1..65534) {
                portNum = userPortNum
            }
        } catch (e: NumberFormatException) {
            //don't do anything, since we will use default port
        }
    }

    if (args.size >= 2) {
        SqliteController.databaseRoot = args[1]
    }

    port(portNum)

    //live reload static files in development
    if (args.isNotEmpty()) {
        staticFiles.location("/public")
    } else {
        staticFiles.externalLocation(System.getProperty("user.dir") + "/src/main/resources/public")
    }

    //allow routes to match with trailing slash
    before(Filter({ req, res ->
        val path = req.pathInfo()
        if (!path.equals("/") && path.endsWith("/")) {
            res.redirect(path.substring(0, path.length - 1))
        }
    }))

    //set response type to json for api routes
    after(Filter({ req, res ->
        if (req.pathInfo().startsWith("/api")) {
            res.type("application/json")
        }
    }))

    //gzip everything
    after(Filter({ req, res ->
        res.header("Content-Encoding", "gzip")
    }))

    //used to parse and convert JSON
    val gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()


    get("/", { req, res -> AlbumController.index(req, res) })

    get(FolderView.indexUrl(), { req, res -> FolderController.index(req, res) })
    get("${FolderView.indexUrl()}/:uuid", { req, res -> FolderController.show(req, res, ":uuid") })

    get(AlbumView.indexUrl(), { req, res -> AlbumController.index(req, res) })
    get("${AlbumView.indexUrl()}/:id", { req, res -> AlbumController.show(req, res, ":id") })
    get("${AlbumView.indexUrl()}/:album_id/images/:image_id", { req, res -> ImageController.showAlbumImage(req, res, ":album_id", ":image_id") })

    get("/images/:id", { req, res -> ImageController.show(req, res, ":id") })


    get(PersonView.indexUrl(), { req, res -> PersonController.index(req, res) })
    get("${PersonView.indexUrl()}/:id", { req, res -> PersonController.show(req, res, ":id") })


    //API routes
    get("/api/albums", { req, res -> SqliteController.selectAllAlbums() }, { gson.toJson(it) })
    get("/api/albums/:id", { req, res -> SqliteController.selectAlbum(req.params(":id")) }, { gson.toJson(it) })
    get("/api/folders", { req, res -> SqliteController.selectAllFolders() }, { gson.toJson(it) })
    get("/api/albums/:id/images", { req, res -> SqliteController.imagesForAlbum(req.params(":id")) }, { gson.toJson(it) })
    get("/api/people", { req, res -> SqliteController.selectAllPeople() }, { gson.toJson(it) })
    get("/api/people/:id/images", { req, res -> SqliteController.imagesForPerson(req.params(":id")) }, { gson.toJson(it) })

    //Errors
    notFound { req, res -> ErrorController.notFound(req, res) }
}

