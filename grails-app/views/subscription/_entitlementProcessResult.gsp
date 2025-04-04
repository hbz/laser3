<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon" %>
<laser:serviceInjection/>
<g:if test="${success}">
    <ui:msg class="success" showIcon="true" message="subscription.details.addEntitlements.matchingSuccessful" args="[toAddCount, addedCount]"/>
</g:if>
<g:if test="${error}">
    <input id="errorMailto" type="hidden" value="${mailTo.content.join(';')}" />
    <input id="errorMailcc" type="hidden" value="${contextService.getUser().email}" />
    <textarea id="errorMailBody" class="hidden">${mailBody}</textarea>
    <ui:msg class="error" showIcon="true" message="subscription.details.addEntitlements.matchingError"
            args="[notAddedCount, notInPackageCount, perpetuallyPurchasedCount, g.createLink(controller: 'package', action:'downloadLargeFile', params:[token: token, fileformat: 'kbart'])]"/>
    <laser:script file="${this.getGroovyPageFileName()}">
        JSPC.notifyProvider = function () {
            let mailto = $('#errorMailto').val();
            let mailcc = $('#errorMailcc').val();
            let subject = 'Fehlerhafte KBART';
            let body = $('#errorMailBody').html();
            let href = 'mailto:' + mailto + '?subject=' + subject + '&cc=' + mailcc + '&body=' + body;

            window.location.href = encodeURI(href);
        }
    </laser:script>
</g:if>
<g:if test="${truncatedRows}">
    <ui:msg class="error" showIcon="true" message="subscription.details.addEntitlements.truncatedRows" args="[truncatedRows]"/>
</g:if>
<g:elseif test="${errMess}">
    <ui:msg class="error" showIcon="true" message="${errMess}" args="[errorCount]"/>
    <g:link class="${Btn.ICON.SIMPLE}" controller="package" action="downloadLargeFile" params="[token: token, fileformat: 'txt']"><i class="${Icon.CMD.DOWNLOAD}"></i></g:link>
</g:elseif>