<%@ page import="de.laser.utils.FileUtils; de.laser.config.ConfigDefaults; de.laser.config.ConfigMapper; de.laser.utils.DateUtils; de.laser.Doc; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore; de.laser.DocContext" %>

<laser:htmlStart message="menu.admin.simpleDocsCheck" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb message="menu.admin.simpleDocsCheck" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.admin.simpleDocsCheck" type="admin"/>

<nav class="ui secondary menu">
    <g:link controller="admin" action="simpleFilesCheck" class="item">Dateisystem</g:link>
    <g:link controller="admin" action="simpleDocsCheck" class="item active">Datenbank</g:link>
    <g:link controller="admin" action="simpleShareConfCheck" class="item">Sichtbarkeit</g:link>
</nav>

<div class="ui fluid card">
    <div class="content">
        <div class="header">Doc <- DocContext</div>
    </div>
    <div class="content">
        <div class="ui list">
            <div class="item">
                <div class="content"><icon:pathFolder/> Pfad: <strong>${dsPath}</strong></div>
            </div>
            <div class="item">
                <div class="content"><i class="arrow circle right icon"></i> Doc-Einträge: <strong>${Doc.executeQuery('select count(*) from Doc where contentType = 3')[0]}</strong></div>
            </div>
            <div class="item">
                <div class="content"><i class="${Icon.SYM.NO} red"></i> Doc ohne DocContext-Referenz: <strong>${orphanedDocs.size()}</strong></div>
            </div>
        </div>
    </div>
</div>

<div class="ui fluid card">
    <div class="content">
        <div class="header">Doc ohne DocContext-Referenz: ${orphanedDocs.size()}</div>
    </div>
    <div class="content">
        <table class="ui table very compact">
            <thead>
                <tr>
                    <th class="center aligned">#</th>
                    <th>Dateiname</th>
                    <th class="center aligned">Datei</th>
                    <th>Besitzer</th>
                    <th>Erstellt</th>
                    <th>Bearbeitet</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${orphanedDocs}" var="doc">
                    <tr>
                        <td class="center aligned">${doc.id}</td>
                        <td>${doc.filename.size() < 70 ? doc.filename : (doc.filename.take(55) + '[..]' + doc.filename.takeRight(15))}</td>
                        <td class="center aligned">
                            <g:if test="${FileUtils.fileCheck("${dsPath}/${doc.uuid}")}">
                                <i class="${Icon.SYM.YES} green">
                            </g:if>
                            <g:else>
                                <i class="${Icon.SYM.CIRCLE} red">
                            </g:else>
                        </td>
                        <td>${doc.owner?.getDesignation()}</td>
                        <td>${DateUtils.getSDF_yyyyMMdd().format(doc.dateCreated)}</td>
                        <td>${DateUtils.getSDF_yyyyMMdd().format(doc.lastUpdated)}</td>
                    </tr>
                </g:each>
            </tbody>
        </table>

        <g:if test="${orphanedDocs}">
            <br/>
            <br/>
            <g:link controller="admin" action="simpleDocsCheck" params="${[deleteOrphanedDocs: 1]}"
                    class="${Btn.NEGATIVE_CONFIRM}"
                    data-confirm-tokenMsg="Ungültige Doc-Einträge aus der Datenbank löschen?"
                    data-confirm-term-how="ok">Aufräumen</g:link>
        </g:if>
    </div>
</div>

<laser:htmlEnd />
