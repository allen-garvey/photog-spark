package templates

/**
 * Created by allen on 5/16/17.
 */

import kotlinx.html.*
import models.Folder
import models.Image
import views.ImageView
import views.Link

object SharedTemplate{
    fun imageListPage(pageTitle: String, images: MutableList<Image>, imageUrlFunc: (Image) -> String): String{
        return Layout.mainLayout({
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

    fun textListPage(listTitle: String, links: List<Link>, pageTitle: String): String{
        return Layout.mainLayout({
            h2{ +listTitle }
            ul("text-list"){
                links.forEach {
                    li {
                        a(it.url){
                            +it.title
                        }
                    }
                }
            }
        }, subTitle = pageTitle)
    }
}