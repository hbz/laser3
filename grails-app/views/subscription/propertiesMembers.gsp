<%@ page import="de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.properties.PropertyDefinition; de.laser.Person; de.laser.helper.RDStore; de.laser.AuditConfig" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'subscription.propertiesMembers.label', args: args.memberTypeGenitive)}</title>
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

<g:render template="navSubscriberManagement" model="${[args: args]}"/>

<semui:messages data="${flash}"/>

<h2 class="ui header">
    <g:message code="subscription"/>:
    <g:link controller="subscription" action="show" id="${subscription.id}">${subscription.name}</g:link><br /><br />
</h2>

<g:if test="${filteredSubChilds}">
    <div class="ui segment">
        <div class="ui two column very relaxed grid">
            <div class="column">
                <semui:filter>
                    <h3 class="ui header">${message(code: 'subscription.propertiesMembers.onlyPropOfParentSubscription', args: [subscription.name])}</h3>
                    <g:form action="propertiesMembers" method="post" class="ui form" id="${params.id}">
                        <g:render template="/templates/properties/genericFilter"
                                  model="[propList: propList, hideFilterProp: true]"/>

                        <div class="field la-field-right-aligned">
                            <a href="${request.forwardURI}"
                               class="ui reset primary button">${message(code: 'default.button.reset.label')}</a>
                            <input type="submit" value="${message(code: 'default.button.filter.label')}"
                                   class="ui secondary button"/>
                        </div>
                    </g:form>
                </semui:filter>
            </div>

            <div class="column">
                <semui:filter>
                    <h3 class="ui header">${message(code: 'subscription.properties')}:</h3>
                    <g:form action="propertiesMembers" method="post" class="ui form" id="${params.id}">
                        <g:render template="/templates/properties/genericFilter"
                                  model="[propList: PropertyDefinition.findAllByTenantIsNullAndDescr(PropertyDefinition.SUB_PROP)+PropertyDefinition.findAllByTenantAndDescr(contextOrg, PropertyDefinition.SUB_PROP), hideFilterProp: true]"/>

                        <div class="field la-field-right-aligned">
                            <a href="${request.forwardURI}"
                               class="ui reset primary button">${message(code: 'default.button.reset.label')}</a>
                            <input type="submit" value="${message(code: 'default.button.filter.label')}"
                                   class="ui secondary button"/>
                        </div>
                    </g:form>
                </semui:filter>
            </div>
        </div>
        <div class="ui vertical divider"><g:message code="default.or"/> </div>
    </div>

    <g:if test="${!memberProperties}">%{-- check for content --}%
    <div class="ui one stackable cards">
        <div class="ui card la-dl-no-table">
            <div class="content">
                <h3 class="ui header">${message(code: 'subscription.properties.consortium')}</h3>

                <div id="member_props_div">
                    <g:render template="/templates/properties/members" model="${[
                            prop_desc       : PropertyDefinition.SUB_PROP,
                            ownobj          : subscription,
                            custom_props_div: "member_props_div"]}"/>
                </div>
            </div>
        </div>
    </div>
    </g:if>
</g:if>



<g:if test="${filteredSubChilds && filterPropDef}">

    <div class="ui icon info message">
        <i class="info icon"></i>

        <div class="content">
            <div class="header">Info</div>

            <div class="ui bulleted list">
                <div class="item">
                    <g:message code="subscription.propertiesMembers.info2" args="${args.memberTypeSingle}"/>
                </div>

                <div class="item">
                    <g:message code="subscription.propertiesMembers.info3"
                               args="${[args.superOrgType[0], args.memberType[0]]}"/>
                </div>

                <div class="item">
                    <g:message code="subscription.propertiesMembers.info4" args="${args.memberTypeSingle}"/>
                </div>
            </div>
        </div>
    </div>

    <g:set var="editableOld" value="${editable}"/>


    <div class="ui segment">
        <h4 class="ui header">${message(code: 'subscription.propertiesMembers.deletePropertyInfo', args: args.memberType)}</h4>

        <g:link class="ui button negative js-open-confirm-modal"
                data-confirm-tokenMsg="${message(code: 'subscription.propertiesMembers.deleteProperty.button.confirm')}"
                data-confirm-term-how="ok" action="processDeletePropertiesMembers" id="${params.id}"
                params="[filterPropDef: filterPropDef]">${message(code: 'subscription.propertiesMembers.deleteProperty.button', args: [filterPropDef.getI10n('name')])}</g:link>

    </div>

    <div class="divider"></div>


    <div class="ui segment">
        <h3 class="ui header"><g:message code="subscription.propertiesMembers.subscription" args="${args.superOrgType}"/></h3>
        <table class="ui celled la-table table">
            <thead>
            <tr>
                <th>${message(code: 'subscription')}</th>
                <th>${message(code: 'default.startDate.label')}</th>
                <th>${message(code: 'default.endDate.label')}</th>
                <th>${message(code: 'default.status.label')}</th>
                <th>${message(code: 'subscription.propertiesMembers.propertySelected')}: ${filterPropDef.getI10n('name')}</th>
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

                <div class="ui middle aligned selection list">

                    <g:if test="${filterPropDef.tenant == null}">
                        <div class="item">

                            <div class="right floated content">
                                <span class="la-popup-tooltip la-delay" data-content="Anzahl der allg. Merkmale in der Lizenz" data-position="top right">
                                <semui:totalNumber
                                        total="${subscription.propertySet.findAll {  (it.tenant?.id == contextOrg.id || it.tenant == null || (it.tenant?.id != contextOrg.id && it.isPublic ))}.size()}"/>
                                </span>
                            </div>

                            <g:set var="customProperties"
                                   value="${subscription.propertySet.findAll {  (it.tenant?.id == contextOrg.id || it.tenant == null || (it.tenant?.id != contextOrg.id && it.isPublic )) && it.type == filterPropDef }}"/>
                            <g:if test="${customProperties}">
                                <g:each in="${customProperties}" var="customProperty">
                                    <div class="header">${message(code: 'subscription.propertiesMembers.CustomProperty')}: ${filterPropDef.getI10n('name')}</div>

                                    <div class="content">

                                        %{-- <g:set var="editable" value="${!(AuditConfig.getConfig(customProperty))}"
                                                scope="request"/>--}%

                                        <g:if test="${customProperty.type.isIntegerType()}">
                                            <semui:xEditable owner="${customProperty}" type="number"
                                                             field="intValue"/>
                                        </g:if>
                                        <g:elseif test="${customProperty.type.isStringType()}">
                                            <semui:xEditable owner="${customProperty}" type="text"
                                                             field="stringValue"/>
                                        </g:elseif>
                                        <g:elseif test="${customProperty.type.isBigDecimalType()}">
                                            <semui:xEditable owner="${customProperty}" type="text"
                                                             field="decValue"/>
                                        </g:elseif>
                                        <g:elseif test="${customProperty.type.isDateType()}">
                                            <semui:xEditable owner="${customProperty}" type="date"
                                                             field="dateValue"/>
                                        </g:elseif>
                                        <g:elseif test="${customProperty.type.isURLType()}">
                                            <semui:xEditable owner="${customProperty}" type="url" field="urlValue"

                                                             class="la-overflow la-ellipsis"/>
                                            <g:if test="${customProperty.value}">
                                                <semui:linkIcon href="${customProperty.value}"/>
                                            </g:if>
                                        </g:elseif>
                                        <g:elseif test="${customProperty.type.isRefdataValueType()}">
                                            <semui:xEditableRefData owner="${customProperty}" type="text"
                                                                    field="refValue"
                                                                    config="${customProperty.type.refdataCategory}"/>
                                        </g:elseif>

                                        <%
                                            if (AuditConfig.getConfig(customProperty)) {
                                                if (subscription.isSlaved) {
                                                    println '&nbsp; <span class="la-popup-tooltip la-delay" data-content="Wert wird automatisch geerbt." data-position="top right"><i class="icon thumbtack blue"></i></span>'
                                                } else {
                                                    println '&nbsp; <span class="la-popup-tooltip la-delay" data-content="Wert wird geerbt." data-position="top right"><i class="icon thumbtack grey"></i></span>'
                                                }
                                            }
                                        %>

                                    </div>
                                </g:each>
                            </g:if><g:else>
                            <div class="content">
                                ${message(code: 'subscription.propertiesMembers.noCustomProperty')}
                                <g:link class="ui button" controller="ajax" action="addCustomPropertyValue" params="[
                                        propIdent:      filterPropDef.id,
                                        ownerId:        subscription.id,
                                        ownerClass:     subscription.class,
                                        withoutRender:  true,
                                        url:            createLink(absolute: true, controller: 'subscription', action: 'propertiesMembers', params: [id: subscription.id, filterPropDef: filterPropDef])
                                ]">
                                    ${message(code:'default.button.add.label')}
                                </g:link>
                            </div>
                        </g:else>

                        </div>
                    </g:if>

                    <g:if test="${filterPropDef?.tenant != null}">

                        <div class="item">

                            <div class="right floated content">
                                <span class="la-popup-tooltip la-delay" data-content="Anzahl der priv. Merkmale in der Lizenz" data-position="top right">
                                <semui:totalNumber
                                        total="${subscription.propertySet.findAll { it.type.tenant?.id == contextOrg.id }.size()}"/>
                                </span>
                            </div>

                            <g:set var="privateProperties"
                                   value="${subscription.propertySet.findAll { it.type.tenant?.id == contextOrg.id && it.type == filterPropDef  }}"/>
                            <g:if test="${privateProperties}">
                                <g:each in="${privateProperties}" var="privateProperty">
                                    <div class="header">${message(code: 'subscription.propertiesMembers.PrivateProperty')} ${contextService.getOrg()}: ${filterPropDef?.getI10n('name')}</div>

                                    <div class="content">

                                        <g:set var="editable" value="${!(AuditConfig.getConfig(privateProperty))}"
                                               scope="request"/>

                                        <g:if test="${privateProperty.type.isIntegerType()}">
                                            <semui:xEditable owner="${privateProperty}" type="number"
                                                             field="intValue"/>
                                        </g:if>
                                        <g:elseif test="${privateProperty.type.isStringType()}">
                                            <semui:xEditable owner="${privateProperty}" type="text"
                                                             field="stringValue"/>
                                        </g:elseif>
                                        <g:elseif test="${privateProperty.type.isBigDecimalType()}">
                                            <semui:xEditable owner="${privateProperty}" type="text"
                                                             field="decValue"/>
                                        </g:elseif>
                                        <g:elseif test="${privateProperty.type.isDateType()}">
                                            <semui:xEditable owner="${privateProperty}" type="date"
                                                             field="dateValue"/>
                                        </g:elseif>
                                        <g:elseif test="${privateProperty.type.isURLType()}">
                                            <semui:xEditable owner="${privateProperty}" type="url" field="urlValue"

                                                             class="la-overflow la-ellipsis"/>
                                            <g:if test="${privateProperty.value}">
                                                <semui:linkIcon href="${privateProperty.value}"/>
                                            </g:if>
                                        </g:elseif>
                                        <g:elseif test="${privateProperty.type.isRefdataValueType()}">
                                            <semui:xEditableRefData owner="${privateProperty}" type="text"
                                                                    field="refValue"
                                                                    config="${privateProperty.type.refdataCategory}"/>
                                        </g:elseif>

                                        <%
                                            if (AuditConfig.getConfig(privateProperty)) {
                                                if (subscription.isSlaved) {
                                                    println '&nbsp; <span class="la-popup-tooltip la-delay" data-content="Wert wird automatisch geerbt." data-position="top right"><i class="icon thumbtack blue"></i></span>'
                                                } else {
                                                    println '&nbsp; <span class="la-popup-tooltip la-delay" data-content="Wert wird geerbt." data-position="top right"><i class="icon thumbtack grey"></i></span>'
                                                }
                                            }
                                        %>

                                    </div>
                                </g:each>
                            </g:if><g:else>
                            <div class="content">
                                ${message(code: 'subscription.propertiesMembers.noPrivateProperty')}
                                <g:link class="ui button" controller="ajax" action="addPrivatePropertyValue" params="[
                                        propIdent:      filterPropDef.id,
                                        ownerId:        subscription.id,
                                        ownerClass:     subscription.class,
                                        withoutRender:  true,
                                        url:            createLink(absolute: true, controller: 'subscription', action: 'propertiesMembers', params: [id: subscription.id, filterPropDef: filterPropDef])
                                ]">
                                    ${message(code:'default.button.add.label')}
                                </g:link>
                            </div>
                        </g:else>

                        </div>
                    </g:if>
                </div>

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
        <g:form action="processPropertiesMembers" method="post" class="ui form">
            <g:hiddenField id="ppm_id_${params.id}" name="id" value="${params.id}"/>

            <div class="field required">
                <h4 class="ui header">${message(code: 'subscription.propertiesMembers.info', args: args.memberType)}</h4>

                <div class="inline field">
                    <label>${message(code: 'subscription.propertiesMembers.propertySelected')}:</label>

                    <strong>${filterPropDef?.getI10n('name')}
                        <g:if test="${filterPropDef?.tenant != null}">
                            <i class="shield alternate icon"></i>
                        </g:if>
                    </strong>

                </div>
                <g:hiddenField name="filterPropDef" value="${filterPropDef}"/>


                ${message(code: 'default.type.label')}: ${PropertyDefinition.getLocalizedValue(filterPropDef?.type)}
                <g:if test="${filterPropDef?.isRefdataValueType()}">
                    <g:set var="refdataValues" value="${[]}"/>
                    <g:each in="${RefdataCategory.getAllRefdataValues(filterPropDef.refdataCategory)}"
                            var="refdataValue">
                        <g:if test="${refdataValue.getI10n('value')}">
                            <g:set var="refdataValues" value="${refdataValues + refdataValue.getI10n('value')}"/>
                        </g:if>
                    </g:each>

                    (${refdataValues.join('/')})
                </g:if>

            </div>

            <div class="field required">
                <label for="filterPropValue">${message(code: 'subscription.property.value')}</label>
                <g:if test="${filterPropDef?.isRefdataValueType()}">
                    <g:select class="ui search dropdown"
                              optionKey="id" optionValue="${{ it.getI10n('value') }}"
                              from="${RefdataCategory.getAllRefdataValues(filterPropDef.refdataCategory)}"
                              name="filterPropValue" value=""
                              required=""
                              noSelection='["": "${message(code: 'default.select.choose.label')}"]'/>
                </g:if>
                <g:else>
                    <input id="filterPropValue" type="text" name="filterPropValue"
                           placeholder="${message(code: 'license.search.property.ph')}"/>
                </g:else>
            </div>

            <button class="ui button" type="submit">${message(code: 'default.button.save_changes')}</button>


            <h3 class="ui header">${message(code: 'subscription.propertiesMembers.subscriber')} <semui:totalNumber
                    total="${filteredSubChilds?.size()}"/></h3>
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
                    <th class="la-no-uppercase">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                              data-content="${message(code: 'subscription.isMultiYear.consortial.label')}">
                            <i class="map orange icon"></i>
                        </span>
                    </th>
                    <th>${message(code: 'subscription.propertiesMembers.propertySelected')}: ${filterPropDef?.getI10n('name')}</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${filteredSubChilds}" status="i" var="sub">
                    <g:set var="subscr" value="${sub.getSubscriber()}"/>
                    <tr>
                        <td>
                            <g:checkBox id="selectedMembers_${sub.id}" name="selectedMembers" value="${sub.id}" checked="false"/>
                        </td>
                        <td>${i + 1}</td>
                        <td>
                            ${subscr.sortname ?: subscr.name}
                        </td>
                        <td>
                            <g:link controller="organisation" action="show" id="${subscr.id}">${subscr}</g:link>

                            <g:if test="${sub.isSlaved}">
                                <span data-position="top right"
                                      class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'license.details.isSlaved.tooltip')}">
                                    <i class="thumbtack blue icon"></i>
                                </span>
                            </g:if>

                            <g:if test="${subscr.getCustomerType() == 'ORG_INST'}">
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                      data-content="${subscr.getCustomerTypeI10n()}">
                                    <i class="chess rook grey icon"></i>
                                </span>
                            </g:if>
                        </td>

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
                            ${sub.status.getI10n('value')}
                            <semui:auditButton auditable="[sub, 'status']"/>
                        </td>
                        <td>
                            <g:if test="${sub.isMultiYear}">
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                      data-content="${message(code: 'subscription.isMultiYear.consortial.label')}">
                                    <i class="map orange icon"></i>
                                </span>
                            </g:if>
                        </td>
                        <td>

                            <div class="ui middle aligned selection list">

                                <g:if test="${filterPropDef?.tenant == null}">
                                    <div class="item">

                                        <div class="right floated content">
                                            <span class="la-popup-tooltip la-delay" data-content="Anzahl der allg. Merkmale in der Lizenz" data-position="top right">
                                            <semui:totalNumber
                                                    total="${sub.propertySet.findAll { (it.tenant?.id == contextOrg.id || it.tenant == null || (it.tenant?.id != contextOrg.id && it.isPublic )) }.size()}"/>
                                            </span>
                                        </div>

                                        <g:set var="customProperties"
                                               value="${sub.propertySet.findAll {  (it.tenant?.id == contextOrg.id || it.tenant == null || (it.tenant?.id != contextOrg.id && it.isPublic )) && it.type == filterPropDef }}"/>
                                        <g:if test="${customProperties}">
                                            <g:each in="${customProperties}" var="customProperty">
                                                <div class="header">${message(code: 'subscription.propertiesMembers.CustomProperty')}: ${filterPropDef?.getI10n('name')}</div>

                                                <div class="content">
                                                    <g:if test="${customProperty.type.isIntegerType()}">
                                                        <semui:xEditable owner="${customProperty}" type="number"
                                                                         field="intValue"/>
                                                    </g:if>
                                                    <g:elseif test="${customProperty.type.isStringType()}">
                                                        <semui:xEditable owner="${customProperty}" type="text"
                                                                         field="stringValue"/>
                                                    </g:elseif>
                                                    <g:elseif
                                                            test="${customProperty.type.isBigDecimalType()}">
                                                        <semui:xEditable owner="${customProperty}" type="text"
                                                                         field="decValue"/>
                                                    </g:elseif>
                                                    <g:elseif test="${customProperty.type.isDateType()}">
                                                        <semui:xEditable owner="${customProperty}" type="date"
                                                                         field="dateValue"/>
                                                    </g:elseif>
                                                    <g:elseif test="${customProperty.type.isURLType()}">
                                                        <semui:xEditable owner="${customProperty}" type="url"
                                                                         field="urlValue"

                                                                         class="la-overflow la-ellipsis"/>
                                                        <g:if test="${customProperty.value}">
                                                            <semui:linkIcon href="${customProperty.value}"/>
                                                        </g:if>
                                                    </g:elseif>
                                                    <g:elseif
                                                            test="${customProperty.type.isRefdataValueType()}">
                                                        <semui:xEditableRefData owner="${customProperty}" type="text"
                                                                                field="refValue"
                                                                                config="${customProperty.type.refdataCategory}"/>
                                                    </g:elseif>

                                                    <%
                                                        if (customProperty.hasProperty('instanceOf') && customProperty.instanceOf && AuditConfig.getConfig(customProperty.instanceOf)) {
                                                            if (sub.isSlaved) {
                                                                println '&nbsp; <span class="la-popup-tooltip la-delay" data-content="Wert wird automatisch geerbt." data-position="top right"><i class="icon thumbtack blue"></i></span>'
                                                            } else {
                                                                println '&nbsp; <span class="la-popup-tooltip la-delay" data-content="Wert wird geerbt." data-position="top right"><i class="icon thumbtack grey"></i></span>'
                                                            }
                                                        }
                                                    %>

                                                </div>
                                            </g:each>
                                        </g:if><g:else>
                                        <div class="content">
                                            ${message(code: 'subscription.propertiesMembers.noCustomProperty')}
                                            <g:link class="ui button" controller="ajax" action="addCustomPropertyValue" params="[
                                                    propIdent:      filterPropDef.id,
                                                    ownerId:        sub.id,
                                                    ownerClass:     sub.class,
                                                    withoutRender:  true,
                                                    url:            createLink(absolute: true, controller: 'subscription', action: 'propertiesMembers', params: [id: subscription.id, filterPropDef: filterPropDef])
                                            ]">
                                                ${message(code:'default.button.add.label')}
                                            </g:link>
                                        </div>
                                    </g:else>
                                    </div>
                                </g:if>
                                <g:if test="${filterPropDef?.tenant != null}">

                                    <div class="item">

                                        <div class="right floated content">
                                            <span class="la-popup-tooltip la-delay" data-content="Anzahl der priv. Merkmale in der Lizenz" data-position="top right">
                                            <semui:totalNumber
                                                    total="${sub.propertySet.findAll {  it.type.tenant?.id == contextOrg.id }.size()}"/>
                                            </span>
                                        </div>

                                        <g:set var="privateProperties"
                                               value="${sub.propertySet.findAll { it.type.tenant?.id == contextOrg.id && it.type == filterPropDef }}"/>

                                        <g:if test="${privateProperties}">
                                            <g:each in="${privateProperties}" var="privateProperty">
                                                <div class="header">${message(code: 'subscription.propertiesMembers.PrivateProperty')} ${contextService.getOrg()}: ${filterPropDef.getI10n('name')}</div>

                                                <div class="content">
                                                    <g:if test="${privateProperty.type.isIntegerType()}">
                                                        <semui:xEditable owner="${privateProperty}" type="number"
                                                                         field="intValue"/>
                                                    </g:if>
                                                    <g:elseif test="${privateProperty.type.isStringType()}">
                                                        <semui:xEditable owner="${privateProperty}" type="text"
                                                                         field="stringValue"/>
                                                    </g:elseif>
                                                    <g:elseif
                                                            test="${privateProperty.type.isBigDecimalType()}">
                                                        <semui:xEditable owner="${privateProperty}" type="text"
                                                                         field="decValue"/>
                                                    </g:elseif>
                                                    <g:elseif test="${privateProperty.type.isDateType()}">
                                                        <semui:xEditable owner="${privateProperty}" type="date"
                                                                         field="dateValue"/>
                                                    </g:elseif>
                                                    <g:elseif test="${privateProperty.type.isURLType()}">
                                                        <semui:xEditable owner="${privateProperty}" type="url"
                                                                         field="urlValue"
                                                                         class="la-overflow la-ellipsis"/>
                                                        <g:if test="${privateProperty.value}">
                                                            <semui:linkIcon href="${privateProperty.value}"/>
                                                        </g:if>
                                                    </g:elseif>
                                                    <g:elseif
                                                            test="${privateProperty.type.isRefdataValueType()}">
                                                        <semui:xEditableRefData owner="${privateProperty}" type="text"
                                                                                field="refValue"
                                                                                config="${privateProperty.type.refdataCategory}"/>
                                                    </g:elseif>

                                                    <%
                                                        if (privateProperty.hasProperty('instanceOf') && privateProperty.instanceOf && AuditConfig.getConfig(privateProperty.instanceOf)) {
                                                            if (sub.isSlaved) {
                                                                println '&nbsp; <span class="la-popup-tooltip la-delay" data-content="Wert wird automatisch geerbt." data-position="top right"><i class="icon thumbtack blue"></i></span>'
                                                            } else {
                                                                println '&nbsp; <span class="la-popup-tooltip la-delay" data-content="Wert wird geerbt." data-position="top right"><i class="icon thumbtack grey"></i></span>'
                                                            }
                                                        }
                                                    %>

                                                </div>
                                            </g:each>
                                        </g:if><g:else>
                                            <div class="content">
                                                ${message(code: 'subscription.propertiesMembers.noPrivateProperty')}
                                                <g:link class="ui button" controller="ajax" action="addPrivatePropertyValue" params="[
                                                        propIdent:      filterPropDef.id,
                                                        ownerId:        sub.id,
                                                        ownerClass:     sub.class,
                                                        withoutRender:  true,
                                                        url:            createLink(absolute: true, controller: 'subscription', action: 'propertiesMembers', params: [id: subscription.id, filterPropDef: filterPropDef])
                                                ]">
                                                    ${message(code:'default.button.add.label')}
                                                </g:link>
                                            </div>
                                    </g:else>

                                    </div>
                                </g:if>
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
        </g:form>

    </div>
</g:if>
<g:else>

    <br />

    <g:if test="${!filteredSubChilds}">
        <strong><g:message code="subscription.details.nomembers.label" args="${args.memberType}"/></strong>
    </g:if>
    <g:elseif test="${!filterPropDef}">
        <strong><g:message code="subscription.propertiesMembers.noPropertySeleced"
                           args="${args.memberTypeGenitive}"/></strong>
    </g:elseif>
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
</laser:script>

</body>
</html>

