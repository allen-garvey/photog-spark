package views

import spark.template.handlebars.HandlebarsTemplateEngine

/**
 * Created by allen on 3/30/17.
 */

class CustomHandlebarsTemplateEngine: HandlebarsTemplateEngine(){

    fun registerHelpers(helperSource: Any): Unit {
        handlebars.registerHelpers(helperSource)
    }
}
