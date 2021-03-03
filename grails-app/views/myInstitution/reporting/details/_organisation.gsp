<%@ page import="de.laser.properties.OrgProperty; de.laser.IdentifierNamespace; de.laser.Identifier; de.laser.helper.RDStore; de.laser.Org; de.laser.properties.PropertyDefinition; de.laser.reporting.OrganisationConfig;" %>

<h3 class="ui header">3. Details</h3>

<div class="ui message success">
    <p>${label}</p>
</div>

<div class="ui segment">
    <table class="ui table la-table compact">
        <thead>
        <tr>
            <th></th>
            <th>Sortiername</th>
            <th>${label.split('>').first().trim()}</th>
            <g:if test="${query == 'org-property-assignment'}">
                <th>Merkmalswert</th>
            </g:if>
            <g:elseif test="${query == 'org-identifier-assignment'}">
                <th>Identifikator</th>
            </g:elseif>
        </tr>
        </thead>
        <tbody>
            <g:each in="${list}" var="org" status="i">
                <tr>
                    <td>${i + 1}.</td>
                    <td>${org.sortname}</td>
                    <td>
                        <g:link controller="organisation" action="show" id="${org.id}" target="_blank">${org.name}</g:link>
                    </td>
                    <g:if test="${query == 'org-property-assignment'}">
                        <td>
                            <%
                                OrgProperty op = OrgProperty.findByOwnerAndType(org, PropertyDefinition.get(id))
                                if (op) {
                                    if (op.getType().isRefdataValueType()) {
                                        println op.getRefValue()?.getI10n('value')
                                    } else {
                                        println op.getValue()
                                    }
                                }
                            %>
                        </td>
                    </g:if>
                    <g:elseif test="${query == 'org-identifier-assignment'}">
                        <td>
                            <% println Identifier.findByOrgAndNs(org, IdentifierNamespace.get(id)).value %>
                        </td>
                    </g:elseif>
                </tr>
            </g:each>
        </tbody>
    </table>
</div>