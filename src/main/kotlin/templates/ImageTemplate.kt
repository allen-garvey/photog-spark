package templates

/**
 * Created by allen on 4/28/17.
 */

import htmlTags.MAIN
import kotlinx.html.*
import models.Album
import models.Folder
import models.Image
import views.AlbumView
import views.ImageView

object ImageTemplate{
    fun showAlbumImage(image: Image, albums: MutableList<Album>, parentAlbum: Album, images: MutableList<Image>, previousImage: Image?, nextImage: Image?): String{
        return show(image, albums, header = albumImageHeader(image, parentAlbum, images, previousImage, nextImage))
    }

    fun albumImageHeader(image: Image, parentAlbum: Album, images: MutableList<Image>, previousImage: Image?, nextImage: Image?): MAIN.() -> kotlin.Unit{
        return {
            div("album-image-show-header"){
                a(AlbumView().urlForAlbum(parentAlbum)){ +"Back to ${parentAlbum.name}" }
                div("album-image-nav"){
                    if(previousImage != null){
                        a(AlbumView().urlForAlbumImage(parentAlbum, previousImage)){ "Previous" }
                    }
                    else{
                        div()
                    }
                    if(nextImage != null){
                        a(AlbumView().urlForAlbumImage(parentAlbum, nextImage)){ "Next" }
                    }
                }
                div("album-image-nav-previews"){
                    ul("image-preview-list"){
                        images.forEach {
                            li{
                                attributes["id"] = AlbumView().idAttributeForAlbumImage(it)
                                if(it == image){
                                    attributes["class"] = "current-image"
                                }
                                a(AlbumView().urlForAlbumImage(parentAlbum, it), classes = "preview-container"){
                                    img(src = ImageView().urlForThumbnailMini(it.thumbnail))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun show(image: Image, albums: MutableList<Album>, header: MAIN.() -> kotlin.Unit = {}): String{
        return Layout.default({
            header()
            div("image-show-thumbnail-container"){
                a(ImageView().urlForImageFull(image)){
                    img(src=ImageView().urlForThumbnail(image.thumbnail))

                }
            }
            div("image-show-link-container"){
                a(ImageView().urlForImageFull(image)){ +"View full-size" }
            }
            div("image-show-albums"){
                h3("subsection-title"){ +"Albums" }
                ul("image-show-album-list"){
                    albums.forEach {
                        li("image-container"){
                            a(AlbumView().urlForAlbum(it)){ +it.name }
                        }
                    }
                }
            }
        }, subTitle = "Image detail")
    }


}