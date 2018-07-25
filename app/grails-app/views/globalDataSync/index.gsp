<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'globalDataSync.label', default: 'Global Data Sync')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>

<h1 class="ui header"><semui:headerIcon /><g:message code="globalDataSync.label"/></h1>

<semui:messages data="${flash}"/>


<semui:filter>
    <g:form action="index" method="get" class="ui form">
        <div class="fields">
            <div class="field">
                <label>${message(code: 'globalDataSync.search.text')}</label>
                <input type="text" name="q" placeholder="${message(code: 'globalDataSync.search.ph')}"
                       value="${params.q?.encodeAsHTML()}"/>
            </div>


                <div class="field">
                    <label>&nbsp;</label>
                    <a href="${request.forwardURI}" class="ui button">${message(code:'default.button.filterreset.label')}</a>
                </div>
                <div class="field">
                    <label>&nbsp;</label>
                    <input type="submit" class="ui secondary button"
                           value="${message(code: 'default.button.search.label')}"/>
                </div>
        </div>
    </g:form>
    <g:form action="index" method="get" class="ui form">
        <div class="fields">
            <g:link class="ui secondary button" params="[sort: 'ts', max: max, offset: offset, order: order]"><g:message
                    code="globalDataSync.updated"/></g:link>
        </div>
    </g:form>
</semui:filter>

<div>
    <g:form action="index" method="get" class="ui form">
        <div class="fields">
            <div class="field">
                <label>${message(code: 'package.type.change')}</label>
            </div>

            <div class="field">
                <label>&nbsp;</label>
                <g:select name="rectype" from="[0: 'Package', 1: 'Title']" onchange="this.form.submit()"
                          value="${rectype}" optionKey="key" optionValue="value"/>
            </div>
        </div>
    </g:form>

    <g:if test="${items != null}">
        <div class="container" style="text-align:center">
            ${message(code: 'globalDataSync.pagination.text', args: [offset, (offset.toInteger() + max.toInteger()), globalItemTotal])}
        </div>
    </g:if>
    <table class="ui sortable celled la-table table">
        <thead>
        <tr>
            <g:sortableColumn property="identifier" title="${message(code: 'package.identifier.label')}"/>
            <th>${message(code: 'package.name.slash.description')}</th>
            <g:sortableColumn property="source.name" title="${message(code: 'package.source.label')}"/>
            <g:sortableColumn property="kbplusCompliant" title="${message(code: 'package.kbplusCompliant.label')}"/>
            <g:sortableColumn property="globalRecordInfoStatus"
                              title="${message(code: 'package.globalRecordInfoStatus.label')}"/>
            <th>${message(code: 'globalDataSync.tippscount')}</th>
            <th>${message(code: 'default.actions')}</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${items}" var="item" status="k">
            <tr>
                <td><a href="${item.source.baseUrl}resource/show/${item.identifier}">${fieldValue(bean: item, field: "identifier")}</a><br/>
                    <g:message code="globalDataSync.updated.brackets"
                               args="[formatDate(date: item.ts, format: 'dd.MM.yyyy HH:mm')]"/></td>
                <td><a href="${item.source.baseUrl}resource/show/${item.identifier}">${fieldValue(bean: item, field: "name")}</a>
                <hr><a href="${item.source.baseUrl}resource/show/${item.identifier}">${fieldValue(bean: item, field: "desc")}</a>
                </td>
                <td><a href="${item.source.uri}?verb=getRecord&amp;identifier=${item.identifier}&amp;metadataPrefix=${item.source.fullPrefix}">
                    ${item.source.name}</a></td>
                %{--<td><a href="${item.source.baseUrl}search/index?qbe=g:1packages">${item.displayRectype}</a></td>--}%
                <td>${item.kbplusCompliant?.getI10n('value')}</td>
                <td>${item.globalRecordInfoStatus?.getI10n('value')}</td>
                <td>${tippcount[k]}</td>
                <g:if test="${item.globalRecordInfoStatus?.value != 'Current'}">
                    <td><g:link action="newCleanTracker" controller="globalDataSync" id="${item.id}"
                                class="ui negative button"
                                onclick="return confirm('${message(code: 'globalDataSync.trackingDeleted', default: 'Are you sure?')}')">
                        ${message(code: 'globalDataSync.track_new')}</g:link><hr>
                    <g:link action="selectLocalPackage" controller="globalDataSync" id="${item.id}"
                            class="ui negative button"
                            onclick="return confirm('${message(code: 'globalDataSync.trackingDeleted', default: 'Are you sure?')}')">
                        ${message(code: 'globalDataSync.track_merge')}</g:link>
                    </td>
                </g:if>
                <g:else>
                    <td><g:link action="newCleanTracker" controller="globalDataSync" id="${item.id}"
                                class="ui positive button">${message(code: 'globalDataSync.track_new')}</g:link><hr>
                    <g:link action="selectLocalPackage" controller="globalDataSync" id="${item.id}"
                            class="ui positive button">${message(code: 'globalDataSync.track_merge')}</g:link>
                    </td>
                </g:else>
            </tr>
            <g:each in="${item.trackers}" var="tracker">
                <tr>
                    <td colspan="6">
                        -> ${message(code: 'globalDataSync.using_id')}
                        <g:if test="${tracker.localOid != null}">
                            <g:if test="${tracker.localOid.startsWith('com.k_int.kbplus.Package')}">
                                <g:link controller="packageDetails" action="show"
                                        id="${tracker.localOid.split(':')[1]}">
                                    ${tracker.name ?: message(code: 'globalDataSync.noname')}</g:link>
                                <g:if test="${tracker.name == null}">
                                    <g:set var="confirm"
                                           value="${message(code: 'globalDataSync.cancel.confirm.noname')}"/>
                                </g:if>
                                <g:else>
                                    <g:set var="confirm"
                                           value="${message(code: 'globalDataSync.cancel.confirm', args: [tracker.name])}"/>
                                </g:else>
                                <g:link controller="globalDataSync" action="cancelTracking" class="ui negative button"
                                        params="[trackerId: tracker.id, itemName: fieldValue(bean: item, field: 'name')]"
                                        onclick="return confirm('${confirm}')">
                                    <g:message code="globalDataSync.cancel"/>
                                </g:link>
                            </g:if>
                        </g:if>
                        <g:else>No tracker local oid</g:else>
                    </td>
                </tr>
            </g:each>
        </g:each>
        </tbody>
    </table>

    <semui:paginate action="index" controller="globalDataSync" params="${params}" next="Next" prev="Prev" max="${max}"
                    total="${globalItemTotal}"/>

</div>
</body>
</html>
