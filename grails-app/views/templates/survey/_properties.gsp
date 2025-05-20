<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore; de.laser.survey.SurveyResult; de.laser.CustomerTypeService; de.laser.Subscription; de.laser.properties.PropertyDefinitionGroupBinding; de.laser.properties.PropertyDefinitionGroup; de.laser.properties.PropertyDefinition; de.laser.properties.SubscriptionProperty; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.interfaces.CalculatedType" %>
<laser:serviceInjection/>
<!-- _properties -->
<%
    LinkedHashSet groupedProperties = []
%>
<!-- TODO div class="ui card la-dl-no-table" -->
<div class="ui card la-dl-no-table">

    <div class="content">
        <div class="ui header la-flexbox la-justifyContent-spaceBetween">
            <h2>
                <g:message code="surveyConfigsInfo.properties"/>
                <ui:totalNumber total="${surveyConfig.surveyProperties.size()}"/>
            </h2>

            <g:if test="${editable && surveyInfo.status == RDStore.SURVEY_IN_PROCESSING}">
                    <button type="button" class="${Btn.MODERN.SIMPLE_TOOLTIP}"
                            data-content="${message(code: 'license.button.addProperty')}"
                            onclick="JSPC.app.createProperty(${surveyInfo.id}, '${surveyInfo.class.simpleName}','false');">
                        <i class="${Icon.CMD.ADD}"></i>
                    </button>
            </g:if>
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
                    <h3 class="ui header">
                        <i class="${Icon.SYM.PROPERTIES}" style="font-size: 1em; margin-right: .25rem"></i>
                        ${pdg.name}
                        (${message(code: 'surveyConfigsInfo.properties')})
                    </h3>

                    <div id="survey_grouped_custom_properties_${pdg.id}">
                        <laser:render template="/templates/survey/properties_table" model="${[
                                surveyProperties: surveyProperties, pdg: pdg, props_div: "survey_grouped_custom_properties_${pdg.id}"]}"/>
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
                    <h3 class="ui header">
                        <i class="${Icon.SYM.PROPERTIES}" style="font-size: 1em; margin-right: .25rem"></i>
                        ${pdg.name}
                        (${message(code: 'surveyConfigsInfo.properties')})
                    </h3>

                    <div>
                        <laser:render template="/templates/survey/properties_table" model="${[
                                surveyProperties: surveyProperties]}"/>
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

</div><!--.card -->
<%-- private properties --%>
<div class="ui card la-dl-no-table">

    <div class="content">
        <div class="ui header la-flexbox la-justifyContent-spaceBetween">
            <h2>
                ${message(code: 'default.properties.my')}
            </h2>
            <g:if test="${controllerName == 'survey' && actionName == 'show' && editable && surveyInfo.status == RDStore.SURVEY_IN_PROCESSING}">
                <div class="right aligned four wide column">
                    <button type="button" class="${Btn.MODERN.SIMPLE_TOOLTIP}"
                            data-content="${message(code: 'license.button.addProperty')}"
                            onclick="JSPC.app.createProperty(${surveyInfo.id}, '${surveyInfo.class.simpleName}', 'true');">
                        <i class="${Icon.CMD.ADD}"></i>
                    </button>
                </div>
            </g:if>
        </div>
    </div>





    <g:if test="${controllerName == 'survey' && actionName == 'show'}">
        <g:set var="surveyProperties" value="${surveyConfig.getPrivateSurveyConfigProperties()}"/>
        <g:if test="${surveyProperties.size() > 0}">
            <div class="content">
                <h3 class="ui header">
                    <i class="${Icon.SYM.PROPERTIES}" style="font-size: 1em; margin-right: .25rem"></i>
                    ${message(code: 'surveyConfigsInfo.properties.privat')}
                </h3>

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
                <h3 class="ui header">
                    <i class="${Icon.SYM.PROPERTIES}" style="font-size: 1em; margin-right: .25rem"></i>
                    ${message(code: 'surveyConfigsInfo.properties.privat')}
                </h3>

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
