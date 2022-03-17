<%@ page import="de.laser.helper.AppUtils; de.laser.system.SystemSetting" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'menu.yoda.systemSettings')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
    <semui:crumb message="menu.yoda.systemSettings" class="active"/>
</semui:breadcrumbs>

<div>
    <h1 class="ui header la-clear-before la-noMargin-top">${message(code: 'menu.yoda.systemSettings')}</h1>

    <g:set var="mailConfigDisabled" value="${AppUtils.getConfig('grails.mail.disabled')}" />
    <g:set var="maintenanceModeEnabled" value="${SystemSetting.findByName('MaintenanceMode').value == 'true'}" />

    <table class="ui celled la-js-responsive-table la-table table">
        <thead>
        <tr>
            <th>${message(code: 'default.setting.label')}</th>
            <th>${message(code: 'default.value.label')}</th>
            <th></th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td>${message(code: 'system.config.mail.label')}</td>
            <td>
                <g:if test="${mailConfigDisabled}">
                    <i class="icon square full red"></i>${message(code: 'default.deactivated.label')}
                </g:if>
                <g:else>
                    <i class="icon square full green"></i> ${message(code: 'default.activated.label')}
                </g:else>
            </td>
            <td>
                <g:if test="${mailConfigDisabled}">
                    <g:link controller="yoda" action="toggleMailSent" class="ui button positive right floated" params="${[mailSent: true]}">
                        ${message(code: 'system.config.mail.activate')}
                    </g:link>
                </g:if>
                <g:else>
                    <g:link controller="yoda" action="toggleMailSent" class="ui button negative right floated" params="${[mailSent: false]}">
                        ${message(code: 'system.config.mail.deactivate')}
                    </g:link>
                </g:else>
            </td>
        </tr>
        <tr>
            <td>${message(code: 'system.setting.maintenanceMode.label')}</td>
            <td>
                <g:if test="${! maintenanceModeEnabled}">
                    <i class="icon square full red"></i> ${message(code: 'default.deactivated.label')}
                </g:if>
                <g:else>
                    <i class="icon square full green"></i> ${message(code: 'default.activated.label')}
                </g:else>
            </td>
            <td>
                <g:if test="${! maintenanceModeEnabled}">
                    <g:link controller="yoda" action="toggleBoolSetting" class="ui button positive right floated" params="${[setting: 'MaintenanceMode']}">
                        ${message(code: 'system.setting.maintenanceMode.activate')}
                    </g:link>
                </g:if>
                <g:else>
                    <g:link controller="yoda" action="toggleBoolSetting" class="ui button negative right floated" params="${[setting: 'MaintenanceMode']}">
                        ${message(code: 'system.setting.maintenanceMode.deactivate')}
                    </g:link>
                </g:else>
            </td>
        </tr>
        <g:each in="${settings}" var="s">
            <tr>
                <td>${s.name}</td>
                <td>
                    <g:if test="${s.tp == 1}">
                        <g:link controller="yoda" action="toggleBoolSetting" params="${[setting: s.name]}">${s.value}</g:link>
                    </g:if>
                    <g:else>
                        <semui:xEditable owner="${s}" field="value" overwriteEditable="${true}"/>
                    </g:else>
                </td>
                <td>
                    <g:if test="${s.name == 'StatusUpdateInterval'}">Sekunden</g:if>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>
</div>
</body>
</html>
