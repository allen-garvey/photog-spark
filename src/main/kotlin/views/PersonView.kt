package views

import models.Person
import views.BaseView.baseUrl

/**
 * Created by allen on 5/17/17.
 */
object PersonView {
    fun urlForPerson(person: Person): String{
        return baseUrl() + "people/${person.id}"
    }
}