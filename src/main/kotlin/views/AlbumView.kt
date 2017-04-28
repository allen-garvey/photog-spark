package views

import models.Album
import models.Image

/**
 * Created by allen on 3/30/17.
 */

class AlbumView: BaseView(){
    fun urlForAlbum(album: Album): String{
        return "/albums/" + album.id
    }

    fun urlForAlbumImage(album: Album, image: Image): String{
        return "/albums/" + album.id + "/images/" + image.id + "#" + idAttributeForAlbumImage(image)
    }
    fun idAttributeForAlbumImage(image: Image): String{
        return "photo_id" + image.id
    }
}