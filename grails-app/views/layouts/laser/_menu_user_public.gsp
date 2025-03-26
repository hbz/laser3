<%@ page import="de.laser.ui.Icon; de.laser.CustomerTypeService" %>
<laser:serviceInjection />

%{-- menu: public --}%

<div class="ui dropdown item" role="menuitem" aria-haspopup="true">
    <a class="title">
        ${message(code:'menu.public')} <i class="dropdown icon"></i>
    </a>
    <div class="menu" role="menu">
        <sec:ifAnyGranted roles="ROLE_ADMIN">
            <ui:link addItemAttributes="true" controller="organisation" action="index">${message(code:'menu.public.all_orgs')}</ui:link>
        </sec:ifAnyGranted>

        <g:if test="${contextService.isInstUser(CustomerTypeService.ORG_CONSORTIUM_BASIC)}">
            <ui:link addItemAttributes="true" controller="organisation" action="listInstitution">${message(code:'menu.public.all_insts')}</ui:link>
        </g:if>
        <g:elseif test="${contextService.isInstUser(CustomerTypeService.ORG_INST_BASIC)}">
            <ui:link addItemAttributes="true" controller="organisation" action="listConsortia">${message(code:'menu.public.all_cons')}</ui:link>
        </g:elseif>

        <ui:link addItemAttributes="true" controller="provider" action="list">${message(code:'menu.public.all_providers')}</ui:link>
        <ui:link addItemAttributes="true" controller="vendor" action="list">${message(code:'menu.public.all_vendors')}</ui:link>
        <ui:link addItemAttributes="true" controller="platform" action="list">${message(code:'menu.public.all_platforms')}</ui:link>

        <ui:link addItemAttributes="true" controller="package" action="index">${message(code:'menu.public.all_pkg')}</ui:link>
        <ui:link addItemAttributes="true" controller="title" action="index">${message(code:'menu.public.all_titles')}</ui:link>

        <div class="divider"></div>
        <ui:link addItemAttributes="true" target="_blank" onclick="JSPC.app.workaround_targetBlank(event)" controller="gasco"><i class="${Icon.GASCO}"></i> ${message(code:'menu.public.gasco_monitor')}</ui:link>
        <a href="${message(code:'url.wekb.' + currentServer)}" id="wekb" class="item" role="menuitem" target="_blank" onclick="JSPC.app.workaround_targetBlank(event)"><i class="${Icon.WEKB}"></i> we:kb</a>
    </div>
</div>
