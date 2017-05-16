package templates

/**
 * Created by allen on 5/16/17.
 */

import kotlinx.html.*
import models.Folder
import models.Image
import views.ImageView

object SharedTemplate{
    fun imageListPage(folders: MutableList<Folder>, pageTitle: String, images: MutableList<Image>, imageUrlFunc: (Image) -> String): String{
        return Layout.mainLayout(folders, {
            h2 { +pageTitle }
            ul("thumbnail-list"){
                images.forEach {
                    li("image-container"){
                        a(imageUrlFunc(it)){
                            img(src = ImageView.urlForThumbnailMini(it.thumbnail))
                        }
                        if(it.isFavorite){
                            div("heart")
                        }
                    }
                }
            }
        }, subTitle = pageTitle)
    }
}