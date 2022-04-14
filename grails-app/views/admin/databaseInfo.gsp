<%@ page import="de.laser.storage.BeanStorage; de.laser.system.SystemSetting; de.laser.helper.AppUtils; grails.util.Metadata; de.laser.reporting.report.ElasticSearchHelper; de.laser.helper.DateUtils; grails.util.Environment; de.laser.helper.ConfigUtils" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'menu.admin.databaseInfo')}</title>
</head>

<body>

    <semui:breadcrumbs>
        <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
        <semui:crumb message="menu.admin.databaseInfo" class="active"/>
    </semui:breadcrumbs>

    <h1 class="ui left aligned icon header la-noMargin-top"><semui:headerIcon />${message(code:'menu.admin.databaseInfo')}</h1>

    <h2 class="ui header">Meta</h2>

    <table class="ui celled la-js-responsive-table la-table table compact">
        <thead>
            <tr><th class="seven wide">Datenbank</th><th class="nine wide"></th></tr>
        </thead>
        <tbody>
            <tr><td>Database</td><td> ${AppUtils.getConfig('dataSource.url').split('/').last()}</td></tr>
            <tr><td>Collations</td><td>
                <%
                    Set collations = [defaultCollate]
                    AppUtils.getPostgresqlTableInfo().each { it ->
                        List c = it.value['collation'].findAll()
                        if (! c.isEmpty()) { collations.addAll(c) }
                    }
                    collations.each { print it + '<br/>' }
                %>
            </td></tr>
            <tr><td>DBM version</td><td> ${dbmVersion[0]} @ ${dbmVersion[1]} <br/> ${DateUtils.getSDF_NoZ().format(dbmVersion[2])}</td></tr>
            <tr><td>DBM updateOnStart</td><td> ${AppUtils.getPluginConfig('databasemigration.updateOnStart')}</td></tr>
            <tr><td>Config dataSource.dbCreate</td><td> ${AppUtils.getConfig('dataSource.dbCreate')}</td></tr>
            <tr><td>Database size</td><td> ${dbSize}</td></tr>
            <tr><td>User defined functions</td><td>
                <g:each in="${dbFunctions}" var="udf">
                    ${udf.function}; Version ${udf.version}<br />
                </g:each>
            </td></tr>
            <tr><td>Postgresql server</td><td> ${AppUtils.getPostgresqlServerInfo()}</td></tr>
        <tbody>
    </table>

    <h2 class="ui header">Aktivität</h2>

    <table class="ui celled la-js-responsive-table la-table table compact">
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

    <h2 class="ui header">Nutzung (schnelle Berechnung)</h2>

    <table class="ui celled la-js-responsive-table la-table table compact">
        <thead>
        <tr>
            <th>Tabelle</th>
            <th>Einträge</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${dbTableUsage}" var="tbl">
            <tr>
                <td>${tbl.tablename}</td>
                <td><g:formatNumber number="${tbl.rowcount}" format="${message(code:'default.decimal.format')}"/></td>
            </tr>
        </g:each>
        <tbody>
    </table>

</body>
</html>
