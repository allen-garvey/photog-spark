package templates

import kotlinx.html.*
import models.Folder
import models.Image
import models.Person
import views.ImageView
import views.PersonView

/**
 * Created by allen on 5/16/17.
 */

object PersonTemplate{
    fun index(folders: MutableList<Folder>, people: MutableList<Person>): String{
        return Layout.mainLayout(folders, {
            ul(""){
                people.forEach {
                    li {
                        a(PersonView.urlForPerson(it), classes = "image-container"){
                            +it.name
                        }
                    }
                }
            }
        }, subTitle = "All people")
    }
}