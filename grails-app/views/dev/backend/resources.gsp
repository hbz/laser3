<%@ page import="de.laser.utils.PasswordUtils; de.laser.utils.RandomUtils; org.apache.commons.codec.binary.StringUtils; de.laser.Org; de.laser.ui.Btn; de.laser.utils.DateUtils; de.laser.Subscription; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.storage.RDStore; de.laser.auth.*; grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:htmlStart text="Backend: Resources/Webapp" />

<ui:breadcrumbs>
    <ui:crumb message="menu.devDocs" controller="dev" action="index"/>
    <ui:crumb text="Backend" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon text="Backend" type="dev"/>

<g:render template="backend/nav" />

<ui:msg class="info" showIcon="true">
    NOCH IN ARBEIT
</ui:msg>

<div class="ui fluid card">
    <div class="content">
        <div class="header"> <icon:pathFolder/> src/main/resources/* </div>
    </div>
    <div class="content">
        <table class="ui selectable table">
            <thead>
                <tr>
                    <td class="three wide"></td>
                    <td class="eight wide"></td>
                    <td class="five wide"></td>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td><icon:pathFolder/> api / *.md</td>
                    <td>Markdown-Dateien für <g:link controller="public" action="releases" target="_blank">${message(code:'apiRelease')}</g:link></td>
                    <td>Manuelle Einbindung im PublicController</td>
                </tr>
                <tr>
                    <td><icon:pathFolder/> faq / *.md</td>
                    <td>Markdown-Dateien für <g:link controller="public" action="releases" target="_blank">${message(code:'menu.user.faq')}</g:link></td>
                    <td>Manuelle Einbindung im PublicController</td>
                </tr>
                <tr>
                    <td><icon:pathFolder/> help / *.md</td>
                    <td>Markdown-Dateien für <i class="${Icon.UI.HELP}"></i>- Flyouts</td>
                    <td>Automatische Einbindung. Mapping über Dateiname</td>
                </tr>
                <tr>
                    <td><icon:pathFolder/> manual / *.md</td>
                    <td>Markdown-Dateien für <g:link controller="public" action="releases" target="_blank">${message(code:'menu.user.manual')}</g:link></td>
                    <td>Manuelle Einbindung im PublicController</td>
                </tr>
                <tr>
                    <td><icon:pathFolder/> release / *.md</td>
                    <td>Markdown-Dateien für <g:link controller="public" action="releases" target="_blank">${message(code:'releaseNotes')}</g:link></td>
                    <td>Manuelle Einbindung im PublicController</td>
                </tr>
            </tbody>
        </table>
    </div>
</div>

<div class="ui fluid card">
    <div class="content">
        <div class="header"> <icon:pathFolder/> src/main/webapp/* </div>
    </div>
    <div class="content">
        <table class="ui selectable table">
            <thead>
            <tr>
                <td class="three wide"></td>
                <td class="eight wide"></td>
                <td class="five wide"></td>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td><icon:pathFolder/> files / *.csv, *.pdf</td>
                <td>Vorlagen, Download-Dateien, o.ä.</td>
                <td></td>
            </tr>
            <tr>
                <td><icon:pathFolder/> media / *.gif, *.jpeg, *.png</td>
                <td>Inhalte zur Verwendung in Markdown-Dateien o.ä.</td>
                <td></td>
            </tr>
            <tr>
                <td><icon:pathFolder/> setup / *.csv</td>
                <td>Stammdaten (Referenzwerte, Merkmale, o.ä). Werden beim Systemstart geparst und mit der Datenbank abgeglichen</td>
                <td></td>
            </tr>
            <tr>
                <td><icon:pathFolder/> swagger / *</td>
                <td>API</td>
                <td></td>
            </tr>
            <tr>
            </tbody>
        </table>
    </div>
</div>

<laser:htmlEnd />
