package templates

/**
 * Created by allen on 4/28/17.
 */

import htmlTags.MAIN
import htmlTags.main
import kotlinx.html.*
import models.Folder
import views.BaseView.baseUrl
import views.FolderView

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

    fun headerPartial(folders: MutableList<Folder>): BODY.() -> kotlin.Unit{
        return headerPartial(mainNavPartial(folders))
    }

    fun mainNavPartial(folders: MutableList<Folder>): HEADER.() -> kotlin.Unit{
        return {
            nav {
                ul("nav-list"){
                    folders.forEach {
                        li {
                            a(FolderView.urlForFolder(it)){
                                +it.name
                            }
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