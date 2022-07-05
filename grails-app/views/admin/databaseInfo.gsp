<%@ page import="de.laser.AdminController; de.laser.utils.DateUtils; de.laser.helper.DatabaseInfo; de.laser.storage.BeanStore; de.laser.system.SystemSetting; grails.util.Metadata; de.laser.reporting.report.ElasticSearchHelper; grails.util.Environment; de.laser.config.ConfigMapper" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'menu.admin.databaseInfo')}</title>
</head>

<body>

    <semui:breadcrumbs>
        <semui:crumb message="menu.admin" controller="admin" action="index"/>
        <semui:crumb message="menu.admin.databaseInfo" class="active"/>
    </semui:breadcrumbs>

    <semui:h1HeaderWithIcon message="menu.admin.databaseInfo" />

    <h2 class="ui header">Meta</h2>

    <table class="ui celled la-js-responsive-table la-table table la-hover-table compact">
        <thead>
            <tr><th class="seven wide">Datenbank</th><th class="nine wide"></th></tr>
        </thead>
        <tbody>
            <tr><td>Database</td><td> ${ConfigMapper.getConfig('dataSource.url', String).split('/').last()}</td></tr>
            <tr><td>DBM version</td><td> ${dbmVersion[0]} -> ${dbmVersion[1]} <br/> ${DateUtils.getLocalizedSDF_noZ().format(dbmVersion[2])}</td></tr>
            <tr><td>DBM updateOnStart</td><td> ${ConfigMapper.getPluginConfig('databasemigration.updateOnStart', Boolean)}</td></tr>
            <tr><td>Config dataSource.dbCreate</td><td> ${ConfigMapper.getConfig('dataSource.dbCreate', String)}</td></tr>
            <tr><td>Collations</td><td>
                <%
                    Set collations = [defaultCollate]
                    DatabaseInfo.getAllTablesCollationInfo().each { it ->
                        List c = it.value['collation'].findAll()
                        if (! c.isEmpty()) { collations.addAll(c) }
                    }
                    collations.each { print it + '<br/>' }
                %>
            </td></tr>
            <tr><td>User defined functions</td><td>
                <g:each in="${dbUserFunctions}" var="uf">
                    ${uf.function}; Version ${uf.version}<br />
                </g:each>
            </td></tr>
            <tr><td>Conflicts</td><td> ${dbConflicts}</td></tr>
            <tr><td>Database size</td><td> ${dbSize}</td></tr>
            <tr><td>Postgresql server</td><td> ${DatabaseInfo.getServerInfo()}</td></tr>
        <tbody>
    </table>

    <h2 class="ui header">Aktivität</h2>

    <table class="ui celled la-js-responsive-table la-table table la-hover-table compact">
        <thead>
            <tr>
                <th>PID</th>
                <th>Datenbank</th>
                <th>Anwendung</th>
                <th>Client</th>
                <th>Status</th>
            </tr>
        </thead>
        <tbody>
        <g:each in="${dbActivity}" var="dba">
            <tr>
                <td>${dba.pid}</td>
                <td>${dba.datname}</td>
                <td>${dba.application_name}</td>
                <td>${dba.usename}@${dba.client_addr}:${dba.client_port}</td>
                <td>${dba.state}</td>
            </tr>
        </g:each>
        <tbody>
    </table>

    <g:if test="${dbStatistics}">
        <h2 class="ui header">Top20 - Datenbankabbfragen</h2>

        <div class="ui secondary stackable pointing tabular menu">
            <a data-tab="dbStatistics-1" class="item active">HITS</a>
            <a data-tab="dbStatistics-2" class="item">MAX</a>
        </div>

        <div data-tab="dbStatistics-1" class="ui bottom attached tab active">
            <table class="ui celled la-js-responsive-table la-table table la-hover-table compact">
                <thead>
                <tr>
                    <th>Hits</th>
                    <th>total(s)</th>
                    <th>max(ms)</th>
                    <th>avg(ms)</th>
                    <th>Query</th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${dbStatistics.calls}" var="dbs">
                    <tr>
                        <td><strong>${dbs.calls}</strong></td>
                        <td>${(dbs.total_time / 1000).round(2)}</td>
                        <td>${dbs.max_time.round(3)}</td>
                        <td>${dbs.mean_time.round(3)}</td>
                        <td>${dbs.query}</td>
                    </tr>
                </g:each>
                <tbody>
            </table>
        </div>

        <div data-tab="dbStatistics-2" class="ui bottom attached tab">
            <table class="ui celled la-js-responsive-table la-table table la-hover-table compact">
                <thead>
                <tr>
                    <th>max(s)</th>
                    <th>avg(s)</th>
                    <th>Hits</th>
                    <th>total(s)</th>
                    <th>Query</th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${dbStatistics.maxTime}" var="dbs">
                    <tr>
                        <td><strong>${(dbs.max_time / 1000).round(3)}</strong></td>
                        <td>${(dbs.mean_time / 1000).round(3)}</td>
                        <td>${dbs.calls}</td>
                        <td>${(dbs.total_time / 1000).round(3)}</td>
                        <td>${dbs.query}</td>
                    </tr>
                </g:each>
                <tbody>
            </table>
        </div>
    </g:if>

    <h2 class="ui header">Nutzung (schnelle Berechnung)</h2>

    <table class="ui celled la-js-responsive-table la-table table la-hover-table compact">
        <thead>
        <tr>
            <th>#</th>
            <th>Tabelle</th>
            <th>Einträge</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${dbTableUsage}" var="tbl" status="i">
            <tr>
                <td>${i+1}</td>
                <td>${tbl.tablename}</td>
                <td><g:formatNumber number="${tbl.rowcount}" format="${message(code:'default.decimal.format')}"/></td>
            </tr>
        </g:each>
        <tbody>
    </table>

</body>
</html>
