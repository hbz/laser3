<%@ page import="de.laser.helper.RDStore; de.laser.DocContext" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code: "menu.admin.fileConsistency")}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
    <semui:crumb message="menu.admin.fileConsistency" class="active"/>
</semui:breadcrumbs>

<h1 class="ui header la-noMargin-top">${message(code: "menu.admin.fileConsistency")}</h1>

<div class="ui grid">
    <div class="sixtenn wide column">

        <h3 class="ui headerline"><i class="ui hdd icon"></i> Dateien</h3>

        <table class="ui sortable celled la-js-responsive-table la-table compact la-ignore-fixed table">
            <thead>
                <tr>
                    <th>Beschreibung</th>
                    <th></th>
                    <th>Anzahl</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td><strong>Dateien im Filesystem</strong></td>
                    <td>${filePath}</td>
                    <td>${listOfFiles.size()}</td>
                </tr>
                <tr>
                    <td class="table-td-ok">- entspr. Dateiobjekte in der Datenbank existieren</td>
                    <td class="table-td-ok"></td>
                    <td class="table-td-ok">${listOfFilesMatchingDocs.size()}</td>
                </tr>
                <tr>
                    <td class="table-td-error">- entspr. Dateiobjekte in der Datenbank existieren nicht</td>
                    <td class="table-td-error">${listOfFilesOrphaned.join(', ')}</td>
                    <td class="table-td-error">${listOfFilesOrphaned.size()}</td>
                </tr>
            </tbody>
        </table>

        <h3 class="ui headerline"><i class="ui database icon"></i> Objekte: Doc</h3>

         <table class="ui sortable celled la-js-responsive-table la-table compact la-ignore-fixed table">
            <thead>
                <tr>
                    <th>Beschreibung</th>
                    <th></th>
                    <th>Anzahl</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td><strong>Dateiobjekte in der Datenbank</strong></td>
                    <td>Doc(contentType = CONTENT_TYPE_FILE)</td>
                    <td>${listOfDocsInUse.size() + listOfDocsNotInUse.size()}</td>
                </tr>

                <tr>
                    <td><strong>Referenzierte Dateiobjekte</strong></td>
                    <td>DocContext.owner => Doc(contentType = CONTENT_TYPE_FILE)</td>
                    <td>${listOfDocsInUse.size()}</td>
                </tr>
                <tr>
                    <td class="table-td-ok">- entspr. Dateien existieren</td>
                    <td class="table-td-ok"></td>
                    <td class="table-td-ok">${listOfDocsInUse.size() - listOfDocsInUseOrphaned.size()}</td>
                </tr>
                <tr>
                    <td class="table-td-error">- entspr. Dateien existieren nicht</td>
                    <td class="table-td-error">${listOfDocsInUseOrphaned.collect{ it.id }.join(', ')}</td>
                    <td class="table-td-error">${listOfDocsInUseOrphaned.size()}</td>
                </tr>

                <tr>
                    <td><strong>Nicht referenzierte Dateiobjekte</strong></td>
                    <td>Doc(contentType = CONTENT_TYPE_FILE); ohne entspr. DocContext.owner</td>
                    <td>${listOfDocsNotInUse.size()}</td>
                </tr>
                <tr>
                    <td class="table-td-ok">- entspr. Dateien existieren</td>
                    <td class="table-td-ok"></td>
                    <td class="table-td-ok">${listOfDocsNotInUse.size() - listOfDocsNotInUseOrphaned.size()}</td>
                </tr>
                <tr>
                    <td class="table-td-error">- entsprechende Dateien existieren nicht</td>
                    <td class="table-td-error">${listOfDocsNotInUseOrphaned.collect{ it.id }.join(', ')}</td>
                    <td class="table-td-error">${listOfDocsNotInUseOrphaned.size()}</td>
                </tr>
            </tbody>
         </table>

        <h3 class="ui headerline"><i class="ui database icon"></i> Objekte: DocContext</h3>

        <table class="ui sortable celled la-js-responsive-table la-table compact la-ignore-fixed table">
            <thead>
            <tr>
                <th>Beschreibung</th>
                <th>Spec.</th>
                <th>Anzahl</th>
            </tr>
            </thead>
            <tbody>
                <tr>
                    <td><strong>Referenzen auf Dateiobjekte in der Datenbank</strong></td>
                    <td>DocContext.owner => Doc(contentType = CONTENT_TYPE_FILE)</td>
                    <td>${numberOfDocContextsInUse + numberOfDocContextsDeleted}</td>
                </tr>
            <tr>
                    <td>- gültige Referenzen</td>
                    <td>DocContext.owner(status != deleted) => Doc(contentType = CONTENT_TYPE_FILE)</td>
                    <td>${numberOfDocContextsInUse}</td>
                </tr>
                <tr>
                    <td>- ungültige Referenzen</td>
                    <td>DocContext.owner(status = deleted) => Doc(contentType = CONTENT_TYPE_FILE)</td>
                    <td>${numberOfDocContextsDeleted}</td>
                </tr>
            </tbody>
         </table>

        <br />
        <br />
        <br />

        <h3 class="ui headerline"><i class="ui tasks icon"></i> ToDo-Liste (${listOfDocsInUseOrphaned.size()} Dateiobjekte)</h3>

        <p>
            Alle aufgelisteten Einträge repräsentieren referenzierte Dateiobjekte in der Datenbank OHNE entspr. Dateien im Filesystem.
            <br />
            Rote Einträge markieren ungültige Referenzen: DocContext.owner(<span style="color:red">status = deleted</span>) => Doc.
        </p>

        <g:each in="${listOfDocsInUseOrphaned}" var="doc">
            <ul>
                <li>${doc.id} : <strong>${doc.filename}</strong> -> <g:link action="index" controller="docstore" id="${doc.uuid}">${doc.uuid}</g:link>
                    <g:if test="${doc.owner}">
                        (Owner: <g:link action="show" controller="org" id="${doc.owner.id}">${doc.owner.name}</g:link>)
                    </g:if>
                <%
                    print "&nbsp;&nbsp;"
                    print link(action: 'recoveryDoc', controller: 'admin', params:['docID': doc.id], target: '_blank') { '<i class="ui large icon paste yellow"></i>' }
                %>
                </li>

                <ul>
                <g:each in="${DocContext.findAllByOwner(doc)}" var="dc">
                    <li>
                        <%
                            if (dc.status == RDStore.DOC_CTX_STATUS_DELETED) {
                                print "<span style='color:red'>"
                            }
                            print "${dc.id} : "

                            if (dc.isShared) {
                                print " <i class='ui icon share alternate square'></i> "
                            }
                            if (dc.sharedFrom) {
                                print " <i class='ui icon share alternate'></i> "
                            }

                            if (dc.license) {
                                println "License ${dc.license.id} - ${dc.license.reference}, ${dc.license.sortableReference} &nbsp;&nbsp; " +
                                        link(action: 'show', controller: 'lic', id: dc.license.id) { '<i class="ui icon external alternate"></i>' }
                            }
                            if (dc.subscription) {
                                println "Subscription ${dc.subscription.id} - ${dc.subscription.name} &nbsp;&nbsp; " +
                                        link(action: 'show', controller: 'subscription', id: dc.subscription.id) { '<i class="ui icon external alternate"></i>' }
                            }
                            if (dc.pkg) {
                                println "Package ${dc.pkg.id} - ${dc.pkg.name} ${dc.pkg.sortname} &nbsp;&nbsp; " +
                                        link(action: 'show', controller: 'package', id: dc.pkg.id) { '<i class="ui icon external alternate"></i>' }
                            }
                            if (dc.org) {
                                println "Org ${dc.org.id} - ${dc.org.name} ${dc.org.shortname} ${dc.org.sortname} &nbsp;&nbsp;" +
                                        link(action: 'show', controller: 'org', id: dc.org.id) { '<i class="ui icon external alternate"></i>' }
                            }
                            if (dc.link) {
                                println "Links ${dc.link.id} "
                            }
                            if (dc.surveyConfig) {
                                println "SurveyConfig ${dc.surveyConfig.id} - ${dc.surveyConfig.type} ${dc.surveyConfig.header} &nbsp;&nbsp; " +
                                        link(action: 'surveyConfigDocs', controller: 'survey', id: dc.surveyConfig.surveyInfo.id, params:['surveyConfigID': dc.surveyConfig.id]) { '<i class="ui icon external alternate"></i>' }
                            }

                            if (dc.status == RDStore.DOC_CTX_STATUS_DELETED) {
                                print "</span>"
                            }
                        %>
                    </li>
                </g:each>
                </ul>
            </ul>
        </g:each>

    </div>
</div>


</body>
</html>
