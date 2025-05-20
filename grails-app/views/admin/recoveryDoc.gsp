<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore; de.laser.DocContext" %>

<laser:htmlStart message="menu.admin.recoveryDoc" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb message="menu.admin.recoveryDoc" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.admin.recoveryDoc" type="admin"/>

<ui:messages data="${flash}"/>

<g:if test="${doc}">

    <div class="la-inline-lists">
        <div class="ui card">
            <div class="content">
                <div class="header">Dokument in der Datenbank OHNE entspr. Datei im Filesystem.</div>
            </div>

            <div class="content">
                <ul>
                    <li>${doc.id} : <strong>${doc.filename}</strong> -> <g:link controller="document" action="downloadDocument" id="${doc.uuid}">${doc.uuid}</g:link>
                    <g:if test="${doc.owner}">
                        (Owner: <g:link action="show" controller="org" id="${doc.owner.id}">${doc.owner.name}</g:link>)
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
                                        print " <i class='${Icon.SIG.SHARED_OBJECT} square'></i> "
                                    }
                                    if (dc.sharedFrom) {
                                        print " <i class='${Icon.SIG.SHARED_OBJECT_ON}'></i> "
                                    }

                                    if (dc.license) {
                                        println "License ${dc.license.id} - ${dc.license.reference}, ${dc.license.sortableReference} &nbsp;&nbsp; " +
                                                link(action: 'show', controller: 'lic', id: dc.license.id) { '<i class="' + Icon.LNK.EXTERNAL + '"></i>' }
                                    }
                                    if (dc.subscription) {
                                        println "Subscription ${dc.subscription.id} - ${dc.subscription.name} &nbsp;&nbsp; " +
                                                link(action: 'show', controller: 'subscription', id: dc.subscription.id) { '<i class="' + Icon.LNK.EXTERNAL + '"></i>' }
                                    }
                                    if (dc.org) {
                                        println "Org ${dc.org.id} - ${dc.org.name} ${dc.org.sortname} &nbsp;&nbsp;" +
                                                link(action: 'show', controller: 'org', id: dc.org.id) { '<i class="' + Icon.LNK.EXTERNAL + '"></i>' }
                                    }
                                    if (dc.link) {
                                        println "Links ${dc.link.id} "
                                    }
                                    if (dc.surveyConfig) {
                                        println "SurveyConfig ${dc.surveyConfig.id} - ${dc.surveyConfig.type} ${dc.surveyConfig.header} &nbsp;&nbsp; " +
                                                link(action: 'surveyConfigDocs', controller: 'survey', id: dc.surveyConfig.surveyInfo.id, params: ['surveyConfigID': dc.surveyConfig.id]) { '<i class="' + Icon.LNK.EXTERNAL + '"></i>' }
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
                            <li>${docRecovery.id} : <strong>${docRecovery.filename}</strong> -> <g:link controller="document" action="downloadDocument" id="${docRecovery.uuid}">${docRecovery.uuid}</g:link>
                            <g:if test="${docRecovery.owner}">
                                (Owner: <g:link action="show" controller="org" id="${docRecovery.owner.id}">${docRecovery.owner.name}</g:link>)
                            </g:if>

                            <g:link class="${Btn.SIMPLE} mini" action="processRecoveryDoc" params="[sourceDoc: doc.id, targetDoc: docRecovery.id]">Auswählen</g:link>

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
                                                print " <i class='${Icon.SIG.SHARED_OBJECT} square'></i> "
                                            }
                                            if (dc.sharedFrom) {
                                                print " <i class='${Icon.SIG.SHARED_OBJECT_ON}'></i> "
                                            }
                                            if (dc.license) {
                                                println "License ${dc.license.id} - ${dc.license.reference}, ${dc.license.sortableReference} &nbsp;&nbsp; " +
                                                        link(action: 'show', controller: 'lic', id: dc.license.id) { '<i class="' + Icon.LNK.EXTERNAL + '"></i>' }
                                            }
                                            if (dc.subscription) {
                                                println "Subscription ${dc.subscription.id} - ${dc.subscription.name} &nbsp;&nbsp; " +
                                                        link(action: 'show', controller: 'subscription', id: dc.subscription.id) { '<i class="' + Icon.LNK.EXTERNAL + '"></i>' }
                                            }
                                            if (dc.org) {
                                                println "Org ${dc.org.id} - ${dc.org.name} ${dc.org.sortname} &nbsp;&nbsp;" +
                                                        link(action: 'show', controller: 'org', id: dc.org.id) { '<i class="' + Icon.LNK.EXTERNAL + '"></i>' }
                                            }
                                            if (dc.link) {
                                                println "Links ${dc.link.id} "
                                            }
                                            if (dc.surveyConfig) {
                                                println "SurveyConfig ${dc.surveyConfig.id} - ${dc.surveyConfig.type} ${dc.surveyConfig.header} &nbsp;&nbsp; " +
                                                        link(action: 'surveyConfigDocs', controller: 'survey', id: dc.surveyConfig.surveyInfo.id, params: ['surveyConfigID': dc.surveyConfig.id]) { '<i class="' + Icon.LNK.EXTERNAL + '"></i>' }
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

<laser:htmlEnd />
