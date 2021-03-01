<%@ page import="de.laser.helper.RDStore; de.laser.License; de.laser.properties.PropertyDefinition; de.laser.properties.LicenseProperty; de.laser.reporting.OrganisationConfig;de.laser.reporting.LicenseConfig;" %>

<h3 class="ui header">3. Details</h3>

<div class="ui message success">
    <p>${label}</p>
</div>

<div class="ui segment">
    <table class="ui table la-table compact">
        %{-- <thead>
        <tr>
            <th></th>
            <th></th>
            <th></th>
        </tr>
        </thead> --}%
        <tbody>
            <g:each in="${list}" var="lic" status="i">
                <tr>
                    <td>${i + 1}.</td>
                    <td>${lic.reference}</td>
                    <g:if test="${query == ''}">
                        <td><g:link controller="license" action="show" id="${lic.id}" target="_blank">${lic.reference}</g:link></td>
                    </g:if>
                    <g:else>  %{-- default --}%
                        <td><g:link controller="license" action="show" id="${org.id}" target="_blank">${lic.reference}</g:link></td>
                    </g:else>
                </tr>
            </g:each>
        </tbody>
    </table>
</div>