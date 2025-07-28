<g:uploadForm action="${processAction}" method="post">
    <g:if test="${subId}">
        <g:hiddenField name="subId" value="${subId}"/>
    </g:if>

    <ui:msg class="warning" header="${message(code: 'message.attention')}" text="" message="myinst.subscriptionImport.attention" showIcon="true" hideClose="true" />

    <g:render template="/templates/genericFileImportFormatSelector" />
</g:uploadForm>

<g:render template="/templates/genericFileImportJS"/>