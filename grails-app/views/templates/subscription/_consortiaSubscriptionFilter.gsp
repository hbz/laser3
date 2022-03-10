<%@ page import="de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.helper.RDStore;de.laser.helper.RDConstants;de.laser.Org;de.laser.OrgRole;de.laser.properties.PropertyDefinition;de.laser.Subscription;de.laser.finance.CostItem" %>
<%--<laser:serviceInjection />--%>

<g:render template="/templates/filter/javascript" />
<semui:filter showFilterButton="true">
    <g:form action="${actionName}" controller="${controllerName}" method="get" class="ui small form">
        <g:if test="${license}">
            <input type="hidden" name="id" value="${license.id}"/>
        </g:if>
        <div class="four fields">
            <div class="field">
                <%--
               <label>${message(code: 'default.search.text')}
                   <span data-position="right center" data-variation="tiny" data-content="${message(code:'default.search.tooltip.subscription')}">
                       <i class="question circle icon"></i>
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
                <semui:datepicker label="default.valid_on.label" id="validOn" name="validOn" placeholder="filter.placeholder" value="${validOn}" />
            </div>
            <div class="field">
                <label>${message(code: 'default.status.label')}</label>
                <%
                    def fakeList = []
                    fakeList.addAll(RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS))
                    fakeList.add(RefdataValue.getByValueAndCategory('subscription.status.no.status.set.but.null', 'filter.fake.values'))
                %>
                <laser:select class="ui dropdown" name="status"
                              from="${ fakeList }"
                              optionKey="id"
                              optionValue="value"
                              value="${params.status}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
        </div>
        <div class="four fields">
            <g:render template="/templates/properties/genericFilter" model="[propList: filterPropList, label:message(code: 'subscription.property.search')]"/>
            <div class="field">
                <label>${message(code:'subscription.form.label')}</label>
                <laser:select class="ui dropdown" name="form"
                              from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_FORM)}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.form}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
            <div class="field">
                <label>${message(code:'subscription.resource.label')}</label>
                <laser:select class="ui dropdown" name="resource"
                              from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_RESOURCE)}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.resource}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
        </div>
        <div class="four fields">
            <div class="field">
                <legend >${message(code: 'myinst.currentSubscriptions.subscription_kind')}</legend>
                <select id="subKinds" name="subKinds" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>
                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_KIND).sort{it.getI10n('value')}}" var="subKind">
                        <option <%=(params.list('subKinds').contains(subKind.id.toString())) ? 'selected="selected"' : ''%>
                        value="${subKind.id}" ">
                        ${subKind.getI10n('value')}
                        </option>
                    </g:each>
                </select>

            </div>
            <div class="field">
                <label>${message(code:'subscription.isPublicForApi.label')}</label>
                <laser:select class="ui fluid dropdown" name="isPublicForApi"
                              from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.isPublicForApi}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
            <div class="field">
                <label>${message(code:'subscription.hasPerpetualAccess.label')}</label>
                <laser:select class="ui fluid dropdown" name="hasPerpetualAccess"
                              from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.hasPerpetualAccess}"
                              noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>
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
            <g:if test="${institution.globalUID == Org.findByName('LAS:eR Backoffice').globalUID}">
                <div class="field">
                    <fieldset id="subscritionType">
                        <legend >${message(code: 'myinst.currentSubscriptions.subscription_type')}</legend>
                        <div class="inline fields la-filter-inline">
                            <%
                                List subTypes = RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_TYPE)
                                subTypes -= RDStore.SUBSCRIPTION_TYPE_LOCAL
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
            <div class="field la-field-right-aligned">
                <g:if test="${license && !request.forwardURI.contains(license.id.toString())}">
                    <g:set var="returnURL" value="${request.forwardURI+"/"+license.id}"/>
                </g:if>
                <g:else>
                    <g:set var="returnURL" value="${request.forwardURI}"/>
                </g:else>
                <a href="${returnURL}" class="ui reset primary button">${message(code:'default.button.reset.label')}</a>
                <g:hiddenField name="filterSet" value="true"/>
                <input type="submit" class="ui secondary button" value="${message(code:'default.button.filter.label')}">
            </div>

        </div>
    </g:form>
</semui:filter>

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.subStatus = "FETCH_ALL"
    if ($("#status").length > 0) {
        JSPC.app.subStatus = $("#status").val();
        if (JSPC.app.subStatus.length === 0) {
            JSPC.app.subStatus = "FETCH_ALL";
        }
    }
    <g:if test="${'onlyMemberSubs' in tableConfig && params.selSubscription}">
        $("#selSubscription").dropdown('set value',[<g:each in="${params.selSubscription.split(',')}" var="sub" status="i">'${sub}'<g:if test="${i < params.selSubscription.split(',').size()-1}">,</g:if></g:each>]);
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