<%@ page import="de.laser.auth.User; de.laser.utils.DateUtils; de.laser.system.SystemActivityProfiler; de.laser.system.SystemProfiler" %>
<laser:htmlStart text="..">
    <laser:javascript src="echarts.js"/>%{-- dont move --}%
</laser:htmlStart>

<ui:breadcrumbs>
    <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
    <ui:crumb text=".." class="active"/>
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
                <th></th>
                <th></th>
                <th></th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${users}" var="u">
                <tr>
                    <td>
                        ${u[0]}
                    </td>
                    <td></td>
                    <td></td>
                    <td>
                        ${DateUtils.getLocalizedSDF_noZ().format(new Date(u[1]))}
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

<laser:htmlEnd />