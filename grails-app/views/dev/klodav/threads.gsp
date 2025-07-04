<%@ page import="de.laser.utils.PasswordUtils; de.laser.utils.RandomUtils; org.apache.commons.codec.binary.StringUtils; de.laser.Org; de.laser.ui.Btn; de.laser.utils.DateUtils; de.laser.Subscription; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.storage.RDStore; de.laser.auth.*; grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:htmlStart text="Playground: Threads" />

<ui:breadcrumbs>
    <ui:crumb message="menu.devDocs" controller="dev" action="index"/>
    <ui:crumb text="Playground" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon text="Playground" type="dev"/>

<g:render template="klodav/nav" />

<div class="ui fluid card">
    <div class="content">
        ExecutorService: ${executorService}
        <br/>
        <br/>

        <g:each in="${Thread.getAllStackTraces().keySet().findAll{it.getName().startsWith('klodav')}.sort{it.getName()}}" var="thread">
            ${thread} > ${thread.getState()} > ${thread.isAlive()} <br />
        </g:each>

        <pre>
FutureTask.state =
    [Completed normally]
    [Completed exceptionally: ..]
    [Cancelled]
    [Not completed] or [Not completed, task = ..]
        </pre>

        <g:if test="${tasks}">
            <g:each in="${tasks}" var="task">
                <g:if test="${task instanceof java.util.concurrent.FutureTask}">
                    ${task.getProperties()} %{-->>> ${task.state()--}% >>> ${task.toString().substring(task.toString().indexOf('['))} <br />
                </g:if>
                <g:if test="${task instanceof java.lang.Runnable}">
                    ${task} <br />
                </g:if>
            </g:each>
        </g:if>
        <g:else>
            NO TASKS FOUND
        </g:else>
    </div>
</div>


<laser:htmlEnd />