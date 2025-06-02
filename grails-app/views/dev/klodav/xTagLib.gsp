<%@ page import="de.laser.UserSetting; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.storage.RDStore; de.laser.auth.*; grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:htmlStart text="Playground: New Buttons" />

<ui:breadcrumbs>
    <ui:crumb message="menu.devDocs" controller="dev" action="index"/>
    <ui:crumb text="Core Components" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon text="Components" type="dev"/>

<g:render template="klodav/nav" />

<div class="ui fluid card">
    <div class="content">
        <div class="header">ui:xToggle -> XHR-Toggle for RDConstants.Y_N</div>
    </div>
    <div class="content">
        <div class="ui form">
            <div class="field">
                <g:set var="US_DASHBOARD_SHOW_TOPMENU" value="${contextService.getUser().getSetting(UserSetting.KEYS.DASHBOARD_SHOW_TOPMENU, RDStore.YN_YES)}" />

                <label>${message(code: 'profile.dashboardShowTopmenu')}</label>
                <ui:xToggle owner="${US_DASHBOARD_SHOW_TOPMENU}" field="rdValue" overwriteEditable="true" />
                <pre>&#60;ui:xToggle owner="&#36;{US_DASHBOARD_SHOW_TOPMENU}" field="rdValue" overwriteEditable="true" /&#62;</pre>

                <g:set var="US_DASHBOARD_SHOW_WEKBNEWS" value="${contextService.getUser().getSetting(UserSetting.KEYS.DASHBOARD_SHOW_WEKBNEWS, RDStore.YN_YES)}" />

                <label></label>
                <ui:xToggle owner="${US_DASHBOARD_SHOW_WEKBNEWS}" field="rdValue" label="${message(code: 'profile.dashboardShowWekbNews')}" overwriteEditable="true" />
                <pre>&#60;ui:xToggle owner="&#36;{US_DASHBOARD_SHOW_WEKBNEWS}" field="rdValue" label="&#36;{message(code:'profile.dashboardShowWekbNews')}" overwriteEditable="true" /&#62;</pre>
            </div>
        </div>
    </div>
</div>

<laser:htmlEnd />
