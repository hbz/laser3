<%@ page import="java.lang.management.ManagementFactory" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : Yoda Dashboard</title>
</head>

<body>
    <laser:serviceInjection />

    <semui:breadcrumbs>
        <semui:crumb message="menu.yoda.dash" class="active"/>
    </semui:breadcrumbs>

    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />Yoda Dashboard</h1>

    <h2 class="ui header">&nbsp;</h2>

<div class="ui equal width grid">
    <div class="row">

        <div class="column">
            <g:set var="rt" value="${Runtime.getRuntime()}" />
            <g:set var="mb" value="${1024 * 1024}" />

            <h3 class="ui header">JVM/Runtime</h3>

            <div class="ui horizontal statistics">
                <div class="statistic">
                    <div class="value">${((rt.freeMemory() / mb) as float).round(2)}</div>
                    <div class="label">MB &middot; free</div>
                </div>
                <div class="statistic">
                    <div class="value">${(((rt.totalMemory() - rt.freeMemory()) / mb) as float).round(2)}</div>
                    <div class="label">MB &middot; used</div>
                </div>
                <div class="statistic">
                    <div class="value">${((rt.maxMemory() / mb) as float).round(2)}</div>
                    <div class="label">MB &middot; max</div>
                </div>
                <div class="statistic">
                    <div class="value">${((ManagementFactory.getRuntimeMXBean().getUptime() / (1000 * 60 * 60)) as float).round(2)}</div>
                    <div class="label">Hours &middot; uptime</div>
                </div>
                <div class="statistic">
                    <div class="value">${Thread.getAllStackTraces().size()}</div>
                    <div class="label">Threads</div>
                </div>
            </div>
        </div>

        <div class="column">
            <h3 class="ui header">${message(code:'menu.yoda.system')}</h3>
            <div>
                <div class="ui divided relaxed list">
                    <div class="item">
                        <g:link controller="admin" action="systemEvents" target="_blank">${message(code:'menu.admin.systemEvents')}</g:link> <span class="ui mini label">Admin</span>
                    </div>
                    <div class="item">
                        <g:link controller="yoda" action="appConfig" target="_blank">${message(code:'menu.yoda.appConfig')}</g:link>
                    </div>
                    <div class="item">
                        <g:link controller="yoda" action="appThreads" target="_blank">${message(code:'menu.yoda.appThreads')}</g:link>
                    </div>
                    <div class="item">
                        <g:link controller="yoda" action="quartzInfo" target="_blank">${message(code:'menu.yoda.quartzInfo')}</g:link>
                    </div>
                    <div class="item">
                        <g:link controller="yoda" action="cacheInfo" target="_blank">${message(code:'menu.yoda.cacheInfo')}</g:link>
                    </div>
                </div>
            </div>

            <h3 class="ui header">${message(code:'menu.yoda.others')}</h3>
            <div>
                <div class="ui divided relaxed list">
                    <div class="item">
                        <g:link controller="yoda" action="settings" target="_blank">${message(code:'menu.yoda.systemSettings')}</g:link>
                    </div>
                    <div class="item">
                        <g:link controller="admin" action="systemMessages" target="_blank">${message(code: 'menu.admin.systemMessage')}</g:link> <span class="ui mini label">Admin</span>
                    </div>
                    <div class="item">
                        <g:link controller="admin" action="systemAnnouncements" target="_blank">${message(code: 'menu.admin.announcements')}</g:link> <span class="ui mini label">Admin</span>
                    </div>
                </div>
            </div>
        </div>

        <div class="column">
            <h3 class="ui header">${message(code:'menu.yoda.profiler')}</h3>
            <div>
                <div class="ui divided relaxed list">
                    <div class="item">
                        <g:link controller="yoda" action="systemProfiler" target="_blank">${message(code:'menu.yoda.systemProfiler')}</g:link>
                    </div>
                    <div class="item">
                        <g:link controller="yoda" action="activityProfiler" target="_blank">${message(code:'menu.yoda.activityProfiler')}</g:link>
                    </div>
                    <div class="item">
                        <g:link controller="yoda" action="timelineProfiler" target="_blank">${message(code:'menu.yoda.timelineProfiler')}</g:link>
                    </div>
                </div>
            </div>

            <h3 class="ui header">${message(code:'menu.yoda.database')}</h3>
            <div>
                <div class="ui divided relaxed list">
                    <div class="item">
                        <g:link controller="admin" action="databaseInfo" target="_blank">${message(code: "menu.admin.databaseInfo")}</g:link> <span class="ui mini label">Admin</span>
                    </div>
                    <div class="item">
                        <g:link controller="admin" action="databaseCollations" target="_blank">${message(code: "menu.admin.databaseCollations")}</g:link> <span class="ui mini label">Admin</span>
                    </div>
                    <%--<div class="item">
                        <g:link controller="admin" action="databaseStatistics" target="_blank">${message(code: "menu.admin.databaseStatistics")}</g:link> <span class="ui mini label">Admin</span>
                    </div>--%>
                    <div class="item">
                        <g:link controller="admin" action="dataConsistency" target="_blank">${message(code: "menu.admin.dataConsistency")}</g:link> <span class="ui mini label">Admin</span>
                    </div>
                    <div class="item">
                        <g:link controller="admin" action="fileConsistency" target="_blank">${message(code: "menu.admin.fileConsistency")}</g:link> <span class="ui mini label">Admin</span>
                    </div>
                    <div class="item">
                        <g:link controller="admin" action="manageDeletedObjects" target="_blank">${message(code: "menu.admin.deletedObjects")}</g:link> <span class="ui mini label">Admin</span>
                    </div>
                </div>
            </div>

            <h3 class="ui header">${message(code:'menu.yoda.security')}</h3>
            <div>
                <div class="ui divided relaxed list">
                    <div class="item">
                        <g:link controller="yoda" action="appControllers" target="_blank">${message(code:'menu.yoda.appControllers')}</g:link>
                    </div>
                    <div class="item">
                        <g:link controller="yoda" action="userRoleMatrix" target="_blank">${message(code:'menu.yoda.userRoleMatrix')}</g:link>
                    </div>
                </div>
            </div>
        </div>

    </div>
</div>


    <semui:messages data="${flash}" />

    <%--
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
