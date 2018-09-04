<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'myinst.copyLicense')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}"/>
    <semui:crumb controller="myInstitution" action="currentLicenses" message="license.current"/>
    <semui:crumb message="myinst.copyLicense" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <g:render template="actions"/>
</semui:controlButtons>

<h1 class="ui header"><semui:headerIcon />
${message(code: 'myinst.copyLicense')}: ${license.reference}
</h1>

<semui:messages data="${flash}"/>

<semui:form>
    <g:form action="processcopyLicense" controller="licenseDetails" method="post" class="ui form newLicence">


        <div class="field required">
            <label>${message(code: 'myinst.emptyLicense.name', default: 'New License Name')}</label>
            <input required type="text" name="lic_name" value="Kopie ${license.reference}" placeholder=""/>
        </div>


 <hr>
<table class="ui celled table">
    <tbody>

    <input type="hidden" name="baseLicense" value="${params.id}"/>

    <tr><th>${message(code:'default.select.label', default:'Select')}</th><th >${message(code:'license.property', default:'License Properties')}</th><th>${message(code:'default.value.label', default:'Value')}</th></tr>
    <tr>
        <th><g:checkBox name="license.copyDates" value="${true}" /></th>
        <th>${message(code:'license.copyDates', default:'Copy all Dates from License')}</th>
        <td><g:formatDate date="${license?.startDate}" format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}"/>${license?.endDate ? (' - '+formatDate(date:license?.endDate, format: message(code:'default.date.format.notime', default:'yyyy-MM-dd'))):''}</td>
    </tr>
    <tr>
        <th><g:checkBox name="license.copyLinks" value="${true}" /></th>
        <th>${message(code:'license.copyLinks', default:'Copy Links from License')}</th>
        <td>
            <b>${message(code:'license.linktoLicense', default:'License Template')}:</b>
            <g:if test="${license.instanceOf}">
                <g:link controller="licenseDetails" action="show" target="_blank" id="${license.instanceOf.id}">${license.instanceOf}</g:link>
            </g:if>
            <g:else>
                ${message(code:'license.linktoLicenseEmpty', default:'No License Template')}
            </g:else>
            <br>

        <g:each in="${visibleOrgLinks}" var="role">
            <g:if test="${role.org}">
                <b>${role?.roleType?.getI10n("value")}:</b> <g:link controller="Organisations" action="show" target="_blank" id="${role.org.id}">${role?.org?.name}</g:link><br>
            </g:if>
        </g:each>
        </td>
    </tr>
    <tr>
        <th><g:checkBox name="license.copyDocs" value="${true}" /></th>
        <th>${message(code:'license.copyDocs', default:'Copy Documents from License')}</th>
        <td>
            <g:each in="${licenseInstance.documents.sort{it.owner?.title}}" var="docctx">
                <g:if test="${(( (docctx.owner?.contentType==1) || ( docctx.owner?.contentType==3) ) && ( docctx.status?.value!='Deleted'))}">
                                <g:link controller="docstore" id="${docctx.owner.uuid}">
                                <g:if test="${docctx.owner?.title}">
                                    ${docctx.owner.title}
                                </g:if>
                                <g:else>
                                    <g:if test="${docctx.owner?.filename}">
                                        ${docctx.owner.filename}
                                    </g:if>
                                    <g:else>
                                        ${message(code:'template.documents.missing', default: 'Missing title and filename')}
                                    </g:else>
                                </g:else>

                            </g:link>(${docctx.owner.type.getI10n("value")}) <br>
                </g:if>
            </g:each>
        </td>
    </tr>
    <tr>
        <th><g:checkBox name="license.copyAnnouncements" value="${true}" /></th>
        <th>${message(code:'license.copyAnnouncements', default:'Copy Notes from License')}</th>
        <td>
            <g:each in="${licenseInstance.documents.sort{it.owner?.title}}" var="docctx">
                <g:if test="${((docctx.owner?.contentType == com.k_int.kbplus.Doc.CONTENT_TYPE_STRING) && !(docctx.domain) && (docctx.status?.value != 'Deleted') )}">
                            <g:if test="${docctx.owner.title}">
                                <b>${docctx.owner.title}</b>
                            </g:if>
                            <g:else>
                                <b>Ohne Titel</b>
                            </g:else>

                            (${message(code:'template.notes.created')}
                            <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${docctx.owner.dateCreated}"/>)

                            <g:if test="${docctx.alert}">
                                ${message(code:'template.notes.shared')} ${docctx.alert.createdBy.displayName}
                                <g:if test="${docctx.alert.sharingLevel == 1}">
                                    ${message(code:'template.notes.shared_jc')}
                                </g:if>
                                <g:if test="${docctx.alert.sharingLevel == 2}">
                                    ${message(code:'template.notes.shared_community')}
                                </g:if>
                                <div class="comments">
                                    <a href="#modalComments" class="announce" data-id="${docctx.alert.id}">
                                        ${docctx.alert?.comments != null ? docctx.alert?.comments?.size() : 0} Comment(s)
                                    </a>
                                </div>
                            </g:if>
                            <g:else>
                                <!--${message(code:'template.notes.not_shared')}-->
                            </g:else>
                        <br>
                </g:if>
            </g:each>
        </td>
    </tr>
    <tr>
        <th><g:checkBox name="license.copyTasks" value="${true}" /></th>
        <th>${message(code:'license.copyTasks', default:'Copy Tasks from License')}</th>
        <td>
            <g:each in="${tasks}" var="tsk">
                    <div id="summary" class="summary">
                    <b>${tsk?.title}</b> (${message(code:'task.endDate.label')}
                            <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${tsk.endDate}"/>)
                    <br>
            </g:each>
        </td>
    </tr>
    <tr>
        <th><g:checkBox name="license.copyCustomProperties" value="${true}" /></th>
        <th>${message(code:'license.copyCostumProperty', default:'Copy Property from License')}</th>
        <td>${message(code:'license.properties')}<br>
            ${message(code:'license.openaccess.properties')}<br>
            ${message(code:'license.archive.properties')}<br>
        </td>
    </tr>
    <tr>
        <th><g:checkBox name="license.copyPrivateProperties" value="${true}" /></th>
        <th>${message(code:'license.copyPrivateProperty', default:'Copy Property from License')}</th>
        <td>${message(code:'license.properties.private')} ${contextOrg?.name}<br>
        </td>
    </tr>
    </tbody>
</table>
        <input type="submit" class="ui button js-click-control" value="${message(code: 'default.button.create.label', default: 'Create')}"/>
    </g:form>
</semui:form>
</body>
</html>