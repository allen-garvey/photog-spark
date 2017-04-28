package htmlTags

import kotlinx.html.*

/**
 * Created by allen on 4/28/17.
 */
class MAIN(consumer: TagConsumer<*>) :
        HTMLTag("main", consumer, emptyMap(),
                inlineTag = false,
                emptyTag = false), HtmlBlockTag {
}

fun BODY.main(block: MAIN.() -> Unit = {}) {
    MAIN(consumer).visit(block)
}