<%@ page import="java.time.Clock; de.laser.remote.Wekb; de.laser.utils.DateUtils; de.laser.helper.DatabaseInfo; de.laser.utils.AppUtils; de.laser.storage.BeanStore; de.laser.system.SystemSetting; grails.util.Metadata; de.laser.reporting.report.ElasticSearchHelper; grails.util.Environment; de.laser.config.ConfigMapper" %>

<laser:htmlStart message="menu.admin.appInfo" />

    <ui:breadcrumbs>
        <ui:crumb message="menu.admin" controller="admin" action="index"/>
        <ui:crumb message="menu.admin.appInfo" class="active"/>
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.admin.appInfo" type="admin"/>

    <table class="ui celled la-js-responsive-table la-table la-hover-table table compact">
        <thead>
            <tr><th class="seven wide">Application</th><th class="nine wide"></th></tr>
        </thead>
        <tbody>
            <tr><td>App Id/Name/Version</td><td> ${ConfigMapper.getLaserSystemId()} / ${AppUtils.getMeta('info.app.name')} / ${AppUtils.getMeta('info.app.version')}</td></tr>
            <tr><td>Grails version</td><td> ${AppUtils.getMeta('info.app.grailsVersion')}</td></tr>
            <tr><td>Groovy (currently running)</td><td> ${GroovySystem.getVersion()}</td></tr>
            <tr><td>Java (currently running)</td><td> ${System.getProperty('java.version')}</td></tr>
            <tr><td>Configuration file</td><td> ${ConfigMapper.getCurrentConfigFile(this.applicationContext.getEnvironment()).name}</td></tr>
            <tr><td>Environment/Server</td><td> ${Metadata.getCurrent().getEnvironment()} / ${AppUtils.getCurrentServer()}</td></tr>
            <tr><td>Session timeout</td><td> ${(session.getMaxInactiveInterval() / 60)} Minutes</td></tr>
            <tr><td>Timezone</td><td> ${Clock.systemDefaultZone()}</td></tr>
        </tbody>
    </table>

    <table class="ui celled la-js-responsive-table la-table la-hover-table table compact">
        <thead>
            <tr><th class="seven wide">Build</th><th class="nine wide"></th></tr>
        </thead>
        <tbody>
            <tr><td>Build date</td><td> ${AppUtils.getMeta('info.app.build.date')}</td></tr>
            <tr><td>Build host</td><td> ${AppUtils.getMeta('info.app.build.host')}</td></tr>
            <tr><td>Build java version</td><td> ${AppUtils.getMeta('info.app.build.javaVersion')}</td></tr>
        </tbody>
    </table>

    <table class="ui celled la-js-responsive-table la-table la-hover-table table compact">
        <thead>
            <tr>
                <th class="four wide">Database</th>
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
                <g:if test="${! dbInfo.dbmUpdateOnStart}">
                    <td class="error">DBM updateOnStart</td>
                    <td colspan="2" class="error">${dbInfo.dbmUpdateOnStart}</td>
                </g:if>
                <g:else>
                    <td>DBM updateOnStart</td>
                    <td>${dbInfo.dbmUpdateOnStart}</td>
                    <td>${dbInfo.dbmUpdateOnStart}</td>
                </g:else>
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
        <tbody>
    </table>

    <table class="ui celled la-js-responsive-table la-table la-hover-table table compact">
        <thead>
            <tr><th class="seven wide">Files</th><th class="nine wide"></th></tr>
        </thead>
        <tbody>
            <tr><td>Document storage</td><td> ${docStore.folderPath}</td></tr>
            <tr><td>Files count</td><td> ${docStore.filesCount}</td></tr>
            <tr><td>Storage size</td><td> ${docStore.folderSize} MB</td></tr>
        </tbody>
    </table>

    <g:set var="ES_URL" value="${BeanStore.getESWrapperService().getUrl() ?: 'unbekannt'}" />

    <table class="ui celled la-js-responsive-table la-table la-hover-table table compact">
        <thead>
            <tr>
                <th class="seven wide">FTControl / ES Index</th>
                <th class="six wide"></th>
                <th class="three wide"></th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>Url</td>
                <td colspan="2"><a href="${ES_URL}/_cat/indices?v" target="_blank">${ES_URL}</a> -> ${BeanStore.getESWrapperService().ES_Cluster}</td>
            </tr>
            <tr>
                <td>Indices</td>
                <td colspan="2">
                    ${BeanStore.getESWrapperService().ES_Indices}
                </td>
            </tr>
            <tr>
                <g:if test="${dataload.running}">
                    <td class="positive">Currently running</td>
                    <td colspan="2" class="positive">${dataload.running}</td>
                </g:if>
                <g:else>
                    <td>Currently running</td>
                    <td colspan="2">${dataload.running}</td>
                </g:else>
            </tr>
            <tr>
                <td>Last doFTUpdate</td>
                <td colspan="2">${dataload.lastFTIndexUpdateInfo}</td>
            </tr>
            <g:each in="${ftcInfos}" var="ftc">
                <tr>
                    <td>
                        - ${ftc.domainClassName}
                        <g:if test="${! ftc.active}"><span class="sc_grey">(inaktiv)</span></g:if>
                    </td>
                    <td>
                        <g:if test="${ftc.dbElements != ftc.esElements}">
                            <span class="sc_red">
                                Elements in DB: <g:formatNumber number="${ftc.dbElements}" format="${message(code:'default.decimal.format')}"/>,
                                ES: <g:formatNumber number="${ftc.esElements}" format="${message(code:'default.decimal.format')}"/>
                            </span>
                        </g:if>
                        <g:else>
                            Elements: <g:formatNumber number="${ftc.dbElements}" format="${message(code:'default.decimal.format')}"/>
                        </g:else>
                    </td>
                    <td>
                        <g:if test="${ftc.lastTimestamp}">
                            ${DateUtils.getLocalizedSDF_noZ().format(new Date(ftc.lastTimestamp))}
                        </g:if>
                        <g:else>
                            ?
                        </g:else>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>

    <table class="ui celled la-js-responsive-table la-table la-hover-table table compact">
        <thead>
            <tr><th class="seven wide">Global Data Sync</th><th class="nine wide"></th></tr>
        </thead>
        <tbody>
            <tr>
                <g:if test="${globalSourceSync.running}">
                    <td class="positive">Currently running</td><td class="positive">${globalSourceSync.running}</td>
                </g:if>
                <g:else>
                    <td>Currently running</td><td>${globalSourceSync.running}</td>
                </g:else>
            </tr>
        </tbody>
    </table>

    <g:if test="${ConfigMapper.getConfig('reporting.elasticSearch', Map)}">
        <table class="ui celled la-js-responsive-table la-table la-hover-table table compact">
            <thead>
            <tr><th class="seven wide">Reporting</th><th class="nine wide"></th></tr>
            </thead>
            <tbody>
            <tr>
                <td>ElasticSearch (reporting.elasticSearch.url)</td>
                <td><a href="${ConfigMapper.getConfig('reporting.elasticSearch.url', String) + '/_cat/indices?v'}" target="_blank">${ConfigMapper.getConfig('reporting.elasticSearch.url', String)}</a></td>
            </tr>
            <tr>
                <td>ElasticSearch (reporting.elasticSearch.indices)</td>
                <td>
                    <g:if test="${ConfigMapper.getConfig('reporting.elasticSearch.indices', Map)}">
                        <g:each in="${ConfigMapper.getConfig('reporting.elasticSearch.indices', Map)}" var="k, v">
                            <a href="${ConfigMapper.getConfig('reporting.elasticSearch.url', String) + '/' + v + '/_search'}" target="_blank">${v} (${k})</a><br />
                        </g:each>
                    </g:if>
                </td>
            </tr>
            <tr>
                <td>We:kb</td>
                <td>
                    <g:set var="wekb_url" value="${Wekb.getURL()}" />
                    <g:if test="${wekb_url}">
                        <a href="${wekb_url}" target="_blank">${wekb_url}</a>
                    </g:if>
                </td>
            </tr>
            </tbody>
        </table>
    </g:if>

    <table class="ui celled la-js-responsive-table la-table la-hover-table table compact">
        <thead>
            <tr><th class="seven wide">STATS Sync Service</th><th class="nine wide"></th></tr>
        </thead>
        <tbody>
            <tr>
                <g:if test="${statsSync.running}">
                    <td class="positive">Currently running</td><td class="positive">${statsSync.running}</td>
                </g:if>
                <g:else>
                    <td>Currently running</td><td>${statsSync.running}</td>
                </g:else>
            </tr>
            <tr><td>Completed count</td><td>${statsSync.completedCount}</td></tr>
            <tr><td>New fact count</td><td>${statsSync.newFactCount}</td></tr>
            <tr><td>Total time (all threads)</td><td>${statsSync.totalTime} (ms)</td></tr>
            <tr><td>Total time elapsed</td><td>${statsSync.syncElapsed} (ms)</td></tr>
            <tr><td>Thread pool size</td><td>${statsSync.threads}</td></tr>
            <tr><td>Last start time</td>
            <td>
                <g:if test="${statsSync.syncStartTime != 0}">
                    <g:formatDate date="${new Date(statsSync.syncStartTime)}" format="yyyy-MM-dd hh:mm"/>
                </g:if>
                <g:else>
                    Not started yet
                </g:else>
            </tr>
            <tr><td>Initial query time</td><td>${statsSync.queryTime} (ms)</td></tr>

            <g:if test="${((statsSync.completedCount != 0) && (statsSync.totalTime != 0))}">
                <tr><td>Average time per STATS triple (current/last run)</td><td>${statsSync.totalTime/statsSync.completedCount} (ms)</td></tr>
            </g:if>
            <tr><td>Activity histogram</td>
            <td>
                <g:each in="${statsSync.activityHistogram}" var="ah">
                    ${ah.key}:${ah.value}<br />
                </g:each>
            </td></tr>
        </tbody>
    </table>

<laser:htmlEnd />
