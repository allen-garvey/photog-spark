package controllers

import models.*
import java.sql.Connection
import java.sql.SQLException
import java.sql.DriverManager
import java.io.File
import java.sql.Timestamp


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


--select folders
select rkfolder.uuid, rkfolder.name from rkfolder where rkfolder.uuid in (select folderuuid from rkalbum where modelid in (select albumid from rkalbumversion)) and rkfolder.name is not "" order by rkfolder.name;

* */

object SqliteController{
    val ALBUM_TABLE = "RKAlbum"
    val VERSION_TABLE = "RKVersion"
    val MASTER_TABLE = "RKMaster"
    val ALBUM_VERSION_TABLE = "RKAlbumVersion"
    val THUMBNAIL_TABLE = "RKImageProxyState"
    val FOLDER_TABLE = "RKFolder"
    val PERSON_TABLE = "RKPerson"
    val PERSON_VERSION_TABLE = "RKPersonVersion"

    val DATABASE_FOLDER = "data"
    val DATABASE_FILENAME_LIBRARY = "Library.apdb"
    val DATABASE_FILENAME_THUMBNAILS = "ImageProxies.apdb"
    val DATABASE_FILENAME_PERSON = "Person.db"

    var databaseRoot: String = "/home/allen/Pictures/Mac-Photos-Database"

    fun databasePathFor(databaseFilename: String): String{
        return File(databaseRoot, databaseFilename).toString()
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
                albums.add(Album(rs.getString("album_id"), rs.getString("album_name"), Image(rs.getString("coverimage_id"), rs.getString("coverimage_version_id"), rs.getString("coverimage_path"), null, null)))
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


    fun selectAlbum(albumId: String): Album?{
        var album: Album? = null
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
        val sql = "SELECT ${MASTER_TABLE}.modelId as master_id, ${VERSION_TABLE}.modelid as version_id, ${VERSION_TABLE}.isFavorite as is_favorite, ${MASTER_TABLE}.imagepath as master_imagepath, strftime('%s', datetime(${MASTER_TABLE}.imagedate, 'unixepoch', '+372 months', ${MASTER_TABLE}.imageTimeZoneOffsetSeconds || ' seconds')) AS master_timestamp FROM ${ALBUM_VERSION_TABLE} INNER JOIN ${VERSION_TABLE} ON ${VERSION_TABLE}.modelid = ${ALBUM_VERSION_TABLE}.versionid INNER JOIN ${MASTER_TABLE} ON ${MASTER_TABLE}.modelId = ${VERSION_TABLE}.masterid WHERE ${ALBUM_VERSION_TABLE}.albumId = ?"

        executeOperation(DATABASE_FILENAME_LIBRARY,{ it ->
            val stmt  = it.prepareStatement(sql)
            stmt.setString(1, albumId)
            val rs    = stmt.executeQuery()
            while (rs.next()) {
                images.add(Image(rs.getString("master_id"), rs.getString("version_id"), rs.getString("master_imagepath"), Timestamp(rs.getString("master_timestamp").toLong() * 1000), null, rs.getBoolean("is_favorite")))
            }
        })

        images.forEach {
            it.thumbnail = thumbnailForImage(it)
        }

        return images
    }

    fun thumbnailForImage(image: Image): Thumbnail{
        var thumbnail: Thumbnail = Thumbnail("", "")
        val thumbnailSql = "SELECT minithumbnailpath, thumbnailpath FROM ${THUMBNAIL_TABLE} WHERE versionId = ?"

        executeOperation(DATABASE_FILENAME_THUMBNAILS, { it ->
            val stmt = it.prepareStatement(thumbnailSql)
            stmt.setString(1, image.versionId)
            val rs = stmt.executeQuery()
            while (rs.next()) {
                thumbnail = Thumbnail(rs.getString("thumbnailpath"), rs.getString("minithumbnailpath"))
            }
        })
        return thumbnail
    }

    fun selectImage(imageId: String): Image?{
        var image: Image? = null
        val sql = "SELECT ${MASTER_TABLE}.modelId as master_id, ${VERSION_TABLE}.modelid as version_id, ${VERSION_TABLE}.isFavorite as is_favorite, ${MASTER_TABLE}.imagepath as master_imagepath, strftime('%s', datetime(${MASTER_TABLE}.imagedate, 'unixepoch', '+372 months', ${MASTER_TABLE}.imageTimeZoneOffsetSeconds || ' seconds')) AS master_timestamp FROM ${MASTER_TABLE} INNER JOIN ${VERSION_TABLE} ON ${VERSION_TABLE}.masterId = ${MASTER_TABLE}.modelId WHERE ${MASTER_TABLE}.modelId = ?"

        executeOperation(DATABASE_FILENAME_LIBRARY,{ it ->
            val stmt  = it.prepareStatement(sql)
            stmt.setString(1, imageId)
            val rs    = stmt.executeQuery()
            while (rs.next()) {
                image = Image(rs.getString("master_id"), rs.getString("version_id"), rs.getString("master_imagepath"), Timestamp(rs.getString("master_timestamp").toLong() * 1000), null, rs.getBoolean("is_favorite"))

            }
        })
        //compiler complains if we just check for null
        val safeImage: Image = image ?: return null
        safeImage.thumbnail = thumbnailForImage(safeImage)

        return safeImage
    }

    fun albumsForImage(imageId: String): MutableList<Album>{
        val albums: MutableList<Album> = mutableListOf()
        val sql = "select ${ALBUM_TABLE}.modelId as album_id, ${ALBUM_TABLE}.name as album_name FROM ${ALBUM_TABLE} WHERE album_id IN (SELECT albumId from ${ALBUM_VERSION_TABLE} WHERE versionId IN (SELECT modelId from ${VERSION_TABLE} WHERE masterId = ?))"

        executeOperation(DATABASE_FILENAME_LIBRARY,{ it ->
            val stmt  = it.prepareStatement(sql)
            stmt.setString(1, imageId)
            val rs    = stmt.executeQuery()
            while (rs.next()) {
                albums.add(Album(rs.getString("album_id"), rs.getString("album_name"), null))
            }
        })

        return albums
    }

    fun selectFolder(folderUuid: String): Folder?{
        var folder: Folder? = null
        val sql = "SELECT ${FOLDER_TABLE}.uuid as folder_uuid, ${FOLDER_TABLE}.name as folder_name FROM ${FOLDER_TABLE} WHERE folder_uuid is ? AND  ${FOLDER_TABLE}.uuid in (SELECT folderuuid from ${ALBUM_TABLE} WHERE modelid in (select albumid from ${ALBUM_VERSION_TABLE})) and ${FOLDER_TABLE}.name is not \"\""
        executeOperation(DATABASE_FILENAME_LIBRARY, { it ->
            val stmt  = it.prepareStatement(sql)
            stmt.setString(1, folderUuid)
            val rs    = stmt.executeQuery()
            while (rs.next()) {
                folder = Folder(rs.getString("folder_uuid"), rs.getString("folder_name"))
            }
        })
        return folder
    }

    fun selectAllFolders() : MutableList<Folder> {
        val folders : MutableList<Folder> = mutableListOf()
        val sql = "SELECT ${FOLDER_TABLE}.uuid as folder_uuid, ${FOLDER_TABLE}.name as folder_name from ${FOLDER_TABLE} where ${FOLDER_TABLE}.uuid in (select folderuuid from ${ALBUM_TABLE} where modelid in (select albumid from ${ALBUM_VERSION_TABLE})) and ${FOLDER_TABLE}.name is not \"\" order by ${FOLDER_TABLE}.name"

        executeOperation(DATABASE_FILENAME_LIBRARY, { it ->
            val stmt  = it.createStatement()
            val rs    = stmt.executeQuery(sql)

            while (rs.next()) {
                folders.add(Folder(rs.getString("folder_uuid"), rs.getString("folder_name")))
            }
        })

        return folders
    }

    fun albumsForFolder(folderUuid: String): MutableList<Album>{
        val albums : MutableList<Album> = mutableListOf()
        val sql = "select ${ALBUM_TABLE}.modelId as album_id, ${ALBUM_TABLE}.name as album_name, ${MASTER_TABLE}.modelid as coverimage_id, ${VERSION_TABLE}.modelid as coverimage_version_id, ${MASTER_TABLE}.imagepath as coverimage_path from ${ALBUM_TABLE} inner join ${VERSION_TABLE} on ${ALBUM_TABLE}.posterversionuuid = ${VERSION_TABLE}.uuid inner join ${MASTER_TABLE} on ${VERSION_TABLE}.masterid = ${MASTER_TABLE}.modelId where ${ALBUM_TABLE}.name is not null and ${ALBUM_TABLE}.name != \"\" and ${ALBUM_TABLE}.folderUuid is ? order by ${ALBUM_TABLE}.modelId desc"

        executeOperation(DATABASE_FILENAME_LIBRARY, { it ->
            val stmt  = it.prepareStatement(sql)
            stmt.setString(1, folderUuid)
            val rs    = stmt.executeQuery()

            while (rs.next()) {
                albums.add(Album(rs.getString("album_id"), rs.getString("album_name"), Image(rs.getString("coverimage_id"), rs.getString("coverimage_version_id"), rs.getString("coverimage_path"), null, null)))
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

    fun selectAllPeople() : MutableList<Person> {
        val people : MutableList<Person> = mutableListOf()
        val sql = "SELECT ${PERSON_TABLE}.modelid as person_id, ${PERSON_TABLE}.name as person_name from ${PERSON_TABLE} order by ${PERSON_TABLE}.name"

        executeOperation(DATABASE_FILENAME_PERSON, { it ->
            val stmt  = it.createStatement()
            val rs    = stmt.executeQuery(sql)

            while (rs.next()) {
                people.add(Person(rs.getString("person_id"), rs.getString("person_name")))
            }
        })

        return people
    }

    fun imagesForPerson(personId: String): MutableList<Image>{
        val versionIds: MutableList<String> = mutableListOf()

        val versionSql = "SELECT ${PERSON_VERSION_TABLE}.versionId AS version_id FROM ${PERSON_VERSION_TABLE} WHERE ${PERSON_VERSION_TABLE}.personId is ?"

        executeOperation(DATABASE_FILENAME_PERSON, { it ->
            val stmt  = it.prepareStatement(versionSql)
            stmt.setString(1, personId)
            val rs    = stmt.executeQuery()

            while (rs.next()) {
                versionIds.add(rs.getString("version_id"))
            }
        })

        val versionIdsString: String = versionIds.map { "'${it}'" }.joinToString(",")


        val images: MutableList<Image> = mutableListOf()
        val sql = "SELECT ${MASTER_TABLE}.modelId as master_id, ${VERSION_TABLE}.modelid as version_id, ${VERSION_TABLE}.isFavorite as is_favorite, ${MASTER_TABLE}.imagepath as master_imagepath, strftime('%s', datetime(${MASTER_TABLE}.imagedate, 'unixepoch', '+372 months', ${MASTER_TABLE}.imageTimeZoneOffsetSeconds || ' seconds')) AS master_timestamp FROM ${VERSION_TABLE} INNER JOIN ${MASTER_TABLE} ON ${MASTER_TABLE}.modelId = ${VERSION_TABLE}.masterid WHERE ${VERSION_TABLE}.modelId IN (${versionIdsString})"

        executeOperation(DATABASE_FILENAME_LIBRARY,{ it ->
            val stmt  = it.createStatement()
            val rs    = stmt.executeQuery(sql)

            while (rs.next()) {
                images.add(Image(rs.getString("master_id"), rs.getString("version_id"), rs.getString("master_imagepath"), Timestamp(rs.getString("master_timestamp").toLong() * 1000), null, rs.getBoolean("is_favorite")))
            }
        })

        images.forEach {
            it.thumbnail = thumbnailForImage(it)
        }

        return images
    }
}
