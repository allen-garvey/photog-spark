package views

/**
 * Created by allen on 3/31/17.
 */
abstract class BaseView {
    fun uriEncode(s: String): String{
        return s.replace("%", "%25").replace(" ", "%20")
    }
}