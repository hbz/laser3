<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser')} : ${message(code: 'menu.yoda.systemSettings')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
    <semui:crumb message="menu.yoda.systemSettings" class="active"/>
</semui:breadcrumbs>

<div>
    <h2 class="ui header">${message(code: 'menu.yoda.systemSettings')}</h2>

    <table class="ui celled la-table table">
        <thead>
        <tr>
            <th>Setting</th>
            <th>Value</th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td>Mailversand</td>
            <td>
                <g:if test="${grailsApplication.config.grails.mail.disabled}">
                    <div class="ui red horizontal label"> Mailversand ist aktuell ausgeschaltet! </div>
                    <g:link controller="yoda" action="toggleMailSent" class="ui button positive right floated"
                            params="${[mailSent: true]}">
                        Mailversand einschalten
                    </g:link>
                </g:if><g:else>
                <div class="ui green horizontal label"> Mailversand ist aktuell eingeschaltet! </div>
                    <g:link controller="yoda" action="toggleMailSent" class="ui button negative right floated"
                            params="${[mailSent: false]}">
                        Mailversand ausschalten
                    </g:link>
                </g:else>
            </td>
        </tr>
        <g:each in="${settings}" var="s">
            <tr>
                <td>${s.name}</td>
                <td>
                    <g:if test="${s.tp == 1}">
                        <g:link controller="yoda" action="toggleBoolSetting"
                                params="${[setting: s.name]}">${s.value}</g:link>
                    </g:if>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>
</div>
</body>
</html>
