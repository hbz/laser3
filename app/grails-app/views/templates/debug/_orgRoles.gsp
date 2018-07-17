<g:if test="${grailsApplication.config.showDebugInfo}">

    <div class="ui red segment">
        <h4 class="ui red header">
            <i class="bug icon"></i> DEBUG-INFORMATION
        </h4>

        <table class="ui celled la-table la-table-small table">
            <thead>
                <tr>
                    <th>ORG</th>
                    <th>RDV</th>
                    <th>OBJ</th>
                </tr>
            </thead>
            <tr>
                ${debug}
            </tr>
            <g:each in="${debug}" var="role">
                <tr>
                    <td>
                        <g:if test="${role.org}">
                            <g:link controller="organisations" action="show" id="${role.org.id}">${role?.org.name} (${role.org.id})</g:link>
                        </g:if>
                    </td>
                    <td>
                        ${role?.roleType?.getI10n("value")} / ${role?.roleType?.value}
                    </td>
                    <td>
                        <g:if test="${role.pkg}">
                            <g:link controller="packageDetails" action="show" id="${role.pkg.id}">${role?.pkg.name} (${role.pkg.id})</g:link>
                        </g:if>
                        <g:if test="${role.sub}">
                            <g:link controller="subscriptionDetails" action="show" id="${role.sub.id}">${role?.sub.name} (${role.sub.id})</g:link>
                        </g:if>
                        <g:if test="${role.lic}">
                            <g:link controller="licenseDetails" action="show" id="${role.lic.id}">${role?.lic.reference} (${role.lic.id})</g:link>
                        </g:if>
                        <g:if test="${role.cluster}">
                            <g:link controller="cluster" action="show" id="${role.cluster.id}">${role?.cluster.name} (${role.cluster.id})</g:link>
                        </g:if>
                        <g:if test="${role.title}">
                            <g:link controller="titleDetails" action="show" id="${role.title.id}">${role?.title.title} (${role.title.id})</g:link>
                        </g:if>
                    </td>
                </tr>
            </g:each>
        </table>
    </div>
</g:if>
