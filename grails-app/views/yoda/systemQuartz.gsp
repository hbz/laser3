<%@ page import="de.laser.config.ConfigMapper" %>
<laser:htmlStart message="menu.yoda.systemQuartz" />

<ui:breadcrumbs>
    <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
    <ui:crumb message="menu.yoda.systemQuartz" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.yoda.systemQuartz" />

%{--${ConfigMapper.setConfig( ConfigMapper.QUARTZ_HEARTBEAT, new Date())} ##--}%
%{--${ConfigMapper.getQuartzHeartbeat()} ##--}%

<g:each in="${quartz}" var="groupKey, group">
    <table class="ui celled la-js-responsive-table la-table la-hover-table compact table">
        <thead>
            <tr>
                <th>Job</th>
                <th>Services</th>
                <th>Config</th>
                <th>s  m  h  DoM  M  DoW  Y</th>
                <th>Nächste Ausführung</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${group}" var="job">
                <%
                    String tdClass   = ''
                    String tdStyle   = ''

                    boolean isActive = true

                    if (job.configurationProperties) {
                        job.configurationProperties.each { prop ->
                            isActive = isActive && (currentConfig.get(prop[0].trim()) && ! (currentConfig.get(prop[0].trim()) in [null, false]))
                        }
                    }

                    if (job.running || isActive) {
                        tdClass = 'table-td-yoda-green'
                    }
                    else if (! job.available && ! job.nextFireTime) {
                        tdClass = 'table-td-yoda-red'
                        tdStyle = 'color:grey'
                    }
                    else if (! job.available) {
                        tdClass = 'table-td-yoda-yellow'
                        tdStyle = 'color:grey'
                    }
                %>
                <tr>
                    <td class="${tdClass}" style="${tdStyle}">
                        ${job.name}
                    </td>
                    <td class="${tdClass}" style="${tdStyle}">
                        <g:each in="${job.services}" var="srv">
                            ${srv} <br />
                        </g:each>
                    </td>
                    <td class="${tdClass}" style="${tdStyle}">
                        <g:each in="${job.configurationProperties}" var="prop">
                            <g:if test="${currentConfig.get(prop[0].trim())}">
                                ${prop[0]} = ${currentConfig.get(prop[0].trim())} <br />
                            </g:if>
                            <g:else>
                                ${prop[0]} <br />
                            </g:else>
                        </g:each>
                    </td>
                    <td class="${tdClass}" style="${tdStyle}">
                        <code>${job.cronEx}</code>
                    </td>
                    <td class="${tdClass}" style="${tdStyle}">
                        ${job.nextFireTime}
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>
</g:each>

<br />
<br />

    <%-- TODO: implement ajax calls --%>
    <laser:script file="${this.getGroovyPageFileName()}">
        setTimeout(function() {
            window.document.location.reload();
        }, (30 * 1000)); // refresh ~ 30 Seconds
    </laser:script>

<laser:htmlEnd />