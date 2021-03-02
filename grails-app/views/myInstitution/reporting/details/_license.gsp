<%@ page import="de.laser.helper.RDStore; de.laser.License; de.laser.properties.PropertyDefinition; de.laser.properties.LicenseProperty; de.laser.reporting.OrganisationConfig;de.laser.reporting.LicenseConfig;" %>

<h3 class="ui header">3. Details</h3>

<div class="ui message success">
    <p>${label}</p>
</div>

<div class="ui segment">
    <table class="ui table la-table compact">
        <thead>
            <tr>
                <th></th>
                <th>Vertrag</th>
                    <g:if test="${query == 'license-property-assignment'}">
                        <th>Merkmalswert</th>
                    </g:if>
                    <g:else>
                        <th>Lizenzen</th>
                        <th>Teilnehmerverträge</th>
                    </g:else>
                <th>Startdatum</th>
                <th>Enddatum</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${list}" var="lic" status="i">
                <tr>
                    <td>${i + 1}.</td>
                    <td><g:link controller="license" action="show" id="${lic.id}" target="_blank">${lic.reference}</g:link></td>
                    <g:if test="${query == 'license-property-assignment'}">
                        <td>
                            <%
                                LicenseProperty lp = LicenseProperty.findByOwnerAndType(lic, PropertyDefinition.get(id))
                                if (lp) {
                                    if (lp.getType().isRefdataValueType()) {
                                        println lp.getRefValue()?.getI10n('value')
                                    } else {
                                        println lp.getValue()
                                    }
                                }
                            %>
                        </td>
                    </g:if>
                <g:else>
                    <td>
                        <%
                            int subs = License.executeQuery('select count(distinct li.destinationSubscription) from Links li where li.sourceLicense = :lic and li.linkType = :linkType',
                                    [lic: lic, linkType: RDStore.LINKTYPE_LICENSE]
                            )[0]
                            println subs
                        %>
                    </td>
                    <td>
                        <%
                            int instanceOf = License.executeQuery('select count(l) from License l where l.instanceOf = :parent', [parent: lic])[0]
                            println instanceOf
                        %>
                    </td>
                </g:else>
                    %{--<g:else>
                        <td><%
                            int instanceOf = License.executeQuery('select count(l) from License l where l.instanceOf = :parent', [parent: lic])[0]

                            int members = License.executeQuery('select count(li.destinationSubscription) from Links li where li.sourceLicense in (select distinct l from License l where l.instanceOf = :parent) and li.linkType = :linkType',
                                    [parent: lic, linkType: RDStore.LINKTYPE_LICENSE]
                            )[0]

                            println instanceOf + ' / ' + members
                        %></td>
                    </g:else>--}%
                    <td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${lic.startDate}" /></td>
                    <td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${lic.endDate}" /></td>
                </tr>
            </g:each>
        </tbody>
    </table>
</div>