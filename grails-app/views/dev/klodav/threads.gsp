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

        <g:if test="${tasks}">
            <g:each in="${tasks}" var="task">
                ${task.getProperties()} >>> ${task.state()} >>> ${task}<br />
            </g:each>
        </g:if>
        <g:else>
            NO TASKS FOUND
        </g:else>
    </div>
</div>


<laser:htmlEnd />