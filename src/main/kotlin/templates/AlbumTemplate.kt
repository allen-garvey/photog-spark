package templates

import kotlinx.html.*
import models.Album
import models.Folder
import models.Image
import views.AlbumView
import views.ImageView

/**
 * Created by allen on 4/28/17.
 */
object AlbumTemplate{
    fun index(albums: MutableList<Album>, subTitle: String? = null): String{
        val pageSubTitle: String = subTitle ?: "All albums"
        return Layout.mainLayout({
            ul("album-list thumbnail-list"){
                albums.forEach {
                    li {
                        a(AlbumView.urlForAlbum(it), classes = "image-container"){
                            img(src = ImageView.urlForThumbnailMini(it.coverImage?.thumbnail), alt = "Thumbnail for ${it.name}")
                        }
                        h3("album-title"){
                            a(AlbumView.urlForAlbum(it)){
                                +it.name
                            }
                        }
                    }
                }
            }
        }, subTitle = pageSubTitle)
    }
    
}