<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <title>${message(code:'laser')} : ${message(code:'menu.yoda.userMatrix')}</title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
    <semui:crumb message="menu.yoda.userMatrix" class="active"/>
</semui:breadcrumbs>
<br>
<h2 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'menu.yoda.userMatrix')}</h2>

<br />

<div class="ui accordion">
    <g:each in="${matrix}" var="role">
        <g:if test="${role.value.size() > 0}">
            <div class="title">
                <h4 class="ui headline">
                    <i class="dropdown icon"></i> ${role.key} ( ${role.value.size()} Nutzer )
                </h4>
            </div>
            <div class="content">
                <table class="ui celled la-table la-table-small table">
                    <tbody>
                        <g:each in="${role.value.toSorted{a,b -> a.username <=> b.username}}" var="user">
                            <tr>
                                <td>${user.username}</td>
                                <td>${user.display}</td>
                                <td>${user.email}</td>
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