<%@ page import="de.laser.ui.Btn; de.laser.base.AbstractReport; de.laser.helper.Params; de.laser.RefdataCategory; de.laser.storage.RDConstants; de.laser.storage.RDStore; de.laser.FilterService" %>

<ui:filter>
    <g:form controller="${controllerName}" action="${actionName}" method="get" class="ui form">
        <div class="three fields">
            <div class="field">
                <label for="q">${message(code:'default.search.text')}</label>
                <input type="text" id="q" name="q" placeholder="${message(code:'default.search.ph')}" value="${params.q}" />
            </div>

            <div class="field">
                <label for="provider">${message(code: 'provider.label')}</label>
                <div class="ui input">
                    <input type="text" id="provider" name="provider"
                           placeholder="${message(code: 'default.search.ph')}"
                           value="${params.provider}"/>
                </div>
            </div>

            <div class="field">
                <label for="platStatus">${message(code: 'default.status.label')}</label>
                <select name="platStatus" id="platStatus" multiple="multiple" class="ui search selection dropdown">
                    <option value="">${message(code:'default.select.choose.label')}</option>
                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.PLATFORM_STATUS)-RDStore.PLATFORM_STATUS_REMOVED}" var="platStatus">
                        <option <%=Params.getLongList(params, 'platStatus').contains(platStatus.id) ? 'selected=selected"' : ''%> value="${platStatus.id}">
                            ${platStatus.getI10n("value")}
                        </option>
                    </g:each>
                </select>
            </div>
        </div>

        <div class="four fields">
            <laser:render template="/templates/properties/genericFilter" model="[propList: propList, label:message(code: 'subscription.property.search')]"/>

            <div class="field">
                <label for="curatoryGroup">${message(code: 'package.curatoryGroup.label')}</label>
                <g:select class="ui fluid search select dropdown" name="curatoryGroup"
                          from="${curatoryGroups}"
                          optionKey="name"
                          optionValue="name"
                          value="${params.curatoryGroup}"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"/>
            </div>

            <div class="field">
                <label for="curatoryGroupType">${message(code: 'package.curatoryGroup.type')}</label>
                <g:select class="ui fluid search select dropdown" name="curatoryGroupType"
                          from="${curatoryGroupTypes}"
                          optionKey="value"
                          optionValue="name"
                          value="${params.curatoryGroupType}"
                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                />
            </div>
        </div>

        <g:if test="${controllerName == 'myInstitution'}">
            <div class="four fields">
                <div class="field">
                    <label for="status">${message(code:'subscription.status.label')}</label>
                    <select name="status" id="status" multiple="multiple" class="ui search selection dropdown">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS)}" var="status">
                            <option <%=(Params.getLongList(params, 'status').contains(status.id)) ? 'selected=selected"' : ''%> value="${status.id}">
                                ${status.getI10n("value")}
                            </option>
                        </g:each>
                    </select>
                </div>
                <div class="field">
                    <label for="hasPerpetualAccess">${message(code:'subscription.hasPerpetualAccess.label')}</label>
                    <ui:select class="ui fluid dropdown" name="hasPerpetualAccess"
                               from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                               optionKey="id"
                               optionValue="value"
                               value="${params.hasPerpetualAccess}"
                               noSelection="${['' : message(code:'default.select.choose.label')]}"/>
                </div>
                <div class="field"></div>
                <div class="field"></div>
            </div>
        </g:if>
        <g:elseif test="${actionName == 'list'}">
            <div class="four fields">
                <div class="field"></div>
                <div class="field"></div>
                <div class="field"></div>
                <div class="field">
                    <label for="isMyX"><g:message code="filter.isMyX.label" /></label>
                    <%
                        List<Map> isMyXOptions = []
                        isMyXOptions.add([ id: 'wekb_exclusive',    value: "${message(code:'filter.wekb.exclusive')}" ])
                        isMyXOptions.add([ id: 'wekb_not',          value: "${message(code:'filter.wekb.not')}" ])

                        //if (actionName == 'list') {
                        isMyXOptions.add([ id: 'ismyx_exclusive',   value: "${message(code:'filter.isMyX.exclusive', args:["${message(code:'menu.my.platforms')}"])}" ])
                        isMyXOptions.add([ id: 'ismyx_not',         value: "${message(code:'filter.isMyX.not')}" ])
                        //}
                    %>
                    <select id="isMyX" name="isMyX" class="ui selection fluid dropdown" multiple="">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <g:each in="${isMyXOptions}" var="opt">
                            <option <%=(params.list('isMyX').contains(opt.id)) ? 'selected="selected"' : '' %> value="${opt.id}">${opt.value}</option>
                        </g:each>
                    </select>
                </div>
            </div>
        </g:elseif>

        <div class="ui accordion">
            <div class="title">
                <i class="icon dropdown"></i>
                <g:message code="platform.filter.auth.title"/> <g:if test="${params.keySet().intersect(FilterService.PLATFORM_FILTER_AUTH_FIELDS.keySet()).size()}"><span class="ui circular label yellow">${params.keySet().intersect(FilterService.PLATFORM_FILTER_AUTH_FIELDS.keySet()).size()}</span></g:if>
            </div>
            <div class="content">
                <g:each in="${FilterService.PLATFORM_FILTER_AUTH_FIELDS}" var="authField" status="ia">
                    <g:if test="${ia % 2 == 0}">
                        <div class="two fields">
                    </g:if>
                    <div class="field">
                        <label for="${authField.getKey()}"><g:message code="${authField.getValue().label}"/></label>
                        <select name="${authField.getKey()}" id="${authField.getKey()}" multiple="" class="ui search selection dropdown">
                            <option value=""><g:message code="default.select.choose.label"/></option>

                            <g:each in="${RefdataCategory.getAllRefdataValues(authField.getValue().rdcat) + RDStore.GENERIC_NULL_VALUE}" var="auf">
                                <option <%=Params.getLongList(params, authField.getKey()).contains(auf.id) ? 'selected="selected"' : ''%>
                                        value="${auf.id}">
                                    ${auf.getI10n("value")}
                                </option>
                            </g:each>
                        </select>
                    </div>
                    <g:if test="${ia % 2 == 1 || ia == FilterService.PLATFORM_FILTER_AUTH_FIELDS.size()-1}">
                        </div>
                    </g:if>
                </g:each>
            </div>
        </div>

        <div class="ui accordion">
            <div class="title">
                <i class="icon dropdown"></i>
                <g:message code="platform.filter.stats.title"/> <g:if test="${params.keySet().intersect(['statisticsFormat', 'counterSupport', 'counterAPISupport']).size()}"><span class="ui circular label yellow">${params.keySet().intersect(['statisticsFormat', 'counterSupport', 'counterAPISupport']).size()}</span></g:if>
            </div>
            <div class="content">
                <div class="three fields">
                    <div class="field">
                        <label for="statisticsFormat">${message(code: 'platform.stats.format')}</label>
                        <select name="statisticsFormat" id="statisticsFormat" multiple="" class="ui search selection dropdown">
                            <option value="">${message(code: 'default.select.choose.label')}</option>

                            <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.PLATFORM_STATISTICS_FORMAT)+ RDStore.GENERIC_NULL_VALUE}" var="format">
                                <option <%=Params.getLongList(params, 'statisticsFormat').contains(format.id) ? 'selected="selected"' : ''%>
                                        value="${format.id}">
                                    ${format.getI10n("value")}
                                </option>
                            </g:each>
                        </select>
                    </div>
                    <div class="field">
                        <label for="counterSupport">${message(code: 'platform.stats.counter.supportedVersions')}</label>
                        <select name="counterSupport" id="counterSupport" multiple="" class="ui search selection dropdown">
                            <option value="">${message(code: 'default.select.choose.label')}</option>
                            <g:each in="${[(AbstractReport.COUNTER_4): 'COUNTER R4', (AbstractReport.COUNTER_5): 'COUNTER R5']}" var="revision">
                                <option <%=params.list('counterSupport').contains(revision.getKey()) ? 'selected="selected"' : ''%>
                                        value="${revision.getKey()}">
                                    ${revision.getValue()}
                                </option>
                            </g:each>
                        </select>
                    </div>
                    <div class="field">
                        <label for="counterAPISupport">${message(code: 'platform.stats.counter.supportedAPIVersions')}</label>
                        <select name="counterAPISupport" id="counterAPISupport" multiple="" class="ui search selection dropdown">
                            <option value="">${message(code: 'default.select.choose.label')}</option>
                            <g:each in="${[(AbstractReport.COUNTER_4): 'COUNTER API R4', (AbstractReport.COUNTER_5): 'COUNTER API R5']}" var="apiRevision">
                                <option <%=params.list('counterAPISupport').contains(apiRevision.getKey()) ? 'selected="selected"' : ''%>
                                        value="${apiRevision.getKey()}">
                                    ${apiRevision.getValue()}
                                </option>
                            </g:each>
                        </select>
                    </div>
                </div>
            </div>
        </div>

        <div class="ui accordion">
            <div class="title">
                <i class="icon dropdown"></i>
                <g:message code="platform.filter.accessibility.title"/> <g:if test="${params.keySet().intersect(FilterService.PLATFORM_FILTER_ACCESSIBILITY_FIELDS.keySet()).size()}"><span class="ui circular label yellow">${params.keySet().intersect(FilterService.PLATFORM_FILTER_ACCESSIBILITY_FIELDS.keySet()).size()}</span></g:if>
            </div>
            <div class="content">
                <g:each in="${FilterService.PLATFORM_FILTER_ACCESSIBILITY_FIELDS}" var="accessibilityField" status="ib">
                    <g:if test="${ib % 2 == 0}">
                        <div class="two fields">
                    </g:if>
                    <div class="field">
                        <label for="${accessibilityField.getKey()}"><g:message code="${accessibilityField.getValue().label}"/></label>
                        <select name="${accessibilityField.getKey()}" id="${accessibilityField.getKey()}" multiple="" class="ui search selection dropdown">
                            <option value=""><g:message code="default.select.choose.label"/></option>
                            <g:each in="${RefdataCategory.getAllRefdataValues(accessibilityField.getValue().rdcat) + RDStore.GENERIC_NULL_VALUE}" var="af">
                                <option <%=Params.getLongList(params, accessibilityField.getKey()).contains(af.id) ? 'selected="selected"' : ''%> value="${af.id}">
                                    ${af.getI10n("value")}
                                </option>
                            </g:each>
                        </select>
                    </div>
                    <g:if test="${ib % 2 == 1 || ib == FilterService.PLATFORM_FILTER_ACCESSIBILITY_FIELDS.size()-1}">
                        </div>
                    </g:if>
                </g:each>
            </div>
        </div>

        <div class="ui accordion">
            <div class="title">
                <i class="icon dropdown"></i>
                <g:message code="platform.filter.additional.title"/> <g:if test="${params.keySet().intersect(FilterService.PLATFORM_FILTER_ADDITIONAL_SERVICE_FIELDS.keySet()).size()}"><span class="ui circular label yellow">${params.keySet().intersect(FilterService.PLATFORM_FILTER_ADDITIONAL_SERVICE_FIELDS.keySet()).size()}</span></g:if>
            </div>
            <div class="content">
                <g:each in="${FilterService.PLATFORM_FILTER_ADDITIONAL_SERVICE_FIELDS}" var="additionalServiceField" status="is">
                    <g:if test="${is % 2 == 0}">
                        <div class="two fields">
                    </g:if>
                    <div class="field">
                        <label for="${additionalServiceField.getKey()}"><g:message code="${additionalServiceField.getValue().label}"/></label>
                        <select name="${additionalServiceField.getKey()}" id="${additionalServiceField.getKey()}" multiple="" class="ui search selection dropdown">
                            <option value=""><g:message code="default.select.choose.label"/></option>
                            <g:each in="${RefdataCategory.getAllRefdataValues(additionalServiceField.getValue().rdcat) + RDStore.GENERIC_NULL_VALUE}" var="asf">
                                <option <%=Params.getLongList(params, additionalServiceField.getKey()).contains(asf.id) ? 'selected="selected"' : ''%>
                                        value="${asf.id}">
                                    ${asf.getI10n("value")}
                                </option>
                            </g:each>
                        </select>
                    </div>
                    <g:if test="${is % 2 == 1 || is == FilterService.PLATFORM_FILTER_ADDITIONAL_SERVICE_FIELDS.size()-1}">
                        </div>
                    </g:if>
                </g:each>
            </div>
        </div>

        <div class="three fields">
            <div class="field"></div>
            <div class="field"></div>
            <div class="field la-field-right-aligned">
                <a href="${request.forwardURI}" class="${Btn.SECONDARY} reset">${message(code:'default.button.reset.label')}</a>
                <input type="submit" class="${Btn.PRIMARY}" name="filterSet" value="${message(code:'default.button.filter.label')}" />
            </div>
        </div>
    </g:form>
</ui:filter>