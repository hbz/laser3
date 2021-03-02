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
            %{--<th>Lizenzen</th>--}%
            %{--<th>Teilnehmerverträge / Lizenzen</th>--}%
            <th>Startdatum</th>
            <th>Enddatum</th>
        </tr>
        </thead>
        <tbody>
            <g:each in="${list}" var="lic" status="i">
                <tr>
                    <td>${i + 1}.</td>
                    <g:if test="${true}">%{-- default --}%
                        <td><g:link controller="license" action="show" id="${lic.id}" target="_blank">${lic.reference}</g:link></td>
                    </g:if>
                    %{--<g:else>
                        <td><g:link controller="license" action="show" id="${lic.id}" target="_blank">${lic.reference}</g:link></td>
                        <td><%
                            print License.executeQuery('select count(li.destinationSubscription) from Links li where li.sourceLicense = :license and li.linkType = :linkType',
                                    [license: lic, linkType: RDStore.LINKTYPE_LICENSE]
                            )[0]
                        %></td>
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