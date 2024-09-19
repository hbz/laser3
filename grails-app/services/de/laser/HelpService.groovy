package de.laser

import com.vladsch.flexmark.parser.ParserEmulationProfile
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.formatter.Formatter
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataHolder
import com.vladsch.flexmark.util.data.MutableDataSet
import de.laser.config.ConfigMapper
import de.laser.remote.ApiSource
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
            md = replaceTokens( md )

            Parser parser = getMarkdownParser()
            Node document = parser.parse( md )
            HtmlRenderer renderer = getMarkdownHtmlRenderer()
            renderer.render( document )

//            Formatter renderer = getMarkdownFormatter()
//            renderer.render( document )
        }
        catch (Exception e) {
            e.getMessage()
        }
    }

    Map<String, String> getTokenMap() {
        [
            'current_server_laser'  : ConfigMapper.getGrailsServerURL(),
            'current_server_wekb'   : ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)?.baseUrl, // ConfigMapper.getWekbServerURL(), // TODO
            'current_server_webapp' : ConfigMapper.getGrailsServerURL() + '/static',
        ]
    }

    String replaceTokens(String markdown) {
        getTokenMap().each {
            markdown = markdown.replaceAll( ~/\{\{${it.key}\}\}/, it.value )
        }
        markdown
    }

    Parser getMarkdownParser() {
        MutableDataHolder options = new MutableDataSet()
        options.set(Parser.PARSER_EMULATION_PROFILE, ParserEmulationProfile.COMMONMARK)

        Parser.builder(options).build()
    }

    HtmlRenderer getMarkdownHtmlRenderer() {
        MutableDataHolder optionsRenderer = new MutableDataSet()
        optionsRenderer.set(HtmlRenderer.AUTOLINK_WWW_PREFIX, 'https://')
        optionsRenderer.set(HtmlRenderer.ESCAPE_HTML, true)

        HtmlRenderer.builder(optionsRenderer).build()
    }

    Formatter getMarkdownFormatter() {
        MutableDataHolder optionsRenderer = new MutableDataSet()
        Formatter.builder(optionsRenderer).build()
    }
}