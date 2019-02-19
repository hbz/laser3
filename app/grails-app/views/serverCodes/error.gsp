<%@ page import="org.codehaus.groovy.grails.web.errors.ExceptionUtils" %>
<laser:serviceInjection />
<%
    Throwable exception = (Throwable) exception
    def root = ExceptionUtils.getRootCause(exception)
    def causedby
    if (root != null && root != exception && root.message != exception.message) {
        causedby = "Cause: " + root.message
    }

    String nl = "%0D%0A"

    String mailString   = "mailto:laser_support@hbz-nrw.de?subject=Fehlerbericht - ${grailsApplication.config.laserSystemId}" +
                        "&body=Ihre Fehlerbeschreibung (bitte angeben): " + nl + nl +
                        "URI: ${request.forwardURI} " + nl +
                        "Zeitpunkt: ${new Date()} " + nl +
                        "System: ${grailsApplication.config?.laserSystemId} " + nl +
                        "Branch: ${grailsApplication.metadata['repository.branch']} " + nl +
                        "Commit: ${grailsApplication.metadata['repository.revision.number']} " + nl +
                        "Class: ${root?.getClass()?.name ?: exception.getClass().name} " + nl +
                        "Message: ${exception.message} " + nl +
                        "${causedby}" + nl
%>
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
                    <a href="mailto:laser_support@hbz-nrw.de?${mailString}">
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

<g:if test="${params.debug}">
    <div class="ui segment">
        <h4 class="ui red header">
            <i class="bug icon"></i>
            DEBUG-INFORMATION
        </h4>
        <div class="content">${exception.printStackTrace(new java.io.PrintWriter(out))}</div>
    </div>
</g:if>

<g:if test="${grailsApplication.config.getCurrentServer() == contextService.SERVER_DEV}">
    <g:renderException exception="${exception}"/>
</g:if>
<g:elseif env="development">
    <g:renderException exception="${exception}"/>
</g:elseif>

</body>
</html>