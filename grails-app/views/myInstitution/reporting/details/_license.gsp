<%@ page import="de.laser.Subscription; de.laser.reporting.report.myInstitution.base.BaseDetails; de.laser.IdentifierNamespace; de.laser.Identifier; de.laser.storage.RDStore; de.laser.License; de.laser.properties.PropertyDefinition; de.laser.properties.LicenseProperty;" %>
<laser:serviceInjection />

<laser:render template="/myInstitution/reporting/details/top" />

<div class="ui segment">
    <table class="ui table la-js-responsive-table la-table compact">
        <thead>
            <tr>
                <th></th>
                <th>${message(code:'license.label')}</th>
                    <g:if test="${query == 'license-x-identifier'}">
                        <th>${message(code:'identifier.label')}</th>
                    </g:if>
                    <g:if test="${contextService.getOrg().getCustomerType() in ['ORG_CONSORTIUM', 'ORG_CONSORTIUM_PRO']}">
                        <th>${message(code:'subscription.plural')}</th>

                        <g:if test="${query != 'license-x-identifier' && query != 'license-x-property'}">
                            <th>${message(code:'license.member.plural')}</th>
                            <th>${message(code:'subscription.member.plural')}</th>
                        </g:if>
                    </g:if>
                    <g:if test="${query == 'license-x-property'}">
                        <th>${message(code:'reporting.details.property.value')}</th>
                    </g:if>
                <th>${message(code:'default.startDate.label')}</th>
                <th>${message(code:'default.endDate.label')}</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${list}" var="lic" status="i">
                %{-- <tr class="top aligned"> --}%
                <tr>
                    <td>${i + 1}.</td>
                    <td>
                        <g:link controller="license" action="show" id="${lic.id}" target="_blank">${lic.reference}</g:link>
                    </td>
                    <g:if test="${query == 'license-x-identifier'}">
                        <td>
                            <%
                                List<Identifier> identList = Identifier.findAllByLicAndNs(lic, IdentifierNamespace.get(id))
                                println identList.collect{ it.value ?: null }.findAll().join(' ,<br/>') // removing empty and null values
                            %>
                        </td>
                    </g:if>

                    <g:if test="${contextService.getOrg().getCustomerType() in ['ORG_CONSORTIUM', 'ORG_CONSORTIUM_PRO']}">
                        <td>
                            <%
                                println Subscription.executeQuery(
                                        'select status, count(status) from Links li join li.destinationSubscription sub join sub.status status where li.sourceLicense = :lic and li.linkType = :linkType group by status',
                                        [lic: lic, linkType: RDStore.LINKTYPE_LICENSE]
                                ).collect { it[1] + ' ' + it[0].getI10n('value').toLowerCase() /*'<span class="ui circular label">' + it[1] + '</span> ' + it[0].getI10n('value') */ }.join('<br />')
                            %>
                        </td>

                        <g:if test="${query != 'license-x-identifier' && query != 'license-x-property'}">
                            <td>
                                <%
                                    println License.executeQuery('select count(l) from License l where l.instanceOf = :parent group by l.reference', [parent: lic])[0]
                                %>
                            </td>
                            <td>
                                <%
                                    println License.executeQuery('select l from License l where l.instanceOf = :parent order by l.sortableReference, l.reference', [parent: lic]).collect { ll ->
                                        Subscription.executeQuery(
                                                'select status, count(status) from Links li join li.destinationSubscription sub join sub.status status where li.sourceLicense = :lic and li.linkType = :linkType group by status',
                                                    [lic: ll, linkType: RDStore.LINKTYPE_LICENSE]
                                                ).collect { it[1] + ' ' + it[0].getI10n('value').toLowerCase() }.join('<br />')
                                    }.join(', <br />')
                                %>
                            </td>
                        </g:if>
                    </g:if>

                    <g:if test="${query == 'license-x-property'}">
                        <td>
                            <uiReporting:objectProperties owner="${lic}" tenant="${contextService.getOrg()}" propDefId="${id}" />
                        </td>
                    </g:if>

                    <td>
                        <g:formatDate format="${message(code:'default.date.format.notime')}" date="${lic.startDate}" />
                    </td>
                    <td>
                        <g:formatDate format="${message(code:'default.date.format.notime')}" date="${lic.endDate}" />
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>
</div>

<laser:render template="/myInstitution/reporting/export/detailsModal" model="[modalID: 'detailsExportModal', token: token]" />
