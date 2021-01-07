<%@ page import="de.laser.Org; de.laser.properties.PropertyDefinition" %>

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'menu.yoda.appConfig')} </title>
    <%-- r:require module="annotations" / --%>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
    <semui:crumb text="Application Config" class="active"/>
</semui:breadcrumbs>

<h1 class="ui header la-clear-before la-noMargin-top">${message(code:'menu.yoda.appConfig')}</h1>

<p>${message(code:'sys.properties')}</p>

<laser:script file="${this.getGroovyPageFileName()}">
    c3po.initProperties("<g:createLink controller='ajaxJson' action='lookup'/>", "#custom_props_div_1");
</laser:script>

<g:form action="appConfig" method="POST" class="ui form">
    <input type="submit" name="one" class="ui button" value="Refresh"  />
</g:form>
<h2 class="ui header"> Current output for Holders.config</h2>
<div class="ui form">
    <g:each in="${currentconf.keySet().sort()}" var="key">
        <div class="field">
            <label>${key}</label>

            <g:if test="${blacklist.contains(key)}">
                <g:textArea readonly="" rows="2" style="width:95%" name="key" value="=== C O N C E A L E D ==="/>
            </g:if>
            <g:else>
                <g:textArea readonly="" rows="2" style="width:95%" name="key" value="${currentconf.get(key)}" escapeHtml="false" />
            </g:else>

        </div>
    </g:each>
</div>

</body>
</html>
