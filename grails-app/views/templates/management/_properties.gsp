<%@ page import="de.laser.FormService; de.laser.interfaces.CalculatedType; de.laser.storage.PropertyStore; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.properties.SubscriptionProperty; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.properties.PropertyDefinition; de.laser.addressbook.Person; de.laser.storage.RDStore; de.laser.AuditConfig" %>
<laser:serviceInjection/>
    <%
        SortedSet<PropertyDefinition> allProperties = new TreeSet<PropertyDefinition>()
        allProperties.addAll(PropertyDefinition.findAllByTenantIsNullAndDescr(PropertyDefinition.SUB_PROP) + PropertyDefinition.findAllByTenantAndDescr(contextService.getOrg(), PropertyDefinition.SUB_PROP))
    %>


    <g:if test="${controllerName == "subscription"}">
        <div class="ui segment">
            <div class="ui two column very relaxed grid">
                <div class="column">
                        <g:form action="${actionName}" method="post" class="ui form" id="${params.id}" params="[tab: params.tab, showMembersSubWithMultiYear: params.showMembersSubWithMultiYear]">
                            <div class="fields" style="margin-bottom: 0">

                                <laser:render template="/templates/properties/genericFilter"
                                          model="[propList: propList, hideFilterProp: true, newfilterPropDefName: 'propertiesFilterPropDef', label:message(code: 'subscriptionsManagement.onlyPropOfParentSubscription', args: [subscription.name])]"/>

                                <div class="field la-field-noLabel" >
                                    <input type="submit" value="${message(code: 'template.orgLinksModal.select')}" class="${Btn.PRIMARY}"/>
                                </div>
                            </div>
                        </g:form>
                </div>
                <div class="column">
                        <g:form action="${actionName}" method="post" class="ui form" id="${params.id}"
                                params="[tab: params.tab, showMembersSubWithMultiYear: params.showMembersSubWithMultiYear]">
                            <div class="fields" style="margin-bottom: 0">
                                <laser:render template="/templates/properties/genericFilter"
                                          model="[propList: allProperties, hideFilterProp: true, newfilterPropDefName: 'propertiesFilterPropDef',label:message(code: 'subscriptionsManagement.allProperties')]"/>

                                <div class="field la-field-noLabel">
                                    <input type="submit" value="${message(code: 'template.orgLinksModal.select')}"
                                           class="${Btn.PRIMARY}"/>
                                </div>
                            </div>
                        </g:form>
                </div>
            </div>

            <div class="ui vertical divider"><g:message code="default.or"/></div>
        </div>
     </g:if>

    <g:if test="${controllerName == "myInstitution"}">
            <g:form action="${actionName}" method="post" class="ui segment form" id="${params.id}" style="margin-bottom: 0"
                    params="[tab: params.tab]">
                <div class="two fields" style="margin-bottom: 0">
                    <laser:render template="/templates/properties/genericFilter"
                              model="[propList: allProperties, hideFilterProp: true, newfilterPropDefName: 'propertiesFilterPropDef',label:message(code: 'subscriptionsManagement.allProperties')]"/>

                    <div class="field la-field-noLabel">
                        <input type="submit" value="${message(code: 'template.orgLinksModal.select')}"
                               class="${Btn.PRIMARY}"/>
                    </div>
                </div>
            </g:form>
    </g:if>

    <g:if test="${memberProperties}">%{-- check for content --}%
                <div class="ui segment">
                    <h3 class="ui small header">${message(code: 'subscription.properties.consortium')}</h3>

                    <div id="member_props_div">
                        <laser:render template="/templates/properties/members" model="${[
                                prop_desc       : PropertyDefinition.SUB_PROP,
                                ownobj          : subscription,
                                custom_props_div: "member_props_div"]}"/>
                    </div>
                </div>
    </g:if>


<g:if test="${filteredSubscriptions && propertiesFilterPropDef}">

    <g:if test="${controllerName == "subscription"}">

    %{--    <div class="ui segment">
            <h4 class="ui header">${message(code: 'subscriptionsManagement.deletePropertyInfo')}</h4>

            <g:link class="${Btn.NEGATIVE_CONFIRM}"
                    data-confirm-tokenMsg="${message(code: 'subscriptionsManagement.deleteProperty.button.confirm')}"
                    data-confirm-term-how="ok" action="${actionName}" id="${params.id}"
                    params="[processOption: 'deleteAllProperties', propertiesFilterPropDef: propertiesFilterPropDef, tab: params.tab]">${message(code: 'subscriptionsManagement.deleteProperty.button', args: [propertiesFilterPropDef.getI10n('name')])}</g:link>

        </div>--}%

        <div class="ui segment">
            <h3 class="ui header"><g:message code="subscriptionsManagement.subscription" args="${args.superOrgType}"/></h3>
            <table class="ui celled la-js-responsive-table la-table table">
                <thead>
                <tr>
                    <th>${message(code: 'subscription')}</th>
                    <th>${message(code: 'default.startDate.label.shy')}</th>
                    <th>${message(code: 'default.endDate.label.shy')}</th>
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
                        <ui:auditButton auditable="[subscription, 'startDate']"/>
                    </td>
                    <td>
                        <g:formatDate formatName="default.date.format.notime" date="${subscription.endDate}"/>
                        <ui:auditButton auditable="[subscription, 'endDate']"/>
                    </td>
                    <td>
                        ${subscription.status.getI10n('value')}
                        <ui:auditButton auditable="[subscription, 'status']"/>
                    </td>
                    <td>
                        <div class="ui middle aligned selection list">

                            <g:if test="${propertiesFilterPropDef.tenant == null}">
                                <div class="item">

                                    <div class="right floated content">
                                        <span class="la-popup-tooltip"
                                              data-content="Anzahl der allg. Merkmale in der Lizenz"
                                              data-position="top right">
                                            <ui:totalNumber
                                                    total="${subscriptionService.countCustomSubscriptionPropertiesOfSub(contextService.getOrg(), subscription)}"/>
                                        </span>
                                    </div>

                                    <g:set var="customProperties"
                                           value="${SubscriptionProperty.executeQuery('from SubscriptionProperty where owner = :sub AND ((tenant = :contextOrg OR tenant is null) OR (tenant != :contextOrg AND isPublic = true)) AND type = :propertiesFilterPropDef AND type.tenant is null', [contextOrg: contextService.getOrg(), sub: subscription, propertiesFilterPropDef: propertiesFilterPropDef])}"/>
                                    <g:if test="${customProperties}">
                                        <g:each in="${customProperties}" var="customProperty">
                                            <div class="header">${message(code: 'subscriptionsManagement.CustomProperty')}: ${propertiesFilterPropDef.getI10n('name')}</div>

                                            <div class="content">

                                                %{-- <g:set var="editable" value="${!(AuditConfig.getConfig(customProperty))}"
                                                        scope="request"/>--}%

                                                <g:if test="${customProperty.type.isLongType()}">
                                                    <ui:xEditable owner="${customProperty}" type="number"
                                                                     field="longValue"/>
                                                </g:if>
                                                <g:elseif test="${customProperty.type.isStringType()}">
                                                    <ui:xEditable owner="${customProperty}" type="text"
                                                                     field="stringValue"/>
                                                </g:elseif>
                                                <g:elseif test="${customProperty.type.isBigDecimalType()}">
                                                    <ui:xEditable owner="${customProperty}" type="text"
                                                                     field="decValue"/>
                                                </g:elseif>
                                                <g:elseif test="${customProperty.type.isDateType()}">
                                                    <ui:xEditable owner="${customProperty}" type="date"
                                                                     field="dateValue"/>
                                                </g:elseif>
                                                <g:elseif test="${customProperty.type.isURLType()}">
                                                    <ui:xEditable owner="${customProperty}" type="url"
                                                                     field="urlValue"

                                                                     class="la-overflow la-ellipsis"/>
                                                    <g:if test="${customProperty.value}">
                                                        <ui:linkWithIcon href="${customProperty.value}"/>
                                                    </g:if>
                                                </g:elseif>
                                                <g:elseif test="${customProperty.type.isRefdataValueType()}">
                                                    <ui:xEditableRefData owner="${customProperty}" type="text"
                                                                            field="refValue"
                                                                            config="${customProperty.type.refdataCategory}"/>
                                                </g:elseif>

                                                <%
                                                    if (AuditConfig.getConfig(customProperty)) {
                                                        if (subscription.instanceOf) {
                                                            println '&nbsp;' + ui.auditIcon(type: 'auto')
                                                        } else {
                                                            println '&nbsp;' + ui.auditIcon(type: 'default')
                                                        }
                                                    }
                                                %>

                                            </div>
                                        </g:each>
                                    </g:if><g:else>
                                    <div class="content">
                                        ${message(code: 'subscriptionsManagement.noCustomProperty')}
                                        <g:link class="${Btn.SIMPLE}" controller="ajax" action="addCustomPropertyValue"
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
                                        <span class="la-popup-tooltip"
                                              data-content="Anzahl der priv. Merkmale in der Lizenz"
                                              data-position="top right">
                                            <ui:totalNumber
                                                    total="${subscriptionService.countPrivateSubscriptionPropertiesOfSub(contextService.getOrg(), subscription)}"/>
                                        </span>
                                    </div>

                                    <g:set var="privateProperties"
                                           value="${SubscriptionProperty.executeQuery('from SubscriptionProperty where owner = :sub AND (type.tenant = :contextOrg AND tenant = :contextOrg) AND type = :propertiesFilterPropDef', [contextOrg: contextService.getOrg(), sub: subscription, propertiesFilterPropDef: propertiesFilterPropDef])}"/>
                                    <g:if test="${privateProperties}">
                                        <g:each in="${privateProperties}" var="privateProperty">
                                            <div class="header">${message(code: 'subscriptionsManagement.PrivateProperty')} ${contextService.getOrg()}: ${propertiesFilterPropDef.getI10n('name')}</div>

                                            <div class="content">

                                                <g:set var="editable"
                                                       value="${!(AuditConfig.getConfig(privateProperty))}" scope="request"/>

                                                <g:if test="${privateProperty.type.isLongType()}">
                                                    <ui:xEditable owner="${privateProperty}" type="number" field="longValue"/>
                                                </g:if>
                                                <g:elseif test="${privateProperty.type.isStringType()}">
                                                    <ui:xEditable owner="${privateProperty}" type="text" field="stringValue"/>
                                                </g:elseif>
                                                <g:elseif test="${privateProperty.type.isBigDecimalType()}">
                                                    <ui:xEditable owner="${privateProperty}" type="text" field="decValue"/>
                                                </g:elseif>
                                                <g:elseif test="${privateProperty.type.isDateType()}">
                                                    <ui:xEditable owner="${privateProperty}" type="date" field="dateValue"/>
                                                </g:elseif>
                                                <g:elseif test="${privateProperty.type.isURLType()}">
                                                    <ui:xEditable owner="${privateProperty}" type="url" field="urlValue" class="la-overflow la-ellipsis"/>
                                                    <g:if test="${privateProperty.value}">
                                                        <ui:linkWithIcon href="${privateProperty.value}"/>
                                                    </g:if>
                                                </g:elseif>
                                                <g:elseif test="${privateProperty.type.isRefdataValueType()}">
                                                    <ui:xEditableRefData owner="${privateProperty}" type="text" field="refValue" config="${privateProperty.type.refdataCategory}" constraint="removeValues_processingProvOrVendor"/>
                                                </g:elseif>

                                                <%
                                                    if (AuditConfig.getConfig(privateProperty)) {
                                                        if (subscription.instanceOf) {
                                                            println '&nbsp;' + ui.auditIcon(type: 'auto')
                                                        } else {
                                                            println '&nbsp;' + ui.auditIcon(type: 'default')
                                                        }
                                                    }
                                                %>

                                            </div>
                                        </g:each>
                                    </g:if><g:else>
                                    <div class="content">
                                        ${message(code: 'subscriptionsManagement.noPrivateProperty')}
                                        <g:link class="${Btn.SIMPLE}" controller="ajax" action="addPrivatePropertyValue"
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
                                class="${Btn.MODERN.SIMPLE}"
                                role="button"
                                aria-label="${message(code: 'ariaLabel.edit.universal')}">
                            <i aria-hidden="true" class="${Icon.CMD.EDIT}"></i>
                        </g:link>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>

        <div class="ui icon info message">
            <i class="${Icon.UI.INFO}"></i>
            <div class="content">
                <g:message code="subscriptionsManagement.info2" args="${args.memberTypeSingle}"/> <br />

                <g:message code="subscriptionsManagement.info3" args="${[args.superOrgType[0], args.memberType[0]]}"/> <br />

                <g:message code="subscriptionsManagement.info4" args="${args.memberTypeSingle}"/> <br />
            </div>
        </div>
    </g:if>

    <g:set var="editableOld" value="${false}"/>

    <g:if test="${controllerName == "myInstitution"}">
        <laser:render template="${customerTypeService.getSubscriptionFilterTemplatePath()}"/>
    </g:if>

    <h3 class="ui header">${message(code: 'subscriptionsManagement.info.property')}</h3>

    <g:form action="${actionName}" method="post" class="ui form propertiesForm" params="[tab: params.tab]">
        <g:hiddenField id="ppm_id_${params.id}" name="id" value="${params.id}"/>

        <div class="ui segments">
        <div class="ui segment">
            <div class="field required">
                <%
                    List<RefdataValue> propValues = []
                    if(propertiesFilterPropDef.isRefdataValueType()) {
                        propValues = RefdataCategory.getAllRefdataValuesWithOrder(propertiesFilterPropDef.refdataCategory)
                        if(propertiesFilterPropDef == PropertyStore.SUB_PROP_INVOICE_PROCESSING && subscription)
                            propValues.remove(RDStore.INVOICE_PROCESSING_PROVIDER_OR_VENDOR)
                    }
                %>
                <div class="inline field">
                    <label>${message(code: 'subscriptionsManagement.propertySelected')}:</label>

                    <strong>${propertiesFilterPropDef.getI10n('name')}
                        <g:if test="${propertiesFilterPropDef.tenant != null}">
                            <i class="${Icon.PROP.IS_PRIVATE}"></i>
                        </g:if>
                    </strong>
                </div>
                <g:hiddenField name="propertiesFilterPropDef" value="${propertiesFilterPropDef}"/>

                ${message(code: 'default.type.label')}: ${PropertyDefinition.getLocalizedValue(propertiesFilterPropDef.type)}
                <g:if test="${propertiesFilterPropDef.isRefdataValueType()}">
                    <g:set var="refdataValues" value="${[]}"/>
                    <g:each in="${propValues}" var="refdataValue">
                        <g:if test="${refdataValue.getI10n('value')}">
                            <g:set var="refdataValues" value="${refdataValues + refdataValue.getI10n('value')}"/>
                        </g:if>
                    </g:each>

                    (${refdataValues.join('/')})
                </g:if>
            </div>

            <div class="field">
                <label for="filterPropValue">${message(code: 'subscription.property.value')}</label>
                <g:if test="${propertiesFilterPropDef.isRefdataValueType()}">
                    <g:select class="ui search dropdown"
                              optionKey="id" optionValue="${{ it.getI10n('value') }}"
                              from="${propValues}"
                              name="filterPropValue" value="${params.filterPropValue}"
                              required=""
                              noSelection='["": "${message(code: 'default.select.choose.label')}"]'/>
                </g:if>
                <g:else>
                    <input id="filterPropValue" type="text" name="filterPropValue" value="${params.filterPropValue}"
                           placeholder="${message(code: 'license.search.property.ph')}"/>
                </g:else>
            </div>

            <g:if test="${showConsortiaFunctions}">
                <div class="field">
                    <label for="audit">${message(code: 'property.manageProperties.markForAudit')}</label>
                    <div class="ui checkbox">
                        <g:checkBox id="audit" name="audit" />
                    </div>
                </div>
            </g:if>

            <div class="two fields">
                <div class="eight wide field" style="text-align: left;">
                    <div class="ui buttons">
                        <button class="${Btn.POSITIVE}" ${!editable ? 'disabled="disabled"' : ''} type="submit"
                                name="processOption"
                                value="changeCreateProperty">${message(code: 'default.button.save_changes')}</button>
                    </div>
                </div>

                <div class="eight wide field" style="text-align: right;">
                    <div class="ui buttons">
                        <button class="${Btn.NEGATIVE}" ${!editable ? 'disabled="disabled"' : ''} type="submit"
                                name="processOption"
                                value="deleteProperty">${message(code: 'subscriptionsManagement.deleteProperty.button', args: [propertiesFilterPropDef.getI10n('name')])}</button>
                    </div>
                </div>
            </div>

        </div><!-- .segment -->

        <div class="ui segment">
            <h3 class="ui header">
                <g:if test="${controllerName == "subscription"}">
                    ${message(code: 'subscriptionsManagement.subscriber')} <ui:totalNumber total="${filteredSubscriptions.size()}"/>
                </g:if><g:else>
                    ${message(code: 'subscriptionsManagement.subscriptions')} <ui:totalNumber total="${num_sub_rows}"/>
                </g:else>
            </h3>
            <table class="ui celled la-js-responsive-table la-table table">
                <thead>
                <tr>
                    <g:if test="${editable}">
                        <th class="center aligned">
                            <g:checkBox name="membersListToggler" id="membersListToggler" checked="${managementService.checkTogglerState(subIDs, "/${controllerName}/subscriptionManagement/${params.tab}/${user.id}")}"/>
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
                    <th>${message(code: 'default.startDate.label.shy')}</th>
                    <th>${message(code: 'default.endDate.label.shy')}</th>
                    <th>${message(code: 'default.status.label')}</th>
                    <th class="la-no-uppercase">
                        <ui:multiYearIcon />
                    </th>
                    <th>${message(code: 'subscriptionsManagement.propertySelected')}: ${propertiesFilterPropDef.getI10n('name')}</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${filteredSubscriptions}" status="i" var="sub">
                    <g:set var="subscr" value="${sub.getSubscriberRespConsortia()}"/>
                    <tr>
                        <g:if test="${editable}">
                            <td>
                                <%-- This whole construct is necessary for that the form validation works!!! --%>
                                <div class="field">
                                    <div class="ui checkbox">
                                        <g:checkBox id="selectedSubs_${sub.id}" name="selectedSubs" value="${sub.id}" checked="false"/>
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
                                <ui:customerTypeOnlyProIcon org="${subscr}" />
                            </td>
                        </g:if>
                        <g:if test="${controllerName == "myInstitution"}">
                            <td>${sub.name} <span class="${sub.type == RDStore.SUBSCRIPTION_TYPE_CONSORTIAL ? 'sc_blue' : ''}"> (${sub.type.getI10n('value')}) </span></td>
                        </g:if>

                        <td>
                            <ui:xEditable owner="${sub}" field="startDate" type="date" overwriteEditable="${editableOld}"/>
                            %{--<ui:auditButton auditable="[sub, 'startDate']"/>--}%
                        </td>
                        <td><ui:xEditable owner="${sub}" field="endDate" type="date" overwriteEditable="${editableOld}"/>
                        %{--<ui:auditButton auditable="[sub, 'endDate']"/>--}%
                        </td>
                        <td>
                            ${sub.status.getI10n('value')}
                            <ui:auditButton auditable="[sub, 'status']"/>
                        </td>
                        <td>
                            <g:if test="${sub.isMultiYear}">
                                <ui:multiYearIcon isConsortial="true" color="orange" />
                            </g:if>
                        </td>
                        <td>

                            <div class="ui middle aligned selection list">

                                <g:if test="${propertiesFilterPropDef.tenant == null}">
                                    <div class="item">

                                        <div class="right floated content">
                                            <span class="la-popup-tooltip"
                                                  data-content="Anzahl der allg. Merkmale in der Lizenz"
                                                  data-position="top right">
                                                <ui:totalNumber
                                                        total="${subscriptionService.countCustomSubscriptionPropertiesOfSub(contextService.getOrg(), sub)}"/>
                                            </span>
                                        </div>

                                        <g:set var="customProperties"
                                               value="${SubscriptionProperty.executeQuery('from SubscriptionProperty where owner = :sub AND (tenant = :contextOrg OR tenant is null OR (tenant != :contextOrg AND isPublic = true)) AND type = :propertiesFilterPropDef AND type.tenant is null', [contextOrg: contextService.getOrg(), sub: sub, propertiesFilterPropDef: propertiesFilterPropDef])}"/>
                                        <g:if test="${customProperties}">
                                            <g:each in="${customProperties}" var="customProperty">
                                                <div class="header">${message(code: 'subscriptionsManagement.CustomProperty')}: ${propertiesFilterPropDef.getI10n('name')}</div>

                                                <div class="content">
                                                    <g:if test="${customProperty.type.isLongType()}">
                                                        <ui:xEditable owner="${customProperty}" type="number" field="longValue"/>
                                                    </g:if>
                                                    <g:elseif test="${customProperty.type.isStringType()}">
                                                        <ui:xEditable owner="${customProperty}" type="text" field="stringValue"/>
                                                    </g:elseif>
                                                    <g:elseif test="${customProperty.type.isBigDecimalType()}">
                                                        <ui:xEditable owner="${customProperty}" type="text" field="decValue"/>
                                                    </g:elseif>
                                                    <g:elseif test="${customProperty.type.isDateType()}">
                                                        <ui:xEditable owner="${customProperty}" type="date" field="dateValue"/>
                                                    </g:elseif>
                                                    <g:elseif test="${customProperty.type.isURLType()}">
                                                        <ui:xEditable owner="${customProperty}" type="url" field="urlValue" class="la-overflow la-ellipsis"/>
                                                        <g:if test="${customProperty.value}">
                                                            <ui:linkWithIcon href="${customProperty.value}"/>
                                                        </g:if>
                                                    </g:elseif>
                                                    <g:elseif test="${customProperty.type.isRefdataValueType()}">
                                                        <ui:xEditableRefData owner="${customProperty}" type="text"
                                                                                field="refValue"
                                                                                config="${customProperty.type.refdataCategory}"
                                                                             constraint="removeValues_processingProvOrVendor"/>
                                                    </g:elseif>
                                                    <g:if test="${customProperty.hasProperty('instanceOf') && customProperty.instanceOf && AuditConfig.getConfig(customProperty.instanceOf)}">
                                                        <g:if test="${sub.instanceOf}">&nbsp;<ui:auditIcon type="auto"/></g:if>
                                                        <g:else>&nbsp;<ui:auditIcon type="default"/></g:else>
                                                    </g:if>
                                                    <g:elseif test="${sub._getCalculatedType() == CalculatedType.TYPE_CONSORTIAL}">
                                                        <g:if test="${!AuditConfig.getConfig(customProperty)}">
                                                            <g:link class="${Btn.MODERN.SIMPLE_CONFIRM_TOOLTIP}"
                                                                           controller="ajax"
                                                                           action="togglePropertyAuditConfig"
                                                                           params='[propClass                                : customProperty.getClass(),
                                                                                    ownerId                                  : "${sub.id}",
                                                                                    ownerClass                               : "${sub.class}",
                                                                                    noWrapper                                : true,
                                                                                    editable                                 : "${editable}",
                                                                                    (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()
                                                                           ]'
                                                                           data-confirm-tokenMsg="${message(code: "confirm.dialog.inherit2.property", args: [customProperty.type.getI10n('name')])}"
                                                                           data-confirm-term-how="inherit"
                                                                           id="${customProperty.id}"
                                                                           data-content="${message(code:'property.audit.off.tooltip')}"
                                                                           role="button"
                                                            >
                                                                <i class="${Icon.SIG.INHERITANCE_OFF}"></i>
                                                            </g:link>
                                                        </g:if>
                                                        <g:else>
                                                            <g:link class="${Btn.MODERN.POSITIVE_CONFIRM_TOOLTIP}"
                                                                           controller="ajax" action="togglePropertyAuditConfig"
                                                                           params='[propClass: customProperty.getClass(),
                                                                                    ownerId: "${sub.id}",
                                                                                    ownerClass: "${sub.class}",
                                                                                    noWrapper: true,
                                                                                    editable: "${editable}",
                                                                                    (FormService.FORM_SERVICE_TOKEN): formService.getNewToken()
                                                                           ]'
                                                                           id="${customProperty.id}"
                                                                           data-content="${message(code:'property.audit.on.tooltip')}"
                                                                           data-confirm-tokenMsg="${message(code: "confirm.dialog.inherit.property", args: [customProperty.type.getI10n('name')])}"
                                                                           data-confirm-term-how="inherit"
                                                                           role="button"
                                                            >
                                                                <i class="${Icon.SIG.INHERITANCE}"></i>
                                                            </g:link>
                                                        </g:else>
                                                    </g:elseif>
                                                </div>
                                            </g:each>
                                        </g:if><g:else>
                                        <div class="content">
                                            ${message(code: 'subscriptionsManagement.noCustomProperty')}
                                            <g:link class="${Btn.SIMPLE}" controller="ajax" action="addCustomPropertyValue"
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
                                            <span class="la-popup-tooltip"
                                                  data-content="Anzahl der priv. Merkmale in der Lizenz"
                                                  data-position="top right">
                                                <ui:totalNumber
                                                        total="${subscriptionService.countPrivateSubscriptionPropertiesOfSub(contextService.getOrg(), sub)}"/>
                                            </span>
                                        </div>

                                        <g:set var="privateProperties"
                                               value="${SubscriptionProperty.executeQuery('from SubscriptionProperty where owner = :sub AND (type.tenant = :contextOrg AND tenant = :contextOrg) AND type = :propertiesFilterPropDef ', [contextOrg: contextService.getOrg(), sub: sub, propertiesFilterPropDef: propertiesFilterPropDef])}"/>

                                        <g:if test="${privateProperties}">
                                            <g:each in="${privateProperties}" var="privateProperty">
                                                <div class="header">${message(code: 'subscriptionsManagement.PrivateProperty')} ${contextService.getOrg()}: ${propertiesFilterPropDef.getI10n('name')}</div>

                                                <div class="content">
                                                    <g:if test="${privateProperty.type.isLongType()}">
                                                        <ui:xEditable owner="${privateProperty}" type="number" field="longValue"/>
                                                    </g:if>
                                                    <g:elseif test="${privateProperty.type.isStringType()}">
                                                        <ui:xEditable owner="${privateProperty}" type="text" field="stringValue"/>
                                                    </g:elseif>
                                                    <g:elseif test="${privateProperty.type.isBigDecimalType()}">
                                                        <ui:xEditable owner="${privateProperty}" type="text" field="decValue"/>
                                                    </g:elseif>
                                                    <g:elseif test="${privateProperty.type.isDateType()}">
                                                        <ui:xEditable owner="${privateProperty}" type="date" field="dateValue"/>
                                                    </g:elseif>
                                                    <g:elseif test="${privateProperty.type.isURLType()}">
                                                        <ui:xEditable owner="${privateProperty}" type="url" field="urlValue" class="la-overflow la-ellipsis"/>
                                                        <g:if test="${privateProperty.value}">
                                                            <ui:linkWithIcon href="${privateProperty.value}"/>
                                                        </g:if>
                                                    </g:elseif>
                                                    <g:elseif test="${privateProperty.type.isRefdataValueType()}">
                                                        <ui:xEditableRefData owner="${privateProperty}" type="text"
                                                                                field="refValue"
                                                                                config="${privateProperty.type.refdataCategory}"/>
                                                    </g:elseif>

                                                    <%
                                                        if (privateProperty.hasProperty('instanceOf') && privateProperty.instanceOf && AuditConfig.getConfig(privateProperty.instanceOf)) {
                                                            if (sub.instanceOf) {
                                                                println '&nbsp;' + ui.auditIcon(type: 'auto')
                                                            } else {
                                                                println '&nbsp;' + ui.auditIcon(type: 'default')
                                                            }
                                                        }
                                                    %>

                                                </div>
                                            </g:each>
                                        </g:if><g:else>
                                        <div class="content">
                                            ${message(code: 'subscriptionsManagement.noPrivateProperty')}
                                            <g:link class="${Btn.SIMPLE}" controller="ajax" action="addPrivatePropertyValue"
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
                                    class="${Btn.MODERN.SIMPLE}"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                <i aria-hidden="true" class="${Icon.CMD.EDIT}"></i></g:link>
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div><!-- .segment -->
        </div><!-- .segments -->
    </g:form>

    <g:if test="${controllerName == "myInstitution"}">
        <g:if test="${filteredSubscriptions}">
            <ui:paginate action="${actionName}" controller="${controllerName}" params="${params+[propertiesFilterPropDef: propertiesFilterPropDef]}"
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
            <br/><strong><g:message code="filter.result.empty.object" args="${[message(code: "subscription.plural")]}"/></strong>
        </g:if>
        <g:else>
            <br/><strong><g:message code="result.empty.object" args="${[message(code: "subscription.plural")]}"/></strong>
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

    $("input[name=selectedSubs]").checkbox({
        onChange: function() {
            $('#membersListToggler').prop('checked', false);
        }
    });

    /*
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
    */
</laser:script>

