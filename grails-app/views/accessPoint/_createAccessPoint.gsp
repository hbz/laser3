<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore" %>
<laser:serviceInjection/>

    <ui:form controller="accessPoint" action="processCreate" id="${orgInstance.id}">
        <g:hiddenField name="accessMethod" value="${accessMethod.id}" />

        <g:if test="${accessMethod == RDStore.ACCESS_POINT_TYPE_IP}">
            <laser:render template="name" model="${[nameOptions: availableOptions.collectEntries(),
                                                name: availableOptions.first().values().first(),
                                                accessMethod: accessMethod]}"/>
        </g:if>
        <g:elseif test="${accessMethod == RDStore.ACCESS_POINT_TYPE_EZPROXY}">
            <laser:render template="name" model="${[nameOptions: [], name: '']}"/>
            <div class="field required">
                <label>URL
                    <span class="la-long-tooltip la-popup-tooltip"
                          data-content="${message(code: "accessPoint.url.help")}">
                        <i class="${Icon.TOOLTIP.HELP} la-popup"></i></span> <g:message code="messageRequiredField" />
                </label>
                <g:textField name="url" value="${url}" />
            </div>
        </g:elseif>
        <g:elseif test="${accessMethod == RDStore.ACCESS_POINT_TYPE_OA}">
            <div class="field required">
                <label>${message(code: 'accessPoint.oa.name.label')}
                    <span class="la-long-tooltip la-popup-tooltip"
                          data-content="${message(code:'accessPoint.oa.help')}">
                        <i class="${Icon.TOOLTIP.HELP} la-popup"></i></span>  <g:message code="messageRequiredField" />
                </label>
                <g:field type="text" name="name" value="" />
            </div>
            <div class="field required">
                <label>${message(code: 'accessPoint.entitiyId.label')} <g:message code="messageRequiredField" /></label>
                <g:textField name="entityId" value="${entityId}" />
            </div>
        </g:elseif>
        <g:elseif test="${accessMethod == RDStore.ACCESS_POINT_TYPE_PROXY}">
            <laser:render template="name" model="${[nameOptions: [], name: '']}"/>
        </g:elseif>
        <g:elseif test="${accessMethod == RDStore.ACCESS_POINT_TYPE_SHIBBOLETH}">
            <div class="field required">
                <label>${message(code: 'accessPoint.shibboleth.name.label')}
                    <span class="la-long-tooltip la-popup-tooltip"
                          data-content="${message(code:'accessPoint.shibboleth.help')}">
                        <i class="${Icon.TOOLTIP.HELP} la-popup"></i></span> <g:message code="messageRequiredField" />
                </label>
                <g:field type="text" name="name" value="" />
            </div>
            <div class="field">
                <label>${message(code: 'accessPoint.entitiyId.label')}</label>
                <g:textField name="entityId" value="${entityId}" />
            </div>
        </g:elseif>
        <g:elseif test="${accessMethod == RDStore.ACCESS_POINT_TYPE_MAIL_DOMAIN}">
            <laser:render template="name" model="${[nameOptions: [],name: '']}"/>
        </g:elseif>
        <div class="field">
            <label>${message(code:'default.note.label')}</label>
            <g:field type="text" name="note" value="" />
        </div>
        <input type="submit" class="${Btn.SIMPLE_CLICKCONTROL}" value="${message(code: 'default.button.create.label')}"/>
    </ui:form>