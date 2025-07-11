<%@ page import="de.laser.auth.User; de.laser.utils.DateUtils; de.laser.system.SystemActivityProfiler; de.laser.system.SystemProfiler" %>
<laser:htmlStart message="menu.admin.profilerCurrent">
    <laser:javascript src="echarts.js"/>%{-- dont move --}%
</laser:htmlStart>

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb message="menu.admin.profilerCurrent" class="active"/>
</ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.admin.profiler" type="admin" total="${SystemProfiler.executeQuery('select count(*) from SystemProfiler')[0]}" />

    <g:render template="profiler/menu" />

    <div class="ui fluid card">
        <div class="content">
            <table class="ui table compact simple">
                <tbody>
                <g:each in="${users}" var="u">
                    <tr>
                        <td>${User.get(u[0]).formalOrg}</td>
                        <td>${User.get(u[0]).display} (${User.get(u[0]).username})</td>
                        <td>${DateUtils.getSDF_onlyTime().format(new Date(u[1]))}</td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>
    </div>

<laser:htmlEnd />