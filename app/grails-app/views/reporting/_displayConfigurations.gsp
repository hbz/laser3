<%@page import="de.laser.ReportingService" %>
<laser:serviceInjection/>

<g:if test="${entry == 'general'}">
    <a class="ui large label generalLoadingParam" data-requestParam="${queried}" data-display="${ReportingService.CONFIG_LIBRARY_TYPE}"><g:message code="org.libraryType.label"/></a>
    <a class="ui large label generalLoadingParam" data-requestParam="${queried}" data-display="${ReportingService.CONFIG_SUBJECT_GROUP}"><g:message code="org.subjectGroup.label"/></a>
    <a class="ui large label generalLoadingParam" data-requestParam="${queried}" data-display="${ReportingService.CONFIG_REGION}"><g:message code="org.region.label"/></a>
    <a class="ui large label generalLoadingParam" data-requestParam="${queried}" data-display="${ReportingService.CONFIG_LIBRARY_NETWORK}"><g:message code="org.libraryNetwork.label"/></a>
    <a class="ui large label" id="orgProperty" data-requestParam="${queried}" data-display="${ReportingService.CONFIG_ORG_PROPERTY}"><g:message code="subscription.property.search"/></a>
    <%-- TODO [ticket=2923] ask Ingrid about timeline slider --%>
</g:if>
<g:elseif test="${entry == 'subscription'}">
    <a class="ui large label display" data-display="costItemDevelopment">Kostenentwicklung</a>
    <a class="ui large label display" data-display="costItemDivision">Kostenaufteilung</a>
</g:elseif>