<%@ page import="de.laser.utils.PasswordUtils; de.laser.utils.RandomUtils; org.apache.commons.codec.binary.StringUtils; de.laser.Org; de.laser.ui.Btn; de.laser.utils.DateUtils; de.laser.Subscription; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.storage.RDStore; de.laser.auth.*; grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:htmlStart text="Backend: Various" />

<ui:breadcrumbs>
    <ui:crumb message="menu.devDocs" controller="dev" action="index"/>
    <ui:crumb text="Backend" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon text="Backend" type="dev"/>

<g:render template="backend/nav" />

<ui:msg class="info" showIcon="true">
    NOCH IN ARBEIT
</ui:msg>

<div class="ui fluid card">
    <div class="content">
        <div class="header"> <i class="icon history"></i> SystemEvent </div>
    </div>
    <div class="content">
        <table class="ui compact table">
            <thead>
                <tr>
                    <td class="two wide"></td>
                    <td class="three wide"></td>
                    <td class="nine wide"></td>
                    <td class="two wide"></td>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>GDC_INFO</td>
                    <td>GenericDataCleansing</td>
                    <td>
                        <g:link controller="admin" action="systemEvents" target="_blank">SystemEvent</g:link>
                        zur Protokollierung von manuellen Datenkorrekturen
                    </td>
                    <td><i class="${Icon.SYM.YES} green"></i></td>
                </tr>
            </tbody>
        </table>
    </div>
</div>

<laser:htmlEnd />
