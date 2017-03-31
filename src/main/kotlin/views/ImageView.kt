package views

import models.Image
import models.Thumbnail

/**
 * Created by allen on 3/30/17.
 */

class ImageView{
    fun urlForImage(image: Image?): String{
        if(image != null){
            return "http://photog.alaska.dev/media/images/" + image.path
        }
        return ""
    }

    fun urlForThumbnail(thumbnail: Thumbnail?): String{
        if(thumbnail != null){
            //some thumbnail paths contain '%'
            return "http://photog.alaska.dev/media/thumbnails/" + thumbnail.miniThumbnailPath.replace("%", "%25")
        }
        return ""
    }
}
