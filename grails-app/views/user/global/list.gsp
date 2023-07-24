<%@ page import="de.laser.*; de.laser.auth.*" %>
<laser:htmlStart message="menu.institutions.users" serviceInjection="true"/>

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

        <ui:h1HeaderWithIcon text="${titleMessage}" total="${total}" type="${controllerName == 'user' ? 'user' : ''}">
            <g:if test="${isMyOrg}">
                <laser:render template="/templates/iconObjectIsMine"/>
            </g:if>
        </ui:h1HeaderWithIcon>

        <ui:controlButtons>
            <laser:render template="/user/global/actions" />
        </ui:controlButtons>

        <g:if test="${controllerName == 'myInstitution'}">
            <laser:render template="/organisation/nav" model="${navConfig}"/>
        </g:if>
        <g:if test="${controllerName == 'organisation'}">
            <laser:render template="/organisation/nav" model="${navConfig}"/>
        </g:if>

        <laser:render template="/templates/user/filter" model="${filterConfig}"/>

        <g:if test="${multipleAffiliationsWarning}">
            <ui:msg class="info" noClose="true" message="user.edit.info" />
        </g:if>

        <ui:messages data="${flash}" />

        <laser:render template="/templates/user/list" model="${tmplConfig}" />

<laser:htmlEnd />
