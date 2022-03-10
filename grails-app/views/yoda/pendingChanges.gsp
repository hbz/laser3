<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : Pending Changes</title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
    <semui:crumb text="Pending Changes" class="active"/>
</semui:breadcrumbs>

    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />Pending Changes</h1>

    <table class="ui celled table">

       <g:each in="${pending}" var="pc">
           <g:set var="oid" value="${pc.resolveOID()}" />

            <tr>
                <td>${pc.id}</td>

                <td>${oid?.class?.simpleName} (${oid?.id})</td>

                <td><g:link controller="${oid?.class?.simpleName}" action="show" id="${oid?.id}">${oid}</g:link></td>

                <td><g:formatDate date="${pc.ts}" format="${message(code:'default.date.format.noZ')}" /></td>
            </tr>
        </g:each>
    </table>

</body>
</html>
