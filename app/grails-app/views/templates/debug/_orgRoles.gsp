
    <div style="padding: 1em 0;">
        <h5 class="ui red header">ORG_ROLES</h5>

        <table class="ui celled la-table la-table-small table ignore-floatThead">
            <thead>
                <tr>
                    <th></th>
                    <th>ORG</th>
                    <th>RDV</th>
                    <th>OBJ</th>
                </tr>
            </thead>
            <g:each in="${debug.sort{it.id}}" status="c" var="role">
                <tr>
                    <td>${c+1}</td>
                    <td>
                        <g:if test="${role.org}">
                            <g:link controller="organisations" action="show" id="${role.org.id}">${role.org.name} (${role.org.id})</g:link>
                        </g:if>
                    </td>
                    <td>
                        ${role.roleType?.getI10n("value")} / ${role.roleType?.value}
                    </td>
                    <td>
                        <g:if test="${role.pkg}">
                            <g:link controller="package" action="show" id="${role.pkg.id}">${role.pkg.name} (${role.pkg.id})</g:link>
                        </g:if>
                        <g:if test="${role.sub}">
                            <g:link controller="subscriptionDetails" action="show" id="${role.sub.id}">${role.sub.name} (${role.sub.id})</g:link>
                        </g:if>
                        <g:if test="${role.lic}">
                            <g:link controller="license" action="show" id="${role.lic.id}">${role.lic.reference} (${role.lic.id})</g:link>
                        </g:if>
                        <g:if test="${role.cluster}">
                            <g:link controller="cluster" action="show" id="${role.cluster.id}">${role.cluster.name} (${role.cluster.id})</g:link>
                        </g:if>
                        <g:if test="${role.title}">
                            <g:link controller="title" action="show" id="${role.title.id}">${role.title.title} (${role.title.id})</g:link>
                        </g:if>
                    </td>
                </tr>
            </g:each>
        </table>
    </div>

