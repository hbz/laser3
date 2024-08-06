<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.config.ConfigMapper; de.laser.system.SystemSetting; de.laser.jobs.HeartbeatJob" %>
<laser:htmlStart message="menu.yoda.systemSettings" />

<ui:breadcrumbs>
    <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
    <ui:crumb message="menu.yoda.systemSettings" class="active"/>
</ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.yoda.systemSettings" type="yoda" />

    <g:set var="configMailDisabled" value="${ConfigMapper.getGrailsMailDisabled()}" />
    <g:set var="maintenanceModeEnabled" value="${SystemSetting.findByName('MaintenanceMode').value == 'true'}" />
    <g:set var="systemInsightEnabled" value="${SystemSetting.findByName('SystemInsight').value == 'true'}" />

    <ui:msg class="info" hideClose="true">
        <icon:pointingHand /> ${message(code: 'system.maintenanceMode.info.TMP', args: [HeartbeatJob.HEARTBEAT_IN_SECONDS])}
    </ui:msg>

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
            <td>
                ${message(code: 'system.config.mail.label')}
            </td>
            <td>
                <g:if test="${configMailDisabled}">
                    <i class="${Icon.SYM.SQUARE} red"></i>${message(code: 'default.deactivated.label')}
                </g:if>
                <g:else>
                    <i class="${Icon.SYM.SQUARE} green"></i> ${message(code: 'default.activated.label')}
                </g:else>
            </td>
            <td>
                <g:if test="${configMailDisabled}">
                    <g:link controller="yoda" action="toggleMailSent" class="${Btn.POSITIVE} right floated" params="${[mailSent: true]}">
                        ${message(code: 'system.setting.activate', args:[message(code: 'system.config.mail.label')])}
                    </g:link>
                </g:if>
                <g:else>
                    <g:link controller="yoda" action="toggleMailSent" class="${Btn.NEGATIVE} right floated" params="${[mailSent: false]}">
                        ${message(code: 'system.setting.deactivate', args:[message(code: 'system.config.mail.label')])}
                    </g:link>
                </g:else>
            </td>
        </tr>
        <tr>
            <td>
                ${message(code: 'system.setting.maintenanceMode.label')}
            </td>
            <td>
                <g:if test="${! maintenanceModeEnabled}">
                    <i class="${Icon.SYM.SQUARE} red"></i> ${message(code: 'default.deactivated.label')}
                </g:if>
                <g:else>
                    <i class="${Icon.SYM.SQUARE} green"></i> ${message(code: 'default.activated.label')}
                </g:else>
            </td>
            <td>
                <g:if test="${! maintenanceModeEnabled}">
                    <g:link controller="yoda" action="toggleBoolSetting" class="${Btn.POSITIVE} right floated" params="${[setting: 'MaintenanceMode']}">
                        ${message(code: 'system.setting.activate', args:[message(code: 'system.setting.maintenanceMode.label')])}
                    </g:link>
                </g:if>
                <g:else>
                    <g:link controller="yoda" action="toggleBoolSetting" class="${Btn.NEGATIVE} right floated" params="${[setting: 'MaintenanceMode']}">
                        ${message(code: 'system.setting.deactivate', args:[message(code: 'system.setting.maintenanceMode.label')])}
                    </g:link>
                </g:else>
            </td>
        </tr>
        <tr>
            <td>
                ${message(code: 'system.setting.systemInsight.label')}
                <g:if test="${systemInsightEnabled && configMailDisabled}">
                    <span style="color:red;float:right"><em>* erfordert: ${message(code: 'system.config.mail.label')}</em></span>
                </g:if>
            </td>
            <td>
                <g:if test="${! systemInsightEnabled}">
                    <i class="${Icon.SYM.SQUARE} red"></i> ${message(code: 'default.deactivated.label')}
                </g:if>
                <g:else>
                    <i class="${Icon.SYM.SQUARE} green"></i> ${message(code: 'default.activated.label')}
                </g:else>
            </td>
            <td>
                <g:if test="${! systemInsightEnabled}">
                    <g:link controller="yoda" action="toggleBoolSetting" class="${Btn.POSITIVE} right floated" params="${[setting: 'SystemInsight']}">
                        ${message(code: 'system.setting.activate', args:[message(code: 'system.setting.systemInsight.label')])}
                    </g:link>
                </g:if>
                <g:else>
                    <g:link controller="yoda" action="toggleBoolSetting" class="${Btn.NEGATIVE} right floated" params="${[setting: 'SystemInsight']}">
                        ${message(code: 'system.setting.deactivate', args:[message(code: 'system.setting.systemInsight.label')])}
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
