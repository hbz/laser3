<%@ page import="de.laser.utils.AppUtils" %>

<laser:htmlStart message="serverCode.error.message1" serviceInjection="true">
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'fixed/onlyErrors.css')}" type="text/css">
</laser:htmlStart>

<br />

<div class="ui segment piled">
    <div class="content">
        <div>
            <span class="ui orange label huge">${status}</span>
        </div>

        <h2 class="ui header">
            ${message(code: 'serverCode.error.message1')}
        </h2>

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

    </div>
</div>

<sec:ifLoggedIn>
    <g:if test="${exception}">

        <g:if test="${AppUtils.getCurrentServer() in [AppUtils.LOCAL, AppUtils.DEV]}">
            <g:renderException exception="${exception}"/>
        </g:if>
        <g:else>
            <sec:ifAnyGranted roles="ROLE_YODA">
               <h3 class="ui red center aligned header">
                   **// YODA //**
               </h3>
                <g:renderException exception="${exception}"/>
            </sec:ifAnyGranted>
        </g:else>

    </g:if>
</sec:ifLoggedIn>

<laser:htmlEnd />