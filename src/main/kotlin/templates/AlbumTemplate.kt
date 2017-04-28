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
    fun index(folders: MutableList<Folder>, albums: MutableList<Album>, subTitle: String? = null): String{
        val pageSubTitle: String = subTitle ?: "All albums"
        return Layout.mainLayout(folders, {
            ul("album-list thumbnail-list"){
                albums.forEach {
                    li {
                        a(AlbumView().urlForAlbum(it), classes = "image-container"){
                            img(src = ImageView().urlForThumbnailMini(it.coverImage?.thumbnail), alt = "Thumbnail for ${it.name}")
                        }
                        h3("album-title"){
                            a(AlbumView().urlForAlbum(it)){
                                +it.name
                            }
                        }
                    }
                }
            }
        }, subTitle = pageSubTitle)
    }

    fun show(folders: MutableList<Folder>, album: Album, images: MutableList<Image>): String{
        return Layout.mainLayout(folders, {
            h2 { +album.name }
            ul("thumbnail-list"){
                images.forEach {
                    li("image-container"){
                        a(AlbumView().urlForAlbumImage(album, it)){
                            img(src = ImageView().urlForThumbnailMini(it.thumbnail))
                        }
                    }
                }
            }
        }, subTitle = album.name)
    }
}