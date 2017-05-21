package controllers

import views.ImageView
import models.Image
import models.Person
import spark.Request
import spark.Response
import templates.PersonTemplate
import templates.SharedTemplate
import views.Link
import views.PersonView

/**
 * Created by allen on 5/16/17.
 */
object PersonController {
    fun index(request: Request, response: Response): String {
        val people: MutableList<Person> = SqliteController.selectAllPeople()
        val peopleLinks: List<Link> = people.map { Link(it.name, PersonView.urlForPerson(it)) }

        return SharedTemplate.textListPage("People", peopleLinks, "All people")
    }


    fun show(request: Request, response: Response, personIdParameterName: String): String {
        val person: Person = SqliteController.selectPerson(request.params(personIdParameterName)) ?: return ErrorController.notFound(request, response)

        val images: MutableList<Image> = SqliteController.imagesForPerson(person.id)

        return SharedTemplate.imageListPage(person.name, images, { ImageView.urlForImage(it) })

    }
}