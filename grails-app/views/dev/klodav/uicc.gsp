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
        <div class="header">cc:toggle</div>
        <div class="meta">XHR-Toggle for RDConstants.Y_N</div>
    </div>
    <div class="content">
        <div class="ui form">
            <div class="field">
                <g:set var="US_DASHBOARD_SHOW_TOPMENU" value="${contextService.getUser().getSetting(UserSetting.KEYS.DASHBOARD_SHOW_TOPMENU, RDStore.YN_YES)}" />

                <label>${message(code: 'profile.dashboard.showTopmenu')}</label>
                <cc:toggle owner="${US_DASHBOARD_SHOW_TOPMENU}" field="rdValue" overwriteEditable="true" />
                <pre>&#60;cc:toggle owner="&#36;{US_DASHBOARD_SHOW_TOPMENU}" field="rdValue" overwriteEditable="true" /&#62;</pre>

                <div class="ui divider"></div>
                <g:set var="US_DASHBOARD_SHOW_WEKBNEWS" value="${contextService.getUser().getSetting(UserSetting.KEYS.DASHBOARD_SHOW_WEKBNEWS, RDStore.YN_YES)}" />

                <label>${message(code: 'profile.dashboard.showWekbNews')}</label>
                <cc:toggle owner="${US_DASHBOARD_SHOW_WEKBNEWS}" field="rdValue" label="${message(code: 'profile.dashboard.showWekbNews')}" overwriteEditable="true" />
                <pre>&#60;cc:toggle owner="&#36;{US_DASHBOARD_SHOW_WEKBNEWS}" field="rdValue" label="&#36;{message(code:'profile.dashboard.showWekbNews')}" overwriteEditable="true" /&#62;</pre>

                <div class="ui divider"></div>

                <label>Example: (invalid usage)</label>
                <cc:toggle owner="${null}" field="xyz" />
                <pre>&#60;cc:toggle owner="&#36;{null}" field="xyz" /&#62;</pre>

                <div class="ui divider"></div>

                <label>${message(code: 'profile.dashboard.showTopmenu')}</label>
                <cc:toggle owner="${US_DASHBOARD_SHOW_TOPMENU}" field="rdValue" overwriteEditable="false" />
                <pre>&#60;cc:toggle owner="&#36;{US_DASHBOARD_SHOW_TOPMENU}" field="rdValue" overwriteEditable="false" /&#62;</pre>
            </div>
        </div>
    </div>
</div>

<div class="ui fluid card">
    <div class="content">
        <div class="header">cc:boogle</div>
        <div class="meta">XHR-Toggle for Boolean</div>
    </div>
    <div class="content">
        <div class="ui form">
            <div class="field">
                <label>${message(code:'org.isBetaTester.label')}</label>
                <cc:boogle owner="${contextService.getOrg()}" field="isBetaTester" overwriteEditable="false" />
                <pre>&#60;cc:boogle owner="&#36;{contextService.getOrg()}}" field="isBetaTester" overwriteEditable="false" /&#62;</pre>

                <div class="ui divider"></div>

                <label>Example: (invalid usage)</label>
                <cc:boogle owner="${contextService.getOrg()}" field="xyz" />
                <pre>&#60;cc:boogle owner="&#36;{contextService.getOrg()}" field="xyz" /&#62;</pre>
            </div>
        </div>
    </div>
</div>

<laser:htmlEnd />
