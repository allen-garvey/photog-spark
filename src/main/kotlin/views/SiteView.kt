package views

/**
 * Created by allen on 4/1/17.
 */
class SiteView: BaseView() {
    fun pageTitle(subtitle: String): String{
        return "| " + subtitle
    }
}