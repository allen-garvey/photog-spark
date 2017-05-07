package views

import models.Image
import models.Thumbnail
import views.BaseView.baseUrl
import views.BaseView.uriEncode
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by allen on 3/30/17.
 */

object ImageView{
    val ASSET_IMAGE_BASE_URL: String = "http://photog.alaska.dev/media/images/"
    val ASSET_THUMBNAIL_BASE_URL: String = "http://photog.alaska.dev/media/thumbnails/"

    fun urlForImageFull(image: Image?): String{
        if(image != null){
            return ASSET_IMAGE_BASE_URL + uriEncode(image.path)
        }
        return ""
    }

    fun urlForThumbnailMini(thumbnail: Thumbnail?): String{
        if(thumbnail != null){
            return ASSET_THUMBNAIL_BASE_URL + uriEncode(thumbnail.miniThumbnailPath)
        }
        return ""
    }

    fun urlForThumbnail(thumbnail: Thumbnail?): String{
        if(thumbnail != null){
            return ASSET_THUMBNAIL_BASE_URL + uriEncode(thumbnail.thumbnailPath)
        }
        return ""
    }

    fun urlForImage(image: Image?): String{
        if(image != null){
            return  baseUrl() + "images/" + image.id
        }
        return ""
    }

    fun creationDate(image: Image): String?{
        if(image.creation == null){
            return null
        }
        val date = Date()
        date.setTime(image.creation.time)

        val dateFormatter = SimpleDateFormat("MM/dd/yyyy h:mm a")
        dateFormatter.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormatter.format(date)
    }
}
