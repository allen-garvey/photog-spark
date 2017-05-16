package templates

import kotlinx.html.*
import models.Folder
import models.Image
import models.Person
import views.AlbumView
import views.ImageView

/**
 * Created by allen on 5/16/17.
 */

object PersonTemplate{
    fun show(folders: MutableList<Folder>, person: Person, images: MutableList<Image>): String{
        return Layout.mainLayout(folders, {
            h2 { +person.name }
            ul("thumbnail-list"){
                images.forEach {
                    li("image-container"){
                        a(ImageView.urlForImage(it)){
                            img(src = ImageView.urlForThumbnailMini(it.thumbnail))
                        }
                        if(it.isFavorite){
                            div("heart")
                        }
                    }
                }
            }
        }, subTitle = person.name)
    }
}