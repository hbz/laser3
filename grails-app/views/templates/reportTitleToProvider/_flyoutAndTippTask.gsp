<%@ page import="de.laser.ui.Icon; de.laser.addressbook.Person; de.laser.addressbook.Contact; de.laser.CustomerTypeService; de.laser.RefdataCategory; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.storage.BeanStore; de.laser.Task; grails.plugin.springsecurity.SpringSecurityUtils;" %>
<laser:serviceInjection />

<g:if test="${tipp.platform?.provider}">

    <g:if test="${contextService.getOrg().isCustomerType_Basic() && contextService.isInstEditor()}">

        <ui:msg class="info" showIcon="true">
            ${message(code:'tipp.reportTitleToProvider.info1')}
            <a href="#" class="infoFlyout-trigger" data-template="reportTitleToProvider" data-tipp="${tipp.id}">${message(code:'tipp.reportTitleToProvider.mailto')}</a>
            <br />
            ${message(code:'tipp.reportTitleToProvider.proHint')}
        </ui:msg>

    </g:if>%{-- BASIC --}%
    <g:else>
        <g:set var="currentTasks" value="${taskService.getTasks(BeanStore.getContextService().getUser(), tipp)}" />

        <g:if test="${contextService.getOrg().isCustomerType_Pro() && (contextService.isInstEditor() || currentTasks.cmbTaskInstanceList)}">

            <ui:msg class="info" showIcon="true">
                <g:if test="${contextService.isInstEditor()}">
                    ${message(code:'tipp.reportTitleToProvider.info1')}
                    <br />
                    <a href="#" class="infoFlyout-trigger" data-template="reportTitleToProvider" data-tipp="${tipp.id}">${message(code:'tipp.reportTitleToProvider.mailto')}</a>
                    und <a href="#modalCreateRttpTask" data-ui="modal">erstellen Sie sich ggf. eine Aufgabe</a> zur Erinnerung.
                    <br />
                    <br />
                </g:if>

                <g:if test="${currentTasks.cmbTaskInstanceList}">
                        ${message(code:'tipp.reportTitleToProvider.info2')} <br />

                        <g:each in="${currentTasks.cmbTaskInstanceList.sort{ it.dateCreated }.reverse()}" var="tt">
                            <g:if test="${tt in currentTasks.myTaskInstanceList}">
                                <i class="${Icon.SIG.MY_OBJECT}"></i>
                                <g:formatDate format="${message(code:'default.date.format.notime')}" date="${tt.dateCreated}"/> -
                                <a href="#" onclick="JSPC.app.editTask(${tt.id});">${tt.title}</a>
                            </g:if>
                            <g:elseif test="${taskService.hasWRITE()}">
                                <i class="${Icon.TASK}"></i>
                                <g:formatDate format="${message(code:'default.date.format.notime')}" date="${tt.dateCreated}"/> -
                                <a href="#" onclick="JSPC.app.editTask(${tt.id});">${tt.title}</a>
                            </g:elseif>
                            <g:else>
                                <i class="${Icon.CMD.READ}"></i>
                                <g:formatDate format="${message(code:'default.date.format.notime')}" date="${tt.dateCreated}"/> -
                                <a href="#" onclick="JSPC.app.readTask(${tt.id});">${tt.title}</a>
                            </g:else>
                            <br />
                        </g:each>
                </g:if>
            </ui:msg>

            <g:render template="/templates/reportTitleToProvider/js" /> %{-- readTask/writeTask --}%
            <g:render template="/templates/reportTitleToProvider/modal" model="${[tipp: tipp]}"/> %{-- createTask --}%

        </g:if>

    </g:else>%{-- PRO --}%

</g:if>

<g:if test="${contextService.isInstEditor()}">
    <laser:render template="/info/flyoutWrapper"/>
</g:if>
