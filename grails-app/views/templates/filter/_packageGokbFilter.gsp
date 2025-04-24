<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon" %>
<ui:filter>
    <g:form action="${actionName}" controller="${controllerName}" params="${params}" method="get" class="ui small form clearing">
        <input type="hidden" name="isSiteReloaded" value="yes"/>
        <g:if test="${participant}">
            <input type="hidden" name="participant" value="${participant.id}"/>
            <g:hiddenField name="participant" value="${participant.id}"/>
        </g:if>
        <g:if test="${params.viewTab}">
            <g:hiddenField name="viewTab" value="${params.viewTab}"/>
        </g:if>
        <g:if test="${params.subTab}">
            <g:hiddenField name="subTab" value="${params.subTab}"/>
        </g:if>
        <laser:render template="/templates/filter/packageGokbFilterFields" model="[
                tmplConfigShow: filterConfig,
                automaticUpdates: automaticUpdates,
        ]"/>
        <g:each in="${filterAccordionConfig}" var="accordion">
            <div class="ui accordion">
                <div class="title">
                    <i class="icon dropdown"></i>
                    <g:message code="${accordion.getKey()}"/>
                    <%-- TODO <g:if test="${params.keySet().intersect(FilterService.PLATFORM_FILTER_AUTH_FIELDS.keySet()).size()}"><span class="ui circular label yellow">${params.keySet().intersect(FilterService.PLATFORM_FILTER_AUTH_FIELDS.keySet()).size()}</span></g:if>--%>
                </div>
                <div class="content">
                    <laser:render template="/templates/filter/packageGokbFilterFields" model="[
                            tmplConfigShow: accordion.getValue()
                    ]"/>
                </div>
            </div>
        </g:each>
        <div class="field la-field-right-aligned">
            <g:if test="${surveyConfig && participant && controllerName == 'survey'}">
                <g:set var="parame" value="${[surveyConfigID: surveyConfig.id, participant: participant.id, viewTab: params.viewTab, tabStat: params.tabStat]}"/>
                <g:set var="participant" value="${participant}"/>
            </g:if>
            <g:elseif test="${surveyConfig}">
                <g:set var="parame" value="${[surveyConfigID: surveyConfig.id, viewTab: params.viewTab, tabStat: params.tabStat]}"/>
            </g:elseif>
            <g:else>
                <g:set var="parame" value="${[tab: params.tab, tabStat: params.tabStat]}"/>
            </g:else>

            <g:link controller="${controllerName}" action="${actionName}" id="${params.id}" params="${parame}" class="${Btn.SECONDARY} reset">${message(code: 'default.button.reset.label')}</g:link>
            <button type="submit" name="search" value="yes"
                    class="${Btn.PRIMARY}">${message(code: 'default.button.filter.label')}</button>
        </div>
    </g:form>
</ui:filter>