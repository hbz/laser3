<%@ page import="de.laser.survey.SurveyPersonResult; de.laser.Org" %>

<g:if test="${participant}">
    <ui:greySegment>
        <g:set var="choosenOrg" value="${Org.findById(participant.id)}"/>
        <g:set var="choosenOrgCPAs" value="${choosenOrg?.getGeneralContactPersons(false)}"/>
        <g:set var="surveyPersons"
               value="${surveyConfig ? SurveyPersonResult.executeQuery('select spr.person from SurveyPersonResult spr where spr.surveyPerson = true and spr.participant = :participant and spr.surveyConfig = :surveyConfig', [participant: choosenOrg, surveyConfig: surveyConfig]) : []}"/>

        <table class="ui table la-js-responsive-table la-table compact">
            <tbody>
            <tr>
                <td>
                    <p><strong><g:link controller="organisation" action="show"
                                       id="${choosenOrg.id}">${choosenOrg.name} (${choosenOrg.sortname})</g:link></strong></p>

                    ${choosenOrg?.libraryType?.getI10n('value')}
                </td>
                <td>
                    <g:if test="${surveyPersons}">
                        <g:set var="oldEditable" value="${editable}"/>
                        <g:set var="editable" value="${false}" scope="request"/>
                        <g:each in="${surveyPersons}" var="gcp">
                            <laser:render template="/addressbook/person_details"
                                          model="${[person: gcp, tmplHideLinkToAddressbook: true, showFunction: true]}"/>
                        </g:each>
                        <g:set var="editable" value="${oldEditable ?: false}" scope="request"/>
                    </g:if>
                    <g:elseif test="${choosenOrgCPAs}">
                        <g:set var="oldEditable" value="${editable}"/>
                        <g:set var="editable" value="${false}" scope="request"/>
                        <g:each in="${choosenOrgCPAs}" var="gcp">
                            <laser:render template="/addressbook/person_details"
                                          model="${[person: gcp, tmplHideLinkToAddressbook: true, showFunction: true]}"/>
                        </g:each>
                        <g:set var="editable" value="${oldEditable ?: false}" scope="request"/>
                    </g:elseif>
                </td>
            </tr>
            </tbody>
        </table>

    </ui:greySegment>
</g:if>