<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore; de.laser.survey.SurveyResult; de.laser.CustomerTypeService; de.laser.Subscription; de.laser.properties.PropertyDefinitionGroupBinding; de.laser.properties.PropertyDefinitionGroup; de.laser.properties.PropertyDefinition; de.laser.properties.SubscriptionProperty; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.interfaces.CalculatedType" %>
<laser:serviceInjection/>
<!-- _properties -->
<%
    LinkedHashSet groupedProperties = []
%>
<!-- TODO div class="ui card la-dl-no-table" -->
<div class="ui card la-dl-no-table">

    <div class="content">
        <g:if test="${editable && surveyInfo.status == RDStore.SURVEY_IN_PROCESSING}">
            <div class="right aligned four wide column">
                <button type="button" class="${Btn.MODERN.SIMPLE_TOOLTIP}" data-content="${message(code: 'license.button.addProperty')}"
                        onclick="JSPC.app.createProperty(${surveyInfo.id}, '${surveyInfo.class.simpleName}');">
                    <i class="${Icon.CMD.ADD}"></i>
                </button>
            </div>
        </g:if>
        <div class="header">
            <h3 class="ui header"><g:message code="surveyConfigsInfo.properties"/>
            <ui:totalNumber total="${surveyConfig.surveyProperties.size()}"/>
            </h3>
        </div>
    </div>


<%-- grouped custom properties --%>

    <g:set var="allPropDefGroups" value="${surveyConfig.getCalculatedPropDefGroups(surveyInfo.owner)}"/>

    <g:each in="${allPropDefGroups.sorted}" var="entry">
        <%
            PropertyDefinitionGroup pdg = entry[1]
        %>
        <g:if test="${controllerName == 'survey' && actionName == 'show'}">
            <g:set var="surveyProperties" value="${surveyConfig.getSurveyConfigPropertiesByPropDefGroup(pdg)}"/>
            <%
                groupedProperties << surveyProperties
            %>
            <g:if test="${surveyProperties.size() > 0}">
                <div class="content">
                    <h2 class="ui header">
                        ${message(code: 'surveyConfigsInfo.properties')}
                        (${pdg.name})
                    </h2>

                    <div id="survey_grouped_custom_properties">
                        <laser:render template="/templates/survey/properties_table" model="${[
                                surveyProperties: surveyProperties, pdg: pdg, props_div: 'survey_grouped_custom_properties']}"/>
                    </div>
                </div>
            </g:if>
        </g:if>
        <g:else>
            <g:set var="surveyProperties"
                   value="${surveyConfig.getSurveyResultsByPropDefGroupAndOrg(pdg, participant)}"/>
            <%
                groupedProperties << surveyProperties
            %>
            <g:if test="${surveyProperties.size() > 0}">
                <div class="content">
                    <h2 class="ui header">
                        ${message(code: 'surveyConfigsInfo.properties')}
                        (${pdg.name})
                    </h2>

                    <div>
                        <laser:render template="/templates/survey/properties_table" model="${[
                                surveyProperties: surveyProperties, pdg: pdg]}"/>
                    </div>
                </div>
            </g:if>
        </g:else>
    </g:each>

<%-- orphaned properties --%>

<%--<div class="ui card la-dl-no-table"> --%>
    <g:if test="${controllerName == 'survey' && actionName == 'show'}">
        <g:set var="surveyProperties" value="${surveyConfig.getOrphanedSurveyConfigProperties(groupedProperties)}"/>
        <g:if test="${surveyProperties.size() > 0}">
            <div class="content">
                <h2 class="ui header">
                    <g:if test="${allPropDefGroups.global}">
                        ${message(code: 'surveyConfigsInfo.properties.orphaned')}
                    </g:if>
                </h2>

                <div id="survey_orphaned_properties">
                    <laser:render template="/templates/survey/properties_table" model="${[
                            surveyProperties: surveyProperties, props_div: 'survey_orphaned_properties']}"/>
                </div>
            </div>
        </g:if>
    </g:if><g:else>
    <g:set var="surveyProperties"
           value="${surveyConfig.getOrphanedSurveyResultsByOrg(groupedProperties, participant)}"/>
    <g:if test="${surveyProperties.size() > 0}">
        <div class="content">
            <h2 class="ui header">
                <g:if test="${allPropDefGroups.global}">
                    ${message(code: 'surveyConfigsInfo.properties.orphaned')}
                </g:if>
            </h2>

            <div>
                <laser:render template="/templates/survey/properties_table" model="${[
                        surveyProperties: surveyProperties]}"/>
            </div>
        </div>
    </g:if>
</g:else>


<%-- private properties --%>

    <g:if test="${controllerName == 'survey' && actionName == 'show'}">
        <g:set var="surveyProperties" value="${surveyConfig.getPrivateSurveyConfigProperties()}"/>
        <g:if test="${surveyProperties.size() > 0}">
            <div class="content">
                <h2 class="ui header">
                    ${message(code: 'surveyConfigsInfo.properties.privat')}
                </h2>

                <div id="survey_private_properties">
                    <laser:render template="/templates/survey/properties_table" model="${[
                            surveyProperties: surveyProperties, selectablePrivateProperties: true, props_div: 'survey_private_properties']}"/>
                </div>
            </div>
        </g:if>
    </g:if>
    <g:else>
        <g:set var="surveyProperties"
               value="${surveyConfig.getPrivateSurveyResultsByOrg(participant)}"/>
        <g:if test="${surveyProperties.size() > 0}">
            <div class="content">
                <h2 class="ui header">
                    ${message(code: 'surveyConfigsInfo.properties.privat')}
                </h2>

                <div>
                    <laser:render template="/templates/survey/properties_table" model="${[
                            surveyProperties: surveyProperties]}"/>
                </div>
            </div>
        </g:if>
    </g:else>

</div><!--.card -->
<g:if test="${editable && surveyInfo.status == RDStore.SURVEY_IN_PROCESSING}">
    <laser:render template="/templates/properties/createProperty_js"/>
</g:if>

<!-- _properties -->
