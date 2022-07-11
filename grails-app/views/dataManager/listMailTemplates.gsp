<%@ page import="de.laser.MailTemplate; de.laser.storage.RDConstants;de.laser.RefdataCategory" %>
<laser:htmlStart message="mailTemplate.plural.label" />

<ui:breadcrumbs>
    <ui:crumb controller="dataManager" action="index" text="${message(code:'menu.datamanager')}" />
    <ui:crumb message="mailTemplate.plural.label" class="active"/>
</ui:breadcrumbs>

<ui:controlButtons>
    <ui:actionsDropdown>
        <ui:actionsDropdownItem data-ui="modal" href="#modalCreateMailTemplate" message="mailTemplate.create.button"/>
    </ui:actionsDropdown>
</ui:controlButtons>

<ui:h1HeaderWithIcon message="mailTemplate.plural.label" />

<ui:messages data="${flash}"/>
<ui:form>
    <g:if test="${mailTemplates?.size() > 0}">

        <table class="ui celled sortable table table-tworow la-js-responsive-table la-table">
            <thead>
            <tr>
                <th>${message(code: 'sidewide.number')}</th>
                <th>${message(code: 'default.name.label')}</th>
                <th>${message(code: 'default.type.label')}</th>
                <th>${message(code: 'mailTemplate.language.label')}</th>
                <th>${message(code: 'mailTemplate.owner.label')}</th>
                <th class="la-action-info">${message(code: 'default.actions.label')}</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${mailTemplates}" var="template" status="i">
                <tr>
                    <td> ${i+1}</td>
                    <td>${template.name}</td>
                    <td>${template.type.getI10n('value')}</td>
                    <td>${template.language.getI10n('value')}</td>
                    <td>
                        <g:if test="${template.owner}">${template.owner.name}</g:if>
                    </td>
                    <td>
                        <button type="button" class="ui icon button la-popup-tooltip la-delay"
                                data-mailTemplateTarget="${MailTemplate.class.name}:${template.id}"
                                data-mailTemplateName="${template.name}"
                                data-mailTemplateSubject="${template.subject}"
                                data-mailTemplateText="${template.text}"
                                data-mailTemplateType="${template.type.id}"
                                data-mailTemplateLanguage="${template.language.id}"
                                data-ui="modal"
                                data-href="#editMailTemplate"
                                data-content="Mail Template ändern" data-position="top left"><i class="edit icon"></i></button>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>

    </g:if>
    <g:else>
        <div class="container alert-warn">
            ${message(code: 'result.empty')}
        </div>
    </g:else>
</ui:form>


<ui:modal id="editMailTemplate" message="mailTemplate.label" isEditModal="isEditModal">

    <g:form class="ui form" url="[controller: 'dataManager', action: 'editMailTemplate']">
        <input type="hidden" name="target" value="" />
        <div class="field required">
            <label for="mailTemplateName">${message(code:'default.name.label')} <g:message code="messageRequiredField" /></label>
            <input type="text" id="mailTemplateNameEdit" name="name" />
        </div>

        <div class="field required">
            <label for="mailTemplateSubject">${message(code:'mailTemplate.subject.label')} <g:message code="messageRequiredField" /></label>
            <input type="text" id="mailTemplateSubjectEdit" name="subject" />
        </div>

        <div class="field">
            <label for="mailTemplateText">${message(code:'mailTemplate.text.label')}</label>
            <g:textArea id="mailTemplateTextEdit" name="text" rows="5" cols="40"/>
        </div>

        <div  class="field required">
            <label for="mailTemplateLanguage">${message(code:'mailTemplate.language.label')} <g:message code="messageRequiredField" /></label>
            <g:select id="mailTemplateLanguageEdit"
                      name="language"
                      from="${RefdataCategory.getAllRefdataValues(RDConstants.MAIL_TEMPLATE_LANGUAGE)}"
                      optionKey="id"
                      optionValue="${{it.getI10n('value')}}"
                      class="ui dropdown search many-to-one"
                      noSelection="[null: '']"/>
        </div>

        <div  class="field required">
            <label for="mailTemplateType">${message(code:'default.type.label')} <g:message code="messageRequiredField" /></label>
            <g:select id="mailTemplateTypeEdit"
                      name="type"
                      from="${RefdataCategory.getAllRefdataValues(RDConstants.MAIL_TEMPLATE_TYPE)}"
                      optionKey="id"
                      optionValue="${{it.getI10n('value')}}"
                      class="ui dropdown search many-to-one"
                      noSelection="[null: '']"/>
        </div>
    </g:form>

    <laser:script file="${this.getGroovyPageFileName()}">
        JSPC.callbacks.modal.show.editMailTemplate = function(trigger) {
            $('#editMailTemplate #mailTemplateNameEdit').attr('value', $(trigger).attr('data-mailTemplateName'))
            $('#editMailTemplate #mailTemplateSubjectEdit').attr('value', $(trigger).attr('data-mailTemplateSubject'))
            $('#editMailTemplate #mailTemplateTextEdit').text($(trigger).attr('data-mailTemplateText'))
            $('#editMailTemplate input[name=target]').attr('value', $(trigger).attr('data-mailTemplateTarget'))
            $('#editMailTemplate select[name=type]').dropdown('set selected', $(trigger).attr('data-mailTemplateType'))
            $('#editMailTemplate select[name=language]').dropdown('set selected', $(trigger).attr('data-mailTemplateLanguage'))
        }
    </laser:script>

</ui:modal>

<ui:modal id="modalCreateMailTemplate" text="${message(code:'mailTemplate.create.label')}">

    <g:form id="create_mail_template" class="ui form" url="[controller:'dataManager', action:'createMailTemplate']" method="post">

        <div class="field required">
            <label for="mailTemplateName">${message(code:'default.name.label')} <g:message code="messageRequiredField" /></label>
            <input type="text" id="mailTemplateName" name="name" />
        </div>

        <div class="field required">
            <label for="mailTemplateSubject">${message(code:'mailTemplate.subject.label')} <g:message code="messageRequiredField" /></label>
            <input type="text" id="mailTemplateSubject" name="subject" />
        </div>

        <div class="field">
            <label for="mailTemplateText">${message(code:'mailTemplate.text.label')}</label>
            <g:textArea id="mailTemplateText" name="text" rows="5" cols="40"/>
        </div>

        <div  class="field required">
        <label for="mailTemplateLanguage">${message(code:'mailTemplate.language.label')} <g:message code="messageRequiredField" /></label>
        <g:select id="mailTemplateLanguage"
                  name="language"
                  from="${RefdataCategory.getAllRefdataValues(RDConstants.MAIL_TEMPLATE_LANGUAGE)}"
                  optionKey="id"
                  optionValue="${{it.getI10n('value')}}"
                  class="ui dropdown search many-to-one"
                  noSelection="[null: '']"/>
        </div>

        <div  class="field required">
            <label for="mailTemplateType">${message(code:'default.type.label')} <g:message code="messageRequiredField" /></label>
            <g:select id="mailTemplateType"
                      name="type"
                      from="${RefdataCategory.getAllRefdataValues(RDConstants.MAIL_TEMPLATE_TYPE)}"
                      optionKey="id"
                      optionValue="${{it.getI10n('value')}}"
                      class="ui dropdown search many-to-one"
                      noSelection="[null: '']"/>
        </div>

    </g:form>
</ui:modal>

<laser:htmlEnd />
