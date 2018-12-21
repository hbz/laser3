<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <title>${message(code:'laser', default:'LAS:eR')} : Cache Info</title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
    <semui:crumb text="Cache Info" class="active"/>
</semui:breadcrumbs>

<h1 class="ui left aligned icon header"><semui:headerIcon />Quartz Info</h1>

<h3 class="ui header">Triggers</h3>

<table class="ui celled la-table table">
    <thead>
        <tr>
            <th></th>
            <th></th>
        </tr>
    </thead>
    <tbody>

        <g:each in="${triggers}" var="key, group">
            <h5 class="ui header">${key}</h5>

            <g:each in="${group}" var="key2, trigger">
                ${key2} - ${trigger.nextFireTime} <br/>
            </g:each>
        </g:each>
        </tr>
    </tbody>
</table>

<h3 class="ui header">Jobs</h3>

<g:each in="${jobs}" var="key, group">
    <h5 class="ui header">${key}</h5>

    <g:each in="${group}" var="key2, job">
        ${key2} - ${job} <br/>
    </g:each>
</g:each>



</body>
</html>