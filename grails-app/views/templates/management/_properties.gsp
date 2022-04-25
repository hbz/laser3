<%@ page import="de.laser.properties.SubscriptionProperty; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.properties.PropertyDefinition; de.laser.Person; de.laser.storage.RDStore; de.laser.AuditConfig" %>
<laser:serviceInjection/>

    <g:set var="allProperties"
           value="${PropertyDefinition.findAllByTenantIsNullAndDescr(PropertyDefinition.SUB_PROP) + PropertyDefinition.findAllByTenantAndDescr(contextOrg, PropertyDefinition.SUB_PROP)}"/>

    <g:if test="${controllerName == "subscription"}">
        <div class="ui segment" >
            <div class="ui two column very relaxed grid">
                <div class="column">
                        <g:form action="${actionName}" method="post" class="ui form" id="${params.id}"
                                params="[tab: params.tab]">
                            <div class="fields" style="margin-bottom: 0">


                                <laser:render template="/templates/properties/genericFilter"
                                          model="[propList: propList, hideFilterProp: true, newfilterPropDefName: 'propertiesFilterPropDef', label:message(code: 'subscriptionsManagement.onlyPropOfParentSubscription', args: [subscription.name])]"/>

                                <div class="field la-field-noLabel" >
                                    <input type="submit" value="${message(code: 'template.orgLinksModal.select')}"
                                           class="ui secondary button"/>
                                </div>
                            </div>
                        </g:form>
                </div>

                <div class="column">
                        <g:form action="${actionName}" method="post" class="ui form" id="${params.id}"
                                params="[tab: params.tab]">
                            <div class="fields" style="margin-bottom: 0">
                                <laser:render template="/templates/properties/genericFilter"
                                          model="[propList: allProperties, hideFilterProp: true, newfilterPropDefName: 'propertiesFilterPropDef',label:message(code: 'subscriptionsManagement.allProperties')]"/>

                                <div class="field la-field-noLabel">
                                    <input type="submit" value="${message(code: 'template.orgLinksModal.select')}"
                                           class="ui secondary button"/>
                                </div>
                            </div>
                        </g:form>
                </div>
            </div>

            <div class="ui vertical divider"><g:message code="default.or"/></div>
        </div>
     </g:if>

    <g:if test="${controllerName == "myInstitution"}">
        <div class="ui segment" >
            <g:form action="${actionName}" method="post" class="ui form" id="${params.id}" style="margin-bottom: 0"
                    params="[tab: params.tab]">
                <div class="two fields" style="margin-bottom: 0">
                    <laser:render template="/templates/properties/genericFilter"
                              model="[propList: allProperties, hideFilterProp: true, newfilterPropDefName: 'propertiesFilterPropDef',label:message(code: 'subscriptionsManagement.allProperties')]"/>

                    <div class="field la-field-noLabel">
                        <input type="submit" value="${message(code: 'template.orgLinksModal.select')}"
                               class="ui secondary button"/>
                    </div>
                </div>
            </g:form>
        </div>
    </g:if>


    <g:if test="${memberProperties}">%{-- check for content --}%
        <div class="ui one stackable cards">
            <div class="ui card la-dl-no-table">
                <div class="content">
                    <h3 class="ui header">${message(code: 'subscription.properties.consortium')}</h3>

                    <div id="member_props_div">
                        <laser:render template="/templates/properties/members" model="${[
                                prop_desc       : PropertyDefinition.SUB_PROP,
                                ownobj          : subscription,
                                custom_props_div: "member_props_div"]}"/>
                    </div>
                </div>
            </div>
        </div>
    </g:if>




<g:if test="${filteredSubscriptions && propertiesFilterPropDef}">

    <g:if test="${controllerName == "subscription"}">
        <div class="ui icon info message">
            <i class="info icon"></i>

            <div class="content">
                <div class="header">Info</div>

                <div class="ui bulleted list">
                    <div class="item">
                        <g:message code="subscriptionsManagement.info2" args="${args.memberTypeSingle}"/>
                    </div>

                    <div class="item">
                        <g:message code="subscriptionsManagement.info3"
                                   args="${[args.superOrgType[0], args.memberType[0]]}"/>
                    </div>

                    <div class="item">
                        <g:message code="subscriptionsManagement.info4" args="${args.memberTypeSingle}"/>
                    </div>
                </div>
            </div>
        </div>


        <div class="ui segment">
            <h4 class="ui header">${message(code: 'subscriptionsManagement.deletePropertyInfo')}</h4>

            <g:link class="ui button negative js-open-confirm-modal"
                    data-confirm-tokenMsg="${message(code: 'subscriptionsManagement.deleteProperty.button.confirm')}"
                    data-confirm-term-how="ok" action="${actionName}" id="${params.id}"
                    params="[processOption: 'deleteAllProperties', propertiesFilterPropDef: propertiesFilterPropDef, tab: params.tab]">${message(code: 'subscriptionsManagement.deleteProperty.button', args: [propertiesFilterPropDef.getI10n('name')])}</g:link>

        </div>

        <div class="divider"></div>

        <div class="ui segment">
            <h3 class="ui header"><g:message code="subscriptionsManagement.subscription"
                                             args="${args.superOrgType}"/></h3>
            <table class="ui celled la-js-responsive-table la-table table">
                <thead>
                <tr>
                    <th>${message(code: 'subscription')}</th>
                    <th>${message(code: 'default.startDate.label')}</th>
                    <th>${message(code: 'default.endDate.label')}</th>
                    <th>${message(code: 'default.status.label')}</th>
                    <th>${message(code: 'subscriptionsManagement.propertySelected')}: ${propertiesFilterPropDef.getI10n('name')}</th>
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

                            <g:if test="${propertiesFilterPropDef.tenant == null}">
                                <div class="item">

                                    <div class="right floated content">
                                        <span class="la-popup-tooltip la-delay"
                                              data-content="Anzahl der allg. Merkmale in der Lizenz"
                                              data-position="top right">
                                            <semui:totalNumber
                                                    total="${SubscriptionProperty.executeQuery('select count(id) from SubscriptionProperty where owner = :sub AND ((tenant = :contextOrg OR tenant is null) OR (tenant != :contextOrg AND isPublic = true)) AND type.tenant is null', [contextOrg: contextOrg, sub: subscription])[0] }"/>
                                        </span>
                                    </div>

                                    <g:set var="customProperties"
                                           value="${SubscriptionProperty.executeQuery('from SubscriptionProperty where owner = :sub AND ((tenant = :contextOrg OR tenant is null) OR (tenant != :contextOrg AND isPublic = true)) AND type = :propertiesFilterPropDef AND type.tenant is null', [contextOrg: contextOrg, sub: subscription, propertiesFilterPropDef: propertiesFilterPropDef])}"/>
                                    <g:if test="${customProperties}">
                                        <g:each in="${customProperties}" var="customProperty">
                                            <div class="header">${message(code: 'subscriptionsManagement.CustomProperty')}: ${propertiesFilterPropDef.getI10n('name')}</div>

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
                                                    <semui:xEditable owner="${customProperty}" type="url"
                                                                     field="urlValue"

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
                                                            println '&nbsp; <span class="la-popup-tooltip la-delay" data-content="Wert wird automatisch geerbt." data-position="top right"><i class="icon grey la-thumbtack-regular"></i></span>'
                                                        } else {
                                                            println '&nbsp; <span class="la-popup-tooltip la-delay" data-content="Wert wird geerbt." data-position="top right"><i class="icon thumbtack grey"></i></span>'
                                                        }
                                                    }
                                                %>

                                            </div>
                                        </g:each>
                                    </g:if><g:else>
                                    <div class="content">
                                        ${message(code: 'subscriptionsManagement.noCustomProperty')}
                                        <g:link class="ui button" controller="ajax" action="addCustomPropertyValue"
                                                params="[
                                                        propIdent    : propertiesFilterPropDef.id,
                                                        ownerId      : subscription.id,
                                                        ownerClass   : subscription.class,
                                                        withoutRender: true,
                                                        url          : createLink(absolute: true, controller: controllerName, action: actionName, params: [id: subscription.id, tab: params.tab, propertiesFilterPropDef: propertiesFilterPropDef])
                                                ]">
                                            ${message(code: 'default.button.add.label')}
                                        </g:link>
                                    </div>
                                </g:else>

                                </div>
                            </g:if>

                            <g:if test="${propertiesFilterPropDef.tenant != null}">

                                <div class="item">

                                    <div class="right floated content">
                                        <span class="la-popup-tooltip la-delay"
                                              data-content="Anzahl der priv. Merkmale in der Lizenz"
                                              data-position="top right">
                                            <semui:totalNumber
                                                    total="${SubscriptionProperty.executeQuery('select count(id) from SubscriptionProperty where owner = :sub AND (type.tenant = :contextOrg AND tenant = :contextOrg)', [contextOrg: contextOrg, sub: subscription])[0] }"/>
                                        </span>
                                    </div>

                                    <g:set var="privateProperties"
                                           value="${SubscriptionProperty.executeQuery('from SubscriptionProperty where owner = :sub AND (type.tenant = :contextOrg AND tenant = :contextOrg) AND type = :propertiesFilterPropDef', [contextOrg: contextOrg, sub: subscription, propertiesFilterPropDef: propertiesFilterPropDef])}"/>
                                    <g:if test="${privateProperties}">
                                        <g:each in="${privateProperties}" var="privateProperty">
                                            <div class="header">${message(code: 'subscriptionsManagement.PrivateProperty')} ${contextService.getOrg()}: ${propertiesFilterPropDef.getI10n('name')}</div>

                                            <div class="content">

                                                <g:set var="editable"
                                                       value="${!(AuditConfig.getConfig(privateProperty))}"
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
                                                    <semui:xEditable owner="${privateProperty}" type="url"
                                                                     field="urlValue"

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
                                                            println '&nbsp; <span class="la-popup-tooltip la-delay" data-content="Wert wird automatisch geerbt." data-position="top right"><i class="icon grey la-thumbtack-regular"></i></span>'
                                                        } else {
                                                            println '&nbsp; <span class="la-popup-tooltip la-delay" data-content="Wert wird geerbt." data-position="top right"><i class="icon thumbtack grey"></i></span>'
                                                        }
                                                    }
                                                %>

                                            </div>
                                        </g:each>
                                    </g:if><g:else>
                                    <div class="content">
                                        ${message(code: 'subscriptionsManagement.noPrivateProperty')}
                                        <g:link class="ui button" controller="ajax" action="addPrivatePropertyValue"
                                                params="[
                                                        propIdent    : propertiesFilterPropDef.id,
                                                        ownerId      : subscription.id,
                                                        ownerClass   : subscription.class,
                                                        withoutRender: true,
                                                        url          : createLink(absolute: true, controller: controllerName, action: actionName, params: [id: subscription.id, tab: params.tab, propertiesFilterPropDef: propertiesFilterPropDef])
                                                ]">
                                            ${message(code: 'default.button.add.label')}
                                        </g:link>
                                    </div>
                                </g:else>

                                </div>
                            </g:if>
                        </div>

                    </td>

                    <td class="x">
                        <g:link controller="subscription" action="show" id="${subscription.id}"
                                class="ui icon button blue la-modern-button"
                                role="button"
                                aria-label="${message(code: 'ariaLabel.edit.universal')}">
                            <i aria-hidden="true" class="write icon"></i>
                        </g:link>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </g:if>

    <g:set var="editableOld" value="${false}"/>

    <g:if test="${controllerName == "myInstitution"}">
        <laser:render template="/templates/subscription/subscriptionFilter"/>
    </g:if>


    <div class="ui segment">
        <g:form action="${actionName}" method="post" class="ui form propertiesForm"
                params="[tab: params.tab, processOption: 'changeCreateProperty']">
            <g:hiddenField id="ppm_id_${params.id}" name="id" value="${params.id}"/>

            <div class="field required">
                <h4 class="ui header">${message(code: 'subscriptionsManagement.info.property')}</h4>

                <div class="inline field">
                    <label>${message(code: 'subscriptionsManagement.propertySelected')}:</label>

                    <strong>${propertiesFilterPropDef.getI10n('name')}
                        <g:if test="${propertiesFilterPropDef.tenant != null}">
                            <i class="shield alternate icon"></i>
                        </g:if>
                    </strong>

                </div>
                <g:hiddenField name="propertiesFilterPropDef" value="${propertiesFilterPropDef}"/>


                ${message(code: 'default.type.label')}: ${PropertyDefinition.getLocalizedValue(propertiesFilterPropDef.type)}
                <g:if test="${propertiesFilterPropDef.isRefdataValueType()}">
                    <g:set var="refdataValues" value="${[]}"/>
                    <g:each in="${RefdataCategory.getAllRefdataValues(propertiesFilterPropDef.refdataCategory)}"
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
                <g:if test="${propertiesFilterPropDef.isRefdataValueType()}">
                    <g:select class="ui search dropdown"
                              optionKey="id" optionValue="${{ it.getI10n('value') }}"
                              from="${RefdataCategory.getAllRefdataValues(propertiesFilterPropDef.refdataCategory)}"
                              name="filterPropValue" value="${params.filterPropValue}"
                              required=""
                              noSelection='["": "${message(code: 'default.select.choose.label')}"]'/>
                </g:if>
                <g:else>
                    <input id="filterPropValue" type="text" name="filterPropValue" value="${params.filterPropValue}"
                           placeholder="${message(code: 'license.search.property.ph')}"/>
                </g:else>
            </div>

            <button class="ui button" ${!editable ? 'disabled="disabled"' : ''}
                    type="submit">${message(code: 'default.button.save_changes')}</button>


            <h3 class="ui header">
                <g:if test="${controllerName == "subscription"}">
                    ${message(code: 'subscriptionsManagement.subscriber')} <semui:totalNumber
                        total="${filteredSubscriptions.size()}"/>
                </g:if><g:else>
                    ${message(code: 'subscriptionsManagement.subscriptions')} <semui:totalNumber
                            total="${filteredSubscriptions.size()}/${num_sub_rows}"/>
                </g:else>
            </h3>
            <table class="ui celled la-js-responsive-table la-table table">
                <thead>
                <tr>
                    <g:if test="${editable}">
                        <th>
                            <g:checkBox name="membersListToggler" id="membersListToggler" checked="false"/>
                        </th>
                    </g:if>
                    <th>${message(code: 'sidewide.number')}</th>
                    <g:if test="${controllerName == "subscription"}">
                        <th>${message(code: 'default.sortname.label')}</th>
                        <th>${message(code: 'subscriptionDetails.members.members')}</th>
                    </g:if>
                    <g:if test="${controllerName == "myInstitution"}">
                        <th>${message(code: 'default.subscription.label')}</th>
                    </g:if>
                    <th>${message(code: 'default.startDate.label')}</th>
                    <th>${message(code: 'default.endDate.label')}</th>
                    <th>${message(code: 'default.status.label')}</th>
                    <th class="la-no-uppercase">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                              data-content="${message(code: 'subscription.isMultiYear.consortial.label')}">
                            <i class="map orange icon"></i>
                        </span>
                    </th>
                    <th>${message(code: 'subscriptionsManagement.propertySelected')}: ${propertiesFilterPropDef.getI10n('name')}</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${filteredSubscriptions}" status="i" var="sub">
                    <g:set var="subscr" value="${sub.getSubscriber()}"/>
                    <tr>
                        <g:if test="${editable}">
                            <td>
                                <%-- This whole construct is necessary for that the form validation works!!! --%>
                                <div class="field">
                                    <div class="ui checkbox">
                                        <g:checkBox id="selectedSubs_${sub.id}" name="selectedSubs" value="${sub.id}"
                                                    checked="false"/>
                                    </div>
                                </div>
                            </td>
                        </g:if>
                        <td>${(offset ?: 0) + i + 1}</td>
                        <g:if test="${controllerName == "subscription"}">
                            <td>
                                ${subscr.sortname}
                            </td>
                            <td>
                                <g:link controller="organisation" action="show" id="${subscr.id}">${subscr}</g:link>

                            <g:if test="${sub.isSlaved}">
                                <span data-position="top right"
                                      class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'license.details.isSlaved.tooltip')}">
                                    <i class="grey la-thumbtack-regular icon"></i>
                                </span>
                            </g:if>

                                <g:if test="${subscr.getCustomerType() == 'ORG_INST'}">
                                    <span class="la-long-tooltip la-popup-tooltip la-delay"
                                          data-position="bottom center"
                                          data-content="${subscr.getCustomerTypeI10n()}">
                                        <i class="chess rook grey icon"></i>
                                    </span>
                                </g:if>
                            </td>
                        </g:if>
                        <g:if test="${controllerName == "myInstitution"}">
                            <td>${sub.name}</td>
                        </g:if>

                        <td>
                            <semui:xEditable owner="${sub}" field="startDate" type="date"
                                             overwriteEditable="${editableOld}"/>
                            %{--<semui:auditButton auditable="[sub, 'startDate']"/>--}%
                        </td>
                        <td><semui:xEditable owner="${sub}" field="endDate" type="date"
                                             overwriteEditable="${editableOld}"/>
                        %{--<semui:auditButton auditable="[sub, 'endDate']"/>--}%
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

                                <g:if test="${propertiesFilterPropDef.tenant == null}">
                                    <div class="item">

                                        <div class="right floated content">
                                            <span class="la-popup-tooltip la-delay"
                                                  data-content="Anzahl der allg. Merkmale in der Lizenz"
                                                  data-position="top right">
                                                <semui:totalNumber
                                                        total="${SubscriptionProperty.executeQuery('select count(id) from SubscriptionProperty where owner = :sub AND (tenant = :contextOrg OR tenant is null OR (tenant != :contextOrg AND isPublic = true)) AND type.tenant is null', [contextOrg: contextOrg, sub: sub])[0] }"/>
                                            </span>
                                        </div>

                                        <g:set var="customProperties"
                                               value="${SubscriptionProperty.executeQuery('from SubscriptionProperty where owner = :sub AND (tenant = :contextOrg OR tenant is null OR (tenant != :contextOrg AND isPublic = true)) AND type = :propertiesFilterPropDef AND type.tenant is null', [contextOrg: contextOrg, sub: sub, propertiesFilterPropDef: propertiesFilterPropDef])}"/>
                                        <g:if test="${customProperties}">
                                            <g:each in="${customProperties}" var="customProperty">
                                                <div class="header">${message(code: 'subscriptionsManagement.CustomProperty')}: ${propertiesFilterPropDef.getI10n('name')}</div>

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
                                                                println '&nbsp; <span class="la-popup-tooltip la-delay" data-content="Wert wird automatisch geerbt." data-position="top right"><i class="icon grey la-thumbtack-regular"></i></span>'
                                                            } else {
                                                                println '&nbsp; <span class="la-popup-tooltip la-delay" data-content="Wert wird geerbt." data-position="top right"><i class="icon thumbtack grey"></i></span>'
                                                            }
                                                        }
                                                    %>

                                                </div>
                                            </g:each>
                                        </g:if><g:else>
                                        <div class="content">
                                            ${message(code: 'subscriptionsManagement.noCustomProperty')}
                                            <g:link class="ui button" controller="ajax" action="addCustomPropertyValue"
                                                    params="[
                                                            propIdent    : propertiesFilterPropDef.id,
                                                            ownerId      : sub.id,
                                                            ownerClass   : sub.class,
                                                            withoutRender: true,
                                                            url          : createLink(absolute: true, controller: controllerName, action: actionName, params: [id: params.id, tab: params.tab, propertiesFilterPropDef: propertiesFilterPropDef])
                                                    ]">
                                                ${message(code: 'default.button.add.label')}
                                            </g:link>
                                        </div>
                                    </g:else>
                                    </div>
                                </g:if>
                                <g:if test="${propertiesFilterPropDef.tenant != null}">

                                    <div class="item">

                                        <div class="right floated content">
                                            <span class="la-popup-tooltip la-delay"
                                                  data-content="Anzahl der priv. Merkmale in der Lizenz"
                                                  data-position="top right">
                                                <semui:totalNumber
                                                        total="${SubscriptionProperty.executeQuery('select count(id) from SubscriptionProperty where owner = :sub AND (type.tenant = :contextOrg AND tenant = :contextOrg)', [contextOrg: contextOrg, sub: sub])[0] }"/>
                                            </span>
                                        </div>

                                        <g:set var="privateProperties"
                                               value="${SubscriptionProperty.executeQuery('from SubscriptionProperty where owner = :sub AND (type.tenant = :contextOrg AND tenant = :contextOrg) AND type = :propertiesFilterPropDef ', [contextOrg: contextOrg, sub: sub, propertiesFilterPropDef: propertiesFilterPropDef])}"/>

                                        <g:if test="${privateProperties}">
                                            <g:each in="${privateProperties}" var="privateProperty">
                                                <div class="header">${message(code: 'subscriptionsManagement.PrivateProperty')} ${contextService.getOrg()}: ${propertiesFilterPropDef.getI10n('name')}</div>

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
                                                                println '&nbsp; <span class="la-popup-tooltip la-delay" data-content="Wert wird automatisch geerbt." data-position="top right"><i class="icon grey la-thumbtack-regular"></i></span>'
                                                            } else {
                                                                println '&nbsp; <span class="la-popup-tooltip la-delay" data-content="Wert wird geerbt." data-position="top right"><i class="icon thumbtack grey"></i></span>'
                                                            }
                                                        }
                                                    %>

                                                </div>
                                            </g:each>
                                        </g:if><g:else>
                                        <div class="content">
                                            ${message(code: 'subscriptionsManagement.noPrivateProperty')}
                                            <g:link class="ui button" controller="ajax" action="addPrivatePropertyValue"
                                                    params="[
                                                            propIdent    : propertiesFilterPropDef.id,
                                                            ownerId      : sub.id,
                                                            ownerClass   : sub.class,
                                                            withoutRender: true,
                                                            url          : createLink(absolute: true, controller: controllerName, action: actionName, params: [id: params.id, tab: params.tab, propertiesFilterPropDef: propertiesFilterPropDef])
                                                    ]">
                                                ${message(code: 'default.button.add.label')}
                                            </g:link>
                                        </div>
                                    </g:else>

                                    </div>
                                </g:if>
                            </div>

                        </td>

                        <td class="x">
                            <g:link controller="subscription" action="show" id="${sub.id}"
                                    class="ui icon button blue la-modern-button"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                <i aria-hidden="true" class="write icon"></i></g:link>
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </g:form>

    </div>

    <g:if test="${controllerName == "myInstitution"}">
        <g:if test="${filteredSubscriptions}">
            <semui:paginate action="${actionName}" controller="${controllerName}" params="${params+[propertiesFilterPropDef: propertiesFilterPropDef]}"
                            next="${message(code: 'default.paginate.next')}"
                            prev="${message(code: 'default.paginate.prev')}" max="${max}"
                            total="${num_sub_rows}"/>
        </g:if>
    </g:if>

</g:if>
<g:else>

    <br/>

    <g:if test="${!filteredSubscriptions}">
        <g:if test="${filterSet}">
            <br/><strong><g:message code="filter.result.empty.object"
                                    args="${[message(code: "subscription.plural")]}"/></strong>
        </g:if>
        <g:else>
            <br/><strong><g:message code="result.empty.object"
                                    args="${[message(code: "subscription.plural")]}"/></strong>
        </g:else>
    </g:if>
    <g:elseif test="${!propertiesFilterPropDef}">
        <strong><g:message code="subscriptionsManagement.noPropertySeleced"/></strong>
    </g:elseif>
</g:else>

<div id="magicArea"></div>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#membersListToggler').click(function () {
        if ($(this).prop('checked')) {
            $("tr[class!=disabled] input[name=selectedSubs]").prop('checked', true)
        } else {
            $("tr[class!=disabled] input[name=selectedSubs]").prop('checked', false)
        }
    });

    $('.propertiesForm').form({
        on: 'blur',
        inline: true,
        fields: {
            noSubscription: {
                identifier: 'selectedSubs',
                rules: [
                    {
                        type: 'checked',
                        prompt: '<g:message code="subscriptionsManagement.noSelectedSubscriptions.table"/>'
                    }
                ]
            }
        }
    });
</laser:script>

