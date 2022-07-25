<%@ page import="java.lang.management.ManagementFactory" %>

<laser:htmlStart text="${message(code:'menu.yoda')} ${message(code:'default.dashboard')}" serviceInjection="true" />

    <ui:breadcrumbs>
        <ui:crumb message="menu.yoda" class="active"/>
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.yoda" type="yoda" />

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

            <h3 class="ui header">${docStore.folderPath}</h3>

            <div class="ui horizontal statistics">
                <div class="statistic">
                    <div class="value">${docStore.filesCount}</div>
                    <div class="label">Files</div>
                </div>
                <div class="statistic">
                    <div class="value">${docStore.folderSize}</div>
                    <div class="label">MB &middot; used</div>
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
                        <g:link controller="yoda" action="systemConfiguration" target="_blank">${message(code:'menu.yoda.systemConfiguration')}</g:link>
                    </div>
                    <div class="item">
                        <g:link controller="yoda" action="systemThreads" target="_blank">${message(code:'menu.yoda.systemThreads')}</g:link>
                    </div>
                    <div class="item">
                        <g:link controller="yoda" action="systemQuartz" target="_blank">${message(code:'menu.yoda.systemQuartz')}</g:link>
                    </div>
                    <div class="item">
                        <g:link controller="yoda" action="systemCache" target="_blank">${message(code:'menu.yoda.systemCache')}</g:link>
                    </div>
                </div>
            </div>

            <h3 class="ui header">${message(code:'menu.yoda.others')}</h3>
            <div>
                <div class="ui divided relaxed list">
                    <div class="item">
                        <g:link controller="yoda" action="systemSettings" target="_blank">${message(code:'menu.yoda.systemSettings')}</g:link>
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
                        <g:link controller="yoda" action="profilerLoadtime" target="_blank">${message(code:'menu.yoda.profilerLoadtime')}</g:link>
                    </div>
                    <div class="item">
                        <g:link controller="yoda" action="profilerActivity" target="_blank">${message(code:'menu.yoda.profilerActivity')}</g:link>
                    </div>
                    <div class="item">
                        <g:link controller="yoda" action="profilerTimeline" target="_blank">${message(code:'menu.yoda.profilerTimeline')}</g:link>
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


    <ui:messages data="${flash}" />

    <%--
    <p>TODO: New Errors</p>

    <br />

    <p>TODO: New System Events of Type</p>

    <br />

    <p>TODO: Next Cronjobs</p>

    <br />

    <p>TODO: Cache Memory Information</p>
    --%>

<laser:htmlEnd />
