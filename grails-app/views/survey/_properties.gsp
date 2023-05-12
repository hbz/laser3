<%@ page import="de.laser.storage.RDStore; de.laser.survey.SurveyResult; de.laser.CustomerTypeService; de.laser.Subscription; de.laser.properties.PropertyDefinitionGroupBinding; de.laser.properties.PropertyDefinitionGroup; de.laser.properties.PropertyDefinition; de.laser.properties.SubscriptionProperty; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.interfaces.CalculatedType" %>
<laser:serviceInjection/>
<!-- _properties -->
<%
    LinkedHashSet groupedProperties = []
%>
<!-- TODO div class="ui card la-dl-no-table la-js-hideable" -->
<div class="ui card la-dl-no-table">

    <div class="content">
        <div class="header">
            <h3 class="ui header"><g:message code="surveyConfigsInfo.properties"/>
            <ui:totalNumber total="${surveyConfig.surveyProperties.size()}"/>
            </h3>
        </div>
    </div>


    <g:if test="${controllerName == 'survey' && actionName == 'show' && editable && properties && surveyInfo.status == RDStore.SURVEY_IN_PROCESSING}">
        <div class="content">
            <a class="ui icon right floated button"
               data-ui="modal"
               href="#addSurveyPropToConfigModal">
                <i class="plus icon"></i>
            </a>

            <ui:modal id="addSurveyPropToConfigModal"
                      msgSave="${message(code: 'surveyConfigsInfo.add.button')}"
                      message="surveyConfigsInfo.add.button">
                <g:form action="addSurveyPropToConfig" controller="survey" method="post" class="ui form">
                    <g:hiddenField name="id" value="${surveyInfo.id}"/>
                    <g:hiddenField name="surveyConfigID" value="${surveyConfig.id}"/>

                    <div class="field required">
                        <label>${message(code: 'surveyConfigs.property')} <g:message
                                code="messageRequiredField"/></label>
                        <ui:dropdown name="selectedProperty"
                                     class="la-filterPropDef"
                                     from="${properties}"
                                     iconWhich="shield alternate"
                                     optionKey="${{ "${it.id}" }}"
                                     optionValue="${{ it.getI10n('name') }}"
                                     noSelection="${message(code: 'default.search_for.label', args: [message(code: 'surveyProperty.label')])}"
                                     required=""/>

                    </div>
                </g:form>
            </ui:modal>
        </div>
    </g:if>

<%-- grouped custom properties --%>

    <g:set var="allPropDefGroups" value="${surveyConfig.getCalculatedPropDefGroups(surveyInfo.owner)}"/>

    <g:each in="${allPropDefGroups.sorted}" var="entry">
        <%
            PropertyDefinitionGroup pdg = entry[1]
        %>
        <div class="content">
            <h2 class="ui header">
                ${message(code: 'surveyConfigsInfo.properties')}
                (${pdg.name})
            </h2>
            <g:if test="${controllerName == 'survey' && actionName == 'show'}">
                <g:set var="properties" value="${surveyConfig.getSurveyConfigPropertiesByPropDefGroup(pdg)}"/>
                <%
                    groupedProperties << properties
                %>
            </g:if><g:else>
                <g:set var="properties" value="${surveyConfig.getSurveyResultsByPropDefGroupAndOrg(pdg, institution)}"/>
                <%
                    groupedProperties << properties
                %>
            </g:else>

            <div>
                <laser:render template="/templates/survey/properties" model="${[
                        properties: properties]}"/>
            </div>
        </div>

    </g:each>

<%-- orphaned properties --%>

<%--<div class="ui card la-dl-no-table la-js-hideable"> --%>
    <div class="content">
        <h2 class="ui header">
            <g:if test="${allPropDefGroups.global}">
                ${message(code: 'surveyConfigsInfo.properties.orphaned')}
            </g:if>
        </h2>

        <div>
            <g:if test="${controllerName == 'survey' && actionName == 'show'}">
                <g:set var="properties" value="${surveyConfig.getOrphanedSurveyConfigProperties(groupedProperties)}"/>
            </g:if><g:else>
                <g:set var="properties" value="${surveyConfig.getOrphanedSurveyResultsByOrg(groupedProperties)}"/>
            </g:else>

            <div>
                <laser:render template="/templates/survey/properties" model="${[
                        properties: properties]}"/>
            </div>
        </div>
    </div>
    <%--</div>--%>

</div><!--.card -->

<!-- _properties -->
