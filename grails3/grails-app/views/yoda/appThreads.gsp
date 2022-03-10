<%@ page import="java.lang.management.ManagementFactory" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'menu.yoda.appThreads')}</title>
</head>

<body>
    <laser:serviceInjection />

    <semui:breadcrumbs>
        <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
        <semui:crumb message="menu.yoda.appThreads" class="active"/>
    </semui:breadcrumbs>

    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'menu.yoda.appThreads')}</h1>

    <g:set var="threads" value="${Thread.getAllStackTraces().keySet().sort{ it.id }}" />
    <g:set var="tmxBean" value="${ManagementFactory.getThreadMXBean()}" />

    <h2 class="ui header">Anzahl gefundener Threads: ${threads.size()}</h2>
    <p>
        Davon <strong class="ui label circular">${tmxBean.getDaemonThreadCount()}</strong> Daemons,
        <strong class="ui label circular">${threads.findAll{ it.name.startsWith('I/O ')}.size()}</strong> I/O Dispatcher,
        <strong class="ui label circular">${tmxBean.findDeadlockedThreads()?.size() ?: 0}</strong> Deadlocks.
        Seit Systemstart wurden <strong class="ui label circular">${tmxBean.getTotalStartedThreadCount()}</strong> Threads erzeugt.
        Höchstlast lag bei <strong class="ui label circular">${tmxBean.getPeakThreadCount()}</strong>.
    </p>

    <table class="ui celled la-js-responsive-table la-table compact table" id="contextTable">
        <tbody>
            <g:each in="${threads}" var="thread">

                <tr<g:if test="${thread.getName().startsWith('I/O ')}"> style="background-color:#fef9e7"</g:if>>
                    <td>${thread.getId()}</td>
                    <td>
                        <g:if test="${thread.isDaemon()}">
                            <span><i class="icon blue cog"></i> ${thread.getName()}</span>
                        </g:if>
                        <g:else>
                            <span>${thread.getName()}</span>
                        </g:else>
                    </td>
                    <td>
                        ${thread.getThreadGroup().getName()}
                        <%
                            java.lang.ThreadGroup tmp = thread.getThreadGroup()
                            while( (tmp = tmp.getParent()) != null ) {
                                println " / ${tmp.getName()}"
                            }
                        %>
                    </td>
                    <td>${thread.getPriority()}</td>
                    <td>
                        <g:if test="${thread.isAlive()}">
                            <span>${thread.getState()}</span>
                        </g:if>
                        <g:else>
                            <span style="color:red">${thread.getState()}</span>
                        </g:else>
                    </td>
                    <td>${Math.round(tmxBean.getThreadCpuTime( thread.getId() ) / 1000000000 * 1000) / 1000}s</td>
                </tr>
            </g:each>
        </tbody>
    </table>

</body>
</html>
