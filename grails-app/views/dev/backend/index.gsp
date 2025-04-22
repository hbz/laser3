<%@ page import="de.laser.utils.PasswordUtils; de.laser.utils.RandomUtils; org.apache.commons.codec.binary.StringUtils; de.laser.Org; de.laser.ui.Btn; de.laser.utils.DateUtils; de.laser.Subscription; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.storage.RDStore; de.laser.auth.*; grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:htmlStart text="Backend: Helper & Utils" />

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
        <div class="header"> <i class="icon medkit"></i> de.laser.helper.* </div>
    </div>
    <div class="content">
        <table class="ui selectable table">
            <thead>
                <tr>
                    <td class="three wide"></td>
                    <td class="eleven wide"></td>
                    <td class="two wide"></td>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td><icon:pathFile/> DatabaseInfo</td>
                    <td>Liefert <g:link controller="admin" action="databaseInfo" target="_blank">Informationen</g:link> über die Datenbank, Konfiguration und Statistiken</td>
                    <td><i class="${Icon.SYM.SQUARE} yellow"></i> CTX</td>
                </tr>
                <tr>
                    <td><icon:pathFile/> FactoryResult</td>
                    <td></td>
                    <td><i class="${Icon.SYM.YES} green"></i></td>
                </tr>
                <tr>
                    <td><icon:pathFile/> FilterLogic</td>
                    <td>Temp. Hilfsklasse</td>
                    <td><i class="${Icon.SYM.SQUARE} orange"></i> TMP</td>
                </tr>
                <tr>
                    <td><icon:pathFile/> Params</td>
                    <td>Standardisierter und typsicherer Zugriff auf Parameter-Maps</td>
                    <td><i class="${Icon.SYM.YES} green"></i></td>
                </tr>
                <tr>
                    <td><icon:pathFile/> Profiler</td>
                    <td>Hilfsklasse für den <g:link controller="yoda" action="profilerLoadtime" target="_blank">SystemProfiler</g:link></td>
                    <td><i class="${Icon.SYM.SQUARE} yellow"></i> CTX</td>
                </tr>
            </tbody>
        </table>
    </div>
</div>

<div class="ui fluid card">
    <div class="content">
        <div class="header"> <i class="icon toolbox"></i> de.laser.utils.* </div>
    </div>
    <div class="content">
        <table class="ui selectable table">
            <thead>
                <tr>
                    <td class="three wide"></td>
                    <td class="eleven wide"></td>
                    <td class="two wide"></td>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td><icon:pathFile/> AppUtils</td>
                    <td>Liefert Informationen über den aktuellen Server, bzw. Build</td>
                    <td><i class="${Icon.SYM.YES} green"></i></td>
                </tr>
                <tr>
                    <td><icon:pathFile/> CodeUtils</td>
                    <td>Zugriff auf Domainklassen, ggf. Reflection- und Metaprogramming</td>
                    <td><i class="${Icon.SYM.YES} green"></i></td>
                </tr>
                <tr>
                    <td><icon:pathFile/> DatabaseUtils</td>
                    <td></td>
                    <td><i class="${Icon.SYM.YES} green"></i></td>
                </tr>
                <tr>
                    <td><icon:pathFile/> DateUtils</td>
                    <td>Stellt Formate und Konverter zur Datumsverarbeitung bereit</td>
                    <td><i class="${Icon.SYM.YES} green"></i></td>
                </tr>
                <tr>
                    <td><icon:pathFile/> FileUtils</td>
                    <td></td>
                    <td><i class="${Icon.SYM.YES} green"></i></td>
                </tr>
                <tr>
                    <td><icon:pathFile/> LocaleUtils</td>
                    <td>Stellt Methoden zur Lokalisierung von Objekten bereit</td>
                    <td><i class="${Icon.SYM.YES} green"></i></td>
                </tr>
                <tr>
                    <td><icon:pathFile/> PasswordUtils</td>
                    <td></td>
                    <td><i class="${Icon.SYM.YES} green"></i></td>
                </tr>
                <tr>
                    <td><icon:pathFile/> Pdftils</td>
                    <td></td>
                    <td><i class="${Icon.SYM.YES} green"></i></td>
                </tr>
                <tr>
                    <td><icon:pathFile/> RandomUtils</td>
                    <td></td>
                    <td><i class="${Icon.SYM.YES} green"></i></td>
                </tr>
                <tr>
                    <td><icon:pathFile/> SqlDateUtils</td>
                    <td></td>
                    <td><i class="${Icon.SYM.YES} green"></i></td>
                </tr>
                <tr>
                    <td><icon:pathFile/> SwissKnife</td>
                    <td></td>
                    <td><i class="${Icon.SYM.YES} green"></i></td>
                </tr>
            </tbody>
        </table>
    </div>
</div>

<laser:htmlEnd />
