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
    fun index(people: MutableList<Person>): String{
        return Layout.mainLayout({
            h2{ +"People" }
            ul("text-list"){
                people.forEach {
                    li {
                        a(PersonView.urlForPerson(it)){
                            +it.name
                        }
                    }
                }
            }
        }, subTitle = "All people")
    }
}