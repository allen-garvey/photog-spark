package templates

/**
 * Created by allen on 4/28/17.
 */

import htmlTags.MAIN
import htmlTags.main
import kotlinx.html.*
import models.Folder
import views.AlbumView
import views.BaseView.baseUrl
import views.FolderView
import views.PersonView

object Partial{
    fun mainContentPartial(mainContent: MAIN.() -> kotlin.Unit): BODY.() -> kotlin.Unit{
        return {
            main{
                attributes["class"] = "main container"
                mainContent()
            }
        }
    }

    fun headerPartial(navContent: HEADER.() -> kotlin.Unit = {}): BODY.() -> kotlin.Unit{
        return {
            header{
                attributes["class"] = "header container"
                h1("brand"){
                    a(baseUrl()){
                        +"Photog"
                    }
                }
                navContent()
            }
        }
    }

    fun headerPartial(): BODY.() -> kotlin.Unit{
        return headerPartial(mainNavPartial())
    }

    fun mainNavPartial(): HEADER.() -> kotlin.Unit{
        return {
            nav {
                ul("nav-list"){
                    li {
                        a(AlbumView.indexUrl()){
                            +"Albums"
                        }
                    }
                    li {
                        a(FolderView.indexUrl()){
                            +"Folders"
                        }
                    }
                    li {
                        a(PersonView.indexUrl()){
                            +"People"
                        }
                    }

                }
            }
        }
    }

    fun footerPartial(): BODY.() -> kotlin.Unit{
        return {
            footer("footer container"){
                a("https://github.com/allen-garvey/photog-spark"){
                    +"View source on GitHub"
                }
            }
        }
    }
}