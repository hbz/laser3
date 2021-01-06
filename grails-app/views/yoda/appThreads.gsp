<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'menu.yoda.appThreads')}</title>
</head>

<body>
    <laser:serviceInjection />

    <semui:breadcrumbs>
        <semui:crumb message="menu.yoda.dash" />
        <semui:crumb message="menu.yoda.appThreads" class="active"/>
    </semui:breadcrumbs>

    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code:'menu.yoda.appThreads')}</h1>

    <h2 class="ui header">Anzahl gefundener Threads: ${Thread.getAllStackTraces().keySet().size()}</h2>

    <table class="ui celled la-table compact table" id="contextTable">
        <tbody>
            <g:each in="${Thread.getAllStackTraces().keySet().sort{ it.id }}" var="thread">
                <tr>
                    <td>${thread.getId()}</td>
                    <td>${thread.getName().replaceAll('%002e', '.')}</td>
                    <td>
                        ${thread.getThreadGroup().getName()}
                        <%
                            java.lang.ThreadGroup tmp = thread.getThreadGroup()
                            while( (tmp = tmp.getParent()) != null ) {
                                println " / ${tmp.getName()}"
                            }
                        %>
                    </td>
                    <td>${thread.getState()}</td>
                    <td>${thread.isAlive()}</td>
                </tr>
            </g:each>
        </tbody>
    </table>

</body>
</html>
