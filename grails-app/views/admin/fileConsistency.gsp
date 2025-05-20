<%@ page import="de.laser.ui.Icon; de.laser.storage.RDStore; de.laser.DocContext" %>

<laser:htmlStart message="menu.admin.fileConsistency" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb message="menu.admin.fileConsistency" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.admin.fileConsistency" type="admin"/>


<div class="ui fluid card">
    <div class="content">
        <div class="header"><i class="hdd icon"></i> Dateien </div>
    </div>
    <div class="content">
        <table class="ui table compact la-table">
            <thead>
                <tr>
                    <th>${message(code:'default.description.label')}</th>
                    <th></th>
                    <th>${message(code:'default.count.label')}</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td><strong>Dateien im Filesystem</strong></td>
                    <td>${filePath}</td>
                    <td>${listOfFiles.size()}</td>
                </tr>
                <tr>
                    <td class="positive">- entspr. Dateiobjekte in der Datenbank existieren</td>
                    <td class="positive"></td>
                    <td class="positive">${listOfFilesMatchingDocs.size()}</td>
                </tr>
                <tr>
                    <td class="error">- entspr. Dateiobjekte in der Datenbank existieren nicht</td>
                    <td class="error">${listOfFilesOrphaned.join(', ')}</td>
                    <td class="error">${listOfFilesOrphaned.size()}</td>
                </tr>
            </tbody>
        </table>
    </div>
</div>

<div class="ui fluid card">
    <div class="content">
        <div class="header"><icon:database /> Objekte: Doc </div>
    </div>
    <div class="content">
        <table class="ui table compact la-table">
            <thead>
                <tr>
                    <th>${message(code:'default.description.label')}</th>
                    <th></th>
                    <th>${message(code:'default.count.label')}</th>
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
                    <td class="positive">- entspr. Dateien existieren</td>
                    <td class="positive"></td>
                    <td class="positive">${listOfDocsInUse.size() - listOfDocsInUseOrphaned.size()}</td>
                </tr>
                <tr>
                    <td class="error">- entspr. Dateien existieren nicht</td>
                    <td class="error">${listOfDocsInUseOrphaned.collect{ it.id }.join(', ')}</td>
                    <td class="error">${listOfDocsInUseOrphaned.size()}</td>
                </tr>

                <tr>
                    <td><strong>Nicht referenzierte Dateiobjekte</strong></td>
                    <td>Doc(contentType = CONTENT_TYPE_FILE); ohne entspr. DocContext.owner</td>
                    <td>${listOfDocsNotInUse.size()}</td>
                </tr>
                <tr>
                    <td class="positive">- entspr. Dateien existieren</td>
                    <td class="positive"></td>
                    <td class="positive">${listOfDocsNotInUse.size() - listOfDocsNotInUseOrphaned.size()}</td>
                </tr>
                <tr>
                    <td class="error">- entsprechende Dateien existieren nicht</td>
                    <td class="error">${listOfDocsNotInUseOrphaned.collect{ it.id }.join(', ')}</td>
                    <td class="error">${listOfDocsNotInUseOrphaned.size()}</td>
                </tr>
            </tbody>
         </table>
    </div>
</div>

<div class="ui fluid card">
    <div class="content">
        <div class="header"><icon:database /> Objekte: DocContext </div>
    </div>
    <div class="content">
        <table class="ui table compact la-table">
            <thead>
            <tr>
                <th>${message(code:'default.description.label')}</th>
                <th>Spec.</th>
                <th>${message(code:'default.count.label')}</th>
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

    </div>
</div>

%{--        <div class="header"><i class="tasks icon"></i> ToDo-Liste (${listOfDocsInUseOrphaned.size()} Dateiobjekte) </div>--}%

%{--        <ui:msg class="info" hideClose="true">--}%
%{--            Alle aufgelisteten Einträge repräsentieren referenzierte Dateiobjekte in der Datenbank OHNE entspr. Dateien im Filesystem.--}%
%{--            <br />--}%
%{--            Rote Einträge markieren ungültige Referenzen: DocContext.owner( <span class="sc_red">status = deleted</span> ) => Doc.--}%
%{--        </ui:msg>--}%

%{--        <div class="ui list">--}%
%{--            <g:each in="${listOfDocsInUseOrphaned}" var="doc">--}%

%{--                 <div class="item">${doc.id} : <strong>${doc.filename}</strong> -> <g:link controller="document" action="downloadDocument" id="${doc.uuid}">${doc.uuid}</g:link>--}%
%{--                    <g:if test="${doc.owner}">--}%
%{--                        (Owner: <g:link action="show" controller="org" id="${doc.owner.id}">${doc.owner.name}</g:link>)--}%
%{--                    </g:if>--}%

%{--                     <g:if test="${DocContext.findAllByOwner(doc)}">--}%
%{--                        <div class="ui list">--}%
%{--                        <g:each in="${DocContext.findAllByOwner(doc)}" var="dc">--}%
%{--                            <div class="item">--}%
%{--                        <%--}%
%{--                            if (dc.status == RDStore.DOC_CTX_STATUS_DELETED) {--}%
%{--                                print "<span style='color:red'>"--}%
%{--                            }--}%
%{--                            print "&nbsp;&nbsp;&nbsp;&nbsp; ${dc.id} : "--}%

%{--                            if (dc.isShared) {--}%
%{--                                print " <i class='${Icon.SIG.SHARED_OBJECT} square'></i> "--}%
%{--                            }--}%
%{--                            if (dc.sharedFrom) {--}%
%{--                                print " <i class='${Icon.SIG.SHARED_OBJECT_ON}'></i> "--}%
%{--                            }--}%

%{--                            if (dc.license) {--}%
%{--                                println "License ${dc.license.id} - ${dc.license.reference}, ${dc.license.sortableReference} &nbsp;&nbsp; " +--}%
%{--                                        link(action: 'show', controller: 'lic', id: dc.license.id) { '<i class="' + Icon.LNK.EXTERNAL + '"></i>' }--}%
%{--                            }--}%
%{--                            if (dc.subscription) {--}%
%{--                                println "Subscription ${dc.subscription.id} - ${dc.subscription.name} &nbsp;&nbsp; " +--}%
%{--                                        link(action: 'show', controller: 'subscription', id: dc.subscription.id) { '<i class="' + Icon.LNK.EXTERNAL + '"></i>' }--}%
%{--                            }--}%
%{--                            if (dc.org) {--}%
%{--                                println "Org ${dc.org.id} - ${dc.org.name} ${dc.org.sortname} &nbsp;&nbsp;" +--}%
%{--                                        link(action: 'show', controller: 'org', id: dc.org.id) { '<i class="' + Icon.LNK.EXTERNAL + '"></i>' }--}%
%{--                            }--}%
%{--                            if (dc.link) {--}%
%{--                                println "Links ${dc.link.id} "--}%
%{--                            }--}%
%{--                            if (dc.surveyConfig) {--}%
%{--                                println "SurveyConfig ${dc.surveyConfig.id} - ${dc.surveyConfig.type} ${dc.surveyConfig.header} &nbsp;&nbsp; " +--}%
%{--                                        link(action: 'surveyConfigDocs', controller: 'survey', id: dc.surveyConfig.surveyInfo.id, params:['surveyConfigID': dc.surveyConfig.id]) { '<i class="' + Icon.LNK.EXTERNAL + '"></i>' }--}%
%{--                            }--}%

%{--                            if (dc.status == RDStore.DOC_CTX_STATUS_DELETED) {--}%
%{--                                print "</span>"--}%
%{--                            }--}%
%{--                        %>--}%
%{--                            </div>--}%
%{--                        </g:each>--}%
%{--                        </div>--}%
%{--                     </g:if>--}%
%{--                </div>--}%

%{--            </g:each>--}%
%{--        </div>--}%


<laser:htmlEnd />
