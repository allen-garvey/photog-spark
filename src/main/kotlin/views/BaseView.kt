package views

/**
 * Created by allen on 3/31/17.
 */
abstract class BaseView {
    //based on: https://en.wikipedia.org/wiki/Percent-encoding
    fun uriEncode(s: String): String{
        return s
                .replace("%", "%25")
                .replace(" ", "%20")
                .replace("!", "%21")
                .replace("#", "%23")
                .replace("$", "%24")
                .replace("&", "%26")
                .replace("'", "%27")
                .replace("(", "%28")
                .replace(")", "%29")
                .replace("*", "%2A")
                .replace("+", "%2B")
                .replace(",", "%2C")
                .replace(":", "%3A")
                .replace(";", "%3B")
                .replace("=", "%3D")
                .replace("?", "%3F")
                .replace("@", "%40")
                .replace("[", "%5B")
                .replace("]", "%5D")
    }
}