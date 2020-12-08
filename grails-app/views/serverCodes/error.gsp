<%@ page import="de.laser.helper.ServerUtils" %>
<laser:serviceInjection />
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} - ${message(code: 'serverCode.error.message1')}</title>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'errors.css')}" type="text/css">
</head>

<body>

<br />

<div class="ui segment piled">
    <div class="content">
        <div>
            <span class="ui orange label huge">${status}</span>
        </div>

        <h2 class="ui header">
            ${message(code: 'serverCode.error.message1')}
        </h2>

        <g:if test="${! flash.error}">
            <div>
                <p>${message(code: 'serverCode.error.message2')}</p>
                <p><strong>${request.forwardURI}</strong></p>

                <g:if test="${exception}">
                    <p>${exception.message}</p>
                    <br />
                    <p>
                        <a href="mailto:laser@hbz-nrw.de?${mailString}">
                            <g:message code="serverCode.error.sendSupportMail"/>
                        </a>
                    </p>
                </g:if>

                <br />
                <p>
                    <button class="ui button" onclick="JSPC.helper.goBack()">${message(code: 'default.button.back')}</button>
                </p>
            </div>
        </g:if>
    </div>
</div>

<g:if test="${exception && params.debug}">
    <div class="ui segment">
        <h3 class="ui red header">
            <i class="bug icon"></i> DEBUG-INFORMATION
        </h3>
        <div class="content">
            ${exception.printStackTrace(new java.io.PrintWriter(out))}
        </div>
    </div>
</g:if>

<g:if test="${ServerUtils.getCurrentServer() == ServerUtils.SERVER_DEV}">
    <g:renderException exception="${exception}"/>
</g:if>
<g:elseif env="development">
    <g:renderException exception="${exception}"/>
</g:elseif>

</body>
</html>