package views

import models.Album
import models.Image
import views.BaseView.baseUrl
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by allen on 3/30/17.
 */

object AlbumView{
    fun indexUrl(): String{
        return baseUrl() + "albums"
    }

    fun urlForAlbum(album: Album): String{
        return  indexUrl() + "/" + album.id
    }

    fun urlForAlbumImage(album: Album, image: Image): String{
        return indexUrl() + "/" + album.id + "/images/" + image.id + "#" + idAttributeForAlbumImage(image)
    }
    fun idAttributeForAlbumImage(image: Image): String{
        return "photo_id" + image.id
    }

    fun creationDate(album: Album): String?{
        if(album.creation == null){
            return null
        }
        val date = Date()
        date.setTime(album.creation.time)

        val dateFormatter = SimpleDateFormat("MM/dd/yyyy h:mm a")
        dateFormatter.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormatter.format(date)
    }
}