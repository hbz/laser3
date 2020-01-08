<%@ page import="com.k_int.kbplus.batch.HeartbeatJob" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser')} : Yoda Dashboard</title>
</head>

<body>
    <laser:serviceInjection />

    <semui:breadcrumbs>
        <semui:crumb message="menu.yoda.dash" class="active"/>
    </semui:breadcrumbs>
    <br>
    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />Yoda Dashboard</h1>

    <div class="ui equal width grid">
        <div class="row">

            <div class="column">
                <div class="ui divided relaxed list">
                    <div class="item">
                        <g:link controller="admin" action="systemEvents" target="_blank">${message(code:'menu.admin.systemEvents')}</g:link>
                    </div>
                    <div class="item">
                        <g:link controller="yoda" action="manageSystemMessage" target="_blank">${message(code: 'menu.admin.systemMessage')}</g:link>
                    </div>
                    <div class="item">
                        <g:link class="item" controller="yoda" action="appConfig" target="_blank">${message(code:'menu.yoda.appConfig')}</g:link>
                    </div>
                    <div class="item">
                        <g:link controller="admin" action="databaseStatistics" target="_blank">${message(code: "menu.admin.databaseStatistics")}</g:link>
                    </div>
                    <div class="item">
                        <g:link controller="admin" action="dataConsistency" target="_blank">${message(code: "menu.admin.dataConsistency")}</g:link>
                    </div>
                </div>
            </div>

            <div class="column">
                <div class="ui divided relaxed list">
                    <div class="item">
                        <g:link controller="yoda" action="systemProfiler" target="_blank">${message(code:'menu.yoda.systemProfiler')}</g:link>
                    </div>
                    <div class="item">
                        <g:link controller="yoda" action="activityProfiler" target="_blank">${message(code:'menu.yoda.activityProfiler')}</g:link>
                    </div>
                    <div class="item">
                        <g:link controller="yoda" action="cacheInfo" target="_blank">${message(code:'menu.yoda.cacheInfo')}</g:link>
                    </div>
                    <div class="item">
                        <g:link controller="yoda" action="quartzInfo" target="_blank">${message(code:'menu.yoda.quartzInfo')}</g:link>
                    </div>
                </div>
            </div>

            <div class="column">
                <div class="ui divided relaxed list">
                    <div class="item">
                        <g:link controller="yoda" action="appSecurity" target="_blank">${message(code:'menu.yoda.security')}</g:link>
                    </div>
                    <div class="item">
                        <g:link controller="yoda" action="userMatrix" target="_blank">${message(code:'menu.yoda.userMatrix')}</g:link>
                    </div>
                    <div class="item">
                        <g:link class="item" controller="yoda" action="userRoleDefinitions" target="_blank">${message(code:'menu.yoda.userRoleDefinitions')}</g:link>
                    </div>
                    <div class="item">
                        <g:link class="item" controller="admin" action="manageDeletedObjects" target="_blank">${message(code: "menu.admin.deletedObjects")}</g:link>
                    </div>
                </div>
            </div>

        </div>
    </div>

    <semui:messages data="${flash}" />

        <br />
        <br />

        <h2 class="ui header">Anzahl Threads: ${Thread.getAllStackTraces().keySet().size()}</h2>

        <table class="ui celled la-table la-table-small table" id="contextTable">
            <tbody>
                <g:each in="${Thread.getAllStackTraces().keySet().sort{ it.id }}" var="thread">
                    <tr>
                        <td>${thread.getId()}</td>
                        <td>${thread.getName().replaceAll('%002e', '.')}</td>
                        <td>
                            ${thread.getThreadGroup().getName()}
                            <%
                                java.lang.ThreadGroup tmp = thread.getThreadGroup()
                                while( (tmp = tmp.getParent()) != null ) {
                                    println " / ${tmp.getName()}"
                                }
                            %>
                        </td>
                        <td>${thread.getState()}</td>
                        <td>${thread.isAlive()}</td>
                    </tr>
                </g:each>
            </tbody>
        </table>

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
