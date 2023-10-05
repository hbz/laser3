<%@ page import="de.laser.CustomerTypeService; de.laser.Org; de.laser.RefdataCategory; de.laser.interfaces.CalculatedType;de.laser.storage.RDStore; de.laser.storage.RDConstants;de.laser.OrgRole;de.laser.RefdataValue;de.laser.properties.PropertyDefinition;de.laser.Subscription;de.laser.finance.CostItem" %>
<laser:serviceInjection />

<ui:filter>
    [DEBUG: Support-Filter]
    <g:form action="${actionName}" controller="${controllerName}" method="get" class="ui small form clearing">
        <input type="hidden" name="isSiteReloaded" value="yes"/>
        <g:if test="${license}">
            <input type="hidden" name="id" value="${license.id}"/>
        </g:if>
        <g:if test="${actionName == 'subscriptionsManagement'}">
            <input type="hidden" name="tab" value="${params.tab}"/>
            <input type="hidden" name="propertiesFilterPropDef" value="${propertiesFilterPropDef}"/>
        </g:if>
        <div class="five fields">
            %{--<div class="four fields">--}%
            <% /* 1-1 */ %>
            <div class="field">
                <label for="search-title">${message(code: 'default.search.text')}</label>
                <div class="ui input">
                    <input type="text" id="search-title" name="q" placeholder="${message(code: 'default.search.ph')}" value="${params.q}"/>
                </div>
            </div>
            <% /* 1-2 */ %>
            <div class="field">
                <label for="identifier">${message(code: 'default.search.identifier')}</label>
                <div class="ui input">
                    <input type="text" id="identifier" name="identifier" placeholder="${message(code: 'default.search.identifier.ph')}" value="${params.identifier}"/>
                </div>
            </div>
            <% /* 1-3 */ %>
            <div class="field">
                <ui:datepicker label="default.valid_on.label" id="validOn" name="validOn" placeholder="filter.placeholder" value="${validOn}" />
            </div>
            <% /* 1-4 */ %>
            <div class="field">
                <label for="referenceYears">${message(code: 'subscription.referenceYear.label')}</label>
                <select id="referenceYears" name="referenceYears" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>
                    <g:each in="${referenceYears}" var="referenceYear">
                        <option <%=(params.list('referenceYears').contains(referenceYear.toString())) ? 'selected="selected"' : ''%>
                                value="${referenceYear}">
                            ${referenceYear}
                        </option>
                    </g:each>
                </select>
            </div>
            <% /* 1-5 */ %>
            <div class="field">
                <label for="status"><g:message code="default.status.label"/></label>
                <select id="status" name="status" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS)}" var="status">
                        <option <%=(params.list('status').contains(status.id.toString())) ? 'selected="selected"' : ''%>
                        value="${status.id}">
                        ${status.getI10n('value')}
                        </option>
                    </g:each>
                </select>
            </div>
        </div>

        <div class="four fields">

            <% /* 2-1 and 2-2 */ %>
            <laser:render template="/templates/properties/genericFilter" model="[propList: propList, label:message(code: 'subscription.property.search')]"/>

            <% /* 2-3 */ %>
            <div class="field">
                <label for="form"><g:message code="subscription.form.label"/></label>
                <select id="form" name="form" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_FORM)}" var="form">
                        <option <%=(params.list('form').contains(form.id.toString())) ? 'selected="selected"' : ''%>
                        value="${form.id}">
                        ${form.getI10n('value')}
                        </option>
                    </g:each>
                </select>
            </div>
            <% /* 2-4 */ %>
            <div class="field">
                <label for="resource"><g:message code="subscription.resource.label"/></label>
                <select id="resource" name="resource" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_RESOURCE)}" var="resource">
                        <option <%=(params.list('resource').contains(resource.id.toString())) ? 'selected="selected"' : ''%>
                        value="${resource.id}">
                        ${resource.getI10n('value')}
                        </option>
                    </g:each>
                </select>
            </div>

        </div>

        <div class="four fields">

            <div class="field">
                <label for="subKinds">${message(code: 'myinst.currentSubscriptions.subscription_kind')}</label>
                <select id="subKinds" name="subKinds" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_KIND)}" var="subKind">
                        <option <%=(params.list('subKinds').contains(subKind.id.toString())) ? 'selected="selected"' : ''%>
                        value="${subKind.id}">
                        ${subKind.getI10n('value')}
                        </option>
                    </g:each>
                </select>
            </div>

            <div class="field">
                <label>${message(code: 'myinst.currentSubscriptions.subscription.runTime')}</label>
                <div class="inline fields la-filter-inline">
                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkSubRunTimeMultiYear">${message(code: 'myinst.currentSubscriptions.subscription.runTime.multiYear')}</label>
                            <input id="checkSubRunTimeMultiYear" name="subRunTimeMultiYear" type="checkbox" <g:if test="${params.subRunTimeMultiYear}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>
                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkSubRunTimeNoMultiYear">${message(code: 'myinst.currentSubscriptions.subscription.runTime.NoMultiYear')}</label>
                            <input id="checkSubRunTimeNoMultiYear" name="subRunTime" type="checkbox" <g:if test="${params.subRunTime}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>
                </div>
            </div>

            <div class="field">
                <label></label>
                <fieldset id="subscritionType">
                    <label>${message(code: 'myinst.currentSubscriptions.subscription_type')}</label>
                    <div class="inline fields la-filter-inline">
                        <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_TYPE) - RDStore.SUBSCRIPTION_TYPE_LOCAL}" var="subType">
                            <div class="inline field">
                                <div class="ui checkbox">
                                    <label for="checkSubType-${subType.id}">${subType.getI10n('value')}</label>
                                    <input id="checkSubType-${subType.id}" name="subTypes" type="checkbox" value="${subType.id}"
                                        <g:if test="${params.list('subTypes').contains(subType.id.toString())}"> checked="" </g:if>
                                           tabindex="0">
                                </div>
                            </div>
                        </g:each>
                    </div>
                </fieldset>
            </div>

            <div class="field"></div>
        </div>

        <div class="field la-field-right-aligned">
            <a href="${createLink(controller:controllerName,action:actionName,params:[id:params.id,resetFilter:true, tab: params.tab])}" class="ui reset secondary button">${message(code:'default.button.reset.label')}</a>
            <input type="submit" class="ui primary button" value="${message(code:'default.button.filter.label')}">
        </div>

    </g:form>
</ui:filter>