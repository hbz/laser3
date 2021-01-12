<%@ page import="de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.Person; de.laser.helper.RDStore; de.laser.helper.RDConstants; de.laser.properties.PropertyDefinition; de.laser.AuditConfig; de.laser.FormService" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'subscription.subscriptionPropertiesMembers.header', args: args.memberTypeGenitive)}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="currentSubscriptions"
                 text="${message(code: 'myinst.currentSubscriptions.label')}"/>
    <semui:crumb controller="subscription" action="show" id="${subscription.id}"
                 text="${subscription.name}"/>

    <semui:crumb class="active"
                 text="${message(code: 'subscription.details.subscriberManagement.label', args: args.memberType)}"/>

</semui:breadcrumbs>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>${subscription.name}</h1>

<semui:anualRings object="${subscription}" controller="subscription" action="${actionName}"
                  navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

<g:render template="navSubscriberManagement"/>

<semui:messages data="${flash}"/>

<h2 class="ui header">
    <g:message code="subscription"/>:
    <g:link controller="subscription" action="show" id="${subscription.id}">${subscription.name}</g:link>
</h2>


<g:if test="${filteredSubChilds}">

    <div class="ui top attached tabular menu">

        <g:link class="item ${params.tab == 'generalProperties' ? 'active' : ''}"
                controller="subscription" action="subscriptionPropertiesMembers"
                id="${subscription.id}"
                params="[tab: 'generalProperties']">
            <g:message code="subscription.subscriptionPropertiesMembers.generalProperties"/>

        </g:link>

        <g:link class="item ${params.tab == 'providerAgency' ? 'active' : ''}"
                controller="subscription" action="subscriptionPropertiesMembers"
                id="${subscription.id}"
                params="[tab: 'providerAgency']">
            <g:message code="subscription.subscriptionPropertiesMembers.providerAgency"/>

        </g:link>

        <g:link class="item ${params.tab == 'documents' ? 'active' : ''}"
                controller="subscription" action="subscriptionPropertiesMembers"
                id="${subscription.id}"
                params="[tab: 'documents']">
            <g:message code="subscription.subscriptionPropertiesMembers.documents"/>

        </g:link>

        <g:link class="item ${params.tab == 'notes' ? 'active' : ''}"
                controller="subscription" action="subscriptionPropertiesMembers"
                id="${subscription.id}"
                params="[tab: 'notes']">
            <g:message code="subscription.subscriptionPropertiesMembers.notes"/>

        </g:link>

        <g:link class="item ${params.tab == 'multiYear' ? 'active' : ''}"
                controller="subscription" action="subscriptionPropertiesMembers"
                id="${subscription.id}"
                params="[tab: 'multiYear']">
            <g:message code="subscription.isMultiYear.label"/>

        </g:link>

    </div>


    <g:if test="${params.tab == 'generalProperties'}">
        <div class="ui bottom attached tab segment active">

            <g:set var="editableOld" value="${editable}"/>

            <div class="ui segment">
                <h3 class="ui header"><g:message code="subscription.propertiesMembers.subscription" args="${args.superOrgType}"/></h3>
                <table class="ui celled la-table table">
                    <thead>
                    <tr>
                        <th>${message(code: 'subscription')}</th>
                        <th>${message(code: 'default.startDate.label')}</th>
                        <th>${message(code: 'default.endDate.label')}</th>
                        <th>${message(code: 'default.status.label')}</th>
                        <th>${message(code: 'subscription.kind.label')}</th>
                        <th>${message(code: 'subscription.form.label')}</th>
                        <th>${message(code: 'subscription.resource.label')}</th>
                        <th>${message(code: 'subscription.isPublicForApi.label')}</th>
                        <th>${message(code: 'subscription.hasPerpetualAccess.label')}</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                    <td>${subscription.name}</td>

                    <td>
                        <g:formatDate formatName="default.date.format.notime" date="${subscription.startDate}"/>
                        <semui:auditButton auditable="[subscription, 'startDate']"/>
                    </td>
                    <td>
                        <g:formatDate formatName="default.date.format.notime" date="${subscription.endDate}"/>
                        <semui:auditButton auditable="[subscription, 'endDate']"/>
                    </td>
                    <td>
                        ${subscription.status.getI10n('value')}
                        <semui:auditButton auditable="[subscription, 'status']"/>
                    </td>
                    <td>
                        ${subscription.kind?.getI10n('value')}
                        <semui:auditButton auditable="[subscription, 'kind']"/>
                    </td>
                    <td>
                        ${subscription.form?.getI10n('value')}
                        <semui:auditButton auditable="[subscription, 'form']"/>
                    </td>
                    <td>
                        ${subscription.resource?.getI10n('value')}
                        <semui:auditButton auditable="[subscription, 'resource']"/>
                    </td>
                    <td>
                        ${subscription.isPublicForApi ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}
                        <semui:auditButton auditable="[subscription, 'isPublicForApi']"/>
                    </td>
                    <td>
                        ${subscription.hasPerpetualAccess ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}
                        <semui:auditButton auditable="[subscription, 'hasPerpetualAccess']"/>
                    </td>

                    <td class="x">
                        <g:link controller="subscription" action="show" id="${subscription.id}"
                                class="ui icon button"><i
                                class="write icon"></i></g:link>
                    </td>
                    </tr>
                    </tbody>
                </table>
            </div>

            <div class="divider"></div>

            <div class="ui segment">
                <g:form action="processSubscriptionPropertiesMembers" method="post" class="ui form propertiesSubscription">
                    <g:hiddenField id="pspm_id_${params.id}" name="id" value="${params.id}"/>
                    <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>

                    <h4 class="ui header">${message(code: 'subscription.subscriptionPropertiesMembers.info', args: args.memberType)}</h4>

                    <div class="two fields">
                        <semui:datepicker label="subscription.startDate.label" id="valid_from" name="valid_from"/>

                        <semui:datepicker label="subscription.endDate.label" id="valid_to" name="valid_to"/>
                    </div>

                    <div class="three fields">
                        <div class="field">
                            <label>${message(code: 'default.status.label')}</label>
                            <%
                                def fakeList = []
                                fakeList.addAll(RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS))
                                fakeList.remove(RefdataValue.getByValueAndCategory('Deleted', RDConstants.SUBSCRIPTION_STATUS))
                            %>
                            <laser:select name="status" from="${fakeList}" optionKey="id" optionValue="value"
                                          noSelection="${['': '']}"
                                          value="${['': '']}"/>
                        </div>

                        <div class="field">
                            <label>${message(code: 'subscription.kind.label')}</label>
                            <laser:select name="kind"
                                          from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_KIND)}"
                                          optionKey="id" optionValue="value" noSelection="${['': '']}"
                                          value="${['': '']}"/>
                        </div>

                        <div class="field">
                            <label>${message(code: 'subscription.form.label')}</label>
                            <laser:select name="form"
                                          from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_FORM)}"
                                          optionKey="id" optionValue="value" noSelection="${['': '']}"
                                          value="${['': '']}"/>
                        </div>

                        <div class="field">
                            <label>${message(code: 'subscription.resource.label')}</label>
                            <laser:select name="resource"
                                          from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_RESOURCE)}"
                                          optionKey="id" optionValue="value" noSelection="${['': '']}"
                                          value="${['': '']}"/>
                        </div>

                        <div class="field">
                            <label>${message(code: 'subscription.isPublicForApi.label')}</label>
                            <laser:select name="isPublicForApi"
                                          from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                                          optionKey="id" optionValue="value" noSelection="${['': '']}"
                                          value="${['': '']}"/>
                        </div>

                        <div class="field">
                            <label>${message(code: 'subscription.hasPerpetualAccess.label')}</label>
                            <laser:select name="hasPerpetualAccess"
                                          from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                                          optionKey="id" optionValue="value" noSelection="${['': '']}"
                                          value="${['': '']}"/>
                        </div>

                    </div>

                    <button class="ui button" type="submit">${message(code: 'default.button.save_changes')}</button>


                    <h3 class="ui header">
                        ${message(code: 'subscription.propertiesMembers.subscriber')}
                        <semui:totalNumber total="${filteredSubChilds?.size()}"/>
                    </h3>
                    <table class="ui celled la-table table">
                        <thead>
                        <tr>
                            <th>
                                <g:checkBox name="membersListToggler" id="membersListToggler" checked="false"/>
                            </th>
                            <th>${message(code: 'sidewide.number')}</th>
                            <th>${message(code: 'default.sortname.label')}</th>
                            <th>${message(code: 'subscriptionDetails.members.members')}</th>
                            <th>${message(code: 'default.startDate.label')}</th>
                            <th>${message(code: 'default.endDate.label')}</th>
                            <th>${message(code: 'default.status.label')}</th>
                            <th>${message(code: 'subscription.kind.label')}</th>
                            <th>${message(code: 'subscription.form.label')}</th>
                            <th>${message(code: 'subscription.resource.label')}</th>
                            <th>${message(code: 'subscription.isPublicForApi.label')}</th>
                            <th>${message(code: 'subscription.hasPerpetualAccess.label')}</th>
                            <th class="la-no-uppercase">
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                      data-content="${message(code: 'subscription.isMultiYear.consortial.label')}">
                                    <i class="map orange icon"></i>
                                </span>
                            </th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each in="${filteredSubChilds}" status="i" var="zeile">
                            <g:set var="sub" value="${zeile.sub}"/>
                            <tr>
                                <td>
                                    <g:checkBox id="selectedMembers_${sub.id}" name="selectedMembers" value="${sub.id}" checked="false"/>
                                </td>
                                <td>${i + 1}</td>
                                <g:set var="filteredSubscribers" value="${zeile.orgs}"/>
                                <g:each in="${filteredSubscribers}" var="subscr">
                                    <td>
                                        ${subscr.sortname}
                                    </td>
                                    <td>
                                        <g:link controller="organisation" action="show"
                                                id="${subscr.id}">${subscr}</g:link>

                                        <g:if test="${sub.isSlaved}">
                                            <span data-position="top right"
                                                  class="la-popup-tooltip la-delay"
                                                  data-content="${message(code: 'license.details.isSlaved.tooltip')}">
                                                <i class="thumbtack blue icon"></i>
                                            </span>
                                        </g:if>
                                        <g:if test="${subscr.getCustomerType() in ['ORG_INST', 'ORG_INST_COLLECTIVE']}">
                                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                                  data-content="${subscr.getCustomerTypeI10n()}">
                                                <i class="chess rook grey icon"></i>
                                            </span>
                                        </g:if>

                                    </td>
                                </g:each>
                                <g:if test="${!sub.getAllSubscribers()}">
                                    <td></td>
                                    <td></td>
                                </g:if>

                                <td>
                                    <semui:xEditable owner="${sub}" field="startDate" type="date" overwriteEditable="${editableOld}" validation="datesCheck"/>
                                    <semui:auditButton auditable="[sub, 'startDate']"/>
                                </td>
                                <td><semui:xEditable owner="${sub}" field="endDate" type="date" overwriteEditable="${editableOld}" validation="datesCheck"/>
                                <semui:auditButton auditable="[sub, 'endDate']"/>
                                </td>
                                <td>
                                    <semui:xEditableRefData owner="${sub}" field="status"
                                                            config="${RDConstants.SUBSCRIPTION_STATUS}"
                                                            constraint="removeValue_deleted"
                                                            overwriteEditable="${editableOld}"/>
                                    <semui:auditButton auditable="[sub, 'status']"/>
                                </td>
                                <td>
                                    <semui:xEditableRefData owner="${sub}" field="kind"
                                                            config="${RDConstants.SUBSCRIPTION_KIND}"
                                                            overwriteEditable="${editableOld}"/>
                                    <semui:auditButton auditable="[sub, 'kind']"/>
                                </td>
                                <td>
                                    <semui:xEditableRefData owner="${sub}" field="form"
                                                            config="${RDConstants.SUBSCRIPTION_FORM}"
                                                            overwriteEditable="${editableOld}"/>
                                    <semui:auditButton auditable="[sub, 'form']"/>
                                </td>
                                <td>
                                    <semui:xEditableRefData owner="${sub}" field="resource"
                                                            config="${RDConstants.SUBSCRIPTION_RESOURCE}"
                                                            overwriteEditable="${editableOld}"/>
                                    <semui:auditButton auditable="[sub, 'resource']"/>
                                </td>
                                <td>
                                    <semui:xEditableBoolean owner="${sub}" field="isPublicForApi"
                                                            overwriteEditable="${editableOld}"/>
                                    <semui:auditButton auditable="[sub, 'form']"/>
                                </td>
                                <td>
                                    <semui:xEditableBoolean owner="${sub}" field="hasPerpetualAccess"
                                                            overwriteEditable="${editableOld}"/>
                                    <semui:auditButton auditable="[sub, 'resource']"/>
                                </td>
                                <td>
                                    <g:if test="${sub.isMultiYear}">
                                        <span class="la-long-tooltip la-popup-tooltip la-delay"
                                              data-position="bottom center"
                                              data-content="${message(code: 'subscription.isMultiYear.consortial.label')}">
                                            <i class="map orange icon"></i>
                                        </span>
                                    </g:if>
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
                </g:form>
            </div>
        </div>
    </g:if>

    <g:if test="${params.tab == 'providerAgency'}">
        <div class="ui bottom attached tab segment active">
            <div class="ui segment ">
                <h3 class="ui header"><g:message code="subscription.propertiesMembers.subscription" args="${args.superOrgType}"/></h3>

                <div class="twelve wide column">

                    <div class="la-inline-lists">
                        <div class="ui card sixteen wide">
                            <div class="content">
                                <g:render template="/templates/links/orgLinksAsList"
                                          model="${[roleLinks    : visibleOrgRelations,
                                                    roleObject   : subscription,
                                                    roleRespValue: 'Specific subscription editor',
                                                    editmode     : editable,
                                                    showPersons  : false
                                          ]}"/>
                            </div>
                        </div>

                        <div class="ui segment">
                            <h3 class="ui header">${message(code: 'subscription.propertiesMembers.subscriber')} <semui:totalNumber
                                    total="${filteredSubChilds?.size()}"/></h3>
                            <table class="ui celled la-table table">
                                <thead>
                                <tr>
                                    <th>${message(code: 'sidewide.number')}</th>
                                    <th>${message(code: 'default.sortname.label')}</th>
                                    <th>${message(code: 'subscriptionDetails.members.members')}</th>
                                    <th></th>
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
                                            <td>
                                                ${subscr.sortname}
                                            </td>
                                            <td>
                                                <g:link controller="organisation" action="show"
                                                        id="${subscr.id}">${subscr}</g:link>

                                                <g:if test="${sub.isSlaved}">
                                                    <span data-position="top right"
                                                          class="la-popup-tooltip la-delay"
                                                          data-content="${message(code: 'license.details.isSlaved.tooltip')}">
                                                        <i class="thumbtack blue icon"></i>
                                                    </span>
                                                </g:if>

                                                <g:if test="${subscr.getCustomerType() in ['ORG_INST', 'ORG_INST_COLLECTIVE']}">
                                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                                          data-content="${subscr.getCustomerTypeI10n()}">
                                                        <i class="chess rook grey icon"></i>
                                                    </span>
                                                </g:if>

                                            </td>
                                        </g:each>
                                        <g:if test="${!sub.getAllSubscribers()}">
                                            <td></td>
                                            <td></td>
                                        </g:if>
                                        <td>
                                            <div class="ui card ">
                                                <div class="content">
                                                    <g:render template="/templates/links/orgLinksAsList"
                                                              model="${[roleLinks    : sub.orgRelations?.findAll {
                                                                  !(it.org?.id == contextService.getOrg().id) && !(it.roleType.id in [RDStore.OR_SUBSCRIBER.id, RDStore.OR_SUBSCRIBER_CONS.id, RDStore.OR_SUBSCRIBER_COLLECTIVE.id])
                                                              }.sort { it?.org?.sortname },
                                                                        roleObject   : sub,
                                                                        roleRespValue: 'Specific subscription editor',
                                                                        editmode     : editable,
                                                                        showPersons  : false
                                                              ]}"/>
                                                </div>
                                            </div>
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
                    </div>
                </div>
            </div>
        </div>
    </g:if>

    <g:if test="${params.tab == 'documents'}">
        <div class="ui bottom attached tab segment active">
            <div class="ui segment ">
                <h3 class="ui header"><g:message code="subscription.propertiesMembers.subscription" args="${args.superOrgType}"/></h3>

                <g:render template="/templates/documents/table"
                          model="${[instance: subscription, context: 'documents', redirect: 'subscriptionPropertiesMembers', owntp: 'subscription']}"/>
            </div>

            <div class="ui segment">
                <h3 class="ui header">${message(code: 'subscription.propertiesMembers.subscriber')} <semui:totalNumber
                        total="${filteredSubChilds?.size()}"/></h3>
                <table class="ui celled la-table table">
                    <thead>
                    <tr>
                        <th>${message(code: 'sidewide.number')}</th>
                        <th>${message(code: 'default.sortname.label')}</th>
                        <th>${message(code: 'default.documents.label')}</th>
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
                                <td>
                                    ${subscr.sortname}
                                </td>
                            </g:each>
                            <g:if test="${!sub.getAllSubscribers()}">
                                <td></td>
                                <td></td>
                            </g:if>
                            <td>
                                <g:render template="/templates/documents/table"
                                          model="${[instance: sub, context: 'documents', redirect: 'subscriptionPropertiesMembers', owntp: 'subscription']}"/>

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
        </div>
    </g:if>
    <g:if test="${params.tab == 'notes'}">
        <div class="ui bottom attached tab segment active">
            <div class="ui segment ">
                <h3 class="ui header"><g:message code="subscription.propertiesMembers.subscription" args="${args.superOrgType}"/></h3>

                <g:render template="/templates/notes/table" model="${[instance: subscription, redirect: 'subscriptionPropertiesMembers']}"/>
            </div>

            <div class="ui segment">
                <h3 class="ui header">${message(code: 'subscription.propertiesMembers.subscriber')} <semui:totalNumber
                        total="${filteredSubChilds?.size()}"/></h3>
                <table class="ui celled la-table table">
                    <thead>
                    <tr>
                        <th>${message(code: 'sidewide.number')}</th>
                        <th>${message(code: 'default.sortname.label')}</th>
                        <th>${message(code: 'default.documents.label')}</th>
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
                                <td>
                                    ${subscr.sortname}
                                </td>
                            </g:each>
                            <g:if test="${!sub.getAllSubscribers()}">
                                <td></td>
                                <td></td>
                            </g:if>
                            <td>
                                <g:render template="/templates/notes/table"
                                          model="${[instance: sub, redirect: 'subscriptionPropertiesMembers']}"/>

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
        </div>
    </g:if>
    <g:if test="${params.tab == 'multiYear'}">
        <div class="ui bottom attached tab segment active">
            <div class="ui segment ">
                <h3 class="ui header"><g:message code="subscription.propertiesMembers.subscription" args="${args.superOrgType}"/></h3>

                <table class="ui celled la-table table">
                    <thead>
                    <tr>
                        <th>${message(code: 'subscription')}</th>
                        <th>${message(code: 'default.startDate.label')}</th>
                        <th>${message(code: 'default.endDate.label')}</th>
                        <th>${message(code: 'default.status.label')}</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                    <td>${subscription.name}</td>

                    <td>
                        <g:formatDate formatName="default.date.format.notime" date="${subscription.startDate}"/>
                        <semui:auditButton auditable="[subscription, 'startDate']"/>
                    </td>
                    <td>
                        <g:formatDate formatName="default.date.format.notime" date="${subscription.endDate}"/>
                        <semui:auditButton auditable="[subscription, 'endDate']"/>
                    </td>
                    <td>
                        ${subscription.status.getI10n('value')}
                        <semui:auditButton auditable="[subscription, 'status']"/>
                    </td>
                    <td class="x">
                        <g:link controller="subscription" action="show" id="${subscription.id}"
                                class="ui icon button"><i
                                class="write icon"></i></g:link>
                    </td>
                    </tr>
                    </tbody>
                </table>
            </div>

            <div class="ui segment">
                <h3 class="ui header">${message(code: 'subscription.propertiesMembers.subscriber')} <semui:totalNumber
                        total="${filteredSubChilds?.size()}"/></h3>
                <table class="ui celled la-table table">
                    <thead>
                    <tr>
                        <th>${message(code: 'sidewide.number')}</th>
                        <th>${message(code: 'default.sortname.label')}</th>
                        <th>${message(code: 'subscriptionDetails.members.members')}</th>
                        <th>${message(code: 'default.startDate.label')}</th>
                        <th>${message(code: 'default.endDate.label')}</th>
                        <th>${message(code: 'default.status.label')}</th>
                        <th>${message(code: 'subscription.isMultiYear.label')}</th>
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
                                <td>
                                    ${subscr.sortname}
                                </td>
                                <td>
                                    <g:link controller="organisation" action="show"
                                            id="${subscr.id}">${subscr}</g:link>

                                    <g:if test="${sub.isSlaved}">
                                        <span data-position="top right"
                                              class="la-popup-tooltip la-delay"
                                              data-content="${message(code: 'license.details.isSlaved.tooltip')}">
                                            <i class="thumbtack blue icon"></i>
                                        </span>
                                    </g:if>

                                    <g:if test="${subscr.getCustomerType() in ['ORG_INST', 'ORG_INST_COLLECTIVE']}">
                                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                              data-content="${subscr.getCustomerTypeI10n()}">
                                            <i class="chess rook grey icon"></i>
                                        </span>
                                    </g:if>

                                </td>
                            </g:each>
                            <g:if test="${!sub.getAllSubscribers()}">
                                <td></td>
                                <td></td>
                            </g:if>
                            <td>
                                <semui:xEditable owner="${sub}" field="startDate" type="date" validation="datesCheck"/>
                                <semui:auditButton auditable="[sub, 'startDate']"/>
                            </td>
                            <td><semui:xEditable owner="${sub}" field="endDate" type="date" validation="datesCheck"/>
                            <semui:auditButton auditable="[sub, 'endDate']"/>
                            </td>
                            <td>
                                <semui:xEditableRefData owner="${sub}" field="status"
                                                        config="${RDConstants.SUBSCRIPTION_STATUS}"
                                                        constraint="removeValue_deleted"/>
                                <semui:auditButton auditable="[sub, 'status']"/>
                            </td>
                            <td>
                                <semui:xEditableBoolean owner="${sub}" field="isMultiYear"/>
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
        </div>
    </g:if>
</g:if>
<g:else>

    <br />

    <g:if test="${!filteredSubChilds}">
        <strong><g:message code="subscription.details.nomembers.label" args="${args.memberType}"/></strong>
    </g:if>

</g:else>

<div id="magicArea"></div>

<laser:script file="${this.getGroovyPageFileName()}">

    $('#membersListToggler').click(function () {
        if ($(this).prop('checked')) {
            $("tr[class!=disabled] input[name=selectedMembers]").prop('checked', true)
        } else {
            $("tr[class!=disabled] input[name=selectedMembers]").prop('checked', false)
        }
    });

    $.fn.form.settings.rules.endDateNotBeforeStartDate = function() {
        if($("#valid_from").val() !== '' && $("#valid_to").val() !== '') {
            var startDate = Date.parse(JSPC.helper.formatDate($("#valid_from").val()));
            var endDate = Date.parse(JSPC.helper.formatDate($("#valid_to").val()));
            return (startDate < endDate);
        }
        else return true;
    };

    $('.propertiesSubscription').form({
        on: 'blur',
        inline: true,
        fields: {
            valid_from: {
                identifier: 'valid_from',
                rules: [
                    {
                        type: 'endDateNotBeforeStartDate',
                        prompt: '<g:message code="validation.startDateAfterEndDate"/>'
                    }
                ]
            },
            valid_to: {
                identifier: 'valid_to',
                rules: [
                    {
                        type: 'endDateNotBeforeStartDate',
                        prompt: '<g:message code="validation.endDateBeforeStartDate"/>'
                    }
                ]
            }
        }
    });
</laser:script>

</body>
</html>

