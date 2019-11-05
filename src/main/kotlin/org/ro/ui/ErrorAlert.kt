package org.ro.ui

import org.ro.core.event.LogEntry
import org.ro.org.ro.ui.kv.RoDialog
import org.ro.to.HttpError
import org.ro.ui.uicomp.FormItem

class ErrorAlert(val logEntry: LogEntry) : Command {

    fun open() {
        val error = logEntry.getTransferObject() as HttpError
        val formItems = mutableListOf<FormItem>()
        formItems.add(FormItem("URL", "Text", logEntry.url))
        formItems.add(FormItem("Message", "Text", error.message))
        val detail = error.detail
        if (detail != null) {
            formItems.add(FormItem("StackTrace", "TextArea", toString(detail.element), 20))
            formItems.add(FormItem("Caused by", "Text", detail.causedBy))
        }
        val label = "HttpError " + error.httpStatusCode.toString()
        RoDialog(caption = label, items = formItems, command = this).show()
    }

    override fun execute() {
        //do nothing
    }

    private fun toString(stackTrace: List<String>): String {
        var answer = ""
        for (s in stackTrace) {
            answer += s + "\n"
        }
        return answer
    }

}
