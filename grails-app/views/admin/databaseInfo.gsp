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

    <table class="ui celled la-js-responsive-table la-table table compact">
        <thead>
            <tr><th class="seven wide">Database</th><th class="nine wide"></th></tr>
        </thead>
        <tbody>
            <tr><td>Database</td><td> ${AppUtils.getConfig('dataSource.url').split('/').last()}</td></tr>
            <tr><td>Collations</td><td>
                <%
                    Set collations = []
                    AppUtils.getPostgresqlTableInfo().each { it ->
                        List c = it.value['collation'].findAll()
                        if (! c.isEmpty()) { collations.addAll(c) }
                    }
                    collations.each { print it + '<br/>' }
                %>
            </td></tr>
            <tr><td>DBM version</td><td> ${dbmVersion[0]} @ ${dbmVersion[1]} <br/> ${DateUtils.getSDF_NoZ().format(dbmVersion[2])}</td></tr>
            <tr><td>DBM updateOnStart</td><td> ${AppUtils.getPluginConfig('databasemigration.updateOnStart')}</td></tr>
            <tr><td>DataSource.dbCreate</td><td> ${AppUtils.getConfig('dataSource.dbCreate')}</td></tr>
            <tr><td>Postgresql server</td><td> ${AppUtils.getPostgresqlServerInfo()}</td></tr>
        <tbody>
    </table>

</body>
</html>
