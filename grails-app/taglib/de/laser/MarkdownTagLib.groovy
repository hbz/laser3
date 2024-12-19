package de.laser

class MarkdownTagLib {

    static namespace = 'ui'

    HelpService helpService

    def renderMarkdown = { attrs, body ->

        if (attrs.api) {
            out <<  helpService.parseMarkdown( 'api/' + attrs.api + '.md' )
        }
        else if (attrs.file) {
            out <<  helpService.parseMarkdown( attrs.file )
        }
        else if (attrs.faq) {
            out <<  helpService.parseMarkdown( 'faq/' + attrs.faq + '.md' )
        }
        else if (attrs.help) {
            out <<  helpService.parseMarkdown( 'help/' + attrs.help + '.md' )
        }
        else if (attrs.manual) {
            out <<  helpService.parseMarkdown( 'manual/' + attrs.manual + '.md' )
        }
        else if (attrs.release) {
            out <<  helpService.parseMarkdown( 'release/' + attrs.release + '.md' )
        }
    }

    def renderContentAsMarkdown = { attrs, body ->

        out <<  helpService.parseMarkdown2( body().toString() )
    }
}
