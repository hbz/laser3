<%@ page import="de.laser.utils.LocaleUtils; de.laser.I10nTranslation; de.laser.*; de.laser.auth.Role; de.laser.storage.RDConstants; de.laser.RefdataValue; de.laser.storage.RDStore" %>

<%
    String lang = LocaleUtils.getCurrentLang()
    String getAllRefDataValuesForCategoryQuery = "select rdv from RefdataValue as rdv where rdv.owner.desc=:category order by rdv.order, rdv.value_" + lang
%>

<g:each in="${tmplConfigShow}" var="row">

    <g:set var="numberOfFields" value="${row.size()}" />
    <% if (row.contains('country&region')) { numberOfFields++ } %>
    <% if (row.contains('property&value')) { numberOfFields++ } %>

    <g:if test="${numberOfFields > 1}">
        <div class="${numberOfFields==5 ? 'five fields' : numberOfFields==4 ? 'four fields' : numberOfFields==3 ? 'three fields' : numberOfFields==2 ? 'two fields' : ''}">
    </g:if>

        <g:each in="${row}" var="field" status="fieldCounter">

            <g:if test="${field.equalsIgnoreCase('name')}">
                <div class="field">
                    <label for="orgNameContains">
                        <g:if test="${actionName in ['listProvider', 'currentProviders']}">
                            <g:message code="org.search.provider.contains"/>
                            <span data-position="right center" data-variation="tiny" class="la-popup-tooltip la-delay" data-content="${message(code:'org.search.provider.contains.tooltip')}">
                                <i class="question circle icon"></i>
                            </span>
                        </g:if>
                        <g:else><g:message code="org.search.contains"/></g:else>
                    </label>
                    <input type="text" id="orgNameContains" name="orgNameContains"
                           placeholder="${message(code:'default.search.ph')}"
                           value="${params.orgNameContains}"/>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('identifier')}">
                <div class="field">
                    <label for="orgIdentifier">
                        <g:if test="${actionName in ['listInstitution', 'manageMembers']}">
                            ${message(code: 'org.institution.search.identifier')}
                        </g:if>
                        <g:else>
                            ${message(code: 'default.search.identifier')}
                        </g:else>
                    </label>
                    <div class="ui input">
                        <g:if test="${actionName in ['listInstitution', 'manageMembers']}">
                            <input type="text" id="orgIdentifier" name="orgIdentifier"
                                   placeholder="${message(code: 'org.institution.search.identifier.ph')}"
                                   value="${params.orgIdentifier}"/>
                        </g:if>
                        <g:else>
                            <input type="text" id="orgIdentifier" name="orgIdentifier"
                                   placeholder="${message(code: 'default.search.identifier.ph')}"
                                   value="${params.orgIdentifier}"/>
                        </g:else>
                    </div>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('identifierNamespace')}">
                <div class="field">
                    <label for="identifierNamespace">
                        <g:message code="org.institution.search.identifier.namespace"/>
                    </label>
                    <select id="identifierNamespace" name="identifierNamespace" multiple="" class="ui selection fluid dropdown">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <g:set var="identifierNamespaces" value="${IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS, [sort: 'name_de'])}" scope="request"/>
                        <g:each in="${identifierNamespaces}" var="idns">
                            <option <%=(params.list('identifierNamespace').contains(idns.id.toString())) ? 'selected="selected"' : '' %> value="${idns.id}">${idns.getI10n("name") ?: idns.ns}</option>
                        </g:each>
                    </select>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('customerIDNamespace')}">
                <div class="field">
                    <label for="customerIDNamespace">
                        <g:message code="org.institution.search.customer.identifier.namespace"/>
                    </label>
                    <select id="customerIDNamespace" name="customerIDNamespace" multiple="" class="ui selection fluid dropdown">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <g:each in="${[[id: 'value', value: message(code: 'org.customerIdentifier')], [id: 'requestorKey', value: message(code: 'org.requestorKey')]]}" var="cust">
                            <option <%=(params.list('customerIDNamespace').contains(cust.id)) ? 'selected="selected"' : '' %> value="${cust.id}">${cust.value}</option>
                        </g:each>
                    </select>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('isMyX')}">
                <div class="field">
                    <label for="isMyX">
                        <g:message code="filter.isMyX.label" />
                    </label>
                    <%
                        List<Map> isMyXOptions = []

                        if (actionName == 'listInstitution') {
                            isMyXOptions.add([ id: 'ismyx_exclusive',   value: "${message(code:'filter.isMyX.exclusive', args:["${message(code:'menu.my.insts')}"])}" ])
                            isMyXOptions.add([ id: 'ismyx_not',         value: "${message(code:'filter.isMyX.not')}" ])
                        }
                        else if (actionName == 'listConsortia') {
                            isMyXOptions.add([ id: 'ismyx_exclusive',   value: "${message(code:'filter.isMyX.exclusive', args:["${message(code:'menu.my.consortia')}"])}" ])
                            isMyXOptions.add([ id: 'ismyx_not',         value: "${message(code:'filter.isMyX.not')}" ])
                        }
                        else if (actionName == 'listProvider') {
                            isMyXOptions.add([ id: 'wekb_exclusive',    value: "${message(code:'filter.wekb.exclusive')}" ])
                            isMyXOptions.add([ id: 'wekb_not',          value: "${message(code:'filter.wekb.not')}" ])
                            isMyXOptions.add([ id: 'ismyx_exclusive',   value: "${message(code:'filter.isMyX.exclusive', args:["${message(code:'menu.my.providers')}"])}" ])
                            isMyXOptions.add([ id: 'ismyx_not',         value: "${message(code:'filter.isMyX.not')}" ])
                        }
                        else if (actionName == 'currentProviders') {
                            isMyXOptions.add([ id: 'wekb_exclusive',    value: "${message(code:'filter.wekb.exclusive')}" ])
                            isMyXOptions.add([ id: 'wekb_not',          value: "${message(code:'filter.wekb.not')}" ])
                        }
                    %>
                    <select id="isMyX" name="isMyX" class="ui selection fluid dropdown" multiple="">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <g:each in="${isMyXOptions}" var="opt">
                            <option <%=(params.list('isMyX').contains(opt.id)) ? 'selected="selected"' : '' %> value="${opt.id}">${opt.value}</option>
                        </g:each>
                    </select>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('property&value')}">
                <laser:render template="/templates/properties/genericFilter" model="[propList: propList, label:message(code: 'subscription.property.search')]"/>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('privateContacts')}">
                <div class="field">
                    <label for="privateContact">
                        <g:message code="contact.name"/>
                        <span data-position="right center" data-variation="tiny" class="la-popup-tooltip la-delay" data-content="${message(code:'org.search.contact.tooltip')}">
                            <i class="question circle icon"></i>
                        </span>
                    </label>
                    <input id="privateContact" name="privateContact" type="text" placeholder="${message(code: 'default.search.ph')}" value="${params.privateContact}"/>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('type')}">
                <div class="field">
                    <label for="orgType">${message(code: 'org.orgType.label')}</label>
                    <g:if test="${orgTypes == null || orgTypes.isEmpty()}">
                        <g:set var="orgTypes" value="${RefdataValue.executeQuery(getAllRefDataValuesForCategoryQuery, [category: RDConstants.ORG_TYPE])}" scope="request"/>
                    </g:if>
                    <ui:select class="ui dropdown search" id="orgType" name="orgType"
                                  from="${orgTypes}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${params.orgType}"
                                  noSelection="${['':message(code:'default.select.choose.label')]}"/>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('orgStatus')}">
                <div class="field">
                    <label for="orgStatus">${message(code: 'default.status.label')}</label>
                    <g:if test="${orgStatusSet == null || orgStatusSet.isEmpty()}">
                        <g:set var="orgStatusSet" value="${RefdataCategory.getAllRefdataValues([RDConstants.ORG_STATUS])-RDStore.ORG_STATUS_REMOVED}" scope="request"/>
                    </g:if>
                    <ui:select class="ui dropdown multiple search selection" id="orgStatus" name="orgStatus"
                               from="${orgStatusSet}" optionKey="id" optionValue="value" value="${params.orgStatus}" />
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('role')}">
                <div class="field">
                    <label for="orgRole">${message(code: 'org.orgRole.label')}</label>
                    <g:if test="${orgRoles == null || orgRoles.isEmpty()}">
                        %{--<g:set var="orgRoles" value="${RefdataCategory.getAllRefdataValues(RDConstants.ORGANISATIONAL_ROLE)}"/>--}%
                        <g:set var="orgRoles" value="${RefdataValue.executeQuery(getAllRefDataValuesForCategoryQuery, [category: RDConstants.ORGANISATIONAL_ROLE])}" scope="request"/>
                    </g:if>
                    <ui:select class="ui dropdown search" id="orgRole" name="orgRole"
                                  from="${orgRoles}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${params.orgRole}"
                                  noSelection="${['':message(code:'default.select.choose.label')]}"/>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('providerRole')}">
                <div class="field">
                    <label for="providerRole">${message(code: 'org.orgRole.label')}</label>
                    <%
                        Set<RefdataValue> providerRoles = [
                                RDStore.OT_BROKER, RDStore.OT_CONTENT_PROVIDER, RDStore.OT_IMPRINT, RDStore.OT_ISSUING_BODY,
                                RDStore.OT_LICENSEE, RDStore.OT_LICENSOR, RDStore.OT_PLATFORM_PROVIDER, RDStore.OT_PUBLISHER, RDStore.OT_VENDOR
                        ]
                    %>
                    <select name="providerRole" id="providerRole" multiple="" class="ui fluid select dropdown search">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <g:each in="${providerRoles}" var="providerRole">
                            <option <%= (params.list('providerRole').contains(providerRole.id.toString())) ? 'selected="selected"' : '' %> value="${providerRole.id}">${providerRole.value}</option>
                        </g:each>
                    </select>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('sector')}">
                <div class="field">
                    <label for="orgSector">${message(code: 'org.sector.label')}</label>
                    <g:set var="orgSectors" value="${RefdataValue.executeQuery(getAllRefDataValuesForCategoryQuery, [category: RDConstants.ORG_SECTOR])}" scope="request"/>
                    <ui:select class="ui dropdown search" id="orgSector" name="orgSector"
                                  from="${orgSectors}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${params.orgSector}"
                                  noSelection="${['':message(code:'default.select.choose.label')]}"/>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('country&region')}">
                <laser:render template="/templates/filter/orgRegionsFilter" />
            </g:if>

            <g:if test="${field.equalsIgnoreCase('libraryNetwork')}">
                <div class="field">
                    <label for="libraryNetwork">${message(code: 'org.libraryNetwork.label')}</label>
                    <select id="libraryNetwork" name="libraryNetwork" multiple="" class="ui selection fluid dropdown">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <g:set var="libraryNetworks" value="${RefdataValue.executeQuery(getAllRefDataValuesForCategoryQuery, [category: RDConstants.LIBRARY_NETWORK])}" scope="request"/>
                        <g:each in="${libraryNetworks}" var="rdv">
                            <option <%=(params.list('libraryNetwork').contains(rdv.id.toString())) ? 'selected="selected"' : '' %> value="${rdv.id}">${rdv.getI10n("value")}</option>
                        </g:each>
                    </select>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('libraryType')}">
                <div class="field">
                    <label for="libraryType">${message(code: 'org.libraryType.label')}</label>
                    <select id="libraryType" name="libraryType" multiple="" class="ui selection fluid dropdown">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <g:set var="libraryTypes" value="${RefdataValue.executeQuery(getAllRefDataValuesForCategoryQuery, [category: RDConstants.LIBRARY_TYPE])}" scope="request"/>
                        <g:each in="${libraryTypes}" var="rdv">
                            <option <%=(params.list('libraryType').contains(rdv.id.toString())) ? 'selected="selected"' : '' %> value="${rdv.id}">${rdv.getI10n("value")}</option>
                        </g:each>
                    </select>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('country')}">
                <div class="field">
                    <label for="country">${message(code: 'org.country.label')}</label>
                    <g:set var="countries" value="${RefdataValue.executeQuery(getAllRefDataValuesForCategoryQuery, [category: RDConstants.COUNTRY])}" scope="request"/>
                    <ui:select class="ui dropdown search" id="country" name="country"
                                  from="${countries}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${params.country}"
                                  noSelection="${['':message(code:'default.select.choose.label')]}"/>
                </div>
            </g:if>
            <g:if test="${field.equalsIgnoreCase('isLegallyObliged')}">
                <div class="field">
                    <label for="isLegallyObliged">${message(code: 'org.isLegallyObliged.label')}</label>
                    <g:set var="isLegallyObligedOptions" value="${['yes': message(code:'org.isLegallyObliged.yes.label'), 'no': message(code:'org.isLegallyObliged.no.label')]}" scope="request"/>
                    <select id="isLegallyObliged" name="isLegallyObliged" class="ui search select dropdown">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <g:each in="${isLegallyObligedOptions}" var="iloo">
                            <option <%=(params.isLegallyObliged == iloo.key) ? 'selected="selected"' : '' %> value="${iloo.key}">${iloo.value}</option>
                        </g:each>
                    </select>
                </div>
            </g:if>
            <g:if test="${field.equalsIgnoreCase('isLegallyObligedBy')}">
                <div class="field">
                    <label for="legallyObligedBy">${message(code: 'org.legallyObligedBy.label')}</label>
                    <g:set var="legalObligations" value="${Org.executeQuery('select distinct(lob) from Org o inner join o.legallyObligedBy lob where o.status != :removed order by lob.sortname', [removed: RDStore.ORG_STATUS_REMOVED])}" scope="request"/>
                    <select id="legallyObligedBy" name="legallyObligedBy" multiple="" class="ui search select dropdown">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <g:each in="${legalObligations}" var="legalObligation">
                            <option <%=(params.list('legallyObligedBy').contains(legalObligation.id.toString())) ? 'selected="selected"' : '' %> value="${legalObligation.id}">${legalObligation.sortname}</option>
                        </g:each>
                    </select>
                </div>
            </g:if>
            <g:if test="${field.equalsIgnoreCase('customerType')}">
                <div class="field">
                    <label for="customerType">${message(code:'org.customerType.label')}</label>
                    <ui:select id="customerType" name="customerType"
                                  from="${[Role.findByAuthority('FAKE')] + Role.findAllByRoleType('org')}"
                                  optionKey="id"
                                  optionValue="authority"
                                  value="${params.customerType}"
                                  class="ui dropdown"
                                  noSelection="${['':message(code:'default.select.choose.label')]}"
                    />
                </div>
            </g:if>
            <g:if test="${field.equalsIgnoreCase('providers')}">
                <div class="field">
                    <label for="filterPvd">${message(code: 'menu.my.providers')}</label>
                    <select id="filterPvd" name="filterPvd" multiple="" class="ui search selection fluid dropdown">
                        <option value="">${message(code: 'default.select.choose.label')}</option>

                        <g:each in="${providers.sort { it.name }}" var="provider">
                            <option <%=(params.list('filterPvd').contains(provider.id.toString())) ? 'selected="selected"' : ''%>
                                    value="${provider.id}">
                                ${provider.name}
                            </option>
                        </g:each>
                    </select>
                </div>
            </g:if>
            <g:if test="${field.equalsIgnoreCase('curatoryGroup')}">
                <div class="field">
                    <label for="curatoryGroup">${message(code: 'package.curatoryGroup.label')}</label>
                    <g:select class="ui fluid search select dropdown" name="curatoryGroup"
                              from="${curatoryGroups.sort{it.name.toLowerCase()}}"
                              optionKey="name"
                              optionValue="name"
                              value="${params.curatoryGroup}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
                </div>
            </g:if>
            <g:if test="${field.equalsIgnoreCase('subscription')}">
                <div class="field">
                    <label for="subscription">${message(code:'subscription')}</label>
                    <select id="subscription" name="subscription" multiple="" class="ui selection fluid dropdown">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <g:each in="${subscriptions}" var="sub">
                            <option <%=(params.list('subscription').contains(sub.id.toString())) ? 'selected="selected"' : '' %> value="${sub.id}">${sub.dropdownNamingConvention()}</option>
                        </g:each>
                    </select>
                </div>
            </g:if>
            <g:if test="${field.equalsIgnoreCase('subStatus')}">
                <div class="field">
                    <label for="subStatus">${message(code:'subscription.status.label')}</label>
                    <ui:select id="subStatus" name="subStatus"
                                  from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS)}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${params.subStatus}"
                                  class="ui dropdown"
                                  noSelection="${['':message(code:'default.select.choose.label')]}"
                    />
                    <%--<select id="subscriptionStatus" name="subscription" multiple="" class="ui selection fluid dropdown">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <g:each in="${subscriptions}" var="sub">
                            <option <%=(params.list('subscription').contains(sub.id.toString())) ? 'selected="selected"' : '' %> value="${sub.id}">${sub.dropdownNamingConvention()}</option>
                        </g:each>
                    </select>--%>
                </div>
            </g:if>
            <g:if test="${field.equalsIgnoreCase('subValidOn')}">
                <div class="field">
                    <ui:datepicker label="default.valid_on.label" id="subValidOn" name="subValidOn" placeholder="filter.placeholder" value="${params.subValidOn}" />
                </div>
            </g:if>
            <g:if test="${field.equalsIgnoreCase('subPerpetualAccess')}">
                <div class="field">
                    <div class="inline fields la-filter-inline">
                        <div class="inline field">
                            <div class="ui checkbox">
                                <label for="checkSubPerpetual">${message(code: 'subscription.hasPerpetualAccess.label')}</label>
                                <input id="checkSubPerpetual" name="subPerpetual" type="checkbox" <g:if test="${params.subPerpetual == "on"}">checked=""</g:if>
                                       tabindex="0">
                            </div>
                        </div>
                    </div>
                </div>
            </g:if>
            <g:if test="${field.equalsIgnoreCase('subRunTimeMultiYear')}">
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
            </g:if>

            <g:if test="${field.equalsIgnoreCase('platform')}">
                <div class="field">
                    <label for="platform"><g:message code="platform.name"/></label>
                    <input id="platform" name="platform" type="text" placeholder="${message(code: 'default.search.ph')}" value="${params.platform}"/>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('subjectGroup')}">
                <div class="field">
                    <label for="subjectGroup">${message(code: 'org.subjectGroup.label')}</label>
                    <select id="subjectGroup" name="subjectGroup" multiple="" class="ui selection fluid dropdown">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <g:set var="subjectGroups" value="${RefdataValue.executeQuery(getAllRefDataValuesForCategoryQuery, [category: RDConstants.SUBJECT_GROUP])}" scope="request"/>
                        <g:each in="${subjectGroups}" var="rdv">
                            <option <%=(params.list('subjectGroup').contains(rdv.id.toString())) ? 'selected="selected"' : '' %> value="${rdv.id}">${rdv.getI10n("value")}</option>
                        </g:each>
                    </select>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('discoverySystemsFrontend')}">
                <div class="field">
                    <label for="discoverySystemsFrontend">${message(code: 'org.discoverySystems.frontend.label')}</label>
                    <select id="discoverySystemsFrontend" name="discoverySystemsFrontend" multiple="" class="ui selection fluid dropdown">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <g:set var="frontends" value="${RefdataValue.executeQuery(getAllRefDataValuesForCategoryQuery, [category: RDConstants.DISCOVERY_SYSTEM_FRONTEND])}" scope="request"/>
                        <g:each in="${frontends}" var="rdv">
                            <option <%=(params.list('discoverySystemsFrontend').contains(rdv.id.toString())) ? 'selected="selected"' : '' %> value="${rdv.id}">${rdv.getI10n("value")}</option>
                        </g:each>
                    </select>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('discoverySystemsIndex')}">
                <div class="field">
                    <label for="discoverySystemsIndex">${message(code: 'org.discoverySystems.index.label')}</label>
                    <select id="discoverySystemsIndex" name="discoverySystemsIndex" multiple="" class="ui selection fluid dropdown">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <g:set var="indices" value="${RefdataValue.executeQuery(getAllRefDataValuesForCategoryQuery, [category: RDConstants.DISCOVERY_SYSTEM_INDEX])}" scope="request"/>
                        <g:each in="${indices}" var="rdv">
                            <option <%=(params.list('discoverySystemsIndex').contains(rdv.id.toString())) ? 'selected="selected"' : '' %> value="${rdv.id}">${rdv.getI10n("value")}</option>
                        </g:each>
                    </select>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('hasSubscription')}">
                <div class="field">
                    <div class="inline fields la-filter-inline">
                        <div class="inline field">
                            <div class="ui checkbox">
                                <label for="checkHasSubscription">${message(code: 'surveyEvaluation.filter.hasSubscription')}</label>
                                <input id="checkHasSubscription" name="hasSubscription" type="checkbox" <g:if test="${params.hasSubscription == "on"}">checked=""</g:if>
                                       tabindex="0">
                            </div>
                        </div>
                        <div class="inline field">
                            <div class="ui checkbox">
                                <label for="checkHasNotSubscription">${message(code: 'surveyEvaluation.filter.hasNotSubscription')}</label>
                                <input id="checkHasNotSubscription" name="hasNotSubscription" type="checkbox" <g:if test="${params.hasNotSubscription == "on"}">checked=""</g:if>
                                       tabindex="0">
                            </div>
                        </div>
                    </div>
                </div>
            </g:if>


        </g:each>
    <g:if test="${numberOfFields > 1}">
        </div><!-- .fields -->
    </g:if>

</g:each>

<div class="field la-field-right-aligned">

        <a href="${request.forwardURI}" class="ui reset secondary button">${message(code:'default.button.reset.label')}</a>

        <input name="filterSet" type="hidden" value="true">
        <g:if test="${tmplConfigFormFilter}">
            <input type="submit" value="${message(code:'default.button.filter.label')}" class="ui primary button" onclick="JSPC.app.formFilter(event)" />
            <laser:script file="${this.getGroovyPageFileName()}">
                JSPC.app.formFilter = function (e) {
                    e.preventDefault()

                    var form = $(e.target).parents('form')
                    $(form).find(':input').filter(function () {
                        return !this.value
                    }).attr('disabled', 'disabled')

                    form.submit()
                }
            </laser:script>
        </g:if>
        <g:else>
            <input type="submit" value="${message(code:'default.button.filter.label')}" class="ui primary button"/>
        </g:else>

</div>


