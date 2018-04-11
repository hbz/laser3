<%@ page import="com.k_int.kbplus.Person" %>
<%@ page import="com.k_int.kbplus.RefdataValue" %>
<% def contextService = grailsApplication.mainContext.getBean("contextService") %>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'subscription.label', default:'Subscription')}</title>
</head>
<body>

    <g:render template="breadcrumb" model="${[ params:params ]}"/>
    <semui:controlButtons>
        <g:render template="actions" />
    </semui:controlButtons>
    <h1 class="ui header"><semui:headerIcon />

        <semui:xEditable owner="${subscriptionInstance}" field="name" />
    </h1>

    <g:render template="nav" />

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

    <table class="ui stripped table">
        <thead>
            <tr>
                <th>Teilnehmer</th>
                <th>${message(code:'person.contacts.label')}</th>
                <th>${message(code:'default.startDate.label')}</th>
                <th>${message(code:'default.endDate.label')}</th>
                <th>${message(code:'subscription.details.status')}</th>
                <th></th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${subscriptionChildren}" var="sub">
                <tr>
                    <td>
                        <g:each in="${sub.getAllSubscribers()}" var="subscr">
                            <g:link controller="organisations" action="show" id="${subscr.id}">${subscr}</g:link>
                        </g:each>
                    </td>
                    <td>
                        <g:each in="${sub.getAllSubscribers()}" var="subscr">
                            <g:set var="rdvGcp" value="${RefdataValue.findByValue('General contact person')}"/>
                            <g:set var="rdvSse" value="${RefdataValue.findByValue('Specific subscription editor')}"/>

                            <g:each in="${Person.getByOrgAndFunction(subscr, 'General contact person')}" var="gcp">
                                ${rdvGcp.getI10n('value')}
                                <br />
                                <g:link controller="person" action="show" id="${gcp.id}">${gcp}</g:link>
                                <br />
                            </g:each>
                            <g:each in="${Person.getByOrgAndFunctionFromAddressbook(subscr, 'General contact person', contextService.getOrg())}" var="gcp">
                                <i class="address book outline icon"></i>
                                ${rdvGcp.getI10n('value')}
                                <br />
                                <g:link controller="person" action="show" id="${gcp.id}">${gcp}</g:link>
                                <br />
                            </g:each>
                            <g:each in="${Person.getByOrgAndObjectAndResponsibility(subscr, sub, 'Specific subscription editor')}" var="sse">
                                ${rdvSse.getI10n('value')}
                                <br />
                                <g:link controller="person" action="show" id="${sse.id}">${sse}</g:link>
                                <br />
                            </g:each>
                            <g:each in="${Person.getByOrgAndObjectAndResponsibilityFromAddressbook(subscr, sub, 'Specific subscription editor', contextService.getOrg())}" var="sse">
                                <i class="address book outline icon"></i>
                                ${rdvSse.getI10n('value')}
                                <br />
                                <g:link controller="person" action="show" id="${sse.id}">${sse}</g:link>
                                <br />
                            </g:each>
                        </g:each>
                    </td>
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

                        <g:if test="${editable}">
                            <g:link controller="subscriptionDetails" action="deleteMember" class="ui icon negative button"
                                    params="${[id:subscriptionInstance.id, basesubscription: sub.id]}"
                                    onclick="return confirm('${message(code:'license.details.delete.confirm', args:[(sub.name?:'this subscription')])}')">
                                <i class="trash alternate icon"></i>
                            </g:link>
                        </g:if>

                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>

    <g:link controller="subscriptionDetails" action="addMembers"  params="${[id:params.id]}" target="_blank"><input type="button" class="ui button" value="${message(code:'subscription.details.addMembers.label')}" /></g:link>

</body>
</html>

