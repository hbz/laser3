<%@ page import="de.laser.helper.DatabaseUtils; de.laser.storage.BeanStore; de.laser.system.SystemSetting; de.laser.helper.AppUtils; grails.util.Metadata; de.laser.reporting.report.ElasticSearchHelper; de.laser.helper.DateUtils; grails.util.Environment; de.laser.helper.ConfigMapper" %>
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

    <h1 class="ui left aligned icon header la-noMargin-top"><semui:headerIcon />${message(code:'menu.admin.databaseInfo')}</h1>

    <h2 class="ui header">Meta</h2>

    <table class="ui celled la-js-responsive-table la-table table la-hover-table compact">
        <thead>
            <tr><th class="seven wide">Datenbank</th><th class="nine wide"></th></tr>
        </thead>
        <tbody>
            <tr><td>Database</td><td> ${ConfigMapper.getConfig('dataSource.url', String).split('/').last()}</td></tr>
            <tr><td>DBM version</td><td> ${dbmVersion[0]} @ ${dbmVersion[1]} <br/> ${DateUtils.getLocalizedSDF_noZ().format(dbmVersion[2])}</td></tr>
            <tr><td>DBM updateOnStart</td><td> ${ConfigMapper.getPluginConfig('databasemigration.updateOnStart', Boolean)}</td></tr>
            <tr><td>Config dataSource.dbCreate</td><td> ${ConfigMapper.getConfig('dataSource.dbCreate', String)}</td></tr>
            <tr><td>Collations</td><td>
                <%
                    Set collations = [defaultCollate]
                    DatabaseUtils.getAllTablesCollationInfo().each { it ->
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
            <tr><td>Database size</td><td> ${dbSize}</td></tr>
            <tr><td>Postgresql server</td><td> ${DatabaseUtils.getServerInfo()}</td></tr>
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
