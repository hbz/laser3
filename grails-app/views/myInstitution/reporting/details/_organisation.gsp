<%@ page import="de.laser.properties.OrgProperty; de.laser.IdentifierNamespace; de.laser.Identifier; de.laser.helper.RDStore; de.laser.Org; de.laser.properties.PropertyDefinition; de.laser.reporting.myInstitution.OrganisationConfig;" %>
<laser:serviceInjection />

<g:render template="/myInstitution/reporting/details/base.part1" />

<div class="ui segment">
    <table class="ui table la-table compact">
        <thead>
        <tr>
            <th></th>
            <th>${message(code:'org.sortname.label')}</th>
            %{-- TODO --}%<th>${(labels.first().trim() != 'Verteilung') ? labels.first().trim() : 'Name'}</th>%{-- TODO --}%
            <g:if test="${query == 'org-property-assignment'}">
                <th>${message(code:'reporting.details.property.value')}</th>
            </g:if>
            <g:elseif test="${query == 'org-identifier-assignment'}">
                <th>${message(code:'identifier.label')}</th>
            </g:elseif>
            <g:if test="${query.startsWith('provider-')}">
                <th>${message(code:'org.platforms.label')}</th>
            </g:if>
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
                                List<OrgProperty> properties = OrgProperty.executeQuery(
                                        "select op from OrgProperty op join op.type pd where op.owner = :org and pd.id = :pdId " +
                                                "and (op.isPublic = true or op.tenant = :ctxOrg) and pd.descr like '%Property' ",
                                        [org: org, pdId: id as Long, ctxOrg: contextService.getOrg()]
                                )
                                println properties.collect { op ->
                                    if (op.getType().isRefdataValueType()) {
                                        op.getRefValue()?.getI10n('value')
                                    } else {
                                        op.getValue()
                                    }
                                }.findAll().join(' ,<br/>') // removing empty and null values
                            %>
                        </td>
                    </g:if>
                    <g:elseif test="${query == 'org-identifier-assignment'}">
                        <td>
                            <%
                                List<Identifier> identList = Identifier.findAllByOrgAndNs(org, IdentifierNamespace.get(id))
                                println identList.collect{ it.value ?: null }.findAll().join(' ,<br/>') // removing empty and null values
                            %>
                        </td>
                    </g:elseif>
                    <g:if test="${query.startsWith('provider-')}">
                        <td>
                            <g:each in="${org.platforms}" var="plt">
                                <g:link controller="platform" action="show" id="${plt.id}" target="_blank">${plt.name}</g:link><br/>
                            </g:each>
                        </td>
                    </g:if>
                </tr>
            </g:each>
        </tbody>
    </table>
</div>

<g:render template="/templates/copyEmailaddresses" model="[modalID: 'chartDetailsCopyEmailModal', orgList: list]" />

<g:render template="/templates/reporting/chartExport" model="[modalID: 'chartDetailsExportModal', query: query, orgList: list]" />

<laser:script file="${this.getGroovyPageFileName()}">
    r2d2.initDynamicSemuiStuff('#chart-details')
</laser:script>