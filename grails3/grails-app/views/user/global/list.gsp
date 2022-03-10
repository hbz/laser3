<%@ page import="com.k_int.kbplus.*; de.laser.*; de.laser.auth.*" %>
<laser:serviceInjection />
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'menu.institutions.users')}</title>
</head>
<body>
        <g:if test="${controllerName == 'myInstitution'}">
        %{-- myInstitution has no breadcrumb yet --}%
            <g:render template="/organisation/breadcrumb" model="${[ inContextOrg: inContextOrg, orgInstance: orgInstance, institutionalView: institutionalView, params:params ]}"/>
        </g:if>
        <g:if test="${controllerName == 'organisation'}">
            <g:render template="/organisation/breadcrumb" model="${[ inContextOrg: inContextOrg, orgInstance: orgInstance, institutionalView: institutionalView, params:params ]}"/>
        </g:if>
        <g:if test="${controllerName == 'user'}">
            <g:render template="/user/breadcrumb" model="${[ inContextOrg: inContextOrg, orgInstance: orgInstance, institutionalView: institutionalView, params:params ]}"/>
        </g:if>

        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />
            ${titleMessage} <semui:totalNumber total="${total}"/>
        </h1>

        <semui:controlButtons>
            <g:render template="/user/global/actions" />
        </semui:controlButtons>

        <g:if test="${controllerName == 'myInstitution'}">
            <g:render template="/organisation/nav" model="${navConfig}"/>
        </g:if>
        <g:if test="${controllerName == 'organisation'}">
            <g:render template="/organisation/nav" model="${navConfig}"/>
        </g:if>

        <g:render template="/templates/user/filter" model="${filterConfig}"/>

        <g:if test="${multipleAffiliationsWarning}">
            <div class="ui info message la-clear-before">${message(code:'user.edit.info')}</div>
        </g:if>

        <semui:messages data="${flash}" />

        <g:render template="/templates/user/list" model="${tmplConfig}" />

</body>
</html>
