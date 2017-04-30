package views

import models.Album
import models.Image
import views.BaseView.baseUrl

/**
 * Created by allen on 3/30/17.
 */

object AlbumView{
    fun urlForAlbum(album: Album): String{
        return  baseUrl() + "albums/" + album.id
    }

    fun urlForAlbumImage(album: Album, image: Image): String{
        return baseUrl() + "albums/" + album.id + "/images/" + image.id + "#" + idAttributeForAlbumImage(image)
    }
    fun idAttributeForAlbumImage(image: Image): String{
        return "photo_id" + image.id
    }
}