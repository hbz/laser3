<%@ page import="de.laser.utils.PasswordUtils; de.laser.utils.RandomUtils; org.apache.commons.codec.binary.StringUtils; de.laser.Org; de.laser.ui.Btn; de.laser.utils.DateUtils; de.laser.Subscription; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.storage.RDStore; de.laser.auth.*; grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:htmlStart text="Playground: Test" />

<ui:breadcrumbs>
    <ui:crumb message="menu.devDocs" controller="dev" action="index"/>
    <ui:crumb text="Playground" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon text="Playground" type="dev"/>

<g:render template="klodav/nav" />

    <div id="webk-content">
        <iframe src="https://wekb-dev.hbz-nrw.de"></iframe>
    </div>

<style>
    #webk-content {
        width: 100%;
        max-width: 100%;
        padding-top: 56.25%;
        position: relative;
    }
    #webk-content iframe {
        width: 100%;
        height: 100%;
        top: 0;
        left: 0;
        position: absolute;
        border: none;
    }
</style>

<laser:htmlEnd />