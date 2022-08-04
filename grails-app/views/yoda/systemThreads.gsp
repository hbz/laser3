<%@ page import="java.lang.management.ManagementFactory" %>
<laser:htmlStart message="menu.yoda.systemThreads" serviceInjection="true"/>

    <ui:breadcrumbs>
        <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
        <ui:crumb message="menu.yoda.systemThreads" class="active"/>
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="menu.yoda.systemThreads" />

    <g:set var="threads" value="${Thread.getAllStackTraces().keySet().sort{ it.id }}" />
    <g:set var="tmxBean" value="${ManagementFactory.getThreadMXBean()}" />

    <ui:msg class="info" noClose="true">
            <strong>Anzahl gefundener Threads: ${threads.size()}</strong>
            <br />
            <br />
            Davon <strong>${tmxBean.getDaemonThreadCount()}</strong> Daemons und <strong>${threads.findAll{ it.name.startsWith('I/O ')}.size()}</strong> I/O Dispatcher.
            <g:if test="${tmxBean.findDeadlockedThreads()}">
                Es wurden <strong>${tmxBean.findDeadlockedThreads()?.size() ?: 0}</strong> Deadlocks gefunden.
            </g:if>
            Seit Systemstart wurden <strong>${tmxBean.getTotalStartedThreadCount()}</strong> Threads erzeugt.
            Die Höchstlast lag bei <strong>${tmxBean.getPeakThreadCount()}</strong> Threads.
    </ui:msg>

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
                            <span class="sc_red">${thread.getState()}</span>
                        </g:else>
                    </td>
                    <td class="${thClass}">${(tmxBean.getThreadCpuTime( thread.getId() ) / 1000000000).round(3)}s</td>
                </tr>
            </g:each>
        </tbody>
    </table>

<laser:htmlEnd />
