<%@ page import="de.laser.config.ConfigMapper; de.laser.system.SystemSetting; de.laser.jobs.HeartbeatJob" %>
<laser:htmlStart message="menu.yoda.systemSettings" />

<ui:breadcrumbs>
    <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
    <ui:crumb message="menu.yoda.systemSettings" class="active"/>
</ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.yoda.systemSettings" />

    <g:set var="mailConfigDisabled" value="${ConfigMapper.getGrailsMailDisabled()}" />
    <g:set var="maintenanceModeEnabled" value="${SystemSetting.findByName('MaintenanceMode').value == 'true'}" />

    <div class="ui info message">
        <i class="ui icon hand point right"></i> ${message(code: 'system.maintenanceMode.info', args: [HeartbeatJob.HEARTBEAT_IN_SECONDS])}
    </div>

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
                        <ui:xEditable owner="${s}" field="value" overwriteEditable="${true}"/>
                    </g:else>
                </td>
                <td>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>

<laser:htmlEnd />
