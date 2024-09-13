package de.laser


import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import grails.gorm.transactions.Transactional
import org.grails.io.support.GrailsResourceUtils

@Transactional
class HelpService {

    public static final String GSP = 'GSP'
    public static final String MD  = 'MD'

    ContextService contextService

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

    String getFlag(String controllerName, String actionName) {
        String flag

        String mapping  = getMapping(controllerName, actionName)
        String file     = 'help/_' + mapping + '.gsp'
        URL resource    = getResource( file )

        if (resource) {
            flag = GSP
        }
        else {
            file = 'help/' + mapping + '.md'
            resource = getResource( file )

            if (resource) {
                flag = MD
            }
        }
        flag
    }

    String parseMarkdown(String file) {
        try {
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
}