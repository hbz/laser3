<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : Yoda Dashboard</title>
</head>

<body>

    <semui:breadcrumbs>
        <semui:crumb message="menu.yoda.dash" class="active"/>
    </semui:breadcrumbs>

    <h1 class="ui left aligned icon header"><semui:headerIcon />Yoda Dashboard</h1>

    <div class="ui equal width grid">
        <div class="row">

            <div class="column">
                <div class="ui divided relaxed list">
                    <div class="item">
                        <g:link controller="yoda" action="manageSystemMessage">${message(code: 'menu.admin.systemMessage')}</g:link>
                    </div>
                    <div class="item">
                        <g:link controller="admin" action="dataConsistency">${message(code: "menu.admin.dataConsistency")}</g:link>
                    </div>
                    <div class="item">
                        <br />
                        <%--<g:link controller="admin" action="manageAffiliationRequests">${message(code: "menu.institutions.affiliation_requests")}</g:link>--%>
                    </div>
                </div>
            </div>

            <div class="column">
                <div class="ui divided relaxed list">
                    <div class="item">
                        <g:link controller="admin" action="systemEvents">${message(code:'menu.admin.systemEvents')}</g:link>
                    </div>
                    <div class="item">
                        <g:link controller="yoda" action="appSecurity">${message(code:'menu.yoda.security')}</g:link>
                    </div>
                    <div class="item">
                        <g:link controller="yoda" action="userMatrix">${message(code:'menu.yoda.userMatrix')}</g:link>
                    </div>
                </div>
            </div>

            <div class="column">
                <div class="ui divided relaxed list">
                    <div class="item">
                        <g:link controller="yoda" action="profiler">${message(code:'menu.yoda.profiler')}</g:link>
                    </div>
                    <div class="item">
                        <g:link controller="yoda" action="cacheInfo">${message(code:'menu.yoda.cacheInfo')}</g:link>
                    </div>
                    <div class="item">
                        <g:link controller="yoda" action="quartzInfo">${message(code:'menu.yoda.quartzInfo')}</g:link>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <br />
    <br />
    <br />
    <%--
    <p>TODO: Offene Beitrittsanfragen</p>

    <br />

    <p>TODO: New Errors</p>

    <br />

    <p>TODO: New System Events of Type</p>

    <br />

    <p>TODO: Next Cronjobs</p>

    <br />

    <p>TODO: Cache Memory Information</p>
    --%>

</body>
</html>
