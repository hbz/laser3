<%@ page import="de.laser.*; de.laser.auth.*" %>
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
            <laser:render template="/organisation/breadcrumb" model="${[ inContextOrg: inContextOrg, orgInstance: orgInstance, institutionalView: institutionalView, params:params ]}"/>
        </g:if>
        <g:if test="${controllerName == 'organisation'}">
            <laser:render template="/organisation/breadcrumb" model="${[ inContextOrg: inContextOrg, orgInstance: orgInstance, institutionalView: institutionalView, params:params ]}"/>
        </g:if>
        <g:if test="${controllerName == 'user'}">
            <laser:render template="/user/breadcrumb" model="${[ inContextOrg: inContextOrg, orgInstance: orgInstance, institutionalView: institutionalView, params:params ]}"/>
        </g:if>

        <semui:headerWithIcon text="${titleMessage}">
            <semui:totalNumber total="${total}"/>
        </semui:headerWithIcon>

        <semui:controlButtons>
            <laser:render template="/user/global/actions" />
        </semui:controlButtons>

        <g:if test="${controllerName == 'myInstitution'}">
            <laser:render template="/organisation/nav" model="${navConfig}"/>
        </g:if>
        <g:if test="${controllerName == 'organisation'}">
            <laser:render template="/organisation/nav" model="${navConfig}"/>
        </g:if>

        <laser:render template="/templates/user/filter" model="${filterConfig}"/>

        <g:if test="${multipleAffiliationsWarning}">
            <div class="ui info message la-clear-before">${message(code:'user.edit.info')}</div>
        </g:if>

        <semui:messages data="${flash}" />

        <laser:render template="/templates/user/list" model="${tmplConfig}" />

</body>
</html>
