<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; java.nio.file.Files" %>
<laser:htmlStart message="menu.yoda.stats.cache"/>

<ui:breadcrumbs>
    <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
    <ui:crumb message="menu.yoda.stats.cache" class="active"/>
</ui:breadcrumbs>

<div class="ui grid">
    <div class="sixteen wide column">

        <h3 class="ui header"><i class="hdd icon"></i><span class="content">Dateien</span></h3>

        <g:link action="deleteTempFile" class="${Btn.ICON.NEGATIVE_CONFIRM}" data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.tempDir")}" params="[emptyDir: true]" role="button" aria-label="${message(code: 'ariaLabel.delete.universal')}">
            <i class="${Icon.CMD.DELETE}"></i> Gesamtes Verzeichnis leeren
        </g:link>

        <table class="ui sortable celled la-js-responsive-table la-table compact la-ignore-fixed table">
            <thead>
                <tr>
                    <th>Dateiname</th>
                    <th>Erstelldatum</th>
                    <th>Aktion</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${tempFiles}" var="tempFile">
                    <tr>
                        <td>${tempFile.getName()}</td>
                        <td>${Files.getAttribute(tempFile.toPath(), 'creationTime')}</td>
                        <td><g:link action="deleteTempFile" class="${Btn.ICON.NEGATIVE}" params="[filename: tempFile.getName()]"><i class="${Icon.CMD.DELETE}"></i></g:link></td>
                    </tr>
                </g:each>
            </tbody>
        </table>

    </div>
</div>

<laser:htmlEnd/>