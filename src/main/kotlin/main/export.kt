/*
* * Exports Apple Photos database in format suitable for https://github.com/allen-garvey/photog-phoenix
* */

package main

import controllers.SqliteController
import models.Folder
import models.Import
import java.sql.Timestamp

val IMAGES_TABLE_NAME = "images"
val ALBUMS_TABLE_NAME = "albums"
val PEOPLE_TABLE_NAME = "persons"
val TAGS_TABLE_NAME = "tags"
val ALBUM_TAGS_TABLE_NAME = "album_tags"
val IMPORTS_TABLE_NAME = "imports"

val TIMESTAMPS_COLUMN_NAMES = ",inserted_at, updated_at"
val TIMESTAMPS_COLUMN_VALUES = ",now(), now()"


fun sqlEscapeOptionalString(s: String?): String{
    if(s == null){
        return "null"
    }
    return sqlEscapeString(s)
}

fun sqlEscapeString(s: String): String{
    return "'${s.replace("'", "''")}'"
}

fun sqlOptionalInt(d: Int?): String{
    if(d == null){
        return "null"
    }
    return d.toString()
}

fun sqlBool(b: Boolean = false): String{
    if(b){
        return "true"
    }
    return "false"
}

fun sqlOptionalInt(s: String?): String{
    if(s == null){
        return "null"
    }
    return s
}

//postgresql to timestamp https://www.postgresql.org/docs/8.2/static/functions-formatting.html
fun sqlTimestamp(t: Timestamp): String{
//    return "to_timestamp('${t.toInstant().toString()}', 'YYYY-MM-DD HH:MI:SS')"
    return "to_timestamp('${t.toString().replace(Regex("\\.0$"), "")}', 'YYYY-MM-DD HH24:MI:SS')"
}

fun sqlDate(t: Timestamp): String{
    return "to_date('${t.toString().replace(Regex(" .*$"), "")}', 'YYYY-MM-DD')"
}

fun relatedImageId(imageId: String): String{
    return "(SELECT ${IMAGES_TABLE_NAME}.id FROM ${IMAGES_TABLE_NAME} WHERE ${IMAGES_TABLE_NAME}.apple_photos_id = ${imageId} LIMIT 1)"
}

fun relatedPersonId(personId: String): String{
    return "(SELECT ${PEOPLE_TABLE_NAME}.id FROM ${PEOPLE_TABLE_NAME} WHERE ${PEOPLE_TABLE_NAME}.apple_photos_id = ${personId} LIMIT 1)"
}

fun relatedAlbumId(albumId: String): String{
    return "(SELECT ${ALBUMS_TABLE_NAME}.id FROM ${ALBUMS_TABLE_NAME} WHERE ${ALBUMS_TABLE_NAME}.apple_photos_id = ${albumId} LIMIT 1)"
}

fun relatedFolderUuid(folderUuid: String): String{
    return "(SELECT ${TAGS_TABLE_NAME}.id FROM ${TAGS_TABLE_NAME} WHERE ${TAGS_TABLE_NAME}.apple_photos_uuid = ${folderUuid} LIMIT 1)"
}

fun relatedImportUuid(importUuid: String): String{
    return "(SELECT ${IMPORTS_TABLE_NAME}.id FROM ${IMPORTS_TABLE_NAME} WHERE ${IMPORTS_TABLE_NAME}.apple_photos_uuid = ${importUuid} LIMIT 1)"
}


fun main(args: Array<String>) {
    if(args.size >= 1){
        SqliteController.databaseRoot = args[0]
    }

    //create import resources
    println("\n\n--Imports\n")
    val imports = SqliteController.selectAllImports()
    imports.forEach {
        println("INSERT INTO ${IMPORTS_TABLE_NAME} (apple_photos_uuid, import_time ${TIMESTAMPS_COLUMN_NAMES} ) VALUES (${sqlEscapeString(it.uuid)}, ${sqlTimestamp(it.timestamp)} ${TIMESTAMPS_COLUMN_VALUES});")
    }


    println("\n\n--Tags (called Folders in Apple Photos)\n")
    var folders = SqliteController.selectAllFolders()
    //have to add library folder since it is an implicit folder not contained in table
    folders.add(Folder("LibraryFolder", "Library"))
    folders.reverse()
    folders.forEach{
        println("INSERT INTO ${TAGS_TABLE_NAME} (apple_photos_uuid, name ${TIMESTAMPS_COLUMN_NAMES}) VALUES (${sqlEscapeString(it.uuid)}, ${sqlEscapeString(it.name)} ${TIMESTAMPS_COLUMN_VALUES});")
    }

    println("\n\n--Images\n")
    SqliteController.selectAllImages().forEach{
        println("INSERT INTO ${IMAGES_TABLE_NAME} (import_id, apple_photos_id, creation_time, master_path, thumbnail_path, mini_thumbnail_path, is_favorite ${TIMESTAMPS_COLUMN_NAMES}) VALUES (${relatedImportUuid(sqlEscapeString(it.importUuid!!))}, ${it.id}, ${sqlTimestamp(it.creation!!)}, ${sqlEscapeString(it.path)}, ${sqlEscapeString(it.thumbnail!!.thumbnailPath)}, ${sqlEscapeString(it.thumbnail!!.miniThumbnailPath)}, ${sqlBool(it.isFavorite)} ${TIMESTAMPS_COLUMN_VALUES});")
    }

    
    println("\n\n--Albums\n")
    var albums = SqliteController.selectAllAlbums()
    albums.sortBy({it.id.toInt()})
    albums.forEach{
        println("INSERT INTO ${ALBUMS_TABLE_NAME} (apple_photos_id, name, cover_image_id, creation_date ${TIMESTAMPS_COLUMN_NAMES}) VALUES (${it.id}, ${sqlEscapeString(it.name)}, ${relatedImageId(it.coverImage!!.id)}, ${sqlDate(it.creation!!)} ${TIMESTAMPS_COLUMN_VALUES});")
    }

    println("\n\n--Album Tags\n")
    albums.forEach{
        println("INSERT INTO ${ALBUM_TAGS_TABLE_NAME} (album_id, tag_id, album_order ${TIMESTAMPS_COLUMN_NAMES}) VALUES (${relatedAlbumId(it.id)}, ${relatedFolderUuid(sqlEscapeString(it.folderUuid))}, ${it.folderOrder!!} ${TIMESTAMPS_COLUMN_VALUES});")
    }


    println("\n\n--People\n")
    var people = SqliteController.selectAllPeople()
    people.sortBy({it.id.toInt()})
    people.forEach{
        println("INSERT INTO ${PEOPLE_TABLE_NAME} (apple_photos_id, name, cover_image_id ${TIMESTAMPS_COLUMN_NAMES}) VALUES (${it.id}, ${sqlEscapeString(it.name)}, ${relatedImageId(it.coverImageId!!)} ${TIMESTAMPS_COLUMN_VALUES});")
    }



    println("\n\n--Person Images\n")
    SqliteController.selectAllPersonImages().forEach{
        println("INSERT INTO person_images (person_id, image_id ${TIMESTAMPS_COLUMN_NAMES}) VALUES (${relatedPersonId(it.personId)}, ${relatedImageId(it.imageId)} ${TIMESTAMPS_COLUMN_VALUES});")
    }

    println("\n\n--Album Images\n")
    SqliteController.selectAllAlbumImages().forEach{
        println("INSERT INTO album_images (album_id, image_id, image_order ${TIMESTAMPS_COLUMN_NAMES}) VALUES (${relatedAlbumId(it.albumId)}, ${relatedImageId(it.imageId)}, ${it.order} ${TIMESTAMPS_COLUMN_VALUES});")
    }

}