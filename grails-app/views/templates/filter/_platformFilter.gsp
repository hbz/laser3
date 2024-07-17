<%@ page import="de.laser.ui.Button; de.laser.base.AbstractReport; de.laser.helper.Params; de.laser.RefdataCategory; de.laser.storage.RDConstants; de.laser.storage.RDStore" %>

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

            <laser:render template="/templates/properties/genericFilter" model="[propList: propList, label:message(code: 'subscription.property.search')]"/>

            <div class="field">
                <label for="platStatus">${message(code: 'default.status.label')}</label>
                <select name="platStatus" id="platStatus" multiple="multiple" class="ui search selection dropdown">
                    <option value="">${message(code:'default.select.choose.label')}</option>
                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.PLATFORM_STATUS)}" var="platStatus">
                        <option <%=Params.getLongList(params, 'platStatus').contains(platStatus.id) ? 'selected=selected"' : ''%> value="${platStatus.id}">
                            ${platStatus.getI10n("value")}
                        </option>
                    </g:each>
                </select>
            </div>
        </div>

        <div class="three fields">
            <div class="field">
                <label for="ipSupport">${message(code: 'platform.auth.ip.supported')}</label>
                <select name="ipSupport" id="ipSupport" multiple="" class="ui search selection dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.IP_AUTHENTICATION)}" var="ip">
                        <option <%=Params.getLongList(params, 'ipSupport').contains(ip.id) ? 'selected="selected"' : ''%>
                                value="${ip.id}">
                            ${ip.getI10n("value")}
                        </option>
                    </g:each>
                </select>
            </div>

            <div class="field">
                <label for="shibbolethSupport">${message(code: 'platform.auth.shibboleth.supported')}</label>
                <select name="shibbolethSupport" id="shibbolethSupport" multiple="" class="ui search selection dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)+ RDStore.GENERIC_NULL_VALUE}" var="shibboleth">
                        <option <%=Params.getLongList(params, 'shibbolethSupport').contains(shibboleth.id) ? 'selected="selected"' : ''%>
                                value="${shibboleth.id}">
                            ${shibboleth.getI10n("value")}
                        </option>
                    </g:each>
                </select>
            </div>

            <div class="field">
                <label for="counterCertified">${message(code: 'platform.stats.counter.certified')}</label>
                <select name="counterCertified" id="counterCertified" multiple="" class="ui search selection dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)+ RDStore.GENERIC_NULL_VALUE}" var="counter">
                        <option <%=Params.getLongList(params, 'counterCertified').contains(counter.id) ? 'selected="selected"' : ''%>
                                value="${counter.id}">
                            ${counter.getI10n("value")}
                        </option>
                    </g:each>
                </select>
            </div>
        </div>

        <div class="three fields">
            <div class="field">
                <label for="counterSushiSupport">${message(code: 'platform.stats.counter.supportedVersions')}</label>
                <select name="counterSushiSupport" id="counterSushiSupport" multiple="" class="ui search selection dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>
                    <g:each in="${[(AbstractReport.COUNTER_4): 'COUNTER Sushi API R4', (AbstractReport.COUNTER_5): 'COUNTER Sushi API R5']}" var="revision">
                        <option <%=params.list('counterSushiSupport').contains(revision.getKey()) ? 'selected="selected"' : ''%>
                                value="${revision.getKey()}">
                            ${revision.getValue()}
                        </option>
                    </g:each>
                </select>
            </div>

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


        <div class="three fields">
            <div class="field">
                <g:if test="${controllerName == 'myInstitution'}">
                    <label for="status">${message(code:'subscription.status.label')}</label>
                    <select name="status" id="status" multiple="multiple" class="ui search selection dropdown">
                        <option value="">${message(code:'default.select.choose.label')}</option>
                        <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS)}" var="status">
                            <option <%=(Params.getLongList(params, 'status').contains(status.id)) ? 'selected=selected"' : ''%> value="${status.id}">
                                ${status.getI10n("value")}
                            </option>
                        </g:each>
                    </select>
                </g:if>
            </div>
            <div class="field">
                <g:if test="${controllerName == 'myInstitution'}">
                    <label for="hasPerpetualAccess">${message(code:'subscription.hasPerpetualAccess.label')}</label>
                    <ui:select class="ui fluid dropdown" name="hasPerpetualAccess"
                               from="${RefdataCategory.getAllRefdataValues(RDConstants.Y_N)}"
                               optionKey="id"
                               optionValue="value"
                               value="${params.hasPerpetualAccess}"
                               noSelection="${['' : message(code:'default.select.choose.label')]}"/>
                </g:if>
            </div>
            <g:if test="${actionName == 'list'}">
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
            </g:if>
        </div>

        <div class="three fields">
            <div class="field"></div>
            <div class="field"></div>
            <div class="field la-field-right-aligned">
                <a href="${request.forwardURI}" class="${Button.SECONDARY} reset">${message(code:'default.button.reset.label')}</a>
                <input type="submit" class="${Button.PRIMARY}" name="filterSet" value="${message(code:'default.button.filter.label')}" />
            </div>
        </div>
    </g:form>
</ui:filter>