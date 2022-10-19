<laser:htmlStart message="menu.yoda.userRoleMatrix" />

<ui:breadcrumbs>
    <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
    <ui:crumb message="menu.yoda.userRoleMatrix" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.yoda.userRoleMatrix" />

<div class="ui accordion">
    <g:each in="${matrix}" var="role">
        <g:if test="${role.value.size() > 0}">
            <div class="title">
                <strong><i class="dropdown icon"></i> ${role.key} ( ${role.value.size()} Nutzer )</strong>
            </div>
            <div class="content">
                <table class="ui celled la-js-responsive-table la-table compact table">
                    <tbody>
                        <g:each in="${role.value.toSorted{a,b -> a.username <=> b.username}}" var="user">
                            <tr>
                                <td>${user.username}</td>
                                <td>${user.display}</td>
                                <td>${user.email}</td>
                                <td class="center aligned">
                                    <g:link controller="user" action="edit" id="${user.id}" class="ui icon button">
                                        <i class="ui icon write"></i>
                                    </g:link>
                                </td>
                            </tr>
                        </g:each>
                    </tbody>
                </table>
            </div>
        </g:if>
    </g:each>
</div>

<laser:htmlEnd />