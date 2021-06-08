<%@ page import="de.laser.ExportClickMeService;" %>
<laser:serviceInjection/>
<g:set var="exportClickMeService" bean="exportClickMeService"/>
<!-- _individuallyExportModal.gsp -->
<g:set var="formFields" value="${exportClickMeService.getExportSubscriptionFieldsForUI()}"/>

<semui:modal id="${modalID}" text="Excel-Export" hideSubmitButton="true">

    <g:form action="members" controller="subscription" params="${params+[id:params.id, exportClickMeExcel: true]}">
        <div class="ui form">
            <div class="field">
                <label>Zu exportierende Felder</label>
            </div>


            <g:each in="${formFields}" var="fields">
                <div class="field">
                    <label>${fields.value.message ? message(code: fields.value.message) : fields.value.label}</label>
                </div>
                <div class="fields">
                <g:each in="${fields.value.fields}" var="field" status="fc">
                    <div class="wide eight field">

                        <div class="ui checkbox">
                            <input type="checkbox" name="iex:${field.key}" id="iex:${field.key}" ${field.value.defaultChecked ? 'checked="checked"' : ''}>
                            <label for="iex:${field.key}">${field.value.message ? message(code: field.value.message) : field.value.label}</label>
                        </div>

                    </div><!-- .field -->

                    <g:if test="${fc % 2 == 1}">
                        </div>
                        <div class="fields">
                    </g:if>
                </g:each>
                </div>
            </g:each>
            <div class="fields">

            </div><!-- .fields -->

            <br/>

            <div class="fields">

                <div class="wide eight field">
                    <label for="filename"><g:message code="default.fileName.label"/></label>
                    <input name="filename" id="filename" value="${message(code: 'subscriptionDetails.members.members')}"/>
                </div>
                <div class="wide eight field">
                    <br>
                    <button class="ui button positive right floated"
                            id="export-renewal-as-excel">Export Excel</button>
                </div>

            </div><!-- .fields -->
        </div><!-- .form -->

    </g:form>

</semui:modal>
<!-- _individuallyExportModal.gsp -->

