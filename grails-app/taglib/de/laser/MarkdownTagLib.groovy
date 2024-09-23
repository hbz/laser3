package de.laser

class MarkdownTagLib {

    static namespace = 'ui'

    HelpService helpService

    def renderMarkdown = { attrs, body ->

        if (attrs.file) {
            out <<  helpService.parseMarkdown( attrs.file )
        }
        else if (attrs.help) {
            out <<  helpService.parseMarkdown( 'help/' + attrs.help + '.md' )
        }
        else if (attrs.releaseNotes) {
            out <<  helpService.parseMarkdown( 'release/' + attrs.releaseNotes + '.md' )
        }
    }

    def renderContentAsMarkdown = { attrs, body ->

        out <<  helpService.parseMarkdown2( body().toString() )
    }
}
