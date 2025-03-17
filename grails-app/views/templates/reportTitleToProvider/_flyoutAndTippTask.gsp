<%@ page import="org.apache.commons.lang3.RandomStringUtils; de.laser.ui.Icon; de.laser.addressbook.Person; de.laser.addressbook.Contact; de.laser.CustomerTypeService; de.laser.RefdataCategory; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.storage.BeanStore; de.laser.Task; grails.plugin.springsecurity.SpringSecurityUtils;" %>
<laser:serviceInjection />

<g:if test="${contextService.isInstEditor()}">

    <g:if test="${tipp.platform?.provider}">
        <g:set var="currentTasks" value="${taskService.getTasks(BeanStore.getContextService().getUser(), tipp)}" />

        <g:if test="${contextService.getOrg().isCustomerType_Basic()}">

            <ui:msg class="info" showIcon="true">
                ${message(code:'tipp.reportTitleToProvider.info1')}
                <a href="#" class="infoFlyout-trigger" data-template="reportTitleToProvider" data-tipp="${tipp.id}">${message(code:'tipp.reportTitleToProvider.mailto')}</a>
                <br />
                ${message(code:'tipp.reportTitleToProvider.proHint')}
            </ui:msg>

        </g:if>%{-- BASIC --}%
        <g:elseif test="${contextService.getOrg().isCustomerType_Pro()}">

            <ui:msg class="info" showIcon="true">
                ${message(code:'tipp.reportTitleToProvider.info1')}
                <br />
                <a href="#" class="infoFlyout-trigger" data-template="reportTitleToProvider" data-tipp="${tipp.id}">${message(code:'tipp.reportTitleToProvider.mailto')}</a>
                und <a href="#modalCreateRttpTask" data-ui="modal">erstellen Sie sich ggf. eine Aufgabe</a> zur Erinnerung.
                <br />

                <g:if test="${currentTasks.cmbTaskInstanceList}">
                        <br />
                        ${message(code:'tipp.reportTitleToProvider.info2')} <br />

                        <g:each in="${currentTasks.myTaskInstanceList.sort{ it.dateCreated }.reverse()}" var="tt">
                            <i class="${Icon.TASK}"></i>
                            <g:formatDate format="${message(code:'default.date.format.notime')}" date="${tt.dateCreated}"/> -
                            <a href="#" onclick="JSPC.app.editTask(${tt.id});">${tt.title}</a> <br />
                        </g:each>
                </g:if>
            </ui:msg>

        </g:elseif>%{-- PRO --}%

    </g:if>

    <g:if test="${contextService.getOrg().isCustomerType_Pro()}">

        <laser:script file="${this.getGroovyPageFileName()}">
            JSPC.app.editTask = function (id) {
                var func = bb8.ajax4SimpleModalFunction("#modalEditTask", "<g:createLink controller="ajaxHtml" action="editTask"/>?id=" + id);
            func();
        };
        </laser:script>

        <g:render template="/templates/reportTitleToProvider/modal" model="${[tipp: tipp]}"/>

    </g:if>%{-- PRO --}%

    <laser:render template="/info/flyoutWrapper"/>
</g:if>