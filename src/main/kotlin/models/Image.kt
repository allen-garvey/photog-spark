package models

/**
 * Created by allen on 3/29/17.
 */

data class Image(val id: String, val versionId: String, val path: String, var thumbnail: Thumbnail?){
    fun equals(otherImage: Image): Boolean {
        return otherImage.id == this.id
    }
}