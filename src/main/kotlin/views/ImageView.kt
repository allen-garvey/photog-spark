package views

import models.Image

/**
 * Created by allen on 3/30/17.
 */

class ImageView{
    fun urlForImage(image: Image?): String{
        if(image != null){
            return "http://google.com/" + image.path
        }
        return ""
    }
}
