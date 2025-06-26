<%@ page import="de.laser.ui.Icon; de.laser.config.ConfigMapper; de.laser.utils.DateUtils; grails.util.Metadata; de.laser.utils.AppUtils" %>

<laser:htmlStart text="${message(code:'menu.admin')} ${message(code:'default.dashboard')}" />

    <ui:breadcrumbs>
        <ui:crumb message="menu.admin" controller="admin" action="index" class="active"/>
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.admin" type="admin"/>

    <div class="ui equal width grid">
        <div class="row">
            <div class="column">
                <div class="ui divided relaxed list">
                    <div class="item">
                        <g:link controller="admin" action="systemEvents" target="_blank">${message(code:'menu.admin.systemEvents')}</g:link>
                    </div>
                    <div class="item">
                        <g:link controller="admin" action="appInfo" target="_blank">${message(code:'menu.admin.appInfo')}</g:link>
                    </div>
                </div>
            </div>
            <div class="column">
                <div class="ui divided relaxed list">
                    <div class="item">
                        <g:link controller="admin" action="systemMessages" target="_blank">${message(code: 'menu.admin.systemMessages')}</g:link>
                    </div>
                    <div class="item">
                        <g:link controller="admin" action="serviceMessages" target="_blank">${message(code: 'menu.admin.serviceMessages')}</g:link>
                    </div>
                </div>
            </div>
            <div class="column">
                <div class="ui divided relaxed list">
                    <div class="item">
                        <g:link controller="admin" action="databaseInfo" target="_blank">${message(code: "menu.admin.databaseInfo")}</g:link>
                    </div>
                %{--                    <div class="item">--}%
                %{--                        <g:link controller="admin" action="databaseIndices" target="_blank">${message(code: "menu.admin.databaseIndices")}</g:link>--}%
                %{--                    </div>--}%
                    <div class="item">
                        <g:link controller="admin" action="databaseCollations" target="_blank">${message(code: "menu.admin.databaseCollations")}</g:link>
                    </div>
                </div>
            </div>
        </div>
    </div>

<br />

<div class="ui fluid card">
    <div class="content">

    <table class="ui celled la-js-responsive-table la-table la-hover-table table compact">
        <thead>
            <tr>
                <th class="four wide">${AppUtils.getMeta('info.app.name')}</th>
                <th class="six wide"></th>
                <th class="six wide"></th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>App Id/Version/Build</td>
                <td colspan="2">${ConfigMapper.getLaserSystemId()} / ${AppUtils.getMeta('info.app.version')} / ${AppUtils.getMeta('info.app.build.date')}</td>
            </tr>
            <tr>
                <td>Configuration file</td>
                <td colspan="2">${ConfigMapper.getCurrentConfigFile(this.applicationContext.getEnvironment()).name}</td>
            </tr>
            <tr>
                <td>Environment/Server</td>
                <td colspan="2">${Metadata.getCurrent().getEnvironment()} / ${AppUtils.getCurrentServer()}</td>
            </tr>
            <tr>
                <td>Database</td>
                <td>${database.default.dbName}</td>
                <td>${database.storage.dbName}</td>
            </tr>
            <tr>
                <td>DBM version</td>
                <td>${database.default.dbmVersion[0]} -> ${database.default.dbmVersion[1]} <br/> ${DateUtils.getLocalizedSDF_noZ().format(database.default.dbmVersion[2])}</td>
                <td>${database.storage.dbmVersion[0]} -> ${database.storage.dbmVersion[1]} <br/> ${DateUtils.getLocalizedSDF_noZ().format(database.storage.dbmVersion[2])}</td>
            </tr>
            <tr>
                <td>Document storage</td>
                <td colspan="2">${docStore.filesCount} Files -> ${docStore.folderSize} MB</td>
            </tr>
        </tbody>
    </table>

    </div>
</div>

<div class="ui fluid card">
    <div class="content">
        <div class="ui labels">
            <g:each in="${systemService.serviceCheck()}" var="systemCheck">
                <div class="ui label" style="margin:0.3em 0.2em">
                    <i class="${Icon.SYM.SQUARE} ${systemCheck.value.equalsIgnoreCase('yes') ? 'green' : 'red'}"></i>
                    ${systemCheck.key}
                </div>
            </g:each>
        </div>
    </div>
</div>

<g:if test="${events}">
    <div class="ui fluid card">
        <div class="content">

    <table class="ui celled la-js-responsive-table la-table la-hover-table table compact">
        <thead>
        <tr>
            <th scope="col" class="two wide">${message(code:'default.date.label')}</th>
            <th scope="col" class="two wide">${message(code:'default.category.label')}</th>
            <th scope="col" class="two wide">${message(code:'default.relevance.label')}</th>
            <th scope="col" class="two wide">${message(code:'default.source.label')}</th>
            <th scope="col" class="three wide">${message(code:'default.event.label')}</th>
            <th scope="col" class="five wide">Payload</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${events}" var="el" status="i">
            <%
                String tdClass = 'table-td-yoda-blue'
                switch (el.relevance?.value?.toLowerCase()) {
                    case 'info'     : tdClass = ''; break
                    case 'ok'       : tdClass = 'positive'; break
                    case 'warning'  : tdClass = 'warning'; break
                    case 'error'    : tdClass = 'error'; break
                }
                if (! el.hasChanged) {
                    tdClass += ' sf_simple'
                }
            %>
            <tr>
                <td class="${tdClass}">
                    <g:formatDate date="${el.created}" format="${message(code:'default.date.format.noZ')}" />
                </td>
                <td class="${tdClass}"> ${el.category} </td>
                <td class="${tdClass}"> ${el.relevance} </td>
                <td class="${tdClass}"> ${el.source} </td>
                <td class="${tdClass}"> ${el.event} </td>
                <td class="${tdClass}"> ${el.payload?.replaceAll(',', ', ')} </td>
            </tr>
        </g:each>
        </tbody>
    </table>

        </div>
    </div>
</g:if>

<laser:htmlEnd />
