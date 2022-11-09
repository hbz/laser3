
    <div style="padding: 1em 0;">
        <h5 class="ui header">PERSON_ROLES</h5>

        <table class="ui celled la-js-responsive-table la-table compact table la-ignore-fixed">
            <thead>
                <tr>
                    <th></th>
                    <th>PRS</th>
                    <th>ORG</th>
                    <th>RDV</th>
                    <th>OBJ</th>
                </tr>
            </thead>
            <g:each in="${debug.sort{it.id}}" status="c" var="role">
                <tr>
                    <td>${c+1}</td>
                    <td>
                        <g:if test="${role.prs}">
                            ${role.prs}
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${role.org}">
                            <g:link controller="organisation" action="show" id="${role.org.id}">${role.org.name} (${role.org.id})</g:link>
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${role.functionType}">   
                            ${role.functionType?.getI10n("value")} / ${role.functionType?.value}
                        </g:if>
                        <g:if test="${role.responsibilityType}">
                            ${role.responsibilityType?.getI10n("value")} / ${role.responsibilityType?.value}
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${role.pkg}">
                            <g:link controller="package" action="show" id="${role.pkg.id}">${role.pkg.name} (${role.pkg.id})</g:link>
                        </g:if>
                        <g:if test="${role.sub}">
                            <g:link controller="subscription" action="show" id="${role.sub.id}">${role.sub.name} (${role.sub.id})</g:link>
                        </g:if>
                        <g:if test="${role.lic}">
                            <g:link controller="license" action="show" id="${role.lic.id}">${role.lic.reference} (${role.lic.id})</g:link>
                        </g:if>
                        <g:if test="${role.tipp}">
                            <g:link controller="tipp" action="show" id="${role.tipp.id}">${role.tipp.name} (${role.tipp.id})</g:link>
                        </g:if>
                    </td>
                </tr>
            </g:each>
        </table>
    </div>

