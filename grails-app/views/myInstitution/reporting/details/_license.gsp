<%@ page import="de.laser.reporting.myInstitution.base.BaseDetails; de.laser.IdentifierNamespace; de.laser.Identifier; de.laser.helper.RDStore; de.laser.License; de.laser.properties.PropertyDefinition; de.laser.properties.LicenseProperty; de.laser.reporting.myInstitution.OrganisationConfig;de.laser.reporting.myInstitution.LicenseConfig;" %>
<laser:serviceInjection />

<g:render template="/myInstitution/reporting/details/base.part1" />

<div class="ui segment">
    <table class="ui table la-table compact">
        <thead>
            <tr>
                <th></th>
                <th>${message(code:'license.label')}</th>
                    <g:if test="${query == 'license-property-assignment'}">
                        <th>${message(code:'reporting.details.property.value')}</th>
                    </g:if>
                    <g:elseif test="${query == 'org-identifier-assignment'}">
                        <th>${message(code:'identifier.label')}</th>
                    </g:elseif>
                    <g:else>
                        <g:if test="${contextService.getOrg().getCustomerType() == 'ORG_CONSORTIUM'}">
                            <th>${message(code:'subscription.plural')}</th>
                            <th>${message(code:'license.member.plural')}</th>
                        </g:if>
                        <g:elseif test="${contextService.getOrg().getCustomerType() == 'ORG_INST'}">
                        </g:elseif>
                    </g:else>
                <th>${message(code:'default.startDate.label')}</th>
                <th>${message(code:'default.endDate.label')}</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${list}" var="lic" status="i">
                <tr>
                    <td>${i + 1}.</td>
                    <td>
                        <g:link controller="license" action="show" id="${lic.id}" target="_blank">${lic.reference}</g:link>
                    </td>
                    <g:if test="${query == 'license-property-assignment'}">
                        <td>
                            <%
                                List<LicenseProperty> properties = BaseDetails.getPropertiesGeneric(lic, id as Long, contextService.getOrg()) as List<LicenseProperty>

                                println properties.collect { lp ->
                                    String result = (lp.type.tenant?.id == contextService.getOrg().id) ? '<i class="icon shield alternate"></i>' : ''

                                    if (lp.getType().isRefdataValueType()) {
                                        result += (lp.getRefValue() ? lp.getRefValue().getI10n('value') : '')
                                    } else {
                                        result += lp.getValue()
                                    }
                                    result
                                }.findAll().join(' ,<br/>') // removing empty and null values
                            %>
                        </td>
                    </g:if>
                    <g:elseif test="${query == 'org-identifier-assignment'}">
                        <td>
                            <%
                                List<Identifier> identList = Identifier.findAllByLicAndNs(lic, IdentifierNamespace.get(id))
                                println identList.collect{ it.value ?: null }.findAll().join(' ,<br/>') // removing empty and null values
                            %>
                        </td>
                    </g:elseif>
                    <g:else>
                        <g:if test="${contextService.getOrg().getCustomerType() == 'ORG_CONSORTIUM'}">
                            <td>
                                <%
                                    println License.executeQuery('select count(distinct li.destinationSubscription) from Links li where li.sourceLicense = :lic and li.linkType = :linkType',
                                            [lic: lic, linkType: RDStore.LINKTYPE_LICENSE]
                                    )[0]
                                %>
                            </td>
                            <td>
                                <% println License.executeQuery('select count(l) from License l where l.instanceOf = :parent', [parent: lic])[0] %>
                            </td>
                        </g:if>
                        <g:elseif test="${contextService.getOrg().getCustomerType() == 'ORG_INST'}">
                        </g:elseif>
                    </g:else>
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

<g:render template="/myInstitution/reporting/export/chartDetailsModal" model="[modalID: 'chartDetailsExportModal', token: token]" />
