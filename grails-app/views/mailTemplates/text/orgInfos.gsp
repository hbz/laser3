<%@ page import="de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataCategory" %>
<g:message code="gasco.table.product" locale="${language}"/>: ${sub.name}
<g:message code="default.institution" locale="${language}"/>: ${org.name}
<g:message code="org.customerIdentifier" locale="${language}"/>: ${customerIdentifier}


<g:if test="${readerNumberStudents}">${readerNumberStudents.referenceGroup.getI10n('value', language)}: ${readerNumberStudents.value} (${currentSemester.getI10n('value', language)})</g:if>
<g:if test="${readerNumberStaff}">${readerNumberStaff.referenceGroup.getI10n('value', language)}: ${readerNumberStaff.value} (${currentSemester.getI10n('value', language)})</g:if>
<g:if test="${readerNumberFTE}">${readerNumberFTE.referenceGroup.getI10n('value', language)}: ${readerNumberFTE.value} (${currentSemester.getI10n('value', language)})</g:if>

<g:if test="${generalContacts}">${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.getI10n('value', language)}: ${raw(generalContacts)}</g:if>
<g:if test="${responsibleAdmins}">${RDStore.PRS_FUNC_RESPONSIBLE_ADMIN.getI10n('value', language)}: ${raw(responsibleAdmins)}</g:if>
<g:if test="${billingContacts}">${RDStore.PRS_FUNC_INVOICING_CONTACT.getI10n('value', language)}: ${raw(billingContacts)}</g:if>
<g:if test="${billingAddress}">${RDStore.ADDRESS_TYPE_BILLING.getI10n('value', language)}: ${billingAddress}</g:if>
<g:if test="${billingPostBox}">${RDStore.ADDRESS_TYPE_BILLING.getI10n('value', language)}: ${billingPostBox}</g:if>

<g:each in="${accessPoints}" var="accessPoint">
${accessPoint.name}:
<g:if test="${accessPoint.mailDomains}"><g:each in="${accessPoint.mailDomains}" var="mailDomain">${raw(mailDomain)}</g:each></g:if><g:if test="${accessPoint.entityId}">EntityId: ${accessPoint.entityId}</g:if><g:if test="${accessPoint.url}">URL: ${accessPoint.url}</g:if>
<g:if test="${accessPoint.ipv4Ranges}"><g:each in="${accessPoint.ipv4Ranges}" var="ipv4Range">
${ipv4Range}</g:each></g:if>
<g:if test="${accessPoint.ipv6Ranges}"><g:each in="${accessPoint.ipv6Ranges}" var="ipv6Range">
${ipv6Range}</g:each></g:if>
</g:each>