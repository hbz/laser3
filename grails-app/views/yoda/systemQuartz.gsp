<%@ page import="grails.util.Holders; de.laser.config.ConfigMapper" %>
<laser:htmlStart message="menu.yoda.systemQuartz" />

<ui:breadcrumbs>
    <ui:crumb message="menu.yoda" controller="yoda" action="index"/>
    <ui:crumb message="menu.yoda.systemQuartz" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="menu.yoda.systemQuartz" type="yoda" />

%{--${ConfigMapper.setConfig( ConfigMapper.QUARTZ_HEARTBEAT, new Date())} ##--}%
%{--${ConfigMapper.getQuartzHeartbeat()} ##--}%

<ui:msg class="info" noClose="true">
    <i class="check icon"></i> Job is active <br />
    <i class="stop icon"></i> Job is NOT active <br />
    <i class="exclamation triangle icon"></i> Job is NOT available - due deactivation oder missing configuration <br />
</ui:msg>

<g:each in="${quartz}" var="groupKey, group">
    <table class="ui celled la-js-responsive-table la-table la-hover-table compact table">
        <thead>
            <tr>
                <th>#</th>
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
                    String tdClass = '', tdIcon = 'question'

                    boolean isActive = true

                    if (job.configurationProperties) {
                        job.configurationProperties.each { prop ->
//                            isActive = isActive && (currentConfig.get(prop[0].trim()) && ! (currentConfig.get(prop[0].trim()) in [null, false]))
                            def cpval = Holders.grailsApplication.config.getProperty(prop[0].trim(), Object)
                            isActive = isActive && (cpval != null && cpval != false)
                        }
                    }

                    if (job.running || isActive) {
                        tdClass   = 'positive'
                        tdIcon    = 'check'
                    }
                    else if (! job.available && ! job.nextFireTime) {
                        tdClass   = 'grey'
                        tdIcon    = 'stop'
                    }
                    else if (! job.available) {
                        tdClass   = 'error'
                        tdIcon    = 'exclamation triangle'
                    }
                %>
                <tr>
                    <td class="${tdClass}">
                        <i class="${tdIcon} icon"></i>
                    </td>
                    <td class="${tdClass}">
                        ${job.name}
                    </td>
                    <td class="${tdClass}">
                        <g:each in="${job.services}" var="srv">
                            ${srv} <br />
                        </g:each>
                    </td>
                    <td class="${tdClass}">
                        <g:each in="${job.configurationProperties}" var="prop">
%{--                            <g:if test="${currentConfig.get(prop[0].trim())}">--}%
%{--                                ${prop[0]} = ${currentConfig.get(prop[0].trim())} <br />--}%
                            <g:set var="cpval" value="${Holders.grailsApplication.config.getProperty(prop[0].trim(), Object)}" />
                            <g:if test="${cpval}">
                                ${prop[0]} = ${cpval} <br />
                            </g:if>
                            <g:else>
                                ${prop[0]} <br />
                            </g:else>
                        </g:each>
                    </td>
                    <td class="${tdClass}">
                        <code>${job.cronEx}</code>
                    </td>
                    <td class="${tdClass}">
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