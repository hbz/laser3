<%@ page import="java.lang.management.ManagementFactory" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'menu.yoda.systemThreads')}</title>
</head>

<body>
    <laser:serviceInjection />

    <semui:breadcrumbs>
        <semui:crumb message="menu.yoda" controller="yoda" action="index"/>
        <semui:crumb message="menu.yoda.systemThreads" class="active"/>
    </semui:breadcrumbs>

    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'menu.yoda.systemThreads')}</h1>

    <g:set var="threads" value="${Thread.getAllStackTraces().keySet().sort{ it.id }}" />
    <g:set var="tmxBean" value="${ManagementFactory.getThreadMXBean()}" />

    <div class="ui message info">
        <p>
            <strong>Anzahl gefundener Threads: ${threads.size()}</strong>
            <br />
            <br />
            Davon <strong>${tmxBean.getDaemonThreadCount()}</strong> Daemons und <strong>${threads.findAll{ it.name.startsWith('I/O ')}.size()}</strong> I/O Dispatcher.
            <g:if test="${tmxBean.findDeadlockedThreads()}">
                Es wurden <strong>${tmxBean.findDeadlockedThreads()?.size() ?: 0}</strong> Deadlocks gefunden.
            </g:if>
            Seit Systemstart wurden <strong>${tmxBean.getTotalStartedThreadCount()}</strong> Threads erzeugt.
            Die Höchstlast lag bei <strong>${tmxBean.getPeakThreadCount()}</strong> Threads.
        </p>
    </div>

    <table class="ui celled la-js-responsive-table la-table la-hover-table compact table" id="contextTable">
        <tbody>
            <g:each in="${threads}" var="thread">
                <g:set var="thClass" value="${thread.getName().startsWith('I/O ') ? 'table-td-yoda-yellow' : ''}" />
                <g:set var="thClass" value="${thread.isDaemon() ? 'table-td-yoda-green' : thClass}" />

                <tr>
                    <td class="${thClass}">${thread.getId()}</td>
                    <td class="${thClass}">
                        <g:if test="${thread.isDaemon()}">
                            <span><i class="icon blue cog"></i> ${thread.getName()}</span>
                        </g:if>
                        <g:else>
                            <span>${thread.getName()}</span>
                        </g:else>
                    </td>
                    <td class="${thClass}">
                        ${thread.getThreadGroup().getName()}
                        <%
                            java.lang.ThreadGroup tmp = thread.getThreadGroup()
                            while( (tmp = tmp.getParent()) != null ) {
                                println " / ${tmp.getName()}"
                            }
                        %>
                    </td>
                    <td class="${thClass}">${thread.getPriority()}</td>
                    <td class="${thClass}">
                        <g:if test="${thread.isAlive()}">
                            <span>${thread.getState()}</span>
                        </g:if>
                        <g:else>
                            <span style="color:red">${thread.getState()}</span>
                        </g:else>
                    </td>
                    <td class="${thClass}">${Math.round(tmxBean.getThreadCpuTime( thread.getId() ) / 1000000000 * 1000) / 1000}s</td>
                </tr>
            </g:each>
        </tbody>
    </table>

</body>
</html>
