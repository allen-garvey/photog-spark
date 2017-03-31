package views

import models.Image
import models.Thumbnail

/**
 * Created by allen on 3/30/17.
 */

class ImageView: BaseView(){
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
            return "/images/" + image.id
        }
        return ""
    }
}
