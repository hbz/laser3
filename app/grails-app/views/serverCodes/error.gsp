<%@ page import="org.codehaus.groovy.grails.web.errors.ExceptionUtils" %>
<laser:serviceInjection />

<% Throwable exception = (Throwable) exception %>
<% def root = ExceptionUtils.getRootCause(exception) %>
<% def causedby;
if (root != null && root != exception && root.message != exception.message) {
    causedby = "Cause: " + root.message
} %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} - ${message(code: 'serverCode.error.message2')}</title>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'errors.css')}" type="text/css">
</head>

<body>

<br />

<div class="ui segment">
    <div class="content">
        <h3 class="ui header">
            <i class="icon frown outline"></i>
            ${message(code: 'serverCode.error.message2')}
        </h3>

        <g:if test="${!flash.error}">
            <div>
                <h4 class="ui header">${message(code: 'serverCode.error.message')}</h4>
                <p>${request.forwardURI}</p>
                <p>${exception.message}</p>
                <br/>
                <p>
                    <a href="mailto:laser_support@hbz-nrw.de?subject=Server Error - ${grailsApplication.config.laserSystemId}&body=Ihre detaillierte Beschreibung des Fehlers (Wie?, Wodurch?): %0D%0AZeitpunkt: ${new Date()} %0D%0ACommit:${grailsApplication.metadata['repository.revision.number']}%0D%0ABranch:${grailsApplication.metadata['repository.branch']}%0D%0AURI: ${request.forwardURI} %0D%0AClass: ${root?.getClass()?.name ?: exception.getClass().name} %0D%0AMessage: ${exception.message} %0D%0A${causedby}">
                        E-Mail an Support verschicken
                    </a>
                </p>
                <br/>
                <p>
                    <button class="ui button" onclick="javascript:window.history.back()">${message(code: 'default.button.back')}</button>
                </p>
            </div>
        </g:if>
    </div>
</div>

<g:if test="${grailsApplication.config.getCurrentServer() == contextService.SERVER_DEV}">
    <g:renderException exception="${exception}"/>
</g:if>
<g:elseif env="development">
    <g:renderException exception="${exception}"/>
</g:elseif>

</body>
</html>