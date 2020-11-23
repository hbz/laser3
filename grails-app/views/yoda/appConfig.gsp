<%@ page import="de.laser.Org; de.laser.properties.PropertyDefinition" %>

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'menu.yoda.appConfig')} </title>
    <%-- r:require module="annotations" / --%>
    <asset:javascript src="properties.js"/>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
    <semui:crumb text="Application Config" class="active"/>
</semui:breadcrumbs>

<h2 class="ui header">${message(code:'menu.yoda.appConfig')}</h2>

<p>${message(code:'sys.properties')}</p>

<asset:script type="text/javascript">
    $(document).ready(function(){
        c3po.initProperties("<g:createLink controller='ajaxJson' action='lookup'/>", "#custom_props_div_1");
    });
</asset:script>

<g:form action="appConfig" method="POST" class="ui form">
    <input type="submit" name="one"class="ui button" value="Refresh"  />
</g:form>
<h3 class="ui header"> Current output for Holders.config</h3>
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
