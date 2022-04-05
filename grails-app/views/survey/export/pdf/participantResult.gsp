<%@ page import="de.laser.SurveyConfig; de.laser.storage.RDStore; de.laser.properties.PropertyDefinition;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.Org; de.laser.DocContext; de.laser.SurveyOrg;" %>
<laser:serviceInjection/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
    <title></title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

    <asset:stylesheet src="semantic.css"/>

</head>
<body>
    <h1>
        LAS:eR <g:message code="survey.label"/>: ${surveyInfo.name}
    </h1>

    <div class="queryInfo">
        <g:if test="${participant}">
            <g:set var="choosenOrg" value="${Org.findById(participant.id)}"/>
            <g:set var="choosenOrgCPAs" value="${choosenOrg?.getGeneralContactPersons(false)}"/>

            <table class="ui table la-js-responsive-table la-table compact">
                <tbody>
                <tr>
                    <td>
                        <p><strong>${choosenOrg?.name} (${choosenOrg?.shortname})</strong></p>

                        ${choosenOrg?.libraryType?.getI10n('value')}
                    </td>
                    <td>
                        <g:if test="${choosenOrgCPAs}">
                            <g:set var="oldEditable" value="${editable}"/>
                            <g:set var="editable" value="${false}" scope="request"/>
                            <g:each in="${choosenOrgCPAs}" var="gcp">
                                <g:render template="/templates/cpa/person_details"
                                          model="${[person: gcp, tmplHideLinkToAddressbook: true]}"/>
                            </g:each>
                            <g:set var="editable" value="${oldEditable ?: false}" scope="request"/>
                        </g:if>
                    </td>
                </tr>
                </tbody>
            </table>
        </g:if>
</body>
</html>

