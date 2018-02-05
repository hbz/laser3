package de.laser

import groovy.xml.MarkupBuilder
import java.lang.reflect.Method
import grails.web.Action

import java.lang.reflect.Modifier

class SitemapController {

    def grailsApplication

    /**
     * If you want to exclude any controllers in the sitemap, especially Error controllers and services etc, include them in this array
     */
    def controllerNamesToExclude = [ 'sitemap', 'error']

    /**
     * If you want to certain actions excluded, include them in this array. All actions with this name will be ignored
     */
    def actionsToExclude = ['submitForm']

    def index = {
        StringWriter writer = new StringWriter()
        MarkupBuilder mkp = new MarkupBuilder(writer)
        mkp.mkp.xmlDeclaration(version: "1.0", encoding: "UTF-8")
        mkp.urlset(xmlns: "http://www.sitemaps.org/schemas/sitemap/0.9",
                'xmlns:xsi': "http://www.w3.org/2001/XMLSchema-instance",
                'xsi:schemaLocation': "http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd") {

            grailsApplication.controllerClasses.each { controller ->
                Class controllerClass = controller.clazz

                // skip controllers in plugins
                if (controllerClass.name.startsWith('com.k_int.kbplus') && !controllerNamesToExclude.contains(controller.logicalPropertyName)) {
                    String logicalControllerName = controller.logicalPropertyName

                    // get the actions defined as methods (Grails 2)
                    controllerClass.methods.each { Method method ->

                        if (method.getAnnotation(Action) && method.getModifiers() == Modifier.PUBLIC && !actionsToExclude.contains(method.name)) {

                            def viewPart1   = controllerClass.getSimpleName().uncapitalize().replace("Controller", "")
                            def viewPart2   = method.getName() + ".gsp"

                            def ccc = new File('./grails-app/views' + "/" + viewPart1 + "/" + viewPart2)
                            if (ccc.exists()) {
                                mkp.url {
                                    loc(g.createLink(absolute: true, controller: logicalControllerName, action: method.name))
                                    changefreq('hourly')
                                    priority(0.8)
                                }
                            }
                            else {
                                println "IGNORED, because not match from controller.action to gsp: " + ccc.getCanonicalPath()
                            }
                        }
                    }
                }
            }
        }
        render(text: writer.toString(),contentType: "text/xml", encoding: "UTF-8")
    }
}
