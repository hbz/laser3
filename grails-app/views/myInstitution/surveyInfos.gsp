<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDConstants; de.laser.survey.SurveyOrg; de.laser.survey.SurveyConfig;de.laser.RefdataCategory;de.laser.properties.PropertyDefinition;de.laser.RefdataValue; de.laser.Org" %>
<laser:htmlStart text="${surveyInfo.type.getI10n('value')}" />

<ui:breadcrumbs>
    <ui:crumb controller="myInstitution" action="currentSurveys" message="currentSurveys.label"/>
    <ui:crumb text="${surveyInfo.type.getI10n('value')} - ${surveyInfo.name}" class="active"/>
</ui:breadcrumbs>

<ui:controlButtons>
   <ui:exportDropdown>
        <ui:exportDropdownItem>
            <g:link class="item" controller="myInstitution" action="surveyInfos"
                    params="${params + [exportXLSX: true, surveyConfigID: surveyConfig.id]}">${message(code: 'survey.exportSurvey')}</g:link>
        </ui:exportDropdownItem>
    </ui:exportDropdown>
</ui:controlButtons>

<ui:h1HeaderWithIcon text="${surveyInfo.type.getI10n('value')} - ${surveyInfo.name}" type="Survey">
    <uiSurvey:status object="${surveyInfo}"/>
</ui:h1HeaderWithIcon>

<ui:messages data="${flash}"/>

<br/>
<g:if test="${surveyConfig.isResultsSetFinishByOrg(contextService.getOrg())}">
    <ui:msg class="success" showIcon="true" hideClose="true">
                <%-- <g:message code="surveyInfo.finishOrSurveyCompleted"/> --%>
                <g:message
                        code="${surveyInfo.isMandatory ? 'surveyResult.finish.mandatory.info' : 'surveyResult.finish.info'}"/>.
    </ui:msg>
</g:if>

<g:if test="${ownerId}">
    <g:set var="choosenOrg" value="${Org.findById(ownerId)}"/>
    <ui:greySegment>
        <h3 class="ui header"><g:message code="surveyInfo.owner.label"/>:</h3>

        <g:if test="${choosenOrg}">
            <g:set var="choosenOrgCPAs" value="${choosenOrg.getGeneralContactPersons(false)}"/>
            <table class="ui table la-js-responsive-table la-table compact">
                <tbody>
                <tr>
                    <td>
                        <p><strong><g:link controller="organisation" action="show"
                                           id="${choosenOrg.id}">${choosenOrg.name} (${choosenOrg.sortname})</g:link></strong></p>

                        ${choosenOrg.libraryType?.getI10n('value')}
                    </td>
                    <td>
                        <g:if test="${choosenOrgCPAs}">
                            <g:set var="oldEditable" value="${editable}"/>
                            <g:set var="editable" value="${false}" scope="request"/>
                            <g:each in="${choosenOrgCPAs}" var="gcp">
                                <laser:render template="/addressbook/person_details"
                                              model="${[person: gcp, tmplHideLinkToAddressbook: true]}"/>
                            </g:each>
                            <g:set var="editable" value="${oldEditable ?: false}" scope="request"/>
                        </g:if>
                    </td>
                </tr>
                </tbody>
            </table>
        </g:if>
    </ui:greySegment>
</g:if>

<br/>

<laser:render template="/templates/survey/participantView"/>

<br/>

<g:if test="${editable}">
    <button class="${Btn.POSITIVE} triggerSurveyFinishModal"
            data-href="${g.createLink(controller: 'ajaxHtml', action: 'getSurveyFinishModal', params: [id: surveyInfo.id, surveyConfigID: surveyConfig.id])}"
            role="button"
            aria-label="${surveyInfo.isMandatory ? 'surveyResult.finish.mandatory.info2' : 'surveyResult.finish.info2'}">
        <g:message code="${surveyInfo.isMandatory ? 'surveyResult.finish.mandatory.info2' : 'surveyResult.finish.info2'}"/>
    </button>
</g:if>
<br/>
<br/>

<laser:script file="${this.getGroovyPageFileName()}">
    $('.triggerSurveyFinishModal').on('click', function(e) {
        e.preventDefault();

        $.ajax({
            url: $(this).attr('data-href')
        }).done( function (data) {
            $('.ui.dimmer.modals > #surveyFinishModal').remove();
            $('#dynamicModalContainer').empty().html(data);

            $('#dynamicModalContainer .ui.modal').modal({
               onShow: function () {
                    r2d2.initDynamicUiStuff('#surveyFinishModal');
                    $("html").css("cursor", "auto");
                },
                detachable: true,
                autofocus: false,
                transition: 'scale',
                onApprove : function() {
                    $(this).find('#surveyFinishModal .ui.form').submit();
                    return false;
                }
            }).modal('show');
        })
    });
</laser:script>

<laser:htmlEnd/>
