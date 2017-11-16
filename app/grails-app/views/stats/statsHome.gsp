<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} Data import explorer</title>
    </head>

    <body>

        <semui:breadcrumbs>
            <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
            <semui:crumb text="Stats" class="active"/>

            <li class="dropdown pull-right">
                <a class="dropdown-toggle badge" id="export-menu" role="button" data-toggle="dropdown" data-target="#" href="">Exports<b class="caret"></b></a>
                <ul class="dropdown-menu filtering-dropdown-menu" role="menu" aria-labelledby="export-menu">
                    <li>
                        <g:link controller="stats" action="statsHome" params="${[format:'csv']}">CSV</g:link>
                    </li>
                </ul>
            </li>
        </semui:breadcrumbs>

        <div>
            <div class="row">
                <div class="span12">

                    <h1 class="ui header">Org Info</h1>
                    <table class="ui celled table">
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
                </div>
            </div>
        </div>
    </body>
</html>
