<%@ page import="com.k_int.kbplus.Person; de.laser.helper.RDStore; com.k_int.properties.PropertyDefinition; com.k_int.kbplus.RefdataValue; de.laser.AuditConfig; com.k_int.kbplus.RefdataCategory" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'subscription.details.subscriberManagement.label')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${contextService.getOrg()?.getDesignation()}"/>
    <semui:crumb controller="myInstitution" action="currentSubscriptions"
                 text="${message(code: 'myinst.currentSubscriptions.label')}"/>
    <semui:crumb controller="subscription" action="show" id="${subscriptionInstance.id}"
                 text="${subscriptionInstance.name}"/>

    <semui:crumb class="active" text="${message(code: 'subscription.details.subscriberManagement.label')}"/>

</semui:breadcrumbs>

<h1 class="ui left aligned icon header">
    ${message(code: 'subscription.details.subscriberManagement.label')}
</h1>

<g:render template="navSubscriberManagement"/>

<h3 class="ui left aligned icon header"><semui:headerIcon/>
${message(code: 'subscription.subscriptionPropertiesConsortia.header')}
</h3>

<semui:messages data="${flash}"/>

<h4>
    ${message(code: 'subscription.linkPackagesConsortium.consortialSubscription')}: <g:link
        controller="subscription" action="show"
        id="${parentSub.id}">${parentSub.name}</g:link><br><br>

</h4>


<g:if test="${filteredSubChilds}">

    <div class="ui segment">
        <g:form action="processSubscriptionPropertiesConsortia" method="post" class="ui form">
            <g:hiddenField name="id" value="${params.id}"/>

            <h4>${message(code: 'subscription.subscriptionPropertiesConsortia.info')}</h4>

            <div class="two fields">
                <semui:datepicker label="subscription.startDate.label" id="valid_from" name="valid_from"/>

                <semui:datepicker label="subscription.endDate.label" id="valid_to" name="valid_to" />
            </div>

            <div class="three fields">
                <div class="field">
                    <label>${message(code: 'subscription.details.status')}</label>
                    <%
                        def fakeList = []
                        fakeList.addAll(RefdataCategory.getAllRefdataValues('Subscription Status'))
                        fakeList.remove(com.k_int.kbplus.RefdataValue.getByValueAndCategory('Deleted', 'Subscription Status'))
                    %>
                    <laser:select name="status" from="${fakeList}" optionKey="id" optionValue="value" noSelection="${['':'']}" value="${['':'']}"/>
                </div>

                <div class="field">
                    <label>${message(code: 'subscription.form.label')}</label>
                    <laser:select name="form" from="${RefdataCategory.getAllRefdataValues('Subscription Form')}" optionKey="id" optionValue="value" noSelection="${['':'']}" value="${['':'']}"/>
                </div>

                <div class="field">
                    <label>${message(code: 'subscription.resource.label')}</label>
                    <laser:select name="resource" from="${RefdataCategory.getAllRefdataValues('Subscription Resource')}" optionKey="id" optionValue="value" noSelection="${['':'']}" value="${['':'']}"/>
                </div>


            </div>

            <button class="ui button" type="submit">${message(code: 'default.button.save_changes')}</button>
        </g:form>
    </div>

    <g:set var="editableOld" value="${editable}"/>


    <div class="divider"></div>

    <div class="ui segment">
        <h3>${message(code: 'subscription.propertiesConsortia.consortialSubscription')}</h3>
        <table class="ui celled la-table table">
            <thead>
            <tr>
                <th>${message(code: 'subscription')}</th>
                <th>${message(code: 'default.startDate.label')}</th>
                <th>${message(code: 'default.endDate.label')}</th>
                <th>${message(code: 'subscription.details.status')}</th>
                <th>${message(code: 'subscription.form.label')}</th>
                <th>${message(code: 'subscription.resource.label')}</th>
                <th></th>
            </tr>
            </thead>
            <tbody>

            <td>${parentSub.name}</td>

            <td>
                <g:formatDate formatName="default.date.format.notime" date="${parentSub?.startDate}"/>
                <semui:auditButton auditable="[parentSub, 'startDate']"/>
            </td>
            <td>
                <g:formatDate formatName="default.date.format.notime" date="${parentSub?.endDate}"/>
                <semui:auditButton auditable="[parentSub, 'endDate']"/>
            </td>
            <td>
                ${parentSub.status.getI10n('value')}
                <semui:auditButton auditable="[parentSub, 'status']"/>
            </td>
            <td>
                ${parentSub.form.getI10n('value')}
                <semui:auditButton auditable="[parentSub, 'form']"/>
            </td>
            <td>
                ${parentSub.resource.getI10n('value')}
                <semui:auditButton auditable="[parentSub, 'resource']"/>
            </td>

            <td class="x">
                <g:link controller="subscription" action="show" id="${parentSub.id}"
                        class="ui icon button"><i
                        class="write icon"></i></g:link>
            </td>
            </tr>
            </tbody>
        </table>
    </div>

    <div class="ui segment">
        <h3>${message(code: 'subscription.propertiesConsortia.subscriber')} <semui:totalNumber
                total="${filteredSubChilds?.size()}"/></h3>
        <table class="ui celled la-table table">
            <thead>
            <tr>
                <th>${message(code: 'sidewide.number')}</th>
                <th>${message(code: 'default.sortname.label')}</th>
                <th>${message(code: 'subscriptionDetails.members.members')}</th>
                <th>${message(code: 'default.startDate.label')}</th>
                <th>${message(code: 'default.endDate.label')}</th>
                <th>${message(code: 'subscription.details.status')}</th>
                <th>${message(code: 'subscription.form.label')}</th>
                <th>${message(code: 'subscription.resource.label')}</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${filteredSubChilds}" status="i" var="zeile">
                <g:set var="sub" value="${zeile.sub}"/>
                <tr>
                    <td>${i + 1}</td>
                    <g:set var="filteredSubscribers" value="${zeile.orgs}"/>
                    <g:each in="${filteredSubscribers}" var="subscr">
                        <td>${subscr.sortname}</td>
                        <td>
                            <g:link controller="organisation" action="show"
                                    id="${subscr.id}">${subscr}</g:link>

                            <g:if test="${sub.isSlaved?.value?.equalsIgnoreCase('yes')}">
                                <span data-position="top right"
                                      data-tooltip="${message(code: 'license.details.isSlaved.tooltip')}">
                                    <i class="thumbtack blue icon"></i>
                                </span>
                            </g:if>

                        </td>
                    </g:each>
                    <g:if test="${!sub.getAllSubscribers()}">
                        <td></td>
                        <td></td>
                    </g:if>

                    <td>
                        <semui:xEditable owner="${sub}" field="startDate" type="date"
                                         overwriteEditable="${editableOld}"/>
                        <semui:auditButton auditable="[sub, 'startDate']"/>
                    </td>
                    <td><semui:xEditable owner="${sub}" field="endDate" type="date"
                                         overwriteEditable="${editableOld}"/>
                    <semui:auditButton auditable="[sub, 'endDate']"/>
                    </td>
                    <td>
                        <semui:xEditableRefData owner="${sub}" field="status" config='Subscription Status' constraint="removeValue_deleted" overwriteEditable="${editableOld}"/>
                        <semui:auditButton auditable="[sub, 'status']"/>
                    </td>
                    <td>
                        <semui:xEditableRefData owner="${sub}" field="form" config='Subscription Form'  overwriteEditable="${editableOld}"/>
                        <semui:auditButton auditable="[sub, 'form']"/>
                    </td>
                    <td>
                        <semui:xEditableRefData owner="${sub}" field="resource" config='Subscription Resource' overwriteEditable="${editableOld}"/>
                        <semui:auditButton auditable="[sub, 'resource']"/>
                    </td>
                    <td class="x">
                        <g:link controller="subscription" action="show" id="${sub.id}"
                                class="ui icon button"><i
                                class="write icon"></i></g:link>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>

    </div>
</g:if>
<g:else>

    <br>

    <g:if test="${!filteredSubChilds}">
        <strong><g:message code="subscription.details.nomembers.label"
                           default="No members have been added to this license. You must first add members."/></strong>
    </g:if>

</g:else>

<div id="magicArea"></div>

</body>
</html>

