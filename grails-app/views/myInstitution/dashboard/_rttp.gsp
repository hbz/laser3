<%@ page import="de.laser.ui.IconAttr; de.laser.License; de.laser.Subscription; de.laser.CustomerTypeService; de.laser.OrgSetting; de.laser.auth.User; de.laser.Org; de.laser.utils.DateUtils;  de.laser.storage.RDStore; de.laser.Task; de.laser.system.SystemActivityProfiler; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.remote.Wekb" %>
<laser:serviceInjection />

<g:set var="rttpList" value="[]" />

<g:if test="${contextService.getOrg().isCustomerType_Pro()}">
    <%
        rttpList = Task.executeQuery(
            'select t from Task t where t.tipp != null and t.status = :status ' +
            'and (( t.responsibleUser = :user or t.responsibleOrg = :org ) or t.creator = :user )' +
            'order by endDate, createDate',
            [user: contextService.getUser(), org: contextService.getOrg(), status: RDStore.TASK_STATUS_OPEN]
        )
    %>
</g:if>

<g:if test="${rttpList}">
    <div class="ui fluid card">
        <div class="ui top attached label">
            ${message(code: 'dashboard.card.reportTitleToProvider')}: ${rttpList.size()}
        </div>
        <div class="content">
            <table class="ui unstackable scrolling compact table">
                <tbody style="max-height:110px">%{-- count:180=5;110=3 --}%
                    <g:each in="${rttpList}" var="task">
                        <tr>
                            <td colspan="12">
                                <g:link controller="tipp" action="show" id="${task.tipp?.id}" target="_blank">
                                    <i class="${Icon.TIPP} la-list-icon"></i>${task.tipp?.name}
                                </g:link>
                            </td>
                            <td colspan="4" class="center aligned">
                                <g:if test="${task.creator.id == contextService.getUser().id}">
                                    <a href="#" onclick="JSPC.app.dashboard.editTask(${task.id});">
                                        <i class="${Icon.SIG.MY_OBJECT} la-list-icon"></i>${DateUtils.getLocalizedSDF_noTime().format(task.endDate)}
                                    </a>
                                </g:if>
                                <g:elseif test="${taskService.hasWRITE()}">
                                    <a href="#" onclick="JSPC.app.dashboard.editTask(${task.id});">
                                        <i class="${Icon.TASK} la-list-icon"></i>${DateUtils.getLocalizedSDF_noTime().format(task.endDate)}
                                    </a>
                                </g:elseif>
                                <g:else>
                                    <a href="#" onclick="JSPC.app.dashboard.readTask(${task.id});">
                                        <i class="${Icon.CMD.READ} la-list-icon"></i>${DateUtils.getLocalizedSDF_noTime().format(task.endDate)}
                                    </a>
                                </g:else>
                            </td>
                        </tr>
                    </g:each>
                </tbody>
            </table>
        </div>
        <div class="extra content">
            <div class="right floated">
                <g:link controller="myInstitution" action="tasks" params="${[taskObject: 'tipp']}">
                    ${message(code:'menu.my.tasks')}
                </g:link>
            </div>
        </div>
    </div>
</g:if>
<g:else>
    <div class="ui fluid card">
        <div class="ui top attached label">
            ${message(code: 'dashboard.card.currently')}
        </div>
        <div class="content">
            <div class="ui relaxed list">
                <div class="item">
                    <div class="content">
                        .. sind <strong>${OrgSetting.executeQuery(
                                'select count(os.org) from OrgSetting as os where os.key = :key and os.roleValue.authority in (:roles) and os.org.archiveDate is null',
                                [key: OrgSetting.KEYS.CUSTOMER_TYPE, roles: [CustomerTypeService.ORG_INST_BASIC, CustomerTypeService.ORG_INST_PRO]]
                        )[0]}</strong> aktive Einrichtungen registriert.
                    </div>
                </div>
%{--                <div class="item">--}%
%{--                    <div class="content">--}%
%{--                        .. sind <strong>${OrgSetting.executeQuery(--}%
%{--                                'select count(os.org) from OrgSetting as os where os.key = :key and os.roleValue.authority in (:roles) and os.org.archiveDate is null',--}%
%{--                                [key: OrgSetting.KEYS.CUSTOMER_TYPE, roles: [CustomerTypeService.ORG_CONSORTIUM_BASIC, CustomerTypeService.ORG_CONSORTIUM_PRO]]--}%
%{--                        )[0]}</strong> aktive Konsortialmanager registriert.--}%
%{--                    </div>--}%
%{--                </div>--}%
                <div class="item">
                    <div class="content">
                        .. sind <strong>${User.executeQuery('select count(*) from User where enabled is true')[0]}</strong> aktive Benutzer registriert.
                    </div>
                </div>
%{--                <div class="item">--}%
%{--                    <div class="content">--}%
%{--                        .. werden <strong>${Subscription.executeQuery(--}%
%{--                            'select count(*) from Subscription where status = :current', [current: RDStore.SUBSCRIPTION_CURRENT]--}%
%{--                        )[0]}</strong> aktive Lizenzen und <strong>${License.executeQuery(--}%
%{--                        'select count(*) from License where status = :current', [current: RDStore.LICENSE_CURRENT]--}%
%{--                        )[0]}</strong> aktive Verträge verwaltet.--}%
%{--                    </div>--}%
%{--                </div>--}%
                <div class="item">
                    <div class="content">
                        .. ${SystemActivityProfiler.getNumberOfActiveUsers() == 1 ? 'ist' : 'sind'}
                        <strong>${SystemActivityProfiler.getNumberOfActiveUsers()}</strong> Benutzer online.
                    </div>
                </div>
            </div>
        </div>
    </div>
</g:else>
