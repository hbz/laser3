<%@ page import="de.laser.ui.Icon; de.laser.addressbook.Person; de.laser.addressbook.Contact; de.laser.CustomerTypeService; de.laser.RefdataCategory; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.storage.BeanStore; de.laser.Task; grails.plugin.springsecurity.SpringSecurityUtils;" %>
<laser:serviceInjection />

<g:if test="${contextService.isInstEditor()}">

    <g:if test="${tipp.platform?.provider}">
        <g:set var="currentTasks" value="${taskService.getTasks(BeanStore.getContextService().getUser(), tipp)}" />

        <g:if test="${contextService.getOrg().isCustomerType_Basic()}">

            <ui:msg class="info" hideClose="true">
                ${message(code:'tipp.reportTitleToProvider.info1')}
                <br />
                <a href="#" class="infoFlyout-trigger" data-template="reportTitleToProvider" data-tipp="${tipp.id}">${message(code:'tipp.reportTitleToProvider.mailto')}</a>
                <br />
                ${message(code:'tipp.reportTitleToProvider.proHint')}
            </ui:msg>

        </g:if>
        <g:elseif test="${contextService.getOrg().isCustomerType_Pro()}">

            <ui:msg class="info" hideClose="true">
                ${message(code:'tipp.reportTitleToProvider.info1')}
                <br />
                <a href="#" class="infoFlyout-trigger" data-template="reportTitleToProvider" data-tipp="${tipp.id}">${message(code:'tipp.reportTitleToProvider.mailto')}</a>
%{--                und <a href="#modalRttpCreateTask" data-ui="modal" data-tipp="${tipp.id}">erstellen Sie sich ggf. eine Aufgabe</a> zur Erinnerung.--}%
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

        </g:elseif>

    </g:if>

</g:if>