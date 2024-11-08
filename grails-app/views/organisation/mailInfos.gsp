<g:if test="${institutionalView}">
    <g:set var="entityName" value="${message(code: 'org.institution.label')}"/>
</g:if>
<g:else>
    <g:set var="entityName" value="${message(code: 'org.label')}"/>
</g:else>

<laser:htmlStart message="mail.org.mailInfos" serviceInjection="true"/>

<laser:render template="breadcrumb"
              model="${[orgInstance: orgInstance, inContextOrg: inContextOrg, institutionalView: institutionalView, consortialView: consortialView]}"/>

<ui:h1HeaderWithIcon text="${orgInstance.name}">
    <laser:render template="/templates/iconObjectIsMine" model="${[isMyOrg: isMyOrg]}"/>
</ui:h1HeaderWithIcon>

<ui:objectStatus object="${orgInstance}" status="${orgInstance.status}"/>

<ui:messages data="${flash}"/>

<div class="ui stackable grid">
    <div class="sixteen wide column">
        <g:render template="/templates/info/org"/>
    </div>
</div>

<laser:htmlEnd/>