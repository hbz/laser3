<%@ page import="de.laser.storage.RDStore; de.laser.DocContext" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: "menu.admin.recoveryDoc")}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
    <semui:crumb message="menu.admin.recoveryDoc" class="active"/>
</semui:breadcrumbs>

<h1 class="ui header la-noMargin-top">${message(code: "menu.admin.recoveryDoc")}</h1>

<semui:messages data="${flash}"/>

<g:if test="${doc}">

    <div class="la-inline-lists">
        <div class="ui card">
            <div class="content">
                <div class="header">Dokument in der Datenbank OHNE entspr. Datei im Filesystem.</div>
            </div>

            <div class="content">
                <ul>
                    <li>${doc.id} : <strong>${doc.filename}</strong> -> <g:link action="index" controller="docstore"
                                                                                id="${doc.uuid}">${doc.uuid}</g:link>
                    <g:if test="${doc.owner}">
                        (Owner: <g:link action="show" controller="org"
                                        id="${doc.owner.id}">${doc.owner.name}</g:link>)
                    </g:if>

                    </li>

                    Dokument wird genutzt von:
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
                                                link(action: 'surveyConfigDocs', controller: 'survey', id: dc.surveyConfig.surveyInfo.id, params: ['surveyConfigID': dc.surveyConfig.id]) { '<i class="ui icon external alternate"></i>' }
                                    }

                                    if (dc.status == RDStore.DOC_CTX_STATUS_DELETED) {
                                        print "</span>"
                                    }
                                %>
                            </li>
                        </g:each>
                    </ul>
                </ul>
            </div>
        </div>

        <div class="ui card">
            <div class="content">
                <div class="header">Diese Dokumente entspr. dem oben genannten Dokument mit entspr. Datei im Filesystem.</div>
            </div>

            <div class="content">
                <h4 class="ui header">Machen Sie hier eine Auswahl, welche Datei zum Wiederherstellen ins Filesystem genutzt werden soll.</h4>
                <g:each in="${docsToRecovery}" var="docRecovery">
                    <g:if test="${doc.id != docRecovery.id}">
                        <ul>
                            <li>${docRecovery.id} : <strong>${docRecovery.filename}</strong> -> <g:link
                                    action="index"
                                    controller="docstore"
                                    id="${docRecovery.uuid}">${docRecovery.uuid}</g:link>
                            <g:if test="${docRecovery.owner}">
                                (Owner: <g:link action="show" controller="org"
                                                id="${docRecovery.owner.id}">${docRecovery.owner.name}</g:link>)
                            </g:if>

                            <g:link class="ui mini button" action="processRecoveryDoc"
                                    params="[sourceDoc: doc.id, targetDoc: docRecovery.id]">Auswählen</g:link>

                            </li>

                            Dokument wird genutzt von:
                            <ul>
                                <g:each in="${DocContext.findAllByOwner(docRecovery)}" var="dc">
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
                                                        link(action: 'surveyConfigDocs', controller: 'survey', id: dc.surveyConfig.surveyInfo.id, params: ['surveyConfigID': dc.surveyConfig.id]) { '<i class="ui icon external alternate"></i>' }
                                            }

                                            if (dc.status == RDStore.DOC_CTX_STATUS_DELETED) {
                                                print "</span>"
                                            }
                                        %>
                                    </li>
                                </g:each>
                            </ul>
                        </ul>
                    </g:if>
                </g:each>

            </div>
        </div>
    </div>

</g:if>
<g:else>
    <h2 class="ui header">Dokument in der Datenbank mit entspr. Datei im Filesystem gefunden.</h2>
</g:else>

</body>
</html>
