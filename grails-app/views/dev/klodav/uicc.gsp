<%@ page import="de.laser.UserSetting; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.storage.RDStore; de.laser.auth.*; grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:htmlStart text="Playground: New UI Core Components" />

<ui:breadcrumbs>
    <ui:crumb message="menu.devDocs" controller="dev" action="index"/>
    <ui:crumb text="Playground" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon text="Playground" type="dev"/>

<g:render template="klodav/nav" />

<div class="ui fluid card">
    <div class="content">
        <div class="header">cc:toggle -> XHR-Toggle for RDConstants.Y_N</div>
    </div>
    <div class="content">
        <div class="ui form">
            <div class="field">
                <g:set var="US_DASHBOARD_SHOW_TOPMENU" value="${contextService.getUser().getSetting(UserSetting.KEYS.DASHBOARD_SHOW_TOPMENU, RDStore.YN_YES)}" />

                <label>${message(code: 'profile.dashboardShowTopmenu')}</label>
                <cc:toggle owner="${US_DASHBOARD_SHOW_TOPMENU}" field="rdValue" overwriteEditable="true" />
                <pre>&#60;cc:toggle owner="&#36;{US_DASHBOARD_SHOW_TOPMENU}" field="rdValue" overwriteEditable="true" /&#62;</pre>

                <g:set var="US_DASHBOARD_SHOW_WEKBNEWS" value="${contextService.getUser().getSetting(UserSetting.KEYS.DASHBOARD_SHOW_WEKBNEWS, RDStore.YN_YES)}" />

                <label>.. With Label</label>
                <cc:toggle owner="${US_DASHBOARD_SHOW_WEKBNEWS}" field="rdValue" label="${message(code: 'profile.dashboardShowWekbNews')}" overwriteEditable="true" />
                <pre>&#60;cc:toggle owner="&#36;{US_DASHBOARD_SHOW_WEKBNEWS}" field="rdValue" label="&#36;{message(code:'profile.dashboardShowWekbNews')}" overwriteEditable="true" /&#62;</pre>

                <label>.. Invalid Params</label>
                <cc:toggle owner="${null}" field="xyz" />
                <pre>&#60;cc:toggle owner="&#36;{null}" field="xyz" /&#62;</pre>

                <label>.. Editable/OverwriteEditable = false</label>
                <cc:toggle owner="${US_DASHBOARD_SHOW_TOPMENU}" field="rdValue" overwriteEditable="false" />
                <pre>&#60;cc:toggle owner="&#36;{US_DASHBOARD_SHOW_TOPMENU}" field="rdValue" overwriteEditable="false" /&#62;</pre>
            </div>
        </div>
    </div>
</div>

<laser:htmlEnd />
