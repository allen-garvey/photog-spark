package controllers

import models.Album
import models.Image
import java.sql.Connection
import java.sql.SQLException
import java.sql.DriverManager
import java.io.File





/**
 * Created by allen on 3/29/17.
 */

/*
* select name, albumType, albumSubclass from RKAlbum limit 1;

select modelId, fileName, imagePath, imageDate from RKMaster order by modelId desc limit 1;


-- select albums that an image is in
select name from RKalbum where modelId in (select albumId from RKAlbumVersion where versionId in (select modelid from RKVersion where masterid = 13249));

--select images in album
select imagePath from RKMaster where modelId in (select masterid from RKVersion where modelId in (select versionid from RKAlbumVersion where albumId = 3239));

-- select all albums
select name from RKAlbum where name is not null and name != "" order by modelId desc;

--select cover photo for album
select modelid, imagepath from rkmaster where modelid in (select masterid from rkversion where uuid in (select posterversionuuid from rkalbum where modelid = "3571"));
* */

object SqliteController{
    val ALBUM_TABLE = "RKAlbum"
    val VERSION_TABLE = "RKVersion"
    val MASTER_TABLE = "RKMaster"
    var ALBUM_VERSION_TABLE = "RKAlbumVersion"
    var DATABASE_FILENAME = "data/Library.apdb"


    fun getConnection(): Connection? {
        val classLoader = javaClass.classLoader
        val file = File(classLoader.getResource(DATABASE_FILENAME)!!.file)
        val databasePath: String = file.toString()

        try {
            // db parameters
            val url = "jdbc:sqlite:" + databasePath
            // create a connection to the database
            val conn = DriverManager.getConnection(url)
            return conn
        } catch (e: Exception) {
            println(e.message)
            return null
        }
    }

    fun executeOperation(databaseOperation: (Connection)-> Unit) {
        val conn: Connection = getConnection() ?: return
        try {
            databaseOperation(conn)
        }
        catch (e: SQLException) {
            println(e.message)
        }
        finally {
            try {
                conn.close()
            } catch (ex: SQLException) {
                println(ex.message)
            }

        }
    }

    fun selectAllAlbums() : MutableList<Album> {
        val albums : MutableList<Album> = mutableListOf()
        val sql = "SELECT modelId, name from ${ALBUM_TABLE} where name is not null and name != \"\" order by modelId desc"

        executeOperation { it ->
            val stmt  = it.createStatement()
            val rs    = stmt.executeQuery(sql)

            while (rs.next()) {
                albums.add(Album(rs.getString("modelId"), rs.getString("name")))
            }
        }

        return albums
    }

    fun imagesForAlbum(albumId: String): MutableList<Image>{
        val images: MutableList<Image> = mutableListOf()
        val sql = "SELECT modelId, imagePath from ${MASTER_TABLE} where modelId in (select masterid from ${VERSION_TABLE} where modelId in (select versionid from ${ALBUM_VERSION_TABLE} where albumId = ?))"

        executeOperation { it ->
            val stmt  = it.prepareStatement(sql)
            stmt.setString(1, albumId)
            val rs    = stmt.executeQuery()
            while (rs.next()) {
                images.add(Image(rs.getString("modelId"), rs.getString("imagePath")))
            }
        }
        return images
    }
}
