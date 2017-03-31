package views

import models.Album

/**
 * Created by allen on 3/30/17.
 */

class AlbumView: BaseView(){
    fun urlForAlbum(album: Album): String{
        return "/albums/" + album.id
    }
}