<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.Doc; de.laser.survey.SurveyConfig;de.laser.RefdataCategory;de.laser.properties.PropertyDefinition;de.laser.storage.RDStore;" %>
<laser:htmlStart text="${message(code: 'survey.label')} (${message(code: 'surveyConfigDocs.label')})" />

<ui:breadcrumbs>
%{--    <ui:crumb controller="myInstitution" action="dashboard" text="${contextService.getOrg().getDesignation()}"/>--}%
    <ui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>
    <g:if test="${surveyInfo}">
%{--        <ui:crumb controller="survey" action="show" id="${surveyInfo.id}"--}%
%{--                     params="[surveyConfigID: surveyConfig.id]" text="${surveyConfig?.getConfigNameShort()}"/>--}%
        <ui:crumb class="active" text="${surveyConfig.getConfigNameShort()}" />
    </g:if>
%{--    <ui:crumb message="surveyConfigDocs.label" class="active"/>--}%
</ui:breadcrumbs>

<ui:controlButtons>
    <laser:render template="exports"/>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon text="${surveyInfo.name}" type="Survey"/>

<uiSurvey:statusWithRings object="${surveyInfo}" surveyConfig="${surveyConfig}" controller="survey" action="${actionName}"/>

<g:if test="${surveyConfig.subscription}">
 <ui:buttonWithIcon style="vertical-align: super;" message="${message(code: 'button.message.showLicense')}" variation="tiny" icon="${Icon.SUBSCRIPTION}" href="${createLink(action: 'show', controller: 'subscription', id: surveyConfig.subscription.id)}"/>
</g:if>

<laser:render template="nav"/>

<ui:objectStatus object="${surveyInfo}" />

<ui:messages data="${flash}"/>

<br />

<g:if test="${surveyConfig}">

    <div class="ui grid">

    <div class="sixteen wide stretched column">

        <ui:greySegment>

            <g:if test="${editable}">
                <div class="four wide column">
                    <button type="button" class="${Btn.MODERN.SIMPLE} right floated" data-ui="modal"
                            data-href="#modalCreateDocument"><i class="${Icon.CMD.ADD}"></i></button>
                    %{--                <laser:render template="/templates/documents/modal"--}%
                    %{--                          model="${[ownobj: surveyConfig, owntp: 'surveyConfig']}"/>--}%
                </div>
            </g:if>
            <br /><br />


            <table class="ui celled la-js-responsive-table la-table table documents-table">
                <thead>
                <tr>
                    <th>${message(code:'sidewide.number')}</th>
                    <th>${message(code: 'default.title.label')}</th>
                    <th>${message(code: 'surveyConfigDocs.docs.table.fileName')}</th>
                    <th>${message(code: 'surveyConfigDocs.docs.table.type')}</th>
                    <th>%{--${message(code: 'property.share.tooltip.sharedFrom')}--}%</th>
                    <th class="center aligned">
                        <ui:optionsIcon />
                    </th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${surveyConfig.getCurrentDocs()}" var="docctx" status="i">
                    <tr>
                        <td>${i + 1}</td>
                        <td>
                            %{--ERMS-4524--}%
                            <g:set var="supportedMimeType" value="${Doc.getPreviewMimeTypes().containsKey(docctx.owner.mimeType)}" />
                            <g:if test="${docctx.isDocAFile() && supportedMimeType}">
                                <a href="#documentPreview" data-dctx="${docctx.id}">${docctx.owner.title}</a>
                            </g:if>
                            <g:else>
                                ${docctx.owner.title}
                            </g:else>
                        </td>
                        <td>
                            ${docctx.owner.filename}
                        </td>
                        <td>
                            ${docctx.getDocType()?.getI10n('value')}
                        </td>
                        <td>
                            %{--ERMS-4529--}%
                            <ui:documentIcon doc="${docctx.owner}" showText="false" showTooltip="true"/>
                            %{--//Vorerst alle Umfrage Dokumente als geteilt nur Kennzeichen--}%
                            <span class="la-popup-tooltip" data-content="${message(code: 'property.share.tooltip.on')}">
                                <i class="${Icon.SIG.SHARED_OBJECT_ON} green"></i>
                            </span>
                        </td>
                        <td class="x">
                            <g:if test="${docctx.isDocAFile()}">
                                <g:link controller="document" action="downloadDocument" id="${docctx.owner.uuid}" class="${Btn.MODERN.SIMPLE}" target="_blank"><i
                                        class="${Icon.CMD.DOWNLOAD}"></i></g:link>
                                <g:if test="${editable && !docctx.sharedFrom}">
                                    <button type="button" class="${Btn.MODERN.SIMPLE_TOOLTIP}" data-ui="modal"
                                            href="#modalEditDocument_${docctx.id}"
                                            data-content="${message(code: "template.documents.edit")}"
                                            aria-label="${message(code: 'ariaLabel.change.universal')}">
                                        <i class="${Icon.CMD.EDIT}"></i></button>
                                    <g:link controller="${controllerName}" action="deleteDocuments"
                                            class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.document", args: [docctx.owner.title])}"
                                            data-confirm-term-how="delete"
                                            params='[surveyConfigID: surveyConfig.id, id: surveyInfo.id, deleteId: "${docctx.id}", redirectAction: "${actionName}"]'
                                            role="button"
                                            aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                        <i class="${Icon.CMD.DELETE}"></i>
                                    </g:link>
                                </g:if>
                            </g:if>
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>

            <g:each in="${surveyConfig.getCurrentDocs()}" var="docctx">
                <laser:render template="/templates/documents/modal"
                          model="${[ownobj: surveyConfig, owntp: surveyConfig, docctx: docctx, doc: docctx.owner]}"/>
            </g:each>
        </ui:greySegment>
    </div>

    <laser:script file="${this.getGroovyPageFileName()}">
        docs.init('.documents-table'); %{--ERMS-4524--}%
    </laser:script>

</g:if>

<laser:htmlEnd />
