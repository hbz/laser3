package de.laser


import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import grails.gorm.transactions.Transactional
import org.grails.io.support.GrailsResourceUtils

@Transactional
class HelpService {

    ContextService contextService

    List<String> getAllMappings() {

        [
            'dev_frontend',
            'finance_subFinancialData',
            'myInstitution_financeImport',
            'myInstitution_subscriptionImport',
            'subscription_show'
        ]
    }

    String getMapping(String controllerName, String actionName) {
        controllerName + '_' + actionName
    }

    URL getResource(String file) {
        URL url = GrailsResourceUtils.getClassLoader().getResource( file ) // resources
        if (url) {
            println file + ' >> ' + url
        }
        url
    }

    boolean isActive(String controllerName, String actionName) {
        String mapping = getMapping(controllerName, actionName)
        //        getAllMappings().contains(mapping) // webapp

        String file = 'help/_' + mapping + '.gsp'
        getResource( file )
    }

    String parseMarkdown(String file) {
        try {
//            String md = Holders.grailsApplication.mainContext.getResource( file ).file.text // webapp

            URL url = getResource( file ) // resources
            String md = new File(url.file).text
            Parser parser = Parser.builder().build()
            Node document = parser.parse( md )
            HtmlRenderer renderer = HtmlRenderer.builder().build()
            renderer.render(document)
        }
        catch (Exception e) {
            e.getMessage()
        }
    }

//    def testMarkdown() {
//        PageRenderer gpr = BeanStore.getGroovyPageRenderer()
//        String md =  gpr.render(view: '/help/test.md')
//        String md = Holders.grailsApplication.mainContext.getResource('./help/test.md').file.text
//        Parser parser = Parser.builder().build()
//        Node document = parser.parse( md )
//        HtmlRenderer renderer = HtmlRenderer.builder().build()
//        renderer.render(document)
//    }

//    String getFlyoutContent(String controllerName, String actionName) {
//        try {
//            String pattern = getMapping(controllerName, actionName)
//            PageRenderer gpr = BeanStore.getGroovyPageRenderer()
//            gpr.render(template: '/help/' + pattern)
//        }
//        catch (Exception e) {
//            e.getMessage()
//        }
//    }
}