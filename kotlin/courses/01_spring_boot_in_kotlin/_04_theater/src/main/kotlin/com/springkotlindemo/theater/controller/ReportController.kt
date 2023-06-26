package com.springkotlindemo.theater.controller

import com.springkotlindemo.theater.service.ReportingService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import javax.websocket.server.PathParam
import kotlin.reflect.full.declaredMemberFunctions

@Controller
@RequestMapping("/reports")
class ReportController(val reportingService: ReportingService) {
    // example of use of reflection to get the list of the name of the functions
    // returns functions explicitly declared in the class
    private fun getListOfReports() = reportingService::class.declaredMemberFunctions.map { it.name }

    @RequestMapping("")
    fun main() = ModelAndView("reports", mapOf("reports" to getListOfReports()))

    @RequestMapping( "/getReport")
    fun getReport(@PathParam("report") report: String): ModelAndView {
        val matchedReport = reportingService::class.declaredMemberFunctions.firstOrNull { it.name == report }

        // this way we call the function, passing the instance
        // we see if we matched the report and if the string is result or not, if its empty we return an empty string
        val result = matchedReport?.call(reportingService) ?: ""
        return ModelAndView("reports", mapOf("reports" to getListOfReports(), "result" to result))
    }
}
