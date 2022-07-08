<laser:htmlStart message="menu.admin.statistics" />

        <semui:breadcrumbs>
            <semui:crumb message="menu.admin" controller="admin" action="index"/>
            <semui:crumb message="menu.admin.statistics" class="active"/>
        </semui:breadcrumbs>

        <semui:h1HeaderWithIcon message="menu.admin.statistics" />

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

<laser:htmlEnd />
