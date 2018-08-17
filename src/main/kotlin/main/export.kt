package main

import controllers.SqliteController
import java.sql.Timestamp


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
    return "to_timestamp('${t.toString().replace(Regex("\\.0$"), "")}', 'YYYY-MM-DD HH:MI:SS')"
}


fun main(args: Array<String>) {
    if(args.size >= 1){
        SqliteController.databaseRoot = args[0]
    }



    println("\n\n--Folders\n")
    SqliteController.selectAllFolders().forEach{
        println("INSERT INTO folders (apple_photos_uuid, name) VALUES (${sqlEscapeString(it.uuid)}, ${sqlEscapeString(it.name)});")
    }

    println("\n\n--Albums\n")
    var albums = SqliteController.selectAllAlbums()
    albums.sortBy({it.id.toInt()})
    albums.forEach{
        println("INSERT INTO albums (apple_photos_id, name, folder_id, cover_image_id) VALUES (${it.id}, ${sqlEscapeString(it.name)}, ${sqlEscapeString(it.folderUuid)}, ${sqlOptionalInt(it.coverImage?.id)});")
    }


    println("\n\n--People\n")
    var people = SqliteController.selectAllPeople()
    people.sortBy({it.id.toInt()})
    people.forEach{
        println("INSERT INTO persons (apple_photos_id, name) VALUES (${it.id}, ${sqlEscapeString(it.name)});")
    }


    println("\n\n--Images\n")
    SqliteController.selectAllImages().forEach{
        println("INSERT INTO images (apple_photos_id, apple_photos_version_id, creation_time, master_path, thumbnail_path, mini_thumbnail_path, is_favorite) VALUES (${it.id}, ${it.versionId}, ${sqlTimestamp(it.creation!!)}, ${sqlEscapeString(it.path)}, ${sqlEscapeString(it.thumbnail!!.thumbnailPath)}, ${sqlEscapeString(it.thumbnail!!.miniThumbnailPath)}, ${sqlBool(it.isFavorite)});")
    }



    println("\n\n--Person Images\n")
    SqliteController.selectAllPersonImages().forEach{
        println("INSERT INTO person_images (person_id, image_id) VALUES (${it.personId}, ${it.imageId});")
    }


}