<%@ page import="de.laser.config.ConfigDefaults; de.laser.config.ConfigMapper; de.laser.utils.DateUtils; de.laser.Doc; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore; de.laser.DocContext" %>

<laser:htmlStart message="menu.admin.simpleDocsCheck" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb message="menu.admin.simpleDocsCheck" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.admin.simpleDocsCheck" type="admin"/>

<nav class="ui secondary menu">
    <g:link controller="admin" action="simpleFilesCheck" class="item">Dateisystem</g:link>
    <g:link controller="admin" action="simpleDocsCheck" class="item active">Datenbank</g:link>
</nav>

<%
    Closure fileCheck = { Doc doc ->
        try {
            File test = new File("${dsPath}/${doc.uuid}")
            if (test.exists() && test.isFile()) {
                return true
            }
        }
        catch (Exception e) {
            return false
        }
    }
%>

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
                <div class="content"><i class="${Icon.SYM.CIRCLE} red"></i> DOC ohne DocContext-Referenz: <strong>${docsWithoutContext.size()}</strong></div>
            </div>
        </div>
    </div>
</div>

<div class="ui fluid card">
    <div class="content">
        <div class="header">DOC ohne DocContext-Referenz: ${docsWithoutContext.size()}</div>
    </div>
    <div class="content">
        <table class="ui table very compact">
            <thead>
                <tr>
                    <th class="center aligned">#</th>
                    <th>Dateiname</th>
                    <th class="center aligned">DCTX</th>
                    <th class="center aligned">FILE</th>
                    <th>Besitzer</th>
                    <th>Erstellt</th>
                    <th>Bearbeitet</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${docsWithoutContext}" var="doc">
                    <tr>
                        <td class="center aligned">${doc.id}</td>
                        <td>${doc.filename.size() < 70 ? doc.filename : (doc.filename.take(55) + '[..]' + doc.filename.takeRight(15))}</td>
                        <td class="center aligned">
                            <i class="${DocContext.executeQuery('select count(*) from DocContext where owner.id = :docId', [docId: doc.id])[0] > 0 ? 'green' : 'red'} circle icon"></i>
                        </td>
                        <td class="center aligned">
                            <i class="${Icon.SYM.CIRCLE} ${fileCheck(doc) ? 'green' : 'red'}"></i>
                        </td>
                        <td>${doc.owner.getDesignation()}</td>
                        <td>${DateUtils.getSDF_yyyyMMdd().format(doc.dateCreated)}</td>
                        <td>${DateUtils.getSDF_yyyyMMdd().format(doc.lastUpdated)}</td>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </div>
</div>

<laser:htmlEnd />
