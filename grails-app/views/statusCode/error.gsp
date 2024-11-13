<%@ page import="de.laser.utils.AppUtils" %>
<laser:htmlStart message="statusCode.error.message1">
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'fixed/onlyErrors.css')}" type="text/css">
</laser:htmlStart>

    <br />

    <laser:serverCodeMessage status="${status}"
                             header="${message(code: 'statusCode.error.message1')}"
                             subheader="${message(code: 'statusCode.error.message2')}">

        <p>
            <strong>${request.forwardURI}</strong>
        </p>

        <g:if test="${exception}">
            <p>
                ${exception.message}
            </p>
            <br />
            <p>
                <a href="mailto:laser@hbz-nrw.de?${mailString}">
                    <g:message code="statusCode.error.sendSupportMail"/>
                </a>
            </p>
        </g:if>

    </laser:serverCodeMessage>


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