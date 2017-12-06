<%@ page import="com.k_int.kbplus.Task" %>
<semui:modal id="modalCreateTask" message="task.create.new">

    <g:form class="ui form" id="create_task" url="[controller:'task',action:'create']" method="post">
        <div class="field fieldcontain ${hasErrors(bean: taskInstance, field: 'title', 'error')} required">
            <label for="title">
                <g:message code="task.title.label" default="Title" />
                <span class="required-indicator">*</span>
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

        <div class="field fieldcontain ${hasErrors(bean: taskInstance, field: 'status', 'error')} required">
            <label for="status">
                <g:message code="task.status.label" default="Status" />
                <span class="required-indicator">*</span>
            </label>
            <g:select id="status" name="status.id" from="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues('Task Status')}" optionKey="id" required="" value="${taskInstance?.status?.id}" class="many-to-one"/>
        </div>

        <div class="field fieldcontain ${hasErrors(bean: taskInstance, field: 'creator', 'error')} required">
            <label for="creator">
                <g:message code="task.creator.label" default="Creator" />
                <span class="required-indicator">*</span>
            </label>
            <g:select id="creator" name="creator.id" from="${taskCreator}" optionKey="id" optionValue="display" required="" value="${taskInstance?.creator?.id}" class="many-to-one"/>
        </div>

        <div class="field fieldcontain ${hasErrors(bean: taskInstance, field: 'createDate', 'error')} required">
            <label for="createDate">
                <g:message code="task.createDate.label" default="Create Date" />
                <span class="required-indicator">*</span>
            </label>
            <g:datePicker name="createDate" precision="day"  value="${taskInstance?.createDate}" />
            <!--input size="10" type="text" id="datepicker-createDate" name="createDate" value="${taskInstance?.createDate}"-->
        </div>

        <div class="field fieldcontain ${hasErrors(bean: taskInstance, field: 'endDate', 'error')} required">
            <label for="endDate">
                <g:message code="task.endDate.label" default="End Date" />
                <span class="required-indicator">*</span>
            </label>
            <g:datePicker name="endDate" precision="day"  value="${taskInstance?.endDate}" />
            <!--input size="10" type="text" id="datepicker-endDate" name="endDate" value="${taskInstance?.endDate}"-->
        </div>

        <div class="field fieldcontain ${hasErrors(bean: taskInstance, field: 'responsibleUser', 'error')}">
            <label for="responsibleUser">
                <g:message code="task.responsibleUser.label" default="Responsible User" />
            </label>
            <g:select id="responsibleUser" name="responsibleUser.id" from="${validResponsibleUsers}" optionKey="id" optionValue="display" value="${taskInstance?.responsibleUser?.id}" class="many-to-one" noSelection="['null': '']"/>
        </div>

        <div class="field fieldcontain ${hasErrors(bean: taskInstance, field: 'responsibleOrg', 'error')}">
            <label for="responsibleOrg">
                <g:message code="task.responsibleOrg.label" default="Responsible Org" />
            </label>
            <g:select id="responsibleOrg" name="responsibleOrg.id" from="${validResponsibleOrgs}" optionKey="id" value="${taskInstance?.responsibleOrg?.id}" class="many-to-one" noSelection="['null': '']"/>
        </div>


        <r:script type="text/javascript">

            $("#datepicker-createDate").datepicker({
                format:"${message(code:'default.date.format.notime', default:'yyyy-MM-dd').toLowerCase()}",
                language:"${message(code:'default.locale.label', default:'en')}",
                autoclose:true
            })
            $("#datepicker-endDate").datepicker({
                format:"${message(code:'default.date.format.notime', default:'yyyy-MM-dd').toLowerCase()}",
                language:"${message(code:'default.locale.label', default:'en')}",
                autoclose:true
            })

        </r:script>

    </g:form>
</semui:modal>
