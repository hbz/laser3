<%@ page import="de.laser.CustomerTypeService" %>
<laser:serviceInjection />

%{-- menu: public --}%

<div class="ui dropdown item" role="menuitem" aria-haspopup="true">
    <a class="title">
        ${message(code:'menu.public')} <i class="dropdown icon"></i>
    </a>
    <div class="menu" role="menu">
        <ui:link generateElementId="true" class="item" role="menuitem" controller="package" action="index">${message(code:'menu.public.all_pkg')}</ui:link>
        <ui:link generateElementId="true" class="item" role="menuitem" controller="title" action="index">${message(code:'menu.public.all_titles')}</ui:link>

        <sec:ifAnyGranted roles="ROLE_ADMIN">
            <ui:link generateElementId="true" role="menuitem" controller="organisation" action="index">${message(code:'menu.public.all_orgs')}</ui:link>
        </sec:ifAnyGranted>

        <g:if test="${accessService.ctxPermAffiliation(CustomerTypeService.ORG_CONSORTIUM_BASIC, 'INST_USER')}">
            <ui:link generateElementId="true" role="menuitem" controller="organisation" action="listInstitution">${message(code:'menu.public.all_insts')}</ui:link>
        </g:if>
        <g:elseif test="${accessService.ctxPermAffiliation(CustomerTypeService.ORG_INST_BASIC, 'INST_USER')}">
            <ui:link generateElementId="true" role="menuitem" controller="organisation" action="listConsortia">${message(code:'menu.public.all_cons')}</ui:link>
        </g:elseif>

        <ui:link generateElementId="true" class="item" role="menuitem" controller="organisation" action="listProvider">${message(code:'menu.public.all_providers')}</ui:link>
        <ui:link generateElementId="true" class="item" role="menuitem" controller="platform" action="list">${message(code:'menu.public.all_platforms')}</ui:link>

        <div class="divider"></div>
        <ui:link generateElementId="true" target="_blank" onclick="JSPC.app.workaround_targetBlank(event)" class="item" role="menuitem" controller="gasco">${message(code:'menu.public.gasco_monitor')}</ui:link>
        <a id="wekb" href="${message(code:'url.wekb.' + currentServer)}" target="_blank" onclick="JSPC.app.workaround_targetBlank(event)" class="item" role="menuitem"><i class="ui icon la-gokb"></i> we:kb</a>
    </div>
</div>
