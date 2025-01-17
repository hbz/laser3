<%@ page import="de.laser.*; de.laser.auth.*" %>
<laser:htmlStart message="menu.institutions.users" />
    <ui:debugInfo>
        <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
    </ui:debugInfo>
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

        <ui:h1HeaderWithIcon text="${titleMessage}" total="${total}" type="${controllerName == 'user' ? 'admin' : orgInstance.getCustomerType()}">
            <laser:render template="/templates/iconObjectIsMine" model="${[isMyOrg: isMyOrg]}"/>
        </ui:h1HeaderWithIcon>

        <ui:controlButtons>
            <laser:render template="/user/global/actions" />
        </ui:controlButtons>

        <g:if test="${controllerName == 'myInstitution'}">
            <laser:render template="/organisation/${customerTypeService.getNavTemplatePath()}" model="${navConfig}"/>
        </g:if>
        <g:if test="${controllerName == 'organisation'}">
            <laser:render template="/organisation/${customerTypeService.getNavTemplatePath()}" model="${navConfig}"/>
        </g:if>

        <laser:render template="/templates/user/filter" model="${filterConfig}"/>

        <g:if test="${multipleAffiliationsWarning}">
            <ui:msg class="info" hideClose="true" message="user.edit.info" />
        </g:if>

        <ui:messages data="${flash}" />

        <laser:render template="/templates/user/list" model="${tmplConfig}" />

<laser:htmlEnd />
