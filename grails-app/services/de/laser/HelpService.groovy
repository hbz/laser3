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
import de.laser.remote.Wekb
import de.laser.storage.BeanStore
import grails.gorm.transactions.Transactional
import org.grails.io.support.GrailsResourceUtils
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.WebUtils

@Transactional
class HelpService {

    public static final String GSP  = 'GSP'
    public static final String MD   = 'MD'
    public static final String BOTH = 'BOTH'

    public static final String CONTROLLER_WITH_ID_SUPPORT = 'public'

    ContextService contextService

    String getMapping() {
        GrailsWebRequest request = WebUtils.retrieveGrailsWebRequest()
        String controller = request.getControllerName()
        String action = request.getActionName()
        String id = request.getId()

        if (controller == CONTROLLER_WITH_ID_SUPPORT && id) {
            controller + '_' + action + '_' + id
        }
        else {
            controller + '_' + action
        }
    }

    URL getResource(String file) {
        GrailsResourceUtils.getClassLoader().getResource( file ) // resources
    }

    String getMatch() {
        String flag
        String mapping = getMapping()

        boolean isGSP  = getResource( 'help/_' + mapping + '.gsp' )
        boolean isMD   = getResource( 'help/' + mapping + '.md' )

        if (isGSP && isMD)  { flag = BOTH }
        else if (isGSP)     { flag = GSP }
        else if (isMD)      { flag = MD }

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
            log.warn 'parseMarkdown( ' + file + ' ) > ' + e.getMessage()
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
        ApplicationTagLib g = BeanStore.getApplicationTagLib()

        [
            'url_laser'         : ConfigMapper.getGrailsServerURL(),
            'url_laser_assets'  : ConfigMapper.getGrailsServerURL() + '/assets',
            'url_laser_static'  : ConfigMapper.getGrailsServerURL() + '/static',

            'url_wekb'          : Wekb.getURL(), // ConfigMapper.getWekbServerURL(), // TODO

            'link_org_readerNumber_id' : contextService.getOrg() ? g.createLink(controller: 'organisation', action: 'readerNumber', id: contextService.getOrg().id, absolute: true) : 'NUR_NACH_ANMELDUNG' // TODO

        ] as Map<String, String>
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