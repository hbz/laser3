<%@ page import="com.k_int.kbplus.Task" %>
<semui:modal id="modalCreateTask" message="task.create.new">

    <g:form class="ui form" id="create_task" url="[controller:'task',action:'create']" method="post">
        <div class="field fieldcontain ${hasErrors(bean: taskInstance, field: 'title', 'error')} required">
            <label for="title">
                <g:message code="task.title.label" default="Title" />
            </label>
            <g:textField name="title" required="" value="${taskInstance?.title}"/>
        </div>

        <div class="field fieldcontain ${hasErrors(bean: taskInstance, field: 'description', 'error')}">
            <label for="description">
                <g:message code="task.description.label" default="Description" />
            </label>
            <g:textField name="description" value="${taskInstance?.description}"/>
        </div>

        <g:if test="${owntp == 'license' || enableMyInstFormFields}">
            <div class="field fieldcontain ${hasErrors(bean: taskInstance, field: 'license', 'error')} ">
                <label for="license">
                    <g:message code="task.license.label" default="License" />
                </label>
                <g:select id="license" name="license.id" from="${validLicenses}" optionKey="id" value="${ownobj?.id}" class="many-to-one" noSelection="['null': '']"/>
            </div>
        </g:if>

        <g:if test="${owntp == 'org'}">
            <div class="field fieldcontain ${hasErrors(bean: taskInstance, field: 'org', 'error')} ">
                <label for="org">
                    <g:message code="task.org.label" default="Org" />
                </label>
                <g:select id="org" name="org.id" from="${validOrgs}" optionKey="id" value="${ownobj?.id}" class="many-to-one" noSelection="['null': '']"/>
            </div>
        </g:if>

        <g:if test="${owntp == 'pkg' || enableMyInstFormFields}">
            <div class="field fieldcontain ${hasErrors(bean: taskInstance, field: 'pkg', 'error')} ">
                <label for="pkg">
                    <g:message code="task.pkg.label" default="Pkg" />
                </label>
                <g:select id="pkg" name="pkg.id" from="${validPackages}" optionKey="id" value="${ownobj?.id}" class="many-to-one" noSelection="['null': '']"/>
            </div>
        </g:if>

        <g:if test="${owntp == 'subscription' || enableMyInstFormFields}">
            <div class="field fieldcontain ${hasErrors(bean: taskInstance, field: 'subscription', 'error')} ">
                <label for="subscription">
                    <g:message code="task.subscription.label" default="Subscription" />
                </label>
                <g:select id="subscription" name="subscription.id" from="${validSubscriptions}" optionKey="id" value="${ownobj?.id}" class="many-to-one" noSelection="['null': '']"/>
            </div>
        </g:if>

        <div class="field">
            <div class="two fields">

                <div class="field wide eight fieldcontain ${hasErrors(bean: taskInstance, field: 'status', 'error')} required">
                    <label for="status">
                        <g:message code="task.status.label" default="Status" />
                    </label>
                    <g:select id="status" name="status.id" from="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues('Task Status')}" optionKey="id" required="" value="${taskInstance?.status?.id}" class="many-to-one"/>
                </div>

                <semui:datepicker class="wide eight" label="task.endDate.label" name="endDate" placeholder="default.date.label" value="${taskInstance?.endDate}" required="true" bean="${taskInstance}" />

            </div>
        </div>

        <div class="field">
            <div class="two fields">
                <div class="field wide eight fieldcontain ${hasErrors(bean: taskInstance, field: 'responsibleOrg', 'error')}">
                    <label for="responsibleOrg">
                        <g:message code="task.responsibleOrg.label" default="Responsible Org" />
                    </label>
                    <g:select id="responsibleOrg" name="responsibleOrg.id" from="${validResponsibleOrgs}" optionKey="id" value="${taskInstance?.responsibleOrg?.id}" class="many-to-one" noSelection="['null': '']"/>
                </div>
                <div class="field wide eight fieldcontain ${hasErrors(bean: taskInstance, field: 'responsibleUser', 'error')}">
                    <label for="responsibleUser">
                        <g:message code="task.responsibleUser.label" default="Responsible User" />
                    </label>
                    <g:select id="responsibleUser" name="responsibleUser.id" from="${validResponsibleUsers}" optionKey="id" optionValue="display" value="${taskInstance?.responsibleUser?.id}" class="many-to-one" noSelection="['null': '']"/>
                </div>
            </div>
        </div>

        <div class="field">
            <div class="two fields">

                <div class="field fieldcontain ${hasErrors(bean: taskInstance, field: 'creator', 'error')} required">
                    <label for="creator">
                        <g:message code="task.creator.label" default="Creator" />
                    </label>
                    <g:select id="creator" name="creator.id" from="${taskCreator}" optionKey="id" optionValue="display" required="" value="${taskInstance?.creator?.id}" class="many-to-one"/>
                </div>

                <semui:datepicker label="task.createDate.label" name="createDate" placeholder="default.date.label" value="${taskInstance?.createDate}" required="true" bean="${taskInstance}"  />

            </div>
        </div>

    </g:form>
</semui:modal>
