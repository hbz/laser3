<%@ page import="com.k_int.kbplus.RefdataValue;com.k_int.kbplus.auth.Role;com.k_int.kbplus.auth.UserOrg" %>
<laser:serviceInjection />

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code: 'menu.user.errorReport')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.institutions.help" class="active"/>
</semui:breadcrumbs>
<br>
<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code: 'menu.user.errorReport')}</h1>

<semui:subNav actionName="${actionName}">
    <semui:subNavItem controller="profile" action="errorOverview" message="profile.errorOverview.label" />
    <semui:subNavItem controller="profile" action="errorReport" message="profile.errorReport.label" />
</semui:subNav>

<g:if test="${'ok'.equalsIgnoreCase(sendingStatus)}">
    <semui:msg class="positive" text="Ihr Fehlerbericht wurde übertragen." />
</g:if>
<g:if test="${'fail'.equalsIgnoreCase(sendingStatus)}">
    <semui:msg class="negative" text="Ihr Fehlerbericht konnte leider nicht übertragen werden. Beachten Sie bitte die Pflichtfelder." />
</g:if>

<div class="ui icon info message">
    <i class="icon info"></i>
    <g:message code="profile.errorReport.infobox"/>
</div>

<div class="ui warning message">
    <div class="header"><g:message code="profile.errorReport.errorHeader"/></div>
    <g:message code="profile.errorReport.errorBody"/>
</div>

<semui:form>
    <form method="post">
        <div class="ui form">

            <div class="required field">
                <label for="title"><g:message code="profile.errorReport.title"/></label>
                <input  id="title" name="title" type="text" value="${title}"/>
            </div>

            <div class="field">
                <label><g:message code="profile.errorReport.observed"/></label>
                <textarea name="described">${described}</textarea>
            </div>
            <div class="field">
                <label><g:message code="profile.errorReport.expected"/></label>
                <textarea name="expected">${expected}</textarea>
            </div>
            <div class="field">
                <label><g:message code="profile.errorReport.context"/></label>
                <textarea name="info">${info}</textarea>
            </div>

            <div class="field">
                <input type="submit" name="sendErrorReport" class="ui button" value="<g:message code="profile.errorReport.submit"/>">
            </div>

        </div>
    </form>
</semui:form>

<r:script>
    $('.ui.form').form({
        on: 'blur',
        fields: {
            title: 'empty'
        }
    })
</r:script>
</body>
</html>
