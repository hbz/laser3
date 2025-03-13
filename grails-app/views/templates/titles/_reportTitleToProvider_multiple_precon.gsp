<%@ page import="org.apache.commons.lang3.RandomStringUtils; de.laser.ui.Icon; de.laser.addressbook.Person; de.laser.addressbook.Contact; de.laser.CustomerTypeService; de.laser.RefdataCategory; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.storage.BeanStore; de.laser.Task; grails.plugin.springsecurity.SpringSecurityUtils;" %>
<laser:serviceInjection />

<laser:render template="/info/flyoutWrapper"/>

<style>
.ui.form .info.message { display: flex; }
</style>

<g:if test="${contextService.getOrg().isCustomerType_Pro()}">

    <laser:script file="${this.getGroovyPageFileName()}">
        JSPC.app.editTask = function (id) {
            var func = bb8.ajax4SimpleModalFunction("#modalEditTask", "<g:createLink controller="ajaxHtml" action="editTask"/>?id=" + id);
            func();
        };
    </laser:script>

</g:if>%{-- PRO --}%
