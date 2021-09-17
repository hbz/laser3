<%@ page import="de.laser.ExportClickMeService; de.laser.IdentifierNamespace; de.laser.Org;" %>
<laser:serviceInjection/>
<g:set var="exportClickMeService" bean="exportClickMeService"/>
<!-- _individuallyExportModal.gsp -->
<g:set var="formFields" value="${exportClickMeService.getExportRenewalFieldsForUI()}"/>

<semui:modal id="${modalID}" text="Excel-Export" hideSubmitButton="true">

    <g:form controller="survey" action="renewalEvaluation" id="${surveyInfo.id}"
            params="[surveyConfigID: surveyConfig.id, exportClickMeExcel: true]">
        <div class="ui form">
            <div class="field">
                <label><g:message code="exportClickMe.fieldsToExport"/></label>
            </div>

            <div class="ui top attached tabular menu">
                <g:each in="${formFields}" var="fields" status="i">
                    <a class="${("tab-${i}" == "tab-0") ? 'active' : ''}  item"
                       data-tab="tab-${i}">${fields.value.message ? message(code: fields.value.message) : fields.value.label}</a>
                </g:each>
            </div>

            <g:each in="${formFields}" var="fields" status="i">
                <div class="ui bottom attached ${("tab-${i}" == "tab-0") ? 'active' : ''} tab segment"
                     data-tab="tab-${i}">
                    <div class="ui form">
                    <div class="fields">
                        <g:each in="${fields.value.fields}" var="field" status="fc">

                            <div class="wide eight field">

                                <div class="ui checkbox">
                                    <input type="checkbox" name="iex:${field.key}"
                                           id="iex:${field.key}" ${field.value.defaultChecked ? 'checked="checked"' : ''}>
                                    <label for="iex:${field.key}">${field.value.message ? message(code: field.value.message) : field.value.label}</label>
                                </div>

                            </div><!-- .field -->

                            <g:if test="${fc % 2 == 1}">
                                </div>
                                <div class="fields">
                            </g:if>

                        </g:each>
                    </div>
                    </div>
                </div>
            </g:each>

            <br/>

            <div class="fields">

                <div class="wide eight field">
                    <label for="filename"><g:message code="default.fileName.label"/></label>
                    <input name="filename" id="filename" value="${escapeService.escapeString(surveyConfig.getSurveyName()) + "_" + message(code:'renewalexport.renewals')}"/>
                </div>

                <div class="wide eight field">
                    <br>
                    <button class="ui button positive right floated"
                            id="export-renewal-as-excel">${message(code: 'renewalEvaluation.exportRenewal')}</button>
                </div>

            </div><!-- .fields -->
        </div><!-- .form -->

    </g:form>

</semui:modal>
<!-- _individuallyExportModal.gsp -->

