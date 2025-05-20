<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.helper.Params; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.storage.RDStore;de.laser.storage.RDConstants;de.laser.Org;de.laser.OrgRole;de.laser.properties.PropertyDefinition;de.laser.Subscription;de.laser.finance.CostItem" %>
<%--<laser:serviceInjection />--%>
<ui:filter>
    <g:form action="${actionName}" controller="${controllerName}" method="get" class="ui small form">
        <g:if test="${license}">
            <input type="hidden" name="id" value="${license.id}"/>
        </g:if>
        <div class="four fields">
            <% /* 1-1 */ %>
            <div class="field">
                <%--
               <label>${message(code: 'default.search.text')}
                   <span data-position="right center" data-variation="tiny" data-content="${message(code:'default.search.tooltip.subscription')}">
                       <i class="${Icon.TOOLTIP.HELP}"></i>
                   </span>
               </label>
               <div class="ui input">
                   <input type="text" name="q"
                          placeholder="${message(code: 'default.search.ph')}"
                          value="${params.q}"/>
               </div>
               --%>
                <label>${message(code:'myinst.consortiaSubscriptions.consortia')}</label>
                <g:select class="ui search selection dropdown" name="member"
                          from="${filterConsortiaMembers}"
                          optionKey="id"
                          optionValue="${{ it.sortname + ' (' + it.name + ')'}}"
                          value="${params.member}"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>

            <g:if test="${'onlyMemberSubs' in tableConfig}">
                <div class="field">
                    <label>${message(code:'default.subscription.label')}</label>
                    <div class="ui search selection multiple dropdown" id="selSubscription">
                        <input type="hidden" name="selSubscription">
                        <i class="dropdown icon"></i>
                        <input type="text" class="search">
                        <div class="default text"><g:message code="default.select.all.label"/></div>
                    </div>
                </div>
            </g:if>
            <div class="field">
                <label for="identifier">${message(code: 'default.search.identifier')}</label>
                <div class="ui input">
                    <input type="text" id="identifier" name="identifier"
                           placeholder="${message(code: 'default.search.identifier.ph')}"
                           value="${params.identifier}"/>
                </div>
            </div>
            <div class="field">
                <ui:datepicker label="default.valid_on.label" id="validOn" name="validOn" placeholder="filter.placeholder" value="${validOn}" />
            </div>
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
            <div class="field">
                <label>${message(code: 'default.status.label')}</label>
                <%
                    def fakeList = []
                    fakeList.addAll(RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS))
                    fakeList.add(RefdataValue.getByValueAndCategory('subscription.status.no.status.set.but.null', 'filter.fake.values'))
                %>
                <ui:select class="ui dropdown clearable" name="status"
                              from="${ fakeList }"
                              optionKey="id"
                              optionValue="value"
                              value="${params.status}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
        </div>
        <div class="four fields">
            <% /* 2-1/2 */ %>
            <laser:render template="/templates/properties/genericFilter" model="[propList: filterPropList, label:message(code: 'subscription.property.search')]"/>
            <% /* 2-3 */ %>
            <div class="field">
                <label>${message(code:'subscription.form.label')}</label>
                <ui:select class="ui dropdown clearable" name="form"
                              from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_FORM)}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.form}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
            <% /* 2-4 */ %>
            <div class="field">
                <label for="subKinds">${message(code: 'myinst.currentSubscriptions.subscription_kind')}</label>
                <select id="subKinds" name="subKinds" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>
                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_KIND).sort{it.getI10n('value')}}" var="subKind">
                        <option <%=Params.getLongList(params, 'subKinds').contains(subKind.id) ? 'selected="selected"' : ''%>
                                value="${subKind.id}">
                            ${subKind.getI10n('value')}
                        </option>
                    </g:each>
                </select>
            </div>

        </div>
        <div class="four fields">
            <% /* 3-1 */ %>
            <div class="field">
                <label>${message(code:'subscription.isPublicForApi.label')}</label>
                <ui:select class="ui fluid dropdown" name="isPublicForApi"
                              from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.isPublicForApi}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
            <% /* 3-2 */ %>
            <div class="field">
                <label>${message(code:'subscription.hasPerpetualAccess.label')}</label>
                <ui:select class="ui fluid dropdown" name="hasPerpetualAccess"
                              from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.hasPerpetualAccess}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
            <% /* 3-3 */ %>
            <div class="field">
                <label>${message(code:'subscription.hasPublishComponent.label')}</label>
                <ui:select class="ui fluid dropdown" name="hasPublishComponent"
                              from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.hasPublishComponent}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
            <% /* 3-4 */ %>
            <div class="field">
                <label>${message(code:'subscription.resource.label')}</label>
                <ui:select class="ui dropdown clearable" name="resource"
                           from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_RESOURCE)}"
                           optionKey="id"
                           optionValue="value"
                           value="${params.resource}"
                           noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
        </div>

        <div class="four fields">
            <% /* 4-1 */ %>
            <div class="field">
                <g:if test="${'withCostItems' in tableConfig}">
                    <label for="filterPvd">${message(code: 'menu.my.providers')}</label>
                    <select id="filterPvd" name="filterPvd" multiple="" class="ui search selection fluid dropdown">
                        <option value="">${message(code: 'default.select.choose.label')}</option>

                        <g:each in="${providers}" var="provider">
                            <option <%=Params.getLongList(params, 'filterPvd').contains(provider.id) ? 'selected="selected"' : ''%>
                                    value="${provider.id}">
                                ${provider.name}
                            </option>
                        </g:each>
                    </select>
                </g:if>
            </div>
            <% /* 4-2 */ %>
            <div class="field">
                <g:if test="${'withCostItems' in tableConfig}">
                    <label for="filterVen">${message(code: 'menu.my.vendors')}</label>
                    <select id="filterVen" name="filterVen" multiple="" class="ui search selection fluid dropdown">
                        <option value="">${message(code: 'default.select.choose.label')}</option>

                        <g:each in="${vendors}" var="vendor">
                            <option <%=Params.getLongList(params, 'filterVen').contains(vendor.id) ? 'selected="selected"' : ''%>
                                    value="${vendor.id}">
                                ${vendor.name}
                            </option>
                        </g:each>
                    </select>
                </g:if>
            </div>
            <% /* 4-3 */ %>
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
        </div>
        <% /* 5-1 */ %>
            <div class="field la-field-right-aligned">
                <g:if test="${license && !request.forwardURI.contains(license.id.toString())}">
                    <g:set var="returnURL" value="${request.forwardURI+"/"+license.id}"/>
                </g:if>
                <g:else>
                    <g:set var="returnURL" value="${request.forwardURI}"/>
                </g:else>
                <a href="${returnURL}" class="${Btn.SECONDARY} reset">${message(code:'default.button.reset.label')}</a>
                <g:hiddenField name="filterSet" value="true"/>
                <input type="submit" class="${Btn.PRIMARY}" value="${message(code:'default.button.filter.label')}">
            </div>
    </g:form>
</ui:filter>

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.subStatus = "FETCH_ALL"
    if ($("#status").length > 0) {
        JSPC.app.subStatus = $("#status").val();
        if (JSPC.app.subStatus.length === 0) {
            JSPC.app.subStatus = "FETCH_ALL";
        }
    }
    <g:if test="${'onlyMemberSubs' in tableConfig}">
        <g:if test="${params.selSubscription}">
            $("#selSubscription").dropdown('set selected',[<g:each in="${params.selSubscription.split(',')}" var="sub" status="i">'${sub}'<g:if test="${i < params.selSubscription.split(',').size()-1}">,</g:if></g:each>]);
        </g:if>
        $("#selSubscription").dropdown({
            apiSettings: {
                url: "${createLink([controller:"ajaxJson", action:"lookupSubscriptions"])}?status="+ JSPC.app.subStatus +"&query={query}&ltype=${de.laser.interfaces.CalculatedType.TYPE_CONSORTIAL}",
                cache: false
            },
            clearable: true,
            minCharacters: 1
        });
    </g:if>
</laser:script>