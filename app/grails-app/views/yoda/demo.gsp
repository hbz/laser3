<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <title>${message(code:'laser', default:'LAS:eR')} : Application Security</title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
    <semui:crumb text="Application Security" class="active"/>
</semui:breadcrumbs>

<h1 class="ui header">debug only</h1>

<pre>
user: ${user}

roles: <g:each in="${roles}" var="role"><br />${role}</g:each>

affiliations: <g:each in="${affiliations}" var="aff"><br />${aff}</g:each>

${check1}

${check2}

${check3}

${check4}
</pre>

</body>
</html>
