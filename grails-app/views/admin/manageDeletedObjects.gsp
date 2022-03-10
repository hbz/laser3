<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code: "menu.admin.deletedObjects")}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
    <semui:crumb message="menu.admin.deletedObjects" class="active"/>
</semui:breadcrumbs>

<h1 class="ui header la-noMargin-top">${message(code: "menu.admin.deletedObjects")}</h1>

<div class="ui grid">
    <div class="twelve wide column">

        <table class="ui celled la-js-responsive-table la-table compact la-ignore-fixed table">
            <thead>
                <tr>
                    <th>Objekt</th>
                    <th>Anzahl</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${stats}" var="row">
                    <tr>
                        <td>${row.key}</td>
                        <td>${row.value[0]}</td>
                    </tr>
                </g:each>
            </tbody>
        </table>

    </div>
</div>

</body>
</html>
