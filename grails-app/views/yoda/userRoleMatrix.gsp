<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'menu.yoda.userRoleMatrix')}</title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
    <semui:crumb message="menu.yoda.userRoleMatrix" class="active"/>
</semui:breadcrumbs>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'menu.yoda.userRoleMatrix')}</h1>

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
                                <td class="x">
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

</body>
</html>