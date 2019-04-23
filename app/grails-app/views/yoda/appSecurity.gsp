<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'menu.yoda.security')}</title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
    <semui:crumb message="menu.yoda.security'" class="active"/>
</semui:breadcrumbs>

<h1 class="ui left aligned icon header"><semui:headerIcon />${message(code:'menu.yoda.security')}</h1>

<h3 class="ui header">Hierarchical Global Roles</h3>

<div class="secInfoWrapper">
    <div class="ui list">
        <div class="item">
            <span class="ROLE_YODA">ROLE_YODA</span> &rArr;
            <span class="ROLE_ADMIN">ROLE_ADMIN</span> &rArr;
            <span class="ROLE_DATAMANAGER">ROLE_DATAMANAGER</span> &rArr;
            <span class="ROLE_USER">ROLE_USER</span> &rArr;
            <span class="IS_AUTHENTICATED_FULLY">IS_AUTHENTICATED_FULLY</span>
        </div>
    </div>
</div>

<h3 class="ui header">Independent Global Roles</h3>

<div class="secInfoWrapper">
    <div class="ui list">
        <div class="item">
            <span class="ROLE_ORG_COM_EDITOR">ROLE_GLOBAL_DATA</span> |
            <span class="ROLE_ORG_EDITOR">ROLE_ORG_EDITOR</span> |
            <span class="ROLE_ORG_COM_EDITOR">ROLE_ORG_COM_EDITOR</span> |
            <span class="ROLE_PACKAGE_EDITOR">ROLE_PACKAGE_EDITOR</span> |
            <span class="ROLE_STATISTICS_EDITOR">ROLE_STATISTICS_EDITOR</span> |
            <span class="ROLE_TICKET_EDITOR">ROLE_TICKET_EDITOR</span>
        </div>
    </div>
    <div class="ui list">
        <div class="item">
            <span class="ROLE_API">ROLE_API</span>
        </div>
    </div>
</div>

<h3 class="ui header">Hierarchical? Org Roles</h3>

<div class="secInfoWrapper">
    <div class="ui list">
        <div class="item">
            <span class="ROLE_YODA">ORG_COLLECTIVE</span> &rArr;
            <span class="IS_AUTHENTICATED_FULLY">ORG_BASIC</span> &rArr;
            <span class="ROLE_USER">ORG_MEMBER</span> |
            <span class="ROLE_ADMIN">ORG_CONSORTIUM_SURVEY</span> &rArr;
            <span class="ROLE_DATAMANAGER">ORG_CONSORTIUM</span>
        </div>
    </div>
</div>

<h3 class="ui header">Hierarchical User Roles</h3>

<div class="secInfoWrapper">
    <div class="ui list">
        <div class="item">
            <span>INST_ADM</span> &rArr;
            <span>INST_EDITOR</span> &rArr;
            <span>INST_USER</span>  &nbsp; (implizite Prüfung auf <span class="ROLE_USER">ROLE_USER</span>)
        </div>
        <div class="item">
            <span class="ROLE_YODA">ROLE_YODA</span> und <span class="ROLE_ADMIN">ROLE_ADMIN</span> liefern <code>TRUE</code>
        </div>
    </div>
</div>

<h3 class="ui header">Controller Annotations</h3>

<div class="ui grid">
    <div class="sixteen wide column">

        <div class="secInfoWrapper secInfoWrapper2">
            <g:each in="${controller}" var="c">

                <h5 class="ui header" id="jumpMark_${c.key}">
                    ${c.key}
                    <g:each in="${c.value.secured}" var="cSecured">
                        <span class="${cSecured}">${cSecured}</span> &nbsp;
                    </g:each>
                </h5>

                <div class="ui segment">
                    <div class="ui divided list">
                        <g:each in="${c.value.methods}" var="method">
                            <div class="item">
                                <% def controllerName = c.key.substring(17)
                                controllerName = controllerName.split('Controller') %>
                                <g:link controller="${controllerName[0]}"
                                        action="${method.key}">${method.key}</g:link>

                                <g:each in="${method.value}" var="v">
                                    <g:if test="${v instanceof String}">
                                        <span class="${v}">${v}</span>
                                    </g:if>
                                    <g:else>
                                        <g:if test="${v.value}">
                                            <span class="${v.key}">${v.key}: ${v.value}</span>
                                        </g:if>
                                    </g:else>
                                </g:each>
                            </div>
                        </g:each>
                    </div>
                </div>
            </g:each>
        </div>

    </div>
    <%--
    <div class="four wide column">
        <div class="ui sticky">
            <aside>
                <g:each in="${controller}" var="c">
                    <a href="#jumpMark_${c.key}">${c.key.replaceFirst('com.k_int.kbplus.','').replaceFirst('de.laser.','').replaceAll('Controller', '  ')}</a> |
                </g:each>
            </aside>
        </div>
    </div>
    --%>
</div>

</body>
</html>

