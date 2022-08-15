<%@ page import="de.laser.Org; de.laser.RefdataCategory; de.laser.interfaces.CalculatedType;de.laser.helper.RDStore; de.laser.helper.RDConstants;de.laser.OrgRole;de.laser.RefdataValue;de.laser.properties.PropertyDefinition;de.laser.Subscription;de.laser.finance.CostItem" %>
<laser:serviceInjection />

<g:render template="/templates/filter/javascript" />
<semui:filter showFilterButton="true">
    <g:form action="${actionName}" controller="${controllerName}" method="get" class="ui small form clearing">
        <input type="hidden" name="isSiteReloaded" value="yes"/>
        <g:if test="${license}">
            <input type="hidden" name="id" value="${license.id}"/>
        </g:if>
        <g:if test="${actionName == 'subscriptionsManagement'}">
            <input type="hidden" name="tab" value="${params.tab}"/>
            <input type="hidden" name="propertiesFilterPropDef" value="${propertiesFilterPropDef}"/>
        </g:if>
        <div class="three fields">
            %{--<div class="four fields">--}%
            <% /* 1-1 */ %>
            <div class="field">
                <label for="search-title">${message(code: 'default.search.text')}
                    <span data-position="right center" class="la-popup-tooltip la-delay" data-content="${message(code:'default.search.tooltip.subscription')}">
                        <i class="question circle icon"></i>
                    </span>
                </label>

                <div class="ui input">
                    <input type="text" id="search-title" name="q"
                           placeholder="${message(code: 'default.search.ph')}"
                           value="${params.q}"/>
                </div>
            </div>
            <% /* 1-2 */ %>
            <div class="field">
                <label for="identifier">${message(code: 'default.search.identifier')}
                    <span data-position="right center" class="la-popup-tooltip la-delay" data-content="${message(code:'default.search.tooltip.subscription.identifier')}">
                        <i class="question circle icon"></i>
                    </span>
                </label>

                <div class="ui input">
                    <input type="text" id="identifier" name="identifier"
                           placeholder="${message(code: 'default.search.identifier.ph')}"
                           value="${params.identifier}"/>
                </div>
            </div>
            <% /* 1-3 */ %>
            <div class="field">
                <semui:datepicker label="default.valid_on.label" id="validOn" name="validOn" placeholder="filter.placeholder" value="${validOn}" />
            </div>
            <% /*
            <!-- 1-4 -->
            <div class="field disabled">
                <semui:datepicker label="myinst.currentSubscriptions.filter.renewalDate.label"  id="renewalDate" name="renewalDate"
                                  placeholder="filter.placeholder" value="${params.renewalDate}"/>
            </div>
            <!-- 1-5 -->
            <div class="field disabled">
                <semui:datepicker label="myinst.currentSubscriptions.filter.durationDateEnd.label"
                                  id="durationDate" name="durationDate" placeholder="filter.placeholder" value="${params.durationDate}"/>
            </div>
            */ %>
            <% /* 1-4 */ %>
            <div class="field">
                <label ><g:message code="default.status.label"/></label>
                <select id="status" name="status" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS)}" var="status">
                        <option <%=(params.list('status').contains(status.id.toString())) ? 'selected="selected"' : ''%>
                        value="${status.id}" ">
                        ${status.getI10n('value')}
                        </option>
                    </g:each>
                </select>
            </div>
        </div>

        <div class="four fields">

            <% /* 2-1 and 2-2 */ %>
            <g:render template="/templates/properties/genericFilter" model="[propList: propList, label:message(code: 'subscription.property.search')]"/>
            <%--
                        <!-- 2-1 -->
                        <div class="field disabled">
                            <label>${message(code: 'myinst.currentSubscriptions.filter.consortium.label')}</label>
                            <laser:select name="status" class="ui dropdown"
                                          from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS)}"
                                          optionKey="id"
                                          optionValue="value"
                                          value="${params.consortium}"
                                          noSelection="${['' : message(code:'default.select.choose.label')]}"/>

                        </div>
                        <!-- 2-2 -->
                        <div class="field disabled">
                            <label>${message(code: 'default.status.label')}</label>
                            <laser:select name="status" class="ui dropdown"
                                          from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS)}"
                                          optionKey="id"
                                          optionValue="value"
                                          value="${params.status}"
                                          noSelection="${['' : message(code:'default.select.choose.label')]}"/>
                        </div>

                       --%>
            <% /* 2-3 */ %>
            <div class="field">
                <label ><g:message code="subscription.form.label"/></label>
                <select id="form" name="form" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_FORM)}" var="form">
                        <option <%=(params.list('form').contains(form.id.toString())) ? 'selected="selected"' : ''%>
                        value="${form.id}" ">
                        ${form.getI10n('value')}
                        </option>
                    </g:each>
                </select>
            </div>
            <% /* 2-4 */ %>
            <div class="field">
                <label ><g:message code="subscription.resource.label"/></label>
                <select id="resource" name="resource" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_RESOURCE)}" var="resource">
                        <option <%=(params.list('resource').contains(resource.id.toString())) ? 'selected="selected"' : ''%>
                        value="${resource.id}" ">
                        ${resource.getI10n('value')}
                        </option>
                    </g:each>
                </select>
            </div>

        </div>

        <div class="four fields">
            <% /* 3-1 */ %>
            <div class="field">
                <label >${message(code: 'myinst.currentSubscriptions.subscription_kind')}</label>
                <select id="subKinds" name="subKinds" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_KIND)}" var="subKind">
                        <option <%=(params.list('subKinds').contains(subKind.id.toString())) ? 'selected="selected"' : ''%>
                        value="${subKind.id}" ">
                        ${subKind.getI10n('value')}
                        </option>
                    </g:each>
                </select>

            </div>
            <% /* 3-2 */ %>
            <div class="field">
                <label>${message(code:'subscription.isPublicForApi.label')}</label>
                <laser:select class="ui fluid dropdown" name="isPublicForApi"
                              from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.isPublicForApi}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
            <% /* 3-3 */ %>
            <div class="field">
                <label>${message(code:'subscription.hasPerpetualAccess.label')}</label>
                <laser:select class="ui fluid dropdown" name="hasPerpetualAccess"
                              from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.hasPerpetualAccess}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
            <% /* 3-4 */ %>
            <div class="field">
                <label>${message(code:'subscription.hasPublishComponent.label')}</label>
                <laser:select class="ui fluid dropdown" name="hasPublishComponent"
                              from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.hasPublishComponent}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
        </div>

        <div class="three fields">
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
            <% /* 4-2 */ %>
        <%-- TODO [ticket=2276] provisoric, name check is in order to prevent id mismatch --%>
            <g:if test="${!accessService.checkPerm("ORG_CONSORTIUM") || institution.globalUID == Org.findByName('LAS:eR Backoffice').globalUID}">
                <div class="field">
                    <fieldset id="subscritionType">
                        <label >${message(code: 'myinst.currentSubscriptions.subscription_type')}</label>
                        <div class="inline fields la-filter-inline">
                            <%
                                List subTypes = RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_TYPE)
                                if(institution.globalUID == Org.findByName('LAS:eR Backoffice').globalUID)
                                    subTypes -= RDStore.SUBSCRIPTION_TYPE_LOCAL
                                else
                                    subTypes -= RDStore.SUBSCRIPTION_TYPE_ADMINISTRATIVE
                            %>
                            <g:each in="${subTypes}" var="subType">
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
            </g:if>
            <g:else>
                <div class="field"></div>
            </g:else>

            <g:if test="${accessService.checkPerm("ORG_BASIC_MEMBER")}">
                <div class="field">
                    <fieldset>
                        <legend id="la-legend-searchDropdown">${message(code: 'gasco.filter.consortialAuthority')}</legend>

                        <g:select from="${allConsortia}" id="consortial" class="ui fluid search selection dropdown"
                                  optionKey="${{ Org.class.name + ':' + it.id }}"
                                  optionValue="${{ it.getName() }}"
                                  name="consortia"
                                  noSelection="${['' : message(code:'default.select.choose.label')]}"
                                  value="${params.consortia}"/>
                    </fieldset>
                </div>
            </g:if>
            <div class="field la-field-right-aligned">
                <a href="${createLink(controller:controllerName,action:actionName,params:[id:params.id,resetFilter:true, tab: params.tab])}" class="ui reset primary button">${message(code:'default.button.reset.label')}</a>
                <input type="submit" class="ui secondary button" value="${message(code:'default.button.filter.label')}">
            </div>

        </div>

    </g:form>
</semui:filter>