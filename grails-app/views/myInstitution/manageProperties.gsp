<%@ page import="de.laser.RefdataValue; de.laser.properties.PropertyDefinition; de.laser.Person; de.laser.storage.RDStore; de.laser.RefdataCategory; grails.plugins.orm.auditable.Auditable; de.laser.AuditConfig" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'menu.institutions.manage_props')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.institutions.manage_props" class="active" />
</semui:breadcrumbs>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code: 'menu.institutions.manage_props')}</h1>

<laser:render template="nav"/>

<semui:filter>
    <g:form action="manageProperties" method="post" class="ui form" id="${params.id}">
        <laser:render template="/templates/properties/genericFilter" model="[propList: propList, hideFilterProp: true, label:message(code: 'subscription.property.search')]"/>

        <div class="field la-field-right-aligned">
            <a href="${request.forwardURI}"
               class="ui reset primary button">${message(code: 'default.button.reset.label')}</a>
            <input type="submit" value="${message(code: 'default.button.filter.label')}" class="ui secondary button"/>
        </div>
    </g:form>
</semui:filter>




<g:if test="${filterPropDef}">

%{--<div class="ui segment">

    <strong>${message(code: 'subscriptionsManagement.propertySelected')}: ${filterPropDef?.getI10n('name')}</strong>
    <br />${message(code: 'default.type.label')}: ${PropertyDefinition.getLocalizedValue(filterPropDef?.type)}
    <g:if test="${filterPropDef?.isRefdataValueType()}">
        <g:set var="refdataValues" value="${[]}"/>
        <g:each in="${RefdataCategory.getAllRefdataValues(filterPropDef.refdataCategory)}"
                var="refdataValue">
            <g:set var="refdataValues"
                   value="${refdataValues + refdataValue?.getI10n('value')}"/>
        </g:each>
        <br />
        (${refdataValues.join('/')})
    </g:if>

</div>--}%

    <g:set var="editableOld" value="${editable}"/>

    <div class="ui segment">
        <g:form action="processManageProperties" method="post" class="ui form">
            <div class="field">
                <h2 class="ui header"><g:if test="${filterPropDef.tenant != null}"><i class="shield alternate icon"></i></g:if><g:message code="property.manageProperties.add" args="[filterPropDef.getI10n('name')]"/></h2>${message(code: 'default.type.label')}: ${PropertyDefinition.getLocalizedValue(filterPropDef.type)}
                <g:hiddenField name="filterPropDef" value="${genericOIDService.getOID(filterPropDef)}"/>
                <g:if test="${filterPropDef.isRefdataValueType()}">
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
                <label for="filterPropValue">${message(code: 'subscription.property.value')}  <g:message code="messageRequiredField" /></label>
                <g:if test="${filterPropDef.isRefdataValueType()}">
                    <g:select class="ui search dropdown"
                              optionKey="id" optionValue="${{ it.getI10n('value') }}"
                              from="${RefdataCategory.getAllRefdataValues(filterPropDef.refdataCategory)}"
                              name="filterPropValue" value=""
                              required=""
                              noSelection='["": "${message(code: 'default.select.choose.label')}"]'/>
                </g:if>
                <g:elseif test="${filterPropDef.isIntegerType()}">
                    <input id="filterPropValue" type="number" name="filterPropValue">
                </g:elseif>
                <g:elseif test="${filterPropDef.isBigDecimalType()}">
                    <input id="filterPropValue" type="number" step="0.01" name="filterPropValue">
                </g:elseif>
                <g:elseif test="${filterPropDef.isDateType()}">
                    <g:datePicker name="filterPropValue"/>
                </g:elseif>
                <g:else>
                    <input id="filterPropValue" type="text" name="filterPropValue" placeholder="${message(code: 'license.search.property.ph')}"/>
                </g:else>
            </div>
            <table class="ui celled la-js-responsive-table la-table table" id="withoutPropTable">
                <thead>
                    <tr>
                        <th colspan="5">
                            <input name="filterTable" id="filterTableWithoutProp" value="" placeholder="${message(code:'property.manageProperties.filterTable')}">
                        </th>
                    </tr>
                    <tr>
                        <th>
                            <g:message code="property.manageProperties.markForAdd"/><br />
                            <g:checkBox name="membersAddListToggler" id="membersAddListToggler" checked="false"/>
                        </th>
                        <g:if test="${showConsortiaFunctions && auditable}">
                            <th>
                                <span data-tooltip="${message(code:'property.manageProperties.markForAudit')}"><i class="ui thumbtack icon"></i></span><br />
                                <g:checkBox name="membersAuditListToggler" id="membersAuditListToggler" checked="false"/>
                            </th>
                        </g:if>
                        <g:if test="${sortname}">
                            <th><g:message code="default.sortname.label"/></th>
                        </g:if>
                        <th><g:message code="default.name.label"/></th>
                        <th><g:message code="property.manageProperties.propertySelected"/>: ${filterPropDef.getI10n('name')}</th>
                        <th class="x"><button class="ui button" type="submit">${message(code: 'default.button.save_changes')}</button></th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${objectsWithoutProp}" var="objWithoutProp">
                        <tr>
                            <td><g:checkBox name="newObjects" value="${objWithoutProp.id}" checked="${objWithoutProp.id.toString() in selectedWithout ? 'true' : 'false'}"/></td>
                            <g:if test="${showConsortiaFunctions && auditable}">
                                <td><g:checkBox name="withAudit" value="${objWithoutProp.id}" checked="${objWithoutProp.id.toString() in selectedAudit ? 'true' : 'false'}"/></td>
                            </g:if>
                            <g:if test="${sortname}">
                                <td>${objWithoutProp.sortname}</td>
                            </g:if>
                            <td>${objWithoutProp.name}</td>
                            <td>
                                <div class="ui middle aligned selection list">
                                    <g:if test="${filterPropDef.tenant == null}">
                                        <div class="item">

                                            <g:set var="customProperty" value="${objWithoutProp.propertySet.find { it.tenant?.id == institution.id && it.type == filterPropDef }}"/>
                                            <g:if test="${customProperty}">
                                                <div class="header">${message(code: 'subscriptionsManagement.CustomProperty')}: ${filterPropDef.getI10n('name')}</div>

                                                <div class="content">
                                                    <p>
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
                                                    </p>
                                                    <g:if test="${customProperty.hasProperty('paragraph')}">
                                                        <p><semui:xEditable owner="${customProperty}" type="text" field="paragraph"/></p>
                                                    </g:if>

                                                    <%
                                                        if (AuditConfig.getConfig(customProperty)) {
                                                            if (objWithoutProp.isSlaved) {
                                                                println '&nbsp; <span class="la-popup-tooltip la-delay" data-content="Wert wird automatisch geerbt." data-position="top right"><i class="icon grey la-thumbtack-regular"></i></span>'
                                                            } else {
                                                                println '&nbsp; <span class="la-popup-tooltip la-delay" data-content="Wert wird geerbt." data-position="top right"><i class="icon thumbtack grey"></i></span>'
                                                            }
                                                        }
                                                    %>

                                                </div>
                                            </g:if>
                                            <g:else>
                                                <div class="content">
                                                    ${message(code: 'subscriptionsManagement.noCustomProperty')}
                                                </div>
                                            </g:else>
                                        </div>
                                    </g:if>
                                    <g:if test="${filterPropDef.tenant != null}">

                                        <div class="item">

                                            <g:set var="privateProperty" value="${objWithoutProp.propertySet.find { it.type == filterPropDef }}"/>
                                            <g:if test="${privateProperty}">
                                                <div class="header">${message(code: 'subscriptionsManagement.PrivateProperty')} ${institution}: ${filterPropDef.getI10n('name')}</div>

                                                <div class="content">
                                                    <p>
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
                                                    </p>
                                                    <g:if test="${privateProperty.hasProperty('paragraph')}">
                                                        <p><semui:xEditable owner="${privateProperty}" type="text" field="paragraph"/></p>
                                                    </g:if>

                                                    <%
                                                        if (AuditConfig.getConfig(privateProperty)) {
                                                            if (objWithoutProp.isSlaved) {
                                                                println '&nbsp; <span class="la-popup-tooltip la-delay" data-content="Wert wird automatisch geerbt." data-position="top right"><i class="icon grey la-thumbtack-regular"></i></span>'
                                                            } else {
                                                                println '&nbsp; <span class="la-popup-tooltip la-delay" data-content="Wert wird geerbt." data-position="top right"><i class="icon thumbtack grey"></i></span>'
                                                            }
                                                        }
                                                    %>

                                                </div>
                                            </g:if>
                                            <g:else>
                                                <div class="content">
                                                    ${message(code: 'subscriptionsManagement.noPrivateProperty')}
                                                </div>
                                            </g:else>
                                        </div>
                                    </g:if>
                                </div>
                            </td>
                            <td class="x">
                                <g:link controller="${objWithoutProp.displayController}" action="show" id="${objWithoutProp.id}" class="ui icon button blue la-modern-button"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                    <i aria-hidden="true" class="write icon"></i>
                                </g:link>
                            </td>
                        </tr>
                    </g:each>
                </tbody>
            </table>
        </g:form>
        <semui:paginate params="${params+[setWithout: true, setWith: false]}"
                        next="${message(code: 'default.paginate.next')}"
                        prev="${message(code: 'default.paginate.prev')}"
                        max="${max}" offset="${withoutPropOffset}"
                        total="${countObjWithoutProp}"/>
    </div>

    <div class="ui segment">

        <g:form action="processManageProperties" method="post" class="ui form">
            <g:hiddenField id="pmp_id_${params.id}" name="id" value="${params.id}"/>

            <div class="field">
                <h2 class="ui header">
                    <g:if test="${filterPropDef.tenant != null}">
                        <i class="shield alternate icon"></i>
                    </g:if>
                    <g:message code="property.manageProperties.edit" args="[filterPropDef.getI10n('name')]"/>
                </h2>
                <g:hiddenField name="filterPropDef" value="${genericOIDService.getOID(filterPropDef)}"/>
                ${message(code: 'default.type.label')}: ${PropertyDefinition.getLocalizedValue(filterPropDef.type)}
                <g:if test="${filterPropDef.isRefdataValueType()}">
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

            <div class="field">
                <label for="filterPropValue">${message(code: 'subscription.property.value')}</label>
                <g:if test="${filterPropDef.isRefdataValueType()}">
                    <g:select class="ui search dropdown"
                              optionKey="id" optionValue="${{ it.getI10n('value') }}"
                              from="${RefdataCategory.getAllRefdataValues(filterPropDef.refdataCategory)}"
                              name="filterPropValue" value=""
                              noSelection='["": "${message(code: 'default.select.choose.label')}"]'/>
                </g:if>
                <g:elseif test="${filterPropDef.isIntegerType()}">
                    <input id="filterPropValue" type="number" name="filterPropValue">
                </g:elseif>
                <g:elseif test="${filterPropDef.isBigDecimalType()}">
                    <input id="filterPropValue" type="number" step="0.01" name="filterPropValue">
                </g:elseif>
                <g:elseif test="${filterPropDef.isDateType()}">
                    <g:datePicker name="filterPropValue"/>
                </g:elseif>
                <g:else>
                    <input id="filterPropValue" type="text" name="filterPropValue" placeholder="${message(code: 'license.search.property.ph')}">
                </g:else>
            </div>

            <table class="ui celled la-js-responsive-table la-table table" id="existingObjTable">
                <thead>
                    <tr>
                        <th colspan="6">
                            <input name="filterTable" id="filterTableExistingObj" value="" placeholder="${message(code:'property.manageProperties.filterTable')}">
                        </th>
                    </tr>
                    <tr>
                        <th>
                            <g:checkBox name="membersListToggler" id="membersListToggler" checked="false"/>
                        </th>
                        <th>${message(code: 'sidewide.number')}</th>
                        <g:if test="${sortname}">
                            <th>${message(code: 'default.sortname.label')}</th>
                        </g:if>
                        <th>${message(code: 'default.name.label')}</th>
                        <th>${message(code: 'property.manageProperties.propertySelected')}: ${filterPropDef.getI10n('name')}</th>
                        <th class="x">
                            <button class="ui button" type="submit" name="saveChanges" value="true">${message(code: 'default.button.save_changes')}</button>
                            <button class="ui button negative" type="submit" name="deleteProperties" value="true">${message(code: 'property.manageProperties.deleteProperty.button', args: [filterPropDef.getI10n('name')])}</button>
                                <%-- TODO ask Ingrid
                                    js-open-confirm-modal
                                    data-confirm-tokenMsg="${message(code: 'property.manageProperties.deleteProperty.button.confirm')}"
                                    data-confirm-term-how="ok"
                                --%>
                        </th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${filteredObjs}" status="i" var="row">
                        <tr>
                            <td>
                                <g:checkBox name="selectedObjects" value="${row.id}" checked="${row.id.toString() in selectedWith ? 'true' : 'false'}"/>
                            </td>
                            <td>${i + 1}</td>
                            <td>
                                ${row.name}
                            </td>
                            <g:if test="${row.sortname}">
                                <td>${row.sortname}</td>
                            </g:if>
                            <td>

                                <div class="ui middle aligned selection list">

                                    <g:if test="${filterPropDef.tenant == null}">
                                        <div class="item">

                                            <g:set var="customProperty" value="${row.propertySet.find { it.tenant?.id == institution.id && it.type.id == filterPropDef.id }}"/>
                                            <g:if test="${customProperty}">
                                                <div class="header">${message(code: 'subscriptionsManagement.CustomProperty')}: ${filterPropDef.getI10n('name')}</div>

                                                <div class="content">
                                                    <p><g:if test="${customProperty.type.isIntegerType()}">
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
                                                    </p>

                                                    <g:if test="${customProperty.hasProperty('paragraph')}">
                                                        <p><semui:xEditable owner="${customProperty}" type="text" field="paragraph"/></p>
                                                    </g:if>
                                                    <%
                                                        if ((customProperty.hasProperty('instanceOf') && customProperty.instanceOf && AuditConfig.getConfig(customProperty.instanceOf)) || AuditConfig.getConfig(customProperty)) {
                                                            if (row.isSlaved) {
                                                                print '<span class="la-popup-tooltip la-delay" data-content="Wert wird automatisch geerbt." data-position="top right"><i class="icon grey la-thumbtack-regular"></i></span>'
                                                            } else {
                                                                print '<span class="la-popup-tooltip la-delay" data-content="Wert wird geerbt." data-position="top right"><i class="icon thumbtack grey"></i></span>'
                                                            }
                                                        }
                                                    %>

                                                </div>
                                            </g:if><g:else>
                                            <div class="content">
                                                ${message(code: 'subscriptionsManagement.noCustomProperty')}
                                            </div>
                                        </g:else>
                                        </div>
                                    </g:if>
                                    <g:if test="${filterPropDef.tenant != null}">

                                        <div class="item">

                                            <g:set var="privateProperty" value="${row.propertySet.find { it.type.id == filterPropDef.id }}"/>

                                            <g:if test="${privateProperty}">
                                                <div class="header">${message(code: 'subscriptionsManagement.PrivateProperty')} ${contextService.getOrg()}: ${filterPropDef.getI10n('name')}</div>

                                                <div class="content">
                                                    <p>
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
                                                        <g:elseif test="${privateProperty.type.isRefdataValueType()}">
                                                            <semui:xEditableRefData owner="${privateProperty}" type="text"
                                                                                    field="refValue"
                                                                                    config="${privateProperty.type.refdataCategory}"/>
                                                        </g:elseif>
                                                    </p>
                                                    <g:if test="${privateProperty.hasProperty('paragraph')}">
                                                        <p><semui:xEditable owner="${privateProperty}" type="text" field="paragraph"/></p>
                                                    </g:if>
                                                    <%
                                                        if ((privateProperty.hasProperty('instanceOf') && privateProperty.instanceOf && AuditConfig.getConfig(privateProperty.instanceOf)) || AuditConfig.getConfig(privateProperty)) {
                                                            if (row.isSlaved) {
                                                                println '&nbsp; <span class="la-popup-tooltip la-delay" data-content="Wert wird automatisch geerbt." data-position="top right"><i class="icon grey la-thumbtack-regular"></i></span>'
                                                            } else {
                                                                println '&nbsp; <span class="la-popup-tooltip la-delay" data-content="Wert wird geerbt." data-position="top right"><i class="icon thumbtack grey"></i></span>'
                                                            }
                                                        }
                                                    %>

                                                </div>
                                            </g:if>
                                            <g:else>
                                                <div class="content">
                                                    ${message(code: 'subscriptionsManagement.noPrivateProperty')}
                                                </div>
                                            </g:else>

                                        </div>
                                    </g:if>
                                </div>

                            </td>

                            <td class="x">
                                <g:link controller="${row.displayController}" action="${row.displayAction}" id="${row.id}" class="ui icon button blue la-modern-button"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                    <i aria-hidden="true" class="write icon"></i>
                                </g:link>
                                <g:if test="${row.manageChildren}">
                                    <g:link controller="${row.displayController}" action="${row.manageChildren}" params="${row.manageChildrenParams}" class="ui icon button"><i class="users icon"></i></g:link>
                                </g:if>
                            </td>
                        </tr>
                    </g:each>
                </tbody>
            </table>
        </g:form>
        <semui:paginate params="${params+[setWith: true, setWithout: false]}"
                        next="${message(code: 'default.paginate.next')}"
                        prev="${message(code: 'default.paginate.prev')}"
                        max="${max}" offset="${withPropOffset}"
                        total="${countObjWithProp}"/>
    </div>
</g:if>

<g:else>
    <br />
    <g:if test="${!filterPropDef}">
        <strong><g:message code="property.manageProperties.noPropertySelected"/></strong>
    </g:if>
</g:else>

<div id="magicArea"></div>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#membersListToggler').click(function () {
        if ($(this).prop('checked')) {
            $("tr[class!=disabled] input[name=selectedObjects]:visible").prop('checked', true);
            JSPC.app.updateSelectionCache("all", "with" ,true);
        }
        else {
            $("tr[class!=disabled] input[name=selectedObjects]:visible").prop('checked', false);
            JSPC.app.updateSelectionCache("all", "with" ,false);
        }
    });
    $('#membersAddListToggler').click(function () {
        if ($(this).prop('checked')) {
            $("tr[class!=disabled] input[name=newObjects]:visible").prop('checked', true);
            JSPC.app.updateSelectionCache("all", "without" ,true);
        }
        else {
            $("tr[class!=disabled] input[name=newObjects]:visible").prop('checked', false);
            JSPC.app.updateSelectionCache("all", "without" ,false);
        }
    });
    $('#membersAuditListToggler').click(function () {
        if ($(this).prop('checked')) {
            $("tr[class!=disabled] input[name=withAudit]:visible").prop('checked', true);
            JSPC.app.updateSelectionCache("all", "audit" ,true);
        }
        else {
            $("tr[class!=disabled] input[name=withAudit]:visible").prop('checked', false);
            JSPC.app.updateSelectionCache("all", "audit" ,false);
        }
    });

    JSPC.app.updateSelectionCache = function (index, table, checked) {
        $.ajax({
            url: "<g:createLink controller="ajax" action="updatePropertiesSelection" />",
                data: {
                    key: index,
                    propDef: "${params.filterPropDef}",
                    status: "${params.objStatus}",
                    table: table,
                    checked: checked
                }
            }).done(function(result){

            }).fail(function(xhr,status,message){
                console.log("error occurred, consult logs!");
            });
    }

    $("[name=selectedObjects]").change(function() {
        console.log($(this));
        JSPC.app.updateSelectionCache($(this).val(), "with", $(this).prop('checked'));
    });
    $("[name=newObjects], [name=withAudit]").change(function() {
        JSPC.app.updateSelectionCache($(this).val(), "without", $(this).prop('checked'));
    });
    $('#filterTableWithoutProp').keyup(function() {
        $("#withoutPropTable tbody tr:icontains('"+$(this).val()+"')").show();
        $("#withoutPropTable tbody tr:not(:icontains('"+$(this).val()+"'))").hide();
    });
    $('#filterTableExistingObj').keyup(function() {
        $("#existingObjTable tbody tr:icontains('"+$(this).val()+"')").show();
        $("#existingObjTable tbody tr:not(:icontains('"+$(this).val()+"'))").hide();
        //$("#existingObjTable tr:contains('"+$(this).val()+"')").addClass("positive");
        //$("#existingObjTable tr:not(:contains('"+$(this).val()+"'))").removeClass("positive");
    });
    $.expr[':'].icontains = function(a,i,m) {
        return $(a).text().toUpperCase().indexOf(m[3].toUpperCase()) >= 0;
    }
</laser:script>

</body>
</html>

