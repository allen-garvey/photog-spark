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

--select cover image versions for person (need to convert rkversion.uuid to rkmaster.id)
select rkperson.modelid as person_id, rkface.imageid as cover_image_version_uuid from rkperson inner join rkface on rkperson.representativeFaceId = rkface.modelid;

* */

object SqliteController{
    val ALBUM_TABLE = "RKAlbum"
    val VERSION_TABLE = "RKVersion"
    val MASTER_TABLE = "RKMaster"
    val ALBUM_VERSION_TABLE = "RKAlbumVersion"
    val THUMBNAIL_TABLE = "RKImageProxyState"
    val FOLDER_TABLE = "RKFolder"
    val PERSON_TABLE = "RKPerson"
    var FACE_TABLE = "RKFace"
    val PERSON_VERSION_TABLE = "RKPersonVersion"
    val CUSTOM_SORT_ORDER_TABLE = "RKCustomSortOrder"

    val DATABASE_FOLDER = "data"
    val DATABASE_FILENAME_LIBRARY = "Library.apdb"
    val DATABASE_FILENAME_THUMBNAILS = "ImageProxies.apdb"
    val DATABASE_FILENAME_PERSON = "Person.db"

    var databaseRoot: String = System.getProperty("user.home") + "/Documents/Mac-Photos-Database"

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
        val sql = "select ${ALBUM_TABLE}.modelId as album_id, ${ALBUM_TABLE}.name as album_name, ${ALBUM_TABLE}.folderUuid as album_folder_uuid,  ${MASTER_TABLE}.modelid as coverimage_id, ${VERSION_TABLE}.modelid as coverimage_version_id, ${MASTER_TABLE}.imagepath as coverimage_path from ${ALBUM_TABLE} inner join ${VERSION_TABLE} on ${ALBUM_TABLE}.posterversionuuid = ${VERSION_TABLE}.uuid inner join ${MASTER_TABLE} on ${VERSION_TABLE}.masterid = ${MASTER_TABLE}.modelId where ${ALBUM_TABLE}.name is not null and ${ALBUM_TABLE}.name != \"\" order by ${ALBUM_TABLE}.modelId desc"

        executeOperation(DATABASE_FILENAME_LIBRARY, { it ->
            val stmt  = it.createStatement()
            val rs    = stmt.executeQuery(sql)

            while (rs.next()) {
                albums.add(Album(rs.getString("album_id"), rs.getString("album_name"), rs.getString("album_folder_uuid"), Image(rs.getString("coverimage_id"), rs.getString("coverimage_version_id"), rs.getString("coverimage_path"), null, null)))
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
        val sql = "select ${ALBUM_TABLE}.modelId as album_id, ${ALBUM_TABLE}.name as album_name, ${ALBUM_TABLE}.folderUuid as album_folder_uuid from ${ALBUM_TABLE} where album_id = ?"
        executeOperation(DATABASE_FILENAME_LIBRARY, { it ->
            val stmt  = it.prepareStatement(sql)
            stmt.setString(1, albumId)
            val rs    = stmt.executeQuery()
            while (rs.next()) {
                album = Album(rs.getString("album_id"), rs.getString("album_name"), rs.getString("album_folder_uuid"), null)
            }
        })
        return album
    }

    fun imagesForAlbum(albumId: String): MutableList<Image>{
        val images: MutableList<Image> = mutableListOf()
        val sql = "SELECT ${MASTER_TABLE}.modelId as master_id, ${VERSION_TABLE}.modelid as version_id, ${VERSION_TABLE}.isFavorite as is_favorite, ${MASTER_TABLE}.imagepath as master_imagepath, strftime('%s', datetime(${MASTER_TABLE}.imagedate, 'unixepoch', '+372 months', ${MASTER_TABLE}.imageTimeZoneOffsetSeconds || ' seconds')) AS master_timestamp, (SELECT ${CUSTOM_SORT_ORDER_TABLE}.orderNumber FROM ${CUSTOM_SORT_ORDER_TABLE} WHERE ${CUSTOM_SORT_ORDER_TABLE}.containerUuid = (SELECT ${ALBUM_TABLE}.Uuid from ${ALBUM_TABLE} WHERE ${ALBUM_TABLE}.modelId = ?) AND ${CUSTOM_SORT_ORDER_TABLE}.objectUuid = ${VERSION_TABLE}.uuid) AS sort_order FROM ${ALBUM_VERSION_TABLE} INNER JOIN ${VERSION_TABLE} ON ${VERSION_TABLE}.modelid = ${ALBUM_VERSION_TABLE}.versionid INNER JOIN ${MASTER_TABLE} ON ${MASTER_TABLE}.modelId = ${VERSION_TABLE}.masterid WHERE ${ALBUM_VERSION_TABLE}.albumId = ? ORDER BY sort_order"

        executeOperation(DATABASE_FILENAME_LIBRARY,{ it ->
            val stmt  = it.prepareStatement(sql)
            stmt.setString(1, albumId)
            stmt.setString(2, albumId)
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
        val sql = "SELECT ${MASTER_TABLE}.modelId as master_id, ${VERSION_TABLE}.modelid as version_id, ${VERSION_TABLE}.isFavorite as is_favorite, ${MASTER_TABLE}.imagepath as master_imagepath, strftime('%s', datetime(${MASTER_TABLE}.imagedate, 'unixepoch', '+372 months', ${MASTER_TABLE}.imageTimeZoneOffsetSeconds || ' seconds')) AS master_timestamp FROM ${MASTER_TABLE} INNER JOIN ${VERSION_TABLE} ON ${VERSION_TABLE}.masterId = ${MASTER_TABLE}.modelId WHERE master_id = ?"

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

    fun selectAllImages(): MutableList<Image>{
        val images : MutableList<Image> = mutableListOf()
        val sql = "SELECT ${MASTER_TABLE}.modelId as master_id, ${VERSION_TABLE}.modelid as version_id, ${VERSION_TABLE}.isFavorite as is_favorite, ${MASTER_TABLE}.imagepath as master_imagepath, strftime('%s', datetime(${MASTER_TABLE}.imagedate, 'unixepoch', '+372 months', ${MASTER_TABLE}.imageTimeZoneOffsetSeconds || ' seconds')) AS master_timestamp FROM ${MASTER_TABLE} INNER JOIN ${VERSION_TABLE} ON ${VERSION_TABLE}.masterId = ${MASTER_TABLE}.modelId ORDER BY master_id"

        executeOperation(DATABASE_FILENAME_LIBRARY,{ it ->
            val stmt  = it.prepareStatement(sql)
            val rs    = stmt.executeQuery()
            while (rs.next()) {
                val image = Image(rs.getString("master_id"), rs.getString("version_id"), rs.getString("master_imagepath"), Timestamp(rs.getString("master_timestamp").toLong() * 1000), null, rs.getBoolean("is_favorite"))

                image.thumbnail = thumbnailForImage(image)
                images.add(image)
            }
        })

        return images
    }

    fun albumsForImage(imageId: String): MutableList<Album>{
        val albums: MutableList<Album> = mutableListOf()
        val sql = "select ${ALBUM_TABLE}.modelId as album_id, ${ALBUM_TABLE}.name as album_name, ${ALBUM_TABLE}.folderUuid as album_folder_uuid FROM ${ALBUM_TABLE} WHERE album_id IN (SELECT albumId from ${ALBUM_VERSION_TABLE} WHERE versionId IN (SELECT modelId from ${VERSION_TABLE} WHERE masterId = ?))"

        executeOperation(DATABASE_FILENAME_LIBRARY,{ it ->
            val stmt  = it.prepareStatement(sql)
            stmt.setString(1, imageId)
            val rs    = stmt.executeQuery()
            while (rs.next()) {
                albums.add(Album(rs.getString("album_id"), rs.getString("album_name"), rs.getString("album_folder_uuid"), null))
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
        val sql = "SELECT ${ALBUM_TABLE}.modelId AS album_id, ${ALBUM_TABLE}.name AS album_name, ${MASTER_TABLE}.modelid AS coverimage_id, ${VERSION_TABLE}.modelid AS coverimage_version_id, ${MASTER_TABLE}.imagepath AS coverimage_path, (SELECT ${CUSTOM_SORT_ORDER_TABLE}.orderNumber FROM ${CUSTOM_SORT_ORDER_TABLE} WHERE ${CUSTOM_SORT_ORDER_TABLE}.containerUuid = ? AND ${CUSTOM_SORT_ORDER_TABLE}.objectUuid = ${ALBUM_TABLE}.uuid) AS sort_order FROM ${ALBUM_TABLE} INNER JOIN ${VERSION_TABLE} ON ${ALBUM_TABLE}.posterversionuuid = ${VERSION_TABLE}.uuid INNER JOIN ${MASTER_TABLE} ON ${VERSION_TABLE}.masterid = ${MASTER_TABLE}.modelId WHERE ${ALBUM_TABLE}.name IS NOT NULL AND ${ALBUM_TABLE}.name != \"\" AND ${ALBUM_TABLE}.folderUuid IS ? ORDER BY sort_order, ${ALBUM_TABLE}.modelId DESC"

        executeOperation(DATABASE_FILENAME_LIBRARY, { it ->
            val stmt  = it.prepareStatement(sql)
            stmt.setString(1, folderUuid)
            stmt.setString(2, folderUuid)
            val rs    = stmt.executeQuery()

            while (rs.next()) {
                albums.add(Album(rs.getString("album_id"), rs.getString("album_name"), folderUuid, Image(rs.getString("coverimage_id"), rs.getString("coverimage_version_id"), rs.getString("coverimage_path"), null, null)))
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

    fun selectPerson(personId: String): Person?{
        var person: Person? = null
        val sql = "SELECT ${PERSON_TABLE}.modelid as person_id, ${PERSON_TABLE}.name as person_name from ${PERSON_TABLE} where person_id = ?"
        executeOperation(DATABASE_FILENAME_PERSON, { it ->
            val stmt  = it.prepareStatement(sql)
            stmt.setString(1, personId)
            val rs    = stmt.executeQuery()
            while (rs.next()) {
                person = Person(rs.getString("person_id"), rs.getString("person_name"))
            }
        })
        return person
    }

    fun selectAllPeople() : MutableList<Person> {
        val people : MutableList<Person> = mutableListOf()
        val sql = "SELECT ${PERSON_TABLE}.modelid AS person_id, ${PERSON_TABLE}.name AS person_name, ${FACE_TABLE}.imageid as cover_version_uuid FROM ${PERSON_TABLE} INNER JOIN ${FACE_TABLE} ON ${FACE_TABLE}.modelid = ${PERSON_TABLE}.representativeFaceId WHERE person_id IN (SELECT personid FROM ${PERSON_VERSION_TABLE}) ORDER BY ${PERSON_TABLE}.name"

        executeOperation(DATABASE_FILENAME_PERSON, { it ->
            val stmt  = it.createStatement()
            val rs    = stmt.executeQuery(sql)

            while (rs.next()) {
                people.add(Person(rs.getString("person_id"), rs.getString("person_name"), rs.getString("cover_version_uuid")))
            }
        })

        //translate rkversion.uuid to rkimage.modelId

        val versionSql = "SELECT ${MASTER_TABLE}.modelId as master_id, ${VERSION_TABLE}.uuid as version_uuid FROM ${VERSION_TABLE} INNER JOIN ${MASTER_TABLE} ON master_id = ${VERSION_TABLE}.masterid WHERE version_uuid = ?"

        executeOperation(DATABASE_FILENAME_LIBRARY,{ it ->
            val stmt  = it.prepareStatement(versionSql)

            people.forEach {
                stmt.setString(1, it.coverImageId)
                val rs    = stmt.executeQuery()

                while (rs.next()) {
                    it.coverImageId = rs.getString("master_id")
                }
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

    fun selectAllPersonImages(): MutableList<PersonImage>{

        val personVersions: MutableList<PersonVersion> = mutableListOf()
        val versionSql = "SELECT ${PERSON_VERSION_TABLE}.personId as person_id, ${PERSON_VERSION_TABLE}.versionId AS version_id FROM ${PERSON_VERSION_TABLE}"

        executeOperation(DATABASE_FILENAME_PERSON, { it ->
            val stmt  = it.prepareStatement(versionSql)
            val rs    = stmt.executeQuery()
            while (rs.next()) {
                personVersions.add(PersonVersion(rs.getString("person_id"), rs.getString("version_id")))
            }
        })

        val personImages: MutableList<PersonImage> = mutableListOf()


        val sql = "SELECT ${MASTER_TABLE}.modelId as master_id, ${VERSION_TABLE}.modelid as version_id FROM ${VERSION_TABLE} INNER JOIN ${MASTER_TABLE} ON master_id = ${VERSION_TABLE}.masterid WHERE version_id = ?"

        executeOperation(DATABASE_FILENAME_LIBRARY,{ it ->
            val stmt  = it.prepareStatement(sql)

            personVersions.forEach {
                stmt.setString(1, it.versionId)
                val rs    = stmt.executeQuery()

                while (rs.next()) {
                    personImages.add(PersonImage(it.personId, rs.getString("master_id")))
                }
            }

        })

        return personImages
    }

    fun selectAllAlbumImages(): MutableList<AlbumImage>{

        val albumVersions: MutableList<AlbumVersion> = mutableListOf()
        val versionSql = "SELECT albumId as album_id, versionId as version_id from ${ALBUM_VERSION_TABLE}"

        executeOperation(DATABASE_FILENAME_LIBRARY, { it ->
            val stmt  = it.prepareStatement(versionSql)
            val rs    = stmt.executeQuery()
            while (rs.next()) {
                albumVersions.add(AlbumVersion(rs.getString("album_id"), rs.getString("version_id")))
            }
        })

        val albumImages: MutableList<AlbumImage> = mutableListOf()


        val sql = "SELECT ${MASTER_TABLE}.modelId as master_id, ${VERSION_TABLE}.modelid as version_id FROM ${VERSION_TABLE} INNER JOIN ${MASTER_TABLE} ON master_id = ${VERSION_TABLE}.masterid WHERE version_id = ?"

        executeOperation(DATABASE_FILENAME_LIBRARY,{ it ->
            val stmt  = it.prepareStatement(sql)

            albumVersions.forEach {
                stmt.setString(1, it.versionId)
                val rs    = stmt.executeQuery()

                while (rs.next()) {
                    albumImages.add(AlbumImage(it.albumId, rs.getString("master_id")))
                }
            }

        })

        return albumImages
    }
}
