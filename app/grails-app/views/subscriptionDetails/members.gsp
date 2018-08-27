<%@ page import="com.k_int.kbplus.Person" %>
<%@ page import="com.k_int.kbplus.RefdataValue" %>
<% def contextService = grailsApplication.mainContext.getBean("contextService") %>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'subscription.details.members.label')}</title>
</head>
<body>

    <g:render template="breadcrumb" model="${[ params:params ]}"/>

    <semui:controlButtons>
        <g:render template="actions" />
    </semui:controlButtons>

    <h1 class="ui header">
        <semui:headerIcon />
        <semui:xEditable owner="${subscriptionInstance}" field="name" />
    </h1>

    <g:render template="nav" />

    <%--
    <semui:filter>
        <form class="ui form">
            <div class="fields">
                <div class="field">
                    <div class="ui checkbox">
                        <input class="hidden" type="checkbox" name="showDeleted" value="Y" ${params.showDeleted?'checked="checked"':''}>
                        <label>Gelöschte Teilnehmer anzeigen</label>
                    </div>
                </div>

                <div class="field">
                    <input type="submit" class="ui secondary button" value="${message(code:'default.button.search.label')}" />
                </div>
            </div>
        </form>
    </semui:filter>
    --%>

    <g:if test="${validSubChilds}">

    <g:each in="${[validSubChilds]}" status="i" var="outerLoop">

        <table class="ui celled la-table table">
            <thead>
                <tr>
                    <th>
                        ${message(code:'sidewide.number')}
                    </th>
                    <th>Sortiername</th>
                    <th>
                        ${message(code:'subscriptionDetails.members.members')}
                    </th>

                    <th>${message(code:'default.startDate.label')}</th>
                    <th>${message(code:'default.endDate.label')}</th>
                    <th>${message(code:'subscription.details.status')}</th>
                    <th></th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${outerLoop}" status="j" var="sub">
                    <tr>
                        <td>${j + 1}</td>

                        <g:each in="${sub.getAllSubscribers()}" var="subscr">

                            <td>${subscr.sortname}</td>
                            <td>
                                <g:link controller="organisations" action="show" id="${subscr.id}">${subscr}</g:link>

                                <g:set var="rdvGcp" value="${RefdataValue.findByValue('General contact person')}"/>
                                <g:set var="rdvSse" value="${RefdataValue.findByValue('Specific subscription editor')}"/>

                                <div class="ui list">

                                    <g:each in="${Person.getPublicByOrgAndFunc(subscr, 'General contact person')}" var="gcp">
                                        <div class="item">
                                            <g:link controller="person" action="show" id="${gcp.id}">${gcp}</g:link>
                                            (${rdvGcp.getI10n('value')})
                                        </div>
                                    </g:each>
                                    <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(subscr, 'General contact person', contextService.getOrg())}" var="gcp">
                                        <div class="item">
                                            <g:link controller="person" action="show" id="${gcp.id}">${gcp}</g:link>
                                            (${rdvGcp.getI10n('value')} <i class="address book outline icon" style="display:inline-block"></i>)
                                        </div>
                                    </g:each>
                                    <g:each in="${Person.getPublicByOrgAndObjectResp(subscr, sub, 'Specific subscription editor')}" var="sse">
                                        <div class="item">
                                            <g:link controller="person" action="show" id="${sse.id}">${sse}</g:link>
                                            (${rdvSse.getI10n('value')})
                                        </div>
                                    </g:each>
                                    <g:each in="${Person.getPrivateByOrgAndObjectRespFromAddressbook(subscr, sub, 'Specific subscription editor', contextService.getOrg())}" var="sse">
                                        <div class="item">
                                            <g:link controller="person" action="show" id="${sse.id}">${sse}</g:link>
                                            (${rdvSse.getI10n('value')} <i class="address book outline icon" style="display:inline-block"></i>)
                                        </div>
                                    </g:each>

                                </div>
                            </td>

                        </g:each>
                        <g:if test="${! sub.getAllSubscribers()}">
                            <td></td>
                            <td></td>
                        </g:if>

                        <td>
                            <g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/>
                        </td>
                        <td>
                            <g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/>
                        </td>
                        <td>
                            ${sub.status.getI10n('value')}
                        </td>
                        <td class="x">
                            <g:link controller="subscriptionDetails" action="show" id="${sub.id}" class="ui icon button"><i class="write icon"></i></g:link>

                            <g:if test="${editable && i<1}">
                                <g:link controller="subscriptionDetails" action="deleteMember" class="ui icon negative button"
                                        params="${[id:subscriptionInstance.id, target: sub.id]}"
                                        onclick="return confirm('${message(code:'license.details.delete.confirm', args:[(sub.name?:'this subscription')])}')">
                                    <i class="trash alternate icon"></i>
                                </g:link>
                            </g:if>

                        </td>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </g:each>

    </g:if>
    <g:else>
        <br><strong><g:message code="subscription.details.nomembers.label" default="No members have been added to this license. You must first add members."/></strong>
    </g:else>

</body>
</html>

