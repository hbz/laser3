<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.RefdataCategory; de.laser.properties.PropertyDefinition; de.laser.storage.RDStore;de.laser.RefdataValue; de.laser.survey.SurveyConfig" %>

<laser:htmlStart message="myinst.currentSubscriptions.label" />

<ui:breadcrumbs>
    <ui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>

    <g:if test="${surveyInfo}">
        <ui:crumb controller="survey" action="show" id="${surveyInfo.id}"
                     params="[surveyConfigID: surveyConfig.id]" text="${surveyInfo.name}"/>
    </g:if>
    <ui:crumb message="myinst.currentSubscriptions.label" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon text="${surveyInfo.name}" type="Survey"/>
<uiSurvey:status object="${surveyInfo}"/>

<laser:render template="nav"/>

<ui:messages data="${flash}"/>


<g:if test="${editable}">
    <input class="${Btn.SIMPLE}" value="${message(code:'surveyProperty.create_new')}"
           data-ui="modal" data-href="#createSurveyPropertyModal" type="submit">
</g:if>

<ui:greySegment>

    <h4 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'surveyProperty.all.label')}

        <i class="${Icon.TOOLTIP.HELP} la-popup"></i>

        <div class="ui popup">
            <i class="${Icon.PROP.IS_PRIVATE}"></i> = ${message(code: 'subscription.properties.my')}
        </div>
        <ui:totalNumber total="${properties.size()}"/>

    </h4>

    <table class="ui celled sortable table la-js-responsive-table la-table">
        <thead>
        <tr>
            <th class="center aligned">${message(code: 'sidewide.number')}</th>
            <g:if test="${language?.toLowerCase() in ['de_de', 'de']}">
                <g:set var="SUBSTITUTE" value="de"/>
                <th>${message(code: 'default.name.label')}</th>
                <th>${message(code: 'propertyDefinition.expl.label')}</th>
            </g:if>
            <g:else>
                <g:set var="SUBSTITUTE" value="en"/>
                <th>${message(code: 'default.name.label')}</th>
                <th>${message(code: 'propertyDefinition.expl.label')}</th>
            </g:else>
            <th>${message(code: 'default.type.label')}</th>
            <th class="center aligned">
                <ui:optionsIcon />
            </th>
        </tr>
        </thead>

        <g:each in="${properties.sort { it."name_${SUBSTITUTE}"?.toLowerCase() }}" var="property" status="i">
            <tr>
                <td class="center aligned">
                    ${i + 1}
                </td>
                <td>
                    <g:if test="${property?.tenant && property?.tenant.id == contextService.getOrg().id}">
                        <ui:xEditable owner="${property}" field="name_${SUBSTITUTE}" type="textarea"/>
                        <i class='${Icon.PROP.IS_PRIVATE}'></i>
                    </g:if>
                    <g:else>
                        ${property?.getI10n('name')}
                    </g:else>
                </td>

                <td>

                    <g:if test="${property?.tenant && property?.tenant.id == contextService.getOrg().id}">
                        <ui:xEditable owner="${property}" field="expl_${SUBSTITUTE}" type="textarea"/>
                    </g:if>
                    <g:else>
                        ${property?.getI10n('expl')}
                    </g:else>

                </td>
                <td>
                    ${PropertyDefinition.getLocalizedValue(property.type)}
                    <g:if test="${property.isRefdataValueType()}">
                        <g:set var="refdataValues" value="${[]}"/>
                        <g:each in="${RefdataCategory.getAllRefdataValues(property.refdataCategory)}"
                                var="refdataValue">
                            <g:if test="${refdataValue.getI10n('value')}">
                                <g:set var="refdataValues" value="${refdataValues + refdataValue.getI10n('value')}"/>
                            </g:if>
                        </g:each>
                        <br />
                        (${refdataValues.join('/')})
                    </g:if>
                </td>
                <td class="x">
                    <g:if test="${property.countUsages() == 0 && property.tenant?.id == contextService.getOrg().id}">
                        <g:link action="actionsForSurveyProperty" id="${params.id}" params="[deleteId: property.id, actionForSurveyProperty: 'deleteSurveyProperty']"
                                class="${Btn.ICON.NEGATIVE_CONFIRM}"
                                data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.property", args: [property.getI10n('name')])}"
                                data-confirm-term-how="delete"
                                role="button"
                                aria-label="${message(code: 'ariaLabel.delete.universal')}">
                            <i class="${Icon.CMD.DELETE}"></i>
                        </g:link>
                    </g:if>
                </td>
            </tr>
        </g:each>
    </table>

</ui:greySegment>

<ui:modal id="createSurveyPropertyModal" message="surveyProperty.create_new.label">

    <g:form class="ui form" action="actionsForSurveyProperty" method="post" params="[id: surveyInfo?.id, actionForSurveyProperty: 'createSurveyProperty']">

        <div class="field">
            <label for="pd_name" class="property-label">Name</label>
            <input type="text" name="pd_name" id="pd_name"/>
        </div>

        <div class="field">
            <label for="pd_expl" class="property-label">${message(code: 'propertyDefinition.expl.label')}</label>
            <textarea class="ui textarea la-textarea-resize-vertical" name="pd_expl" id="pd_expl" rows="2"></textarea>
        </div>

        <div class="fields">

            <div class="field six wide">
                <label for="pd_descr" class="property-label">${message(code: 'default.description.label')}</label>
                <%--<g:select name="pd_descr" from="${PropertyDefinition.AVAILABLE_PRIVATE_DESCR}"/>--%>
                <select name="pd_descr" id="pd_descr" class="ui dropdown clearable">
                    <g:each in="${[PropertyDefinition.SVY_PROP]}" var="pd">
                        <option value="${pd}"><g:message code="propertyDefinition.${pd}.label"
                                                         default="${pd}"/></option>
                    </g:each>
                </select>
            </div>

            <div class="field five wide">
                <label for="pd_type" class="property-label"><g:message code="default.type.label"/></label>
                <g:select class="ui dropdown clearable"
                          from="${PropertyDefinition.validTypes.entrySet()}"
                          optionKey="key" optionValue="${{ PropertyDefinition.getLocalizedValue(it.key) }}"
                          name="pd_type"
                          id="pd_type"/>
            </div>

            %{--<div class="field four wide">
                <label class="property-label">Optionen</label>

                <g:checkBox type="text" name="pd_mandatory"/> ${message(code: 'default.mandatory.tooltip')}
                <br />
                <g:checkBox type="text"
                            name="pd_multiple_occurrence"/> ${message(code: 'default.multipleOccurrence.tooltip')}
            </div>--}%

        </div>

        <div class="fields">
            <div class="field hide" id="remoteRefdataSearchWrapper" style="width: 100%">
                <label class="property-label"><g:message code="refdataCategory.label"/></label>
                <select class="ui search selection dropdown clearable remoteRefdataSearch" name="refdatacategory"></select>

                <div class="ui grid" style="margin-top:1em">
                    <div class="ten wide column">
                        <g:each in="${propertyService.getRefdataCategoryUsage()}" var="cat">

                            <p class="hidden" data-prop-def-desc="${cat.key}">
                                Häufig verwendete Kategorien: <br />

                                <%
                                    List catList = cat.value?.take(3)
                                    catList = catList.collect { entry ->
                                        '&nbsp; - ' + (RefdataCategory.getByDesc(entry[0]))?.getI10n('desc')
                                    }
                                    println catList.join('<br />')
                                %>

                            </p>
                        </g:each>
                    </div>

                    <div class="six wide column">
                        <br />
                        <a href="<g:createLink controller="myInstitution" action="manageRefdatas"/>" target="_blank">
                            <i class="icon window maximize outline"></i>
                            Alle Kategorien und Referenzwerte<br />als Übersicht öffnen
                        </a>
                    </div>
                </div><!-- .grid -->
            </div>
        </div>

    </g:form>
</ui:modal>

<laser:script file="${this.getGroovyPageFileName()}">

$('#pd_descr').change(function() {
    $('#pd_type').trigger('change');
});

$('#pd_type').change(function() {
    var selectedText = $( "#pd_type option:selected" ).val();
    if( selectedText == "${RefdataValue.name}") {
        $("#remoteRefdataSearchWrapper").show();

        var $pMatch = $( "p[data-prop-def-desc='" + $( "#pd_descr option:selected" ).val() + "']" )
        if ($pMatch) {
            $( "p[data-prop-def-desc]" ).addClass('hidden')
            $pMatch.removeClass('hidden')
        }
    }
    else {
        $("#remoteRefdataSearchWrapper").hide();
    }
});

$('#pd_type').trigger('change');

c3po.remoteRefdataSearch('${createLink(controller:'ajaxJson', action:'lookup')}', '#remoteRefdataSearchWrapper');

$(".la-popup").popup({
});
</laser:script>

<laser:htmlEnd />