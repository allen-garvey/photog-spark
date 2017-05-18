package controllers

import views.ImageView
import models.Image
import models.Person
import spark.Request
import spark.Response
import templates.PersonTemplate
import templates.SharedTemplate

/**
 * Created by allen on 5/16/17.
 */
object PersonController {
    fun index(request: Request, response: Response): String {
        return PersonTemplate.index(SqliteController.selectAllPeople())
    }


    fun show(request: Request, response: Response, personIdParameterName: String): String {
        val person: Person = SqliteController.selectPerson(request.params(personIdParameterName)) ?: return ErrorController.notFound(request, response)

        val images: MutableList<Image> = SqliteController.imagesForPerson(person.id)

        return SharedTemplate.imageListPage(person.name, images, { ImageView.urlForImage(it) })

    }
}