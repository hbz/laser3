<%@ page import="de.laser.utils.SwissKnife; de.laser.ui.Icon; de.laser.addressbook.Person; de.laser.addressbook.Contact; de.laser.CustomerTypeService; de.laser.RefdataCategory; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.storage.BeanStore; de.laser.Task; grails.plugin.springsecurity.SpringSecurityUtils;" %>
<laser:serviceInjection />

<g:if test="${contextService.isInstEditor()}">
    <g:if test="${contextService.getOrg().isCustomerType_Pro()}">

        <g:render template="/templates/reportTitleToProvider/modal" />

    </g:if>%{-- PRO --}%

    <style>
        .ui.form .info.message { display: flex; }
    </style>

    <laser:render template="/info/flyoutWrapper"/>

</g:if>%{-- INST_EDITOR --}%
