package views

import models.Image
import models.Thumbnail

/**
 * Created by allen on 3/30/17.
 */

class ImageView: BaseView(){
    fun urlForImage(image: Image?): String{
        if(image != null){
            return "http://photog.alaska.dev/media/images/" + uriEncode(image.path)
        }
        return ""
    }

    fun urlForThumbnail(thumbnail: Thumbnail?): String{
        if(thumbnail != null){
            return "http://photog.alaska.dev/media/thumbnails/" + uriEncode(thumbnail.miniThumbnailPath)
        }
        return ""
    }
}
