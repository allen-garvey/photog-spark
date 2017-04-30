package views

import models.Image
import models.Thumbnail
import views.BaseView.baseUrl
import views.BaseView.uriEncode

/**
 * Created by allen on 3/30/17.
 */

object ImageView{
    fun urlForImageFull(image: Image?): String{
        if(image != null){
            return "http://photog.alaska.dev/media/images/" + uriEncode(image.path)
        }
        return ""
    }

    fun urlForThumbnailMini(thumbnail: Thumbnail?): String{
        if(thumbnail != null){
            return "http://photog.alaska.dev/media/thumbnails/" + uriEncode(thumbnail.miniThumbnailPath)
        }
        return ""
    }

    fun urlForThumbnail(thumbnail: Thumbnail?): String{
        if(thumbnail != null){
            return "http://photog.alaska.dev/media/thumbnails/" + uriEncode(thumbnail.thumbnailPath)
        }
        return ""
    }

    fun urlForImage(image: Image?): String{
        if(image != null){
            return  baseUrl() + "images/" + image.id
        }
        return ""
    }
    //handlebars helper functions can only return a string, not boolean
    //so we have to do some workarounds
    fun imageIsEqual(image1: Image?, image2: Image?): String?{
        if(image1 == null){
            return null
        }
        if(image2 == null){
            return null
        }
        if(image1.equals(image2)){
            return "true"
        }
        else{
            return null
        }
    }
}
