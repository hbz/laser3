package de.laser

import de.laser.annotations.UIDoc

class SymbolTagLib {

    // -- simple static icons
    // -- complete markup

    static namespace = 'icon'

    @UIDoc(usage = 'Generic layout helper / mostly wrapped with class="hidden"')
    def placeholder = { attrs, body ->
        out << '<i class="fake icon"></i>'
    }

    @UIDoc(usage = 'Generic symbol for: Arrow [right]')
    def arrow = { attrs, body ->
        out << '<i class="arrow right icon"></i>'
    }
    @UIDoc(usage = 'Generic symbol for: Pointing hand [right]')
    def pointingHand = { attrs, body ->
        out << '<i class="hand point right icon"></i>'
    }

    @UIDoc(usage = 'Generic symbol for: File (Path)')
    def pathFile = { attrs, body ->
        out << '<i class="file outline icon"></i>'
    }
    @UIDoc(usage = 'Generic symbol for: Folder (Path)')
    def pathFolder = { attrs, body ->
        out << '<i class="folder icon"></i>'
    }
    @UIDoc(usage = 'Generic symbol for: Class (Code)')
    def codeClass = { attrs, body ->
        out << '<i class="file outline icon"></i>'
    }
    @UIDoc(usage = 'Generic symbol for: Package (Code)')
    def codePackage = { attrs, body ->
        out << '<i class="boxes icon"></i>'
    }
    @UIDoc(usage = 'Generic symbol for: Data/Database')
    def database = { attrs, body ->
        out << '<i class="database icon"></i>'
    }
    @UIDoc(usage = 'Generic symbol for: Bug')
    def bug = { attrs, body ->
        out << '<i class="bug icon"></i>'
    }


    @UIDoc(usage = 'Generic symbol for: Universal access')
    def universalAccess = { attrs, body ->
        out << '<i class="universal access icon"></i>'
    }

    @UIDoc(usage = 'Generic symbol for: Pdf')
    def pdf = { attrs, body ->
        out << '<i class="file pdf icon"></i>'
    }
    @UIDoc(usage = 'Generic symbol for: Video')
    def video = { attrs, body ->
        out << '<i class="film icon"></i>'
    }

    // profile/help

    @UIDoc(usage = 'Specific symbol for: Chrome [middle aligned & large]')
    def help_chrome = { attrs, body ->
        out << '<i class="chrome icon middle aligned large"></i>'
    }
    @UIDoc(usage = 'Specific symbol for: Firefox [middle aligned & large]')
    def help_firefox = { attrs, body ->
        out << '<i class="firefox icon middle aligned large"></i>'
    }
    @UIDoc(usage = 'Specific symbol for: Internet Explorer [middle aligned & large]')
    def help_ie = { attrs, body ->
        out << '<i class="internet explorer icon middle aligned large"></i>'
    }

    // flags --- != icons

    @UIDoc(usage = 'Generic flag: DE')
    def flag_de = { attrs, body ->
        out << '<i class="de flag"></i>'
    }
    @UIDoc(usage = 'Generic flag: EN')
    def flag_en = { attrs, body ->
        out << '<i class="gb flag"></i>'
    }
}
