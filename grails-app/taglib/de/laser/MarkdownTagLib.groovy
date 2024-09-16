package de.laser

class MarkdownTagLib {

    static namespace = 'md'

    HelpService helpService

    def render = { attrs, body ->
        String file = attrs.file
        out <<  helpService.parseMarkdown( file )
    }

}
