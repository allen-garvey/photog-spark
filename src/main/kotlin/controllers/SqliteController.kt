package controllers

import models.Album
import models.Image
import models.Thumbnail
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
select rkalbum.modelId, rkalbum.name, rkmaster.modelid, rkmaster.imagepath from RKAlbum inner join rkversion on rkalbum.posterversionuuid = rkversion.uuid inner join rkmaster on rkversion.masterid = rkmaster.modelId where rkalbum.name is not null and rkalbum.name != "" order by rkalbum.modelId desc;

--select cover photo for album
select modelid, imagepath from rkmaster where modelid in (select masterid from rkversion where uuid in (select posterversionuuid from rkalbum where modelid = "3571"));
* */

object SqliteController{
    val ALBUM_TABLE = "RKAlbum"
    val VERSION_TABLE = "RKVersion"
    val MASTER_TABLE = "RKMaster"
    val ALBUM_VERSION_TABLE = "RKAlbumVersion"
    val THUMBNAIL_TABLE = "RKImageProxyState"
    val DATABASE_FOLDER = "data"
    val DATABASE_FILENAME_LIBRARY = "Library.apdb"
    val DATABASE_FILENAME_THUMBNAILS = "ImageProxies.apdb"

    var databaseRoot: String? = null

    fun databasePathFor(databaseFilename: String): String{
        if(databaseRoot != null){
            return File(databaseRoot, databaseFilename).toString()
        }
        val classLoader = javaClass.classLoader
        return File(classLoader.getResource(File(DATABASE_FOLDER, databaseFilename).toString())!!.file).toString()
    }


    fun getConnection(databaseFilename: String): Connection? {
        val databasePath: String = databasePathFor(databaseFilename)

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

    fun executeOperation(databaseFilename: String, databaseOperation: (Connection)-> Unit) {
        val conn: Connection = getConnection(databaseFilename) ?: return
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
        val sql = "select ${ALBUM_TABLE}.modelId as album_id, ${ALBUM_TABLE}.name as album_name, ${MASTER_TABLE}.modelid as coverimage_id, ${VERSION_TABLE}.modelid as coverimage_version_id, ${MASTER_TABLE}.imagepath as coverimage_path from ${ALBUM_TABLE} inner join ${VERSION_TABLE} on ${ALBUM_TABLE}.posterversionuuid = ${VERSION_TABLE}.uuid inner join ${MASTER_TABLE} on ${VERSION_TABLE}.masterid = ${MASTER_TABLE}.modelId where ${ALBUM_TABLE}.name is not null and ${ALBUM_TABLE}.name != \"\" order by ${ALBUM_TABLE}.modelId desc"

        executeOperation(DATABASE_FILENAME_LIBRARY, { it ->
            val stmt  = it.createStatement()
            val rs    = stmt.executeQuery(sql)

            while (rs.next()) {
                albums.add(Album(rs.getString("album_id"), rs.getString("album_name"), Image(rs.getString("coverimage_id"), rs.getString("coverimage_version_id"), rs.getString("coverimage_path"), null)))
            }
        })

        val thumbnailSql = "SELECT minithumbnailpath, thumbnailpath FROM ${THUMBNAIL_TABLE} WHERE versionId = ?"

        albums.forEach {
            val album = it
            if(album.coverImage != null) {
                executeOperation(DATABASE_FILENAME_THUMBNAILS, { it ->
                    val stmt = it.prepareStatement(thumbnailSql)
                    stmt.setString(1, album.coverImage.versionId)
                    val rs = stmt.executeQuery()
                    while (rs.next()) {
                        album.coverImage.thumbnail = Thumbnail(rs.getString("thumbnailpath"), rs.getString("minithumbnailpath"))
                    }
                })
            }
        }

        return albums
    }

    fun selectAlbum(albumId: String): Album{
        var album = Album("", "", null)
        val sql = "select ${ALBUM_TABLE}.modelId as album_id, ${ALBUM_TABLE}.name as album_name from ${ALBUM_TABLE} where album_id = ?"
        executeOperation(DATABASE_FILENAME_LIBRARY, { it ->
            val stmt  = it.prepareStatement(sql)
            stmt.setString(1, albumId)
            val rs    = stmt.executeQuery()
            while (rs.next()) {
                album = Album(rs.getString("album_id"), rs.getString("album_name"), null)
            }
        })
        return album
    }

    fun imagesForAlbum(albumId: String): MutableList<Image>{
        val images: MutableList<Image> = mutableListOf()
        val sql = "SELECT ${MASTER_TABLE}.modelId as master_id, ${VERSION_TABLE}.modelid as version_id, ${MASTER_TABLE}.imagepath as master_imagepath FROM ${ALBUM_VERSION_TABLE} INNER JOIN ${VERSION_TABLE} ON ${VERSION_TABLE}.modelid = ${ALBUM_VERSION_TABLE}.versionid INNER JOIN ${MASTER_TABLE} ON ${MASTER_TABLE}.modelId = ${VERSION_TABLE}.masterid WHERE ${ALBUM_VERSION_TABLE}.albumId = ?"

        executeOperation(DATABASE_FILENAME_LIBRARY,{ it ->
            val stmt  = it.prepareStatement(sql)
            stmt.setString(1, albumId)
            val rs    = stmt.executeQuery()
            while (rs.next()) {
                images.add(Image(rs.getString("master_id"), rs.getString("version_id"), rs.getString("master_imagepath"), null))
            }
        })

        val thumbnailSql = "SELECT minithumbnailpath, thumbnailpath FROM ${THUMBNAIL_TABLE} WHERE versionId = ?"

        images.forEach {
            val image = it

            executeOperation(DATABASE_FILENAME_THUMBNAILS, { it ->
                val stmt = it.prepareStatement(thumbnailSql)
                stmt.setString(1, image.versionId)
                val rs = stmt.executeQuery()
                while (rs.next()) {
                    image.thumbnail = Thumbnail(rs.getString("thumbnailpath"), rs.getString("minithumbnailpath"))
                }
            })
        }


        return images
    }
}
