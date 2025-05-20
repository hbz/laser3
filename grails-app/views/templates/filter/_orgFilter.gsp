<%@ page import="de.laser.survey.SurveyConfigSubscription; de.laser.survey.SurveyConfigVendor; de.laser.survey.SurveyConfigPackage; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.api.v0.ApiToolkit; de.laser.helper.Params; de.laser.utils.LocaleUtils; de.laser.I10nTranslation; de.laser.*; de.laser.auth.Role; de.laser.storage.RDConstants; de.laser.RefdataValue; de.laser.storage.RDStore" %>

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
                        <g:message code="org.search.contains"/>
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
                            <option <%=Params.getLongList(params, 'identifierNamespace').contains(idns.id) ? 'selected="selected"' : '' %> value="${idns.id}">${idns.getI10n("name") ?: idns.ns}</option>
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

            <g:if test="${field.equalsIgnoreCase('apiLevel')}">
                <div class="field">
                    <label for="osApiLevel">
                        <g:message code="org.apiLevel.label"/>
                    </label>
                    <select id="osApiLevel" name="osApiLevel" multiple="multiple" class="ui dropdown clearable multiple">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <g:each in="${ApiToolkit.getAllApiLevels()}" var="alf">
                            <option <%=(params.list('osApiLevel').contains(alf)) ? 'selected="selected"' : '' %> value="${alf}">
                                ${alf}
                            </option>
                        </g:each>
                    </select>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('serverAccess')}">
                <div class="field">
                    <label for="osServerAccess">
                        <g:message code="org.serverAccess.label"/>
                    </label>
                    <select id="osServerAccess" name="osServerAccess" multiple="multiple" class="ui dropdown clearable multiple">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <g:each in="${[OrgSetting.KEYS.NATSTAT_SERVER_ACCESS, OrgSetting.KEYS.OAMONITOR_SERVER_ACCESS, OrgSetting.KEYS.EZB_SERVER_ACCESS]}" var="saf">
                            <option <%=(params.list('osServerAccess').contains(saf.toString())) ? 'selected="selected"' : '' %> value="${saf}">
                                ${message(code:'org.setting.' + saf)}
                            </option>
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
                        <span data-position="right center" data-variation="tiny" class="la-popup-tooltip" data-content="${message(code:'org.search.contact.tooltip')}">
                            <i class="${Icon.TOOLTIP.HELP}"></i>
                        </span>
                    </label>
                    <input id="privateContact" name="privateContact" type="text" placeholder="${message(code: 'default.search.ph')}" value="${params.privateContact}"/>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('role')}">
                <div class="field">
                    <label for="orgRole">${message(code: 'org.orgRole.label')}</label>
                    <g:if test="${orgRoles == null || orgRoles.isEmpty()}">
                        %{--<g:set var="orgRoles" value="${RefdataCategory.getAllRefdataValues(RDConstants.ORGANISATIONAL_ROLE)}"/>--}%
                        <g:set var="orgRoles" value="${RefdataValue.executeQuery(getAllRefDataValuesForCategoryQuery, [category: RDConstants.ORGANISATIONAL_ROLE])}" scope="request"/>
                    </g:if>
                    <ui:select class="ui dropdown clearable search" id="orgRole" name="orgRole"
                                  from="${orgRoles}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${params.orgRole}"
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
                            <option <%=Params.getLongList(params, 'libraryNetwork').contains(rdv.id) ? 'selected="selected"' : '' %> value="${rdv.id}">${rdv.getI10n("value")}</option>
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
                            <option <%=Params.getLongList(params, 'libraryType').contains(rdv.id) ? 'selected="selected"' : '' %> value="${rdv.id}">${rdv.getI10n("value")}</option>
                        </g:each>
                    </select>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('country')}">
                <div class="field">
                    <label for="country">${message(code: 'org.country.label')}</label>
                    <g:set var="countries" value="${RefdataValue.executeQuery(getAllRefDataValuesForCategoryQuery, [category: RDConstants.COUNTRY])}" scope="request"/>
                    <ui:select class="ui dropdown clearable search" id="country" name="country"
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
                    <g:set var="legalObligations" value="${Org.executeQuery('select distinct(lob) from Org o inner join o.legallyObligedBy lob order by lob.sortname')}" scope="request"/>
                    <select id="legallyObligedBy" name="legallyObligedBy" multiple="" class="ui search select dropdown">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <g:each in="${legalObligations}" var="legalObligation">
                            <option <%=Params.getLongList(params, 'legallyObligedBy').contains(legalObligation.id) ? 'selected="selected"' : '' %> value="${legalObligation.id}">${legalObligation.sortname}</option>
                        </g:each>
                    </select>
                </div>
            </g:if>
            <g:if test="${field.equalsIgnoreCase('isBetaTester')}">
                <div class="field">
                    <label for="isBetaTester">${message(code:'org.isBetaTester.label')}</label>
                    <ui:select id="isBetaTester" name="isBetaTester"
                               from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                               optionKey="id"
                               optionValue="value"
                               class="ui dropdown clearable"
                               value="${params.isBetaTester}"
                               noSelection="${['' : message(code:'default.select.choose.label')]}"/>
                </div>
            </g:if>
            <g:if test="${field.equalsIgnoreCase('customerType')}">
                <div class="field">
                    <label for="customerType">${message(code:'org.customerType.label')}</label>
                    <select id="customerType" name="customerType" multiple="" class="ui dropdown clearable search">
                        <option value=""><g:message code="default.select.choose.label"/></option>
                        <g:each in="${[Role.findByAuthority('FAKE')] + Role.findAllByRoleType('org')}" var="rr">
                            <option <%=Params.getLongList(params, 'customerType').contains(rr.id) ? 'selected="selected"' : ''%> value="${rr.id}">${rr.getI10n('authority')}</option>
                        </g:each>
                    </select>
                </div>
            </g:if>
            <g:if test="${field.equalsIgnoreCase('providers')}">
                <div class="field">
                    <label for="filterPvd">${message(code: 'menu.my.providers')}</label>
                    <select id="filterPvd" name="filterPvd" multiple="" class="ui search selection fluid dropdown">
                        <option value="">${message(code: 'default.select.choose.label')}</option>

                        <g:each in="${providers.sort { it.name }}" var="provider">
                            <option <%=Params.getLongList(params, 'filterPvd').contains(provider.id) ? 'selected="selected"' : ''%>
                                    value="${provider.id}">
                                ${provider.name}
                            </option>
                        </g:each>
                    </select>
                </div>
            </g:if>
            <g:if test="${field.equalsIgnoreCase('subscription')}">
                <div class="field">
                    <label for="subscription">${message(code:'subscription')}</label>
                    <select id="subscription" name="subscription" multiple="" class="ui selection fluid dropdown">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <g:each in="${subscriptions}" var="sub">
                            <option <%=Params.getLongList(params, 'subscription').contains(sub.id) ? 'selected="selected"' : '' %> value="${sub.id}">${sub.dropdownNamingConvention()}</option>
                        </g:each>
                    </select>
                </div>
            </g:if>
            <g:if test="${field.equalsIgnoreCase('subStatus')}">
                <div class="field">
                    <label for="subStatus">${message(code:'subscription.status.label')}</label>
                    <select id="subStatus" name="subStatus" multiple="" class="ui selection fluid dropdown">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <option value="${RDStore.GENERIC_NULL_VALUE.id}" <%=Params.getLongList(params, 'subStatus').contains(RDStore.GENERIC_NULL_VALUE.id) ? 'selected="selected"' : '' %>>${message(code:'subscription.status.noSubscription')}</option>
                        <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS)}" var="subStatus">
                            <option <%=Params.getLongList(params, 'subStatus').contains(subStatus.id) ? 'selected="selected"' : '' %> value="${subStatus.id}">${subStatus.getI10n('value')}</option>
                        </g:each>
                    </select>
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
                            <option <%=Params.getLongList(params, 'subjectGroup').contains(rdv.id) ? 'selected="selected"' : '' %> value="${rdv.id}">${rdv.getI10n("value")}</option>
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
                            <option <%=Params.getLongList(params, 'discoverySystemsFrontend').contains(rdv.id) ? 'selected="selected"' : '' %> value="${rdv.id}">${rdv.getI10n("value")}</option>
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
                            <option <%=Params.getLongList(params, 'discoverySystemsIndex').contains(rdv.id) ? 'selected="selected"' : '' %> value="${rdv.id}">${rdv.getI10n("value")}</option>
                        </g:each>
                    </select>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('hasSubscription')}">
                <div class="field">
                    <label>${message(code: 'subscription.label')}</label>
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


            <g:if test="${field.equalsIgnoreCase('surveyVendors')}">
                <div class="field">
                    <label for="surveyVendors">${message(code: 'surveyVendors.label')}</label>
                    <select id="surveyVendors" name="surveyVendors" multiple="" class="ui selection fluid dropdown">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <g:set var="surveyVendors" value="${de.laser.survey.SurveyConfigVendor.executeQuery("select scv.vendor from SurveyConfigVendor scv where scv.surveyConfig = :surveyConfig order by scv.vendor.name asc", [surveyConfig: surveyConfig])}"/>
                        <g:each in="${surveyVendors}" var="surveyVendor">
                            <option <%=Params.getLongList(params, 'surveyVendors').contains(surveyVendor.id) ? 'selected="selected"' : '' %> value="${surveyVendor.id}">${surveyVendor.name}</option>
                        </g:each>
                    </select>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('surveyPackages')}">
                <div class="field">
                    <label for="surveyPackages">${message(code: 'surveyPackages.label')}</label>
                    <select id="surveyPackages" name="surveyPackages" multiple="" class="ui selection fluid dropdown">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <g:set var="surveyPackages" value="${SurveyConfigPackage.executeQuery("select scp.pkg from SurveyConfigPackage scp where scp.surveyConfig = :surveyConfig order by scp.pkg.name asc", [surveyConfig: surveyConfig])}"/>
                        <g:each in="${surveyPackages}" var="surveyPackage">
                            <option <%=Params.getLongList(params, 'surveyPackages').contains(surveyPackage.id) ? 'selected="selected"' : '' %> value="${surveyPackage.id}">${surveyPackage.name}</option>
                        </g:each>
                    </select>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('surveySubscriptions')}">
                <div class="field">
                    <label for="surveySubscriptions">${message(code: 'surveySubscriptions.selectedSubscriptions')}</label>
                    <select id="surveySubscriptions" name="surveySubscriptions" multiple="" class="ui selection fluid dropdown">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <g:set var="surveySubscriptions" value="${SurveyConfigSubscription.executeQuery("select scs.subscription from SurveyConfigSubscription scs where scs.surveyConfig = :surveyConfig order by scs.subscription.name asc", [surveyConfig: surveyConfig])}"/>
                        <g:each in="${surveySubscriptions}" var="surveySubscription">
                            <option <%=Params.getLongList(params, 'surveySubscriptions').contains(surveySubscription.id) ? 'selected="selected"' : '' %> value="${surveySubscription.id}">${surveySubscription.name}</option>
                        </g:each>
                    </select>
                </div>
            </g:if>

            <g:if test="${field.equalsIgnoreCase('subscriptionAdjustDropdown')}">
                <ui:greySegment>
                    <div class="two fields">
                        <div class="field">
                            <label for="status">${message(code: 'filter.status')}</label>
                            <ui:select class="ui search selection fluid dropdown" name="status" id="status"
                                       from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS)}"
                                       optionKey="id"
                                       optionValue="value"
                                       value="${RDStore.SUBSCRIPTION_CURRENT.id}"
                                       noSelection="${['': message(code: 'default.select.choose.label')]}"
                                       onchange="JSPC.app.adjustDropdown()"/>
                        </div>

                        <div class="field">
                            <label for="subs">${message(code: 'subscription.label')}</label>
                            <select id="subs" name="subs" class="ui fluid search selection dropdown multiple" multiple="multiple">
                                <option value="">${message(code: 'default.select.choose.label')}</option>
                            </select>
                        </div>
                    </div>
                </ui:greySegment>
            </g:if>

            <g:if test="${field.equals('')}">
                <div class="field"></div>
            </g:if>
        </g:each>
    <g:if test="${numberOfFields > 1}">
        </div><!-- .fields -->
    </g:if>

</g:each>

<div class="field la-field-right-aligned">

        <a href="${request.forwardURI}" class="${Btn.SECONDARY} reset">${message(code:'default.button.reset.label')}</a>

        <input name="filterSet" type="hidden" value="true">
        <g:if test="${tmplConfigFormFilter}">
            <input type="submit" value="${message(code:'default.button.filter.label')}" class="${Btn.PRIMARY}" onclick="JSPC.app.formFilter(event)" />
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
            <input type="submit" value="${message(code:'default.button.filter.label')}" class="${Btn.PRIMARY}"/>
        </g:else>

</div>

<g:if test="${tmplConfigShow && 'subscriptionAdjustDropdown' in tmplConfigShow.flatten()}">
<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.adjustDropdown = function () {
        var url = '<g:createLink controller="ajaxJson" action="adjustSubscriptionList"/>'

        url = url + '?'

        var status = $("select#status").serialize()
        if (status) {
            url = url + '&' + status
        }
    var selectedSubIds = [];
    <g:if test="${params.subs}">
        <g:each in="${params.list('subs')}" var="sub">
            <g:if test="${sub instanceof Subscription}">
                selectedSubIds.push(${sub.id});
            </g:if>
        </g:each>
    </g:if>

    var dropdownSelectedObjects = $('#subs');

    dropdownSelectedObjects.empty();
    dropdownSelectedObjects.append($('<option></option>').attr('value', '').text("${message(code: 'default.select.choose.label')}"));

    $.ajax({
            url: url,
            success: function (data) {
                $.each(data, function (key, entry) {
                <g:if test="${params.subs}">
                    if(jQuery.inArray(entry.value, selectedSubIds) >=0 ){
                       dropdownSelectedObjects.append($('<option></option>').attr('value', entry.value).attr('selected', 'selected').text(entry.text));
                    }else{
                        dropdownSelectedObjects.append($('<option></option>').attr('value', entry.value).text(entry.text));
                    }
                </g:if>
                <g:else>
                        dropdownSelectedObjects.append($('<option></option>').attr('value', entry.value).text(entry.text));
                </g:else>
                   });
                }
        });
    }

    JSPC.app.adjustDropdown();

</laser:script>
</g:if>

