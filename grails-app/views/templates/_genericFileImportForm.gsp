<g:uploadForm action="${processAction}" method="post">
    <g:if test="${subId}">
        <g:hiddenField name="subId" value="${subId}"/>
    </g:if>
    <g:if test="${id}">
        <g:hiddenField name="id" value="${id}"/>
    </g:if>
    <g:if test="${surveyConfigID}">
        <g:hiddenField name="surveyConfigID" value="${surveyConfigID}"/>
    </g:if>
    <g:if test="${surveyPackage}">
        <g:hiddenField name="costItemsForSurveyPackage" value="true"/>
    </g:if>
    <g:if test="${surveySubscriptions}">
        <g:hiddenField name="costItemsForSurveySubscriptions" value="true"/>
    </g:if>

    <ui:msg class="warning" header="${message(code: 'message.attention')}" text="" message="myinst.subscriptionImport.attention" showIcon="true" hideClose="true" />

    <g:render template="/templates/genericFileImportFormatSelector" model="[hideSubmitButtons: hideSubmitButtons, fixedHeaderSetting: fixedHeaderSetting]" />
</g:uploadForm>

<g:render template="/templates/genericFileImportJS"/>