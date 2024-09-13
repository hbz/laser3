package de.laser


import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import de.laser.storage.BeanStore
import grails.gorm.transactions.Transactional
import grails.gsp.PageRenderer
import grails.util.Holders

@Transactional
class HelpService {

    ContextService contextService

    List<String> getActiveMappings() {

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

    boolean isActiveMapped(String controllerName, String actionName) {
        String pattern = getMapping(controllerName, actionName)
        getActiveMappings().contains(pattern)
    }

    String parseMarkdown(String file) {
        try {
            String md = Holders.grailsApplication.mainContext.getResource( file ).file.text
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
//        String md =  gpr.render(view: '/help/flyouts/test.md')
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
//            gpr.render(template: '/help/flyouts/' + pattern)
//        }
//        catch (Exception e) {
//            e.getMessage()
//        }
//    }
}