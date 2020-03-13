<%@ page import="com.k_int.kbplus.Person; de.laser.helper.RDStore; de.laser.helper.RDConstants; com.k_int.properties.PropertyDefinition; com.k_int.kbplus.RefdataValue; de.laser.AuditConfig; com.k_int.kbplus.RefdataCategory" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser')} : ${message(code: 'subscription.subscriptionPropertiesMembers.header', args: args.memberTypeGenitive)}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="currentSubscriptions"
                 text="${message(code: 'myinst.currentSubscriptions.label')}"/>
    <semui:crumb controller="subscription" action="show" id="${subscriptionInstance.id}"
                 text="${subscriptionInstance.name}"/>

    <semui:crumb class="active"
                 text="${message(code: 'subscription.details.subscriberManagement.label', args: args.memberType)}"/>

</semui:breadcrumbs>
<br>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>${subscriptionInstance.name}</h1>

<semui:anualRings object="${subscriptionInstance}" controller="subscription" action="${actionName}"
                  navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

<g:render template="navSubscriberManagement"/>

<semui:messages data="${flash}"/>

<h4>
    <g:message code="subscription"/>: <g:link controller="subscription" action="show"
                                              id="${parentSub.id}">${parentSub.name}</g:link>
    <br><br>

</h4>


<g:if test="${filteredSubChilds}">

    <div class="ui top attached tabular menu">

        <g:link class="item ${params.tab == 'generalProperties' ? 'active' : ''}"
                controller="subscription" action="subscriptionPropertiesMembers"
                id="${parentSub?.id}"
                params="[tab: 'generalProperties']">
            <g:message code="subscription.subscriptionPropertiesMembers.generalProperties"/>

        </g:link>

        <g:link class="item ${params.tab == 'providerAgency' ? 'active' : ''}"
                controller="subscription" action="subscriptionPropertiesMembers"
                id="${parentSub?.id}"
                params="[tab: 'providerAgency']">
            <g:message code="subscription.subscriptionPropertiesMembers.providerAgency"/>

        </g:link>

        <g:link class="item ${params.tab == 'documents' ? 'active' : ''}"
                controller="subscription" action="subscriptionPropertiesMembers"
                id="${parentSub?.id}"
                params="[tab: 'documents']">
            <g:message code="subscription.subscriptionPropertiesMembers.documents"/>

        </g:link>

        <g:link class="item ${params.tab == 'notes' ? 'active' : ''}"
                controller="subscription" action="subscriptionPropertiesMembers"
                id="${parentSub?.id}"
                params="[tab: 'notes']">
            <g:message code="subscription.subscriptionPropertiesMembers.notes"/>

        </g:link>

        <g:link class="item ${params.tab == 'multiYear' ? 'active' : ''}"
                controller="subscription" action="subscriptionPropertiesMembers"
                id="${parentSub?.id}"
                params="[tab: 'multiYear']">
            <g:message code="subscription.isMultiYear.label"/>

        </g:link>

    </div>


    <g:if test="${params.tab == 'generalProperties'}">
        <div class="ui bottom attached tab segment active">

            <g:set var="editableOld" value="${editable}"/>

            <div class="ui segment">
                <h3><g:message code="subscription.propertiesMembers.subscription" args="${args.superOrgType}"/></h3>
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
                        ${parentSub.kind?.getI10n('value')}
                        <semui:auditButton auditable="[parentSub, 'kind']"/>
                    </td>
                    <td>
                        ${parentSub.form?.getI10n('value')}
                        <semui:auditButton auditable="[parentSub, 'form']"/>
                    </td>
                    <td>
                        ${parentSub.resource?.getI10n('value')}
                        <semui:auditButton auditable="[parentSub, 'resource']"/>
                    </td>
                    <td>
                        ${parentSub.isPublicForApi ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}
                        <semui:auditButton auditable="[parentSub, 'isPublicForApi']"/>
                    </td>
                    <td>
                        ${parentSub.hasPerpetualAccess ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}
                        <semui:auditButton auditable="[parentSub, 'hasPerpetualAccess']"/>
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

            <div class="divider"></div>

            <div class="ui segment">
                <g:form action="processSubscriptionPropertiesMembers" method="post" class="ui form">
                    <g:hiddenField name="id" value="${params.id}"/>

                    <h4>${message(code: 'subscription.subscriptionPropertiesMembers.info', args: args.memberType)}</h4>

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
                                fakeList.remove(com.k_int.kbplus.RefdataValue.getByValueAndCategory('Deleted', RDConstants.SUBSCRIPTION_STATUS))
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


                    <h3>
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
                                    <g:checkBox name="selectedMembers" value="${sub.id}" checked="false"/>
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
                <h3><g:message code="subscription.propertiesMembers.subscription" args="${args.superOrgType}"/></h3>

                <div class="twelve wide column">

                    <div class="la-inline-lists">
                        <div class="ui card sixteen wide">
                            <div class="content">
                                <g:render template="/templates/links/orgLinksAsList"
                                          model="${[roleLinks    : visibleOrgRelations,
                                                    roleObject   : parentSub,
                                                    roleRespValue: 'Specific subscription editor',
                                                    editmode     : editable,
                                                    showPersons  : false
                                          ]}"/>
                            </div>
                        </div>

                        <div class="ui segment">
                            <h3>${message(code: 'subscription.propertiesMembers.subscriber')} <semui:totalNumber
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
                                                                  !(it.org?.id == contextService.getOrg()?.id) && !(it.roleType.id in [RDStore.OR_SUBSCRIBER.id, RDStore.OR_SUBSCRIBER_CONS.id, RDStore.OR_SUBSCRIBER_COLLECTIVE.id])
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
                <h3><g:message code="subscription.propertiesMembers.subscription" args="${args.superOrgType}"/></h3>

                <g:render template="/templates/documents/table"
                          model="${[instance: parentSub, context: 'documents', redirect: 'documents', owntp: 'subscription']}"/>
            </div>

            <div class="ui segment">
                <h3>${message(code: 'subscription.propertiesMembers.subscriber')} <semui:totalNumber
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
                                          model="${[instance: sub, context: 'documents', redirect: 'documents', owntp: 'subscription']}"/>

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
                <h3><g:message code="subscription.propertiesMembers.subscription" args="${args.superOrgType}"/></h3>

                <g:render template="/templates/notes/table" model="${[instance: parentSub, redirect: 'notes']}"/>
            </div>

            <div class="ui segment">
                <h3>${message(code: 'subscription.propertiesMembers.subscriber')} <semui:totalNumber
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
                                          model="${[instance: sub, redirect: 'notes']}"/>

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
                <h3><g:message code="subscription.propertiesMembers.subscription" args="${args.superOrgType}"/></h3>

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
                <h3>${message(code: 'subscription.propertiesMembers.subscriber')} <semui:totalNumber
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

                                </td>
                            </g:each>
                            <g:if test="${!sub.getAllSubscribers()}">
                                <td></td>
                                <td></td>
                            </g:if>
                            <td>
                                <semui:xEditable owner="${sub}" field="startDate" type="date"/>
                                <semui:auditButton auditable="[sub, 'startDate']"/>
                            </td>
                            <td><semui:xEditable owner="${sub}" field="endDate" type="date"/>
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

    <br>

    <g:if test="${!filteredSubChilds}">
        <strong><g:message code="subscription.details.nomembers.label" args="${args.memberType}"/></strong>
    </g:if>

</g:else>

<div id="magicArea"></div>

<r:script>
    $(document).ready(function () {
        $('.tabular.menu .item').tab()
    });

    $('#membersListToggler').click(function () {
        if ($(this).prop('checked')) {
            $("tr[class!=disabled] input[name=selectedMembers]").prop('checked', true)
        } else {
            $("tr[class!=disabled] input[name=selectedMembers]").prop('checked', false)
        }
    });
</r:script>

</body>
</html>

