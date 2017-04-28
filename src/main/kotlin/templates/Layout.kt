package templates


import htmlTags.MAIN
import htmlTags.main
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import models.Folder
import templates.Partial.mainContentPartial

/**
 * Created by allen on 4/27/17.
 */


object Layout {

    fun default(mainContent: MAIN.() -> kotlin.Unit, headerContent: BODY.() -> kotlin.Unit = Partial.headerPartial(), subTitle: String? = null): String{
        val defaultPageTitle: String = "Photog"
        val pageTitle: String = if(subTitle != null) "${defaultPageTitle} | ${subTitle}" else defaultPageTitle

        val s = StringBuilder()
        s.append("<!DOCTYPE html>")

        return s.appendHTML().html {
            head{
                meta{
                    attributes["charset"] = "utf-8"
                }
                meta("viewport", "width=device-width, initial-scale=1")
                title(pageTitle)
                meta("description", "Display your Photos Library with Photog")
                link(href="/styles/style.css", rel="stylesheet", type="text/css")

            }
            body{
                headerContent()
                (mainContentPartial({
                    mainContent()
                }))()
                (Partial.footerPartial())()
            }
        }.toString()
    }


    fun mainLayout(folders: MutableList<Folder>, mainContent: MAIN.() -> kotlin.Unit, subTitle: String? = null): String{
        return default(mainContent, headerContent = Partial.headerPartial(folders), subTitle = subTitle)
    }
}