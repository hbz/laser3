<table class="ui sortable celled la-table table">
    <thead>
    <tr>
        <g:if test="${tmplShowCheckbox}">
            <th>
                <g:if test="${orgList}">
                    <g:checkBox name="orgListToggler" id="orgListToggler" checked="false"/>
                </g:if>
            </th>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('name')}">
            <g:sortableColumn title="${message(code: 'org.name.label', default: 'Name')}" property="lower(o.name)"/>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('identifier')}">
            <th>Identifier</th>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('wib')}">
            <th>WIB</th>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('isil')}">
            <th>ISIL</th>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('type')}">
            <th>${message(code: 'org.type.label', default: 'Type')}</th>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('sector')}">
            <th>${message(code: 'org.sector.label', default: 'Sector')}</th>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('federalState')}">
            <th>${message(code: 'org.federalState.label')}</th>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('libraryNetwork')}">
            <th>${message(code: 'org.libraryNetwork.label')}</th>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('libraryType')}">
            <th>${message(code: 'org.libraryType.label')}</th>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('country')}">
            <th>${message(code: 'org.country.label')}</th>
        </g:if>
        <g:if test="${tmplConfigShow?.contains('addSubMembers')}">
            <th>
                ${message(code: 'subscription.details.addMembers.option.package.label')}
            </th>
            <th>
                ${message(code: 'subscription.details.addMembers.option.issueEntitlement.label')}
            </th>
        </g:if>
    </tr>
    </thead>
    <tbody>
        <g:each in="${orgList}" var="org">
            <g:if test="${tmplDisableOrgIds && (org.id in tmplDisableOrgIds)}">
                <tr class="disabled">
            </g:if>
            <g:else>
                <tr>
            </g:else>
            <g:if test="${tmplShowCheckbox}">
                <td>
                    <g:checkBox type="text" name="selectedOrgs" value="${org.id}" checked="false"/>
                </td>
            </g:if>
            <td>
                <g:if test="${tmplDisableOrgIds && (org.id in tmplDisableOrgIds)}">
                        ${fieldValue(bean: org, field: "name")} <br>
                        <g:if test="${org.shortname}">
                            (${fieldValue(bean: org, field: "shortname")})
                        </g:if>
                </g:if>
                <g:else>
                    <g:link controller="organisations" action="show" id="${org.id}">
                        ${fieldValue(bean: org, field: "name")} <br>
                        <g:if test="${org.shortname}">
                            (${fieldValue(bean: org, field: "shortname")})
                        </g:if>
                    </g:link>
                </g:else>
            </td>

            <g:if test="${tmplConfigShow?.contains('identifier')}">
                <td><g:if test="${org.ids}">
                    <ul>
                        <g:each in="${org.ids.sort{it.identifier.ns.ns}}" var="id"><li>${id.identifier.ns.ns}: ${id.identifier.value}</li></g:each>
                    </ul>
                </g:if></td>
            </g:if>
            <g:if test="${tmplConfigShow?.contains('wib')}">
                <td>${org.getIdentifiersByType('wib')?.value?.join(', ')}</td>
            </g:if>
            <g:if test="${tmplConfigShow?.contains('isil')}">
                <td>${org.getIdentifiersByType('isil')?.value?.join(', ')}</td>
            </g:if>
            <g:if test="${tmplConfigShow?.contains('type')}">
                <td>${org.orgType?.getI10n('value')}</td>
            </g:if>
            <g:if test="${tmplConfigShow?.contains('sector')}">
                <td>${org.sector?.getI10n('value')}</td>
            </g:if>
            <g:if test="${tmplConfigShow?.contains('federalState')}">
                <td>${org.federalState?.getI10n('value')}</td>
            </g:if>
            <g:if test="${tmplConfigShow?.contains('libraryNetwork')}">
                <td>${org.libraryNetwork?.getI10n('value')}</td>
            </g:if>
            <g:if test="${tmplConfigShow?.contains('libraryType')}">
                <td>${org.libraryType?.getI10n('value')}</td>
            </g:if>
            <g:if test="${tmplConfigShow?.contains('country')}">
                <td>${org.country?.getI10n('value')}</td>
            </g:if>

            <g:if test="${tmplConfigShow?.contains('addSubMembers')}">
                <g:if test="${subInstance?.packages}">
                    <td><g:each in="${subInstance?.packages}" >
                        <g:checkBox type="text" id="selectedPackage_${org.id+it.pkg.id}" name="selectedPackage_${org.id+it.pkg.id}" value="1"
                                    checked="false" onclick="checkselectedPackage(${org.id+it.pkg.id});"/> ${it.pkg.name}<br>
                        </g:each>
                    </td>
                    <td><g:each in="${subInstance?.packages}" >
                        <g:checkBox type="text" id="selectedIssueEntitlement_${org.id+it.pkg.id}"
                                    name="selectedIssueEntitlement_${org.id+it.pkg.id}" value="1" checked="false"
                                    onclick="checkselectedIssueEntitlement(${org.id+it.pkg.id});"/> ${it.pkg.name}<br>
                    </g:each>
                    </td>
                </g:if><g:else>
                    <td>${message(code: 'subscription.details.addMembers.option.noPackage.label', args: [subInstance?.name])}</td>
                    <td>${message(code: 'subscription.details.addMembers.option.noPackage.label', args: [subInstance?.name])}</td>
                </g:else>
            </g:if>
            </tr>
        </g:each>
    </tbody>
</table>

<g:if test="${tmplShowCheckbox}">
    <script language="JavaScript">
        $('#orgListToggler').click(function () {
            if ($(this).prop('checked')) {
                $("tr[class!=disabled] input[name=selectedOrgs]").prop('checked', true)
            }
            else {
                $("tr[class!=disabled] input[name=selectedOrgs]").prop('checked', false)
            }
        })
        <g:if test="${tmplConfigShow?.contains('addSubMembers')}">
            function checkselectedIssueEntitlement(selectedid) {
                if ($('#selectedIssueEntitlement_' + selectedid).prop('checked')) {
                    $('#selectedPackage_' + selectedid).prop('checked', false);
                }
            }
            function checkselectedPackage(selectedid) {
                if ($('#selectedPackage_' + selectedid).prop('checked')) {
                    $('#selectedIssueEntitlement_' + selectedid).prop('checked', false);
                }

            }
        </g:if>
    </script>
</g:if>
