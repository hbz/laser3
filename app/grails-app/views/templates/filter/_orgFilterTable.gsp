
<table class="ui celled la-table table">
    <thead>
    <tr>
        <g:if test="${tmplShowCheckbox}">
            <th>
                <g:checkBox name="orgListToggler" id="orgListToggler" checked="false"/>
            </th>
        </g:if>
        <th>${message(code: 'org.name.label', default: 'Name')}</th>
        <th>WIB</th>
        <th>ISIL</th>
        <th>${message(code: 'org.type.label', default: 'Type')}</th>
        <th>${message(code: 'org.sector.label', default: 'Sector')}</th>
        <th>${message(code: 'org.federalState.label')}</th>
        <th>${message(code: 'org.libraryNetwork.label')}</th>
        <th>${message(code: 'org.libraryType.label')}</th>
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
                    <g:checkBox type="text" name="selectedOrgs" value="${org.id}" checked="false" />
                </td>
            </g:if>
            <td>
                <g:if test="${tmplDisableOrgIds && (org.id in tmplDisableOrgIds)}">
                    <g:if test="${org.shortname}">
                        ${fieldValue(bean: org, field: "shortname")}
                    </g:if>
                    <g:else>
                        ${fieldValue(bean: org, field: "name")}
                    </g:else>
                </g:if>
                <g:else>
                    <g:link controller="organisations" action="show" id="${org.id}">
                        <g:if test="${org.shortname}">
                            ${fieldValue(bean: org, field: "shortname")}
                        </g:if>
                        <g:else>
                            ${fieldValue(bean: org, field: "name")}
                        </g:else>
                    </g:link>
                </g:else>
            </td>
            <td>${org.getIdentifiersByType('wib')?.value?.join(', ')}</td>
            <td>${org.getIdentifiersByType('isil')?.value?.join(', ')}</td>
            <td>${org.orgType?.getI10n('value')}</td>
            <td>${org.sector?.getI10n('value')}</td>
            <td>${org.federalState?.getI10n('value')}</td>
            <td>${org.libraryNetwork?.getI10n('value')}</td>
            <td>${org.libraryType?.getI10n('value')}</td>
        </tr>
    </g:each>
    </tbody>
</table>


<g:if test="${tmplShowCheckbox}">
    <script language="JavaScript">
        $('#orgListToggler').click(function() {
            if($(this).prop('checked')) {
                $("tr[class!=disabled] input[name=selectedOrgs]").prop('checked', true)
            }
            else {
                $("tr[class!=disabled] input[name=selectedOrgs]").prop('checked', false)
            }
        })
    </script>
</g:if>