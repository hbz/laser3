<%@ page import="de.laser.storage.RDStore;" %>
<ui:exportDropdown>
    <ui:actionsDropdownItem class="triggerSurveyExport" controller="survey" action="exportModal"
                            params="[exportType: 'generally',surveyConfigID: surveyConfig.id, id: surveyInfo.id]" text="Export"/>

    <g:if test="${surveyInfo.type.id in [RDStore.SURVEY_TYPE_RENEWAL.id, RDStore.SURVEY_TYPE_SUBSCRIPTION.id]}">
        <ui:actionsDropdownItem class="triggerSurveyExport" controller="survey" action="exportModal"
                                params="[exportType: 'costItemsExport',surveyConfigID: surveyConfig.id, id: surveyInfo.id]" message="survey.exportSurveyCostItems"/>
    </g:if>

    <g:if test="${surveyConfig.subSurveyUseForTransfer}">
        <ui:actionsDropdownItem class="triggerSurveyExport" controller="survey" action="exportModal"
                                params="[exportType: 'renewalExport',surveyConfigID: surveyConfig.id, id: surveyInfo.id]" message="renewalEvaluation.exportRenewal"/>
    </g:if>
</ui:exportDropdown>

<laser:script file="${this.getGroovyPageFileName()}">
    $('.triggerSurveyExport').on('click', function(e) {
            e.preventDefault();

            $.ajax({
                url: $(this).attr('href')
            }).done( function (data) {
                $('.ui.dimmer.modals > #individuallyExportSurvey').remove();
                $('#dynamicModalContainer').empty().html(data);

                $('#dynamicModalContainer .ui.modal').modal({
                   onShow: function () {
                        r2d2.initDynamicUiStuff('#individuallyExportSurvey');
                        r2d2.initDynamicXEditableStuff('#individuallyExportSurvey');
                        $("html").css("cursor", "auto");
                    },
                    detachable: true,
                    autofocus: false,
                    closable: false,
                    transition: 'scale',
                    onApprove : function() {
                        $(this).find('.ui.form').submit();
                        return false;
                    }
                }).modal('show');
            })
        })
</laser:script>

