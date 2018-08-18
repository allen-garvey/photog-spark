package models

/**
 * Created by allen on 3/29/17.
 */

data class Album(val id:String, val name: String, val folderUuid: String, val coverImage: Image?, val folderOrder: Int?=null)