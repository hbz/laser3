<%@ page import="com.k_int.kbplus.RefdataValue;com.k_int.kbplus.auth.Role;com.k_int.kbplus.auth.UserOrg" %>
<% def contextService = grailsApplication.mainContext.getBean("contextService") %>
<% def securityService = grailsApplication.mainContext.getBean("springSecurityService") %>

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

<h1 class="ui header"><semui:headerIcon />${message(code: 'menu.user.errorReport')}</h1>

<div class="ui message">
    <div class="header">Informieren Sie uns, wenn Sie einen Fehler entdeckt haben.</div>

    <p>
        <br />
        Formulieren Sie bitte kurz ..
    </p>
    <ul class="ui list">
        <li>das beobachtete Verhalten</li>
        <li>das erwartete Verhalten</li>
        <li>den Kontext des Fehlers</li>
    </ul>
    <p>Vielen Dank!</p>
</div>

<semui:form>
    <form>
        <div class="ui form">

            <div class="field">
                <label>Beobachtetes Verhalten</label>
                <textarea name="info1"></textarea>
            </div>
            <div class="field">
                <label>Erwartetes Verhalten</label>
                <textarea name="info2"></textarea>
            </div>
            <div class="field">
                <label>Kontext, z.B. die betroffene URL in der Adressleiste des Browses</label>
                <textarea name="info3"></textarea>
            </div>

            <div class="three fields">
                <div class="field">
                    <label>Betrifft</label>
                    <input type="text" readonly="readonly" value="${contextService.getUser()}">
                </div>
                <div class="field">
                    <label>&nbsp;</label>
                    <input type="text" readonly="readonly" value="${contextService.getOrg()}">
                </div>
                <div class="field">
                    <label>&nbsp;</label>
                    <input type="text" readonly="readonly" name="meta" value="${contextService.getUser()?.id}:${contextService.getOrg()?.id}">
                </div>
            </div>

            <div class="field">
                <input type="submit" class="ui button" disabled="disabled" value="Fehlerbericht absenden">
            </div>

        </div>
    </form>
</semui:form>

</body>
</html>
