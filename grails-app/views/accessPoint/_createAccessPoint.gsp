<%@ page import="de.laser.storage.RDStore" %>
<laser:serviceInjection/>
<ui:greySegment>
    <g:form action="processCreate" controller="accessPoint" id="${orgInstance.id}" method="post" class="ui form">
        <g:hiddenField name="accessMethod" value="${genericOIDService.getOID(accessMethod)}" />
        <g:if test="${accessMethod == RDStore.ACCESS_POINT_TYPE_IP}">
            <laser:render template="name" model="${[nameOptions: availableOptions.collectEntries(),
                                                name: availableOptions.first().values().first(),
                                                accessMethod: accessMethod]}"/>
        </g:if>
        <g:elseif test="${accessMethod == RDStore.ACCESS_POINT_TYPE_EZPROXY}">
            <laser:render template="name" model="${[nameOptions: [], name: '']}"/>
            <div class="field required">
                <label>URL
                    <span class="la-long-tooltip la-popup-tooltip la-delay"
                          data-content="${message(code: "accessPoint.url.help")}">
                        <i class="question circle icon la-popup"></i></span> <g:message code="messageRequiredField" />
                </label>
                <g:textField name="url" value="${url}" />
            </div>
        </g:elseif>
        <g:elseif test="${accessMethod == RDStore.ACCESS_POINT_TYPE_OA}">
            <div class="field required">
                <label>${message(code: 'accessPoint.oa.name.label')}
                    <span class="la-long-tooltip la-popup-tooltip la-delay"
                          data-content="${message(code:'accessPoint.oa.help')}">
                        <i class="question circle icon la-popup"></i></span>  <g:message code="messageRequiredField" />
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
                    <span class="la-long-tooltip la-popup-tooltip la-delay"
                          data-content="${message(code:'accessPoint.shibboleth.help')}">
                        <i class="question circle icon la-popup"></i></span> <g:message code="messageRequiredField" />
                </label>
                <g:field type="text" name="name" value="" />
            </div>
            <div class="field">
                <label>${message(code: 'accessPoint.entitiyId.label')}</label>
                <g:textField name="entityId" value="${entityId}" />
            </div>
        </g:elseif>
        <g:elseif test="${accessMethod == RDStore.ACCESS_POINT_TYPE_VPN}">
            <laser:render template="name" model="${[nameOptions: [],name: '']}"/>
        </g:elseif>
        <input type="submit" class="ui button js-click-control" value="${message(code: 'default.button.create.label')}"/>
    </g:form>
</ui:greySegment>