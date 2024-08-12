<%@ page import="de.laser.auth.User; de.laser.utils.DateUtils; de.laser.system.SystemActivityProfiler; de.laser.system.SystemProfiler" %>
<laser:htmlStart message="menu.yoda.profilerCurrent">
    <laser:javascript src="echarts.js"/>%{-- dont move --}%
</laser:htmlStart>

<ui:breadcrumbs>
    <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
    <ui:crumb message="menu.yoda.profilerCurrent" class="active"/>
</ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.yoda.profiler" type="yoda" total="${SystemProfiler.executeQuery('select count(*) from SystemProfiler')[0]}" />

    <nav class="ui secondary menu">
        <g:link controller="yoda" action="profilerLoadtime" class="item">Ladezeiten</g:link>
        <g:link controller="yoda" action="profilerActivity" class="item">Nutzerzahlen</g:link>
        <g:link controller="yoda" action="profilerTimeline" class="item">Seitenaufrufe</g:link>
        <g:link controller="yoda" action="profilerCurrent" class="item active">..</g:link>
    </nav>

    <div class="ui segment">
        <table class="ui table simple">
            <thead>
            <tr>
                <th class="eight wide"></th>
                <th class="four wide"></th>
                <th class="four wide"></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${users}" var="u">
                <tr>
                    <td>
                        ${User.get(u[0]).formalOrg}
                    </td>
                    <td>
                        <!--${User.get(u[0]).username}-->
                    </td>
                    <td>
                        ${DateUtils.getLocalizedSDF_noZ().format(new Date(u[1]))}
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

<laser:htmlEnd />