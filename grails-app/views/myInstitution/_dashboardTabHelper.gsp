<%@ page import="de.laser.storage.RDStore; de.laser.UserSetting" %>


<g:if test="${tmplKey == UserSetting.KEYS.DASHBOARD_TAB_TIME_CHANGES}">

    <g:set var="tabTimeChanges" value="${user.getSettingsValue(UserSetting.KEYS.DASHBOARD_TAB_TIME_CHANGES, 14)}" />
    <ui:msg class="info" hideClose="true">
        <g:if test="${pendingCount > pending.size()}">
            ${message(code:'changes.dashboard.msg.more', args:[
                    g.createLink(controller:'profile', action:'index'),
                    tabTimeChanges
            ])}
        </g:if>
        <g:else>
            <g:message code="dashboard.tabTime.changes" args="${tabTimeChanges}" />
        </g:else>
    </ui:msg>

</g:if>

<g:elseif test="${tmplKey == 'DUEDATES'}">

    <g:if test="${! dueDates}">
        <ui:msg class="info" hideClose="true" message="profile.noDashboardReminderDates" args="${[createLink(controller:'profile', action:'index')]}" />
    </g:if>
    <g:else>
        <ui:msg class="info" hideClose="true" message="dashboard.tabTime.dueDates" args="${[g.createLink(controller:'profile', action:'index')]}" />
    </g:else>

</g:elseif>

<g:elseif test="${tmplKey == UserSetting.KEYS.DASHBOARD_TAB_TIME_SERVICE_MESSAGES}">

    <ui:msg class="info" hideClose="true">
        <g:message code="dashboard.tabTime.serviceMessages" args="${user.getSettingsValue(UserSetting.KEYS.DASHBOARD_TAB_TIME_SERVICE_MESSAGES, 14)}" />
    </ui:msg>

</g:elseif>

<g:elseif test="${tmplKey == UserSetting.KEYS.DASHBOARD_TAB_TIME_SURVEYS_MANDATORY_ONLY}">

    <g:set var="proLink"   value="${ g.createLink(controller:'survey', action:'workflowsSurveysConsortia', params:[tab:'active']) }" />
    <g:set var="basicLink" value="${ g.createLink(controller:'myInstitution', action:'currentSurveys', params:[tab:'active']) }" />

    <ui:msg class="info" hideClose="true">%{-- copied from myInstitution/_surveys.gsp --}%
        <g:if test="${contextService.getOrg().isCustomerType_Consortium_Pro()}">
            <g:if test="${surveysCount > surveys.size()}">
                ${message(code:'survey.dashboard.msg.more', args:[g.createLink(controller:'profile', action:'index'), proLink])}
            </g:if>
            <g:else>
                <g:message code="dashboard.tabTime.surveys" args="${[proLink]}" />
            </g:else>
        </g:if>
        <g:else>
            <g:if test="${surveysCount > surveys.size()}">
                ${message(code:'survey.dashboard.msg.more', args:[g.createLink(controller:'profile', action:'index'), basicLink])}
            </g:if>
            <g:else>
                <g:message code="dashboard.tabTime.surveys" args="${[basicLink]}" />
            </g:else>
        </g:else>
    </ui:msg>

</g:elseif>

<g:elseif test="${tmplKey == UserSetting.KEYS.DASHBOARD_TAB_TIME_TASKS}">

    <g:set var="tabTimeTasks" value="${user.getSettingsValue(UserSetting.KEYS.DASHBOARD_TAB_TIME_TASKS, 30)}" />
    <ui:msg class="info" hideClose="true">
        <g:if test="${tasksCount > tasks.size()}">
            ${message(code:'task.dashboard.msg.more', args:[
                    g.createLink(controller:'profile', action:'index'),
                    tabTimeTasks,
                    g.createLink(controller:'myInstitution', action:'tasks')
            ])}
        </g:if>
        <g:else>
            <g:message code="dashboard.tabTime.tasks" args="${tabTimeTasks}" />
        </g:else>
    </ui:msg>

</g:elseif>

<g:elseif test="${tmplKey == UserSetting.KEYS.DASHBOARD_TAB_TIME_WORKFLOWS}">

    <g:set var="tabTimeWorkflows" value="${user.getSettingsValue(UserSetting.KEYS.DASHBOARD_TAB_TIME_WORKFLOWS, 20)}" />
    <ui:msg class="info" hideClose="true">
        <g:if test="${wfListCount > wfList.size()}">
            ${message(code:'workflow.dashboard.msg.more', args:[
                    g.createLink(controller:'profile', action:'index'),
                    tabTimeWorkflows,
                    g.createLink(controller:'myInstitution', action:'currentWorkflows', params:[filterStatus:RDStore.WF_WORKFLOW_STATUS_OPEN.id])
            ])}
        </g:if>
        <g:else>
            <g:message code="dashboard.tabTime.workflows" args="${tabTimeWorkflows}" />
        </g:else>
    </ui:msg>

</g:elseif>

