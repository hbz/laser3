<%@ page import="de.laser.AdminController; de.laser.utils.DateUtils; de.laser.helper.DatabaseInfo; de.laser.storage.BeanStore; de.laser.system.SystemSetting; grails.util.Metadata; de.laser.reporting.report.ElasticSearchHelper; grails.util.Environment; de.laser.config.ConfigMapper" %>

<laser:htmlStart message="menu.admin.databaseInfo" />

    <ui:breadcrumbs>
        <ui:crumb message="menu.admin" controller="admin" action="index"/>
        <ui:crumb message="menu.admin.databaseInfo" class="active"/>
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.admin.databaseInfo" type="admin"/>

    <h2 class="ui header">Überblick</h2>

    <table class="ui celled la-js-responsive-table la-table table la-hover-table compact">
        <thead>
            <tr>
                <th class="four wide">Konfiguration</th>
                <th class="six wide"></th>
                <th class="six wide"></th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>Database</td>
                <td>${dbInfo.default.dbName}</td>
                <td>${dbInfo.storage.dbName}</td>
            </tr>
            <tr>
                <td>DBM version</td>
                <td>${dbInfo.default.dbmVersion[0]} -> ${dbInfo.default.dbmVersion[1]} <br/> ${DateUtils.getLocalizedSDF_noZ().format(dbInfo.default.dbmVersion[2])}</td>
                <td>${dbInfo.storage.dbmVersion[0]} -> ${dbInfo.storage.dbmVersion[1]} <br/> ${DateUtils.getLocalizedSDF_noZ().format(dbInfo.storage.dbmVersion[2])}</td>
            </tr>
            <tr>
                <td>DBM updateOnStart</td>
                <td>${dbInfo.dbmUpdateOnStart}</td>
                <td>${dbInfo.dbmUpdateOnStart}</td>
            </tr>
            <tr>
                <td>Config dbCreate</td>
                <td>${dbInfo.default.dbmDbCreate}</td>
                <td>${dbInfo.storage.dbmDbCreate}</td>
            </tr>
            <tr>
                <td>Collations</td>
                <td>
                <%
                    Set collations = [dbInfo.default.defaultCollate]
                    DatabaseInfo.getAllTablesCollationInfo().each { it ->
                        List c = it.value['collation'].findAll()
                        if (! c.isEmpty()) { collations.addAll(c) }
                    }
                    collations.each { print it + '<br/>' }
                %>
                <td>
                <%
                    collations = [dbInfo.storage.defaultCollate]
                    DatabaseInfo.getAllTablesCollationInfo( DatabaseInfo.DS_STORAGE ).each { it ->
                        List c = it.value['collation'].findAll()
                        if (! c.isEmpty()) { collations.addAll(c) }
                    }
                    collations.each { print it + '<br/>' }
                %>
                </td>
            </tr>
            <tr>
                <td>User defined functions</td>
                <td>
                    <g:each in="${dbInfo.default.dbUserFunctions}" var="uf">
                        ${uf.function}; Version ${uf.version}<br />
                    </g:each>
                </td>
                <td>
                    <g:each in="${dbInfo.storage.dbUserFunctions}" var="uf">
                        ${uf.function}; Version ${uf.version}<br />
                    </g:each>
                </td>
            </tr>
            <tr>
                <td>Conflicts</td>
                <td>${dbInfo.default.dbConflicts}</td>
                <td>${dbInfo.storage.dbConflicts}</td>
            </tr>
            <tr>
                <td>Database size</td>
                <td>${dbInfo.default.dbSize}</td>
                <td>${dbInfo.storage.dbSize}</td>
            </tr>
            <tr>
                <td>Postgresql server</td>
                <td>${DatabaseInfo.getServerInfo()}</td>
                <td>${DatabaseInfo.getServerInfo(DatabaseInfo.DS_STORAGE)}</td>
            </tr>
        <tbody>
    </table>

    <h2 class="ui header">Aktivität</h2>

    <table class="ui celled la-js-responsive-table la-table table la-hover-table compact">
        <thead>
            <tr>
                <th>Datenbank</th>
                <th>Anwendung</th>
                <th>Client</th>
                <th>Status</th>
                <th>PID</th>
            </tr>
        </thead>
        <tbody>
        <g:each in="${dbInfo.default.dbActivity + dbInfo.storage.dbActivity}" var="dba">
            <tr>
                <td>${dba.datname}</td>
                <td>${dba.application_name}</td>
                <td>${dba.usename}@${dba.client_addr}:${dba.client_port}</td>
                <td>${dba.state}</td>
                <td>${dba.pid}</td>
            </tr>
        </g:each>
        <tbody>
    </table>

    <g:if test="${dbInfo.default.dbStatistics}">
        <h2 class="ui header">Top ${dbInfo.default.dbStatistics.calls.size()} Datenbankabbfragen: ${dbInfo.default.dbName}</h2>

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
                <g:each in="${dbInfo.default.dbStatistics.calls}" var="dbs">
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
                <g:each in="${dbInfo.default.dbStatistics.maxTime}" var="dbs">
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

    <g:if test="${dbInfo.storage.dbStatistics}">
        <h2 class="ui header">Top ${dbInfo.storage.dbStatistics.calls.size()} Datenbankabbfragen: ${dbInfo.storage.dbName}</h2>

        <div class="ui secondary stackable pointing tabular menu">
            <a data-tab="dbStatistics-3" class="item active">HITS</a>
            <a data-tab="dbStatistics-4" class="item">MAX</a>
        </div>

        <div data-tab="dbStatistics-3" class="ui bottom attached tab active">
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
                <g:each in="${dbInfo.storage.dbStatistics.calls}" var="dbs">
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

        <div data-tab="dbStatistics-4" class="ui bottom attached tab">
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
                <g:each in="${dbInfo.storage.dbStatistics.maxTime}" var="dbs">
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

    <h2 class="ui header">Nutzung (ungefähre/schnelle Berechnung)</h2>

    <table class="ui celled la-js-responsive-table la-table table la-hover-table compact">
        <thead>
        <tr>
            <th class="one wide">#</th>
            <th class="six wide">${dbInfo.default.dbName}</th>
            <th class="two wide">Einträge</th>
            <th class="five wide">${dbInfo.storage.dbName}</th>
            <th class="two wide">Einträge</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${dbInfo.default.dbTableUsage}" var="dummy" status="i">
            <g:set var="tblDefault" value="${i < dbInfo.default.dbTableUsage.size() ? dbInfo.default.dbTableUsage[i] : null}" />
            <g:set var="tblStorage" value="${i < dbInfo.storage.dbTableUsage.size() ? dbInfo.storage.dbTableUsage[i] : null}" />
            <tr>
                <td>${i+1}</td>
                <td>${tblDefault?.tablename}</td>
                <td><g:formatNumber number="${tblDefault?.rowcount}" format="${message(code:'default.decimal.format')}"/></td>
                <td>${tblStorage?.tablename}</td>
                <td><g:formatNumber number="${tblStorage?.rowcount}" format="${message(code:'default.decimal.format')}"/></td>
            </tr>
        </g:each>
        <tbody>
    </table>

<laser:htmlEnd />
