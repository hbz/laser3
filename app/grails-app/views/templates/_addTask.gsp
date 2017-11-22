<%@ page import="com.k_int.kbplus.Task" %>
<!-- Lightbox modal for creating a note taken from licenseNotes.html -->
<div class="modal hide" id="modalCreateTask">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal">×</button>
        <h3>${message(code:'task.create.new')}</h3>
    </div>
    <g:form id="create_task" url="[controller:'task',action:'create']" method="post">
        <!--input type="hidden" name="ownerid" value="${ownobj?.id}"/>
        <input type="hidden" name="ownerclass" value="${ownobj?.class?.name}"/>
        <input type="hidden" name="ownertp" value="${owntp}"/-->
        <div class="modal-body">
            <dl>
                <dt>
                    <label>${message(code:'template.addNote.note', default: 'Note')}:</label>
                </dt>
            </dl>
            <!-- form -->

            <div class="fieldcontain ${hasErrors(bean: taskInstance, field: 'title', 'error')} required">
                <label for="title">
                    <g:message code="task.title.label" default="Title" />
                    <span class="required-indicator">*</span>
                </label>
                <g:textField name="title" required="" value="${taskInstance?.title}"/>

            </div>

            <div class="fieldcontain ${hasErrors(bean: taskInstance, field: 'description', 'error')}">
                <label for="description">
                    <g:message code="task.description.label" default="Description" />
                </label>
                <g:textField name="description" value="${taskInstance?.description}"/>

            </div>

            <g:if test="${owntp == 'license'}">
                <div class="fieldcontain ${hasErrors(bean: taskInstance, field: 'license', 'error')} ">
                    <label for="license">
                        <g:message code="task.license.label" default="License" />
                    </label>
                    <g:select id="license" name="license.id" from="${validLicenses}" optionKey="id" value="${ownobj?.id}" class="many-to-one" noSelection="['null': '']"/>

                </div>
            </g:if>

            <g:if test="${owntp == 'org'}">
                <div class="fieldcontain ${hasErrors(bean: taskInstance, field: 'org', 'error')} ">
                    <label for="org">
                        <g:message code="task.org.label" default="Org" />
                    </label>
                    <g:select id="org" name="org.id" from="${validOrgs}" optionKey="id" value="${ownobj?.id}" class="many-to-one" noSelection="['null': '']"/>

                </div>
            </g:if>

            <g:if test="${owntp == 'pkg'}">
                <div class="fieldcontain ${hasErrors(bean: taskInstance, field: 'pkg', 'error')} ">
                    <label for="pkg">
                        <g:message code="task.pkg.label" default="Pkg" />
                    </label>
                    <g:select id="pkg" name="pkg.id" from="${validPackages}" optionKey="id" value="${ownobj?.id}" class="many-to-one" noSelection="['null': '']"/>

                </div>
            </g:if>

            <g:if test="${owntp == 'subscription'}">
                <div class="fieldcontain ${hasErrors(bean: taskInstance, field: 'subscription', 'error')} ">
                    <label for="subscription">
                        <g:message code="task.subscription.label" default="Subscription" />
                    </label>
                    <g:select id="subscription" name="subscription.id" from="${validSubscriptions}" optionKey="id" value="${ownobj?.id}" class="many-to-one" noSelection="['null': '']"/>

                </div>
            </g:if>

            <div class="fieldcontain ${hasErrors(bean: taskInstance, field: 'status', 'error')} required">
                <label for="status">
                    <g:message code="task.status.label" default="Status" />
                    <span class="required-indicator">*</span>
                </label>
                <g:select id="status" name="status.id" from="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues('Task Status')}" optionKey="id" required="" value="${taskInstance?.status?.id}" class="many-to-one"/>

            </div>

            <div class="fieldcontain ${hasErrors(bean: taskInstance, field: 'owner', 'error')} required">
                <label for="owner">
                    <g:message code="task.owner.label" default="Owner" />
                    <span class="required-indicator">*</span>
                </label>
                <g:select id="owner" name="owner.id" from="${taskOwner}" optionKey="id" optionValue="display" required="" value="${taskInstance?.owner?.id}" class="many-to-one"/>

            </div>

            <div class="fieldcontain ${hasErrors(bean: taskInstance, field: 'createDate', 'error')} required">
                <label for="createDate">
                    <g:message code="task.createDate.label" default="Create Date" />
                    <span class="required-indicator">*</span>
                </label>
                <g:datePicker name="createDate" precision="day"  value="${taskInstance?.createDate}"  />

            </div>

            <div class="fieldcontain ${hasErrors(bean: taskInstance, field: 'endDate', 'error')} required">
                <label for="endDate">
                    <g:message code="task.endDate.label" default="End Date" />
                    <span class="required-indicator">*</span>
                </label>
                <g:datePicker name="endDate" precision="day"  value="${taskInstance?.endDate}"  />

            </div>

            <div class="fieldcontain ${hasErrors(bean: taskInstance, field: 'tenantUser', 'error')}">
                <label for="tenantUser">
                    <g:message code="task.tenantUser.label" default="Tenant User" />
                </label>
                <g:select id="tenantUser" name="tenantUser.id" from="${validTenantUsers}" optionKey="id" optionValue="display" value="${taskInstance?.tenantUser?.id}" class="many-to-one" noSelection="['null': '']"/>

            </div>

            <div class="fieldcontain ${hasErrors(bean: taskInstance, field: 'tenantOrg', 'error')}">
                <label for="tenantOrg">
                    <g:message code="task.tenantOrg.label" default="Tenant Org" />
                </label>
                <g:select id="tenantOrg" name="tenantOrg.id" from="${validTenantOrgs}" optionKey="id" value="${taskInstance?.tenantOrg?.id}" class="many-to-one" noSelection="['null': '']"/>

            </div>
            <!-- form -->

            <input type="hidden" name="licenseNoteShared" value="0"/>
        </div>
        <div class="modal-footer">
            <a href="#" class="ui button" data-dismiss="modal">${message(code: 'default.button.close.label', default:'Close')}</a>
            <input type="submit" class="ui primary button" name ="SaveNote" value="${message(code: 'default.button.save_changes', default: 'Save Changes')}">
        </div>
    </g:form>
</div>
