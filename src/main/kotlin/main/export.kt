package main

import controllers.SqliteController


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

fun sqlOptionalInt(s: String?): String{
    if(s == null){
        return "null"
    }
    return s
}


fun main(args: Array<String>) {
    if(args.size >= 1){
        SqliteController.databaseRoot = args[0]
    }



    println("--Folders\n")
    SqliteController.selectAllFolders().forEach{
        println("INSERT INTO folders (apple_photos_uuid, name) VALUES (${sqlEscapeString(it.uuid)}, ${sqlEscapeString(it.name)});")
    }

    println("--Albums\n")
    SqliteController.selectAllAlbums().forEach{
        println("INSERT INTO albums (apple_photos_id, name, folder_id, cover_image_id) VALUES (${it.id}, ${sqlEscapeString(it.name)}, ${sqlEscapeString(it.folderUuid)}, ${sqlOptionalInt(it.coverImage?.id)});")
    }
}