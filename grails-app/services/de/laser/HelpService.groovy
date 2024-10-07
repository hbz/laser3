package de.laser

import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.parser.ParserEmulationProfile
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.formatter.Formatter
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataHolder
import com.vladsch.flexmark.util.data.MutableDataSet
import com.vladsch.flexmark.util.misc.Extension
import de.laser.config.ConfigMapper
import de.laser.flexmark.BaseExtension
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
//        if (url) {
//            println file + ' >> ' + url
//        }
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
            log.warn 'parseMardown( ' + file + ' ) > ' + e.getMessage()
            return '...'
        }
    }

    String parseMarkdown2(String text) {
        try {
            String md = replaceTokens( text )

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
            'current_server_assets' : ConfigMapper.getGrailsServerURL() + '/assets',
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
        options.set(Parser.EXTENSIONS, Arrays.asList(
                new Extension[] { BaseExtension.create(), StrikethroughExtension.create(), TablesExtension.create() }
        ))

        Parser.builder(options).build()
    }

    HtmlRenderer getMarkdownHtmlRenderer() {
        MutableDataHolder options = new MutableDataSet()
        options.set(HtmlRenderer.AUTOLINK_WWW_PREFIX, 'https://')
        options.set(HtmlRenderer.ESCAPE_HTML, true)
        options.set(Parser.EXTENSIONS, Arrays.asList(
                new Extension[] { BaseExtension.create(), StrikethroughExtension.create(), TablesExtension.create() }
        ))

        HtmlRenderer.builder(options).build()
    }

    Formatter getMarkdownFormatter() {
        MutableDataHolder optionsRenderer = new MutableDataSet()
        Formatter.builder(optionsRenderer).build()
    }
}