<!doctype html>
<html>
    <head>
        <meta name="layout" content="laser">
        <title>${message(code:'laser')} : ${message(code:'menu.admin.statistics')}</title>
    </head>

    <body>

        <semui:breadcrumbs>
            <semui:crumb message="menu.admin" controller="admin" action="index"/>
            <semui:crumb message="menu.admin.statistics" class="active"/>
        </semui:breadcrumbs>

        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'menu.admin.statistics')}</h1>

        <table class="ui celled la-js-responsive-table la-table table">
            <thead>
                <tr>
                    <th>Institution</th>
                    <th>Affiliated Users</th>
                    <th>Total subscriptions</th>
                    <th>Current subscriptions</th>
                    <th>Total licenses</th>
                    <th>Current licenses</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${orginfo}" var="is">
                    <tr>
                        <td>${is.key.name}</td>
                        <td>${is.value['userCount']}</td>
                        <td>${is.value['subCount']}</td>
                        <td>${is.value['currentSoCount']}</td>
                        <td>${is.value['licCount']}</td>
                        <td>${is.value['currentLicCount']}</td>
                    </tr>
                </g:each>
            </tbody>
        </table>

    </body>
</html>
