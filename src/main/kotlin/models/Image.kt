package models

import java.sql.Timestamp

/**
 * Created by allen on 3/29/17.
 */

data class Image(val id: String, val versionId: String, val path: String, val creation: Timestamp?, var thumbnail: Thumbnail?){
    fun equals(otherImage: Image): Boolean {
        return otherImage.id == this.id
    }
}