<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.survey.SurveyConfig; grails.plugin.springsecurity.SpringSecurityUtils; de.laser.*; de.laser.storage.RDStore; de.laser.storage.RDConstants" %>
<laser:serviceInjection/>

<%
    List<DocContext> baseItems = []
    List<DocContext> sharedItems = []

    ownobj.documents?.sort{it.owner?.title}.each{ it ->
        if (it.sharedFrom) {
            sharedItems << it
        }
        else {
            baseItems << it
        }
    }
    boolean editable2 = contextService.isInstEditor(CustomerTypeService.ORG_CONSORTIUM_PRO)

%>

<g:if test="${(contextService.getOrg().isCustomerType_Inst() || contextService.getOrg().isCustomerType_Consortium_Pro())}">
    <ui:card message="${controllerName == 'survey' ? 'surveyConfigsInfo.docs' : 'license.documents'}" class="documents ${css_class}" href="${controllerName == 'survey' ? '#modalCreateDocument' : ''}" editable="${(controllerName == 'survey' && actionName == 'show' && (editable || editable2)) ?: false}">
        <g:each in="${baseItems}" var="docctx">
           <g:if test="${docctx.isDocAFile() && (docctx.status?.value != 'Deleted')}">
                <div class="ui small feed content">
                    <div class="ui grid summary">
                        <div class="nine wide column">
                            <ui:documentIcon doc="${docctx.owner}" showText="false" showTooltip="true"/>

                            %{--//Vorerst alle Umfrage Dokumente als geteilt nur Kennzeichen--}%
                            <span class="la-popup-tooltip" data-content="${message(code:'property.share.tooltip.on')}">
                                <i class="${Icon.SIG.SHARED_OBJECT_ON} green"></i>
                            </span>
                            <g:set var="supportedMimeType" value="${Doc.getPreviewMimeTypes().containsKey(docctx.owner.mimeType)}" />
                            <g:if test="${supportedMimeType}">
                                <a href="#documentPreview" data-dctx="${docctx.id}">${docctx.owner.title ?: docctx.owner.filename}</a>
                            </g:if>
                            <g:else>
                                ${docctx.owner.title ?: docctx.owner.filename}
                            </g:else>
                            <g:if test="${docctx.getDocType()}">
                                (${docctx.getDocType().getI10n("value")})
                            </g:if>

%{--                            <g:link controller="document" action="downloadDocument" id="${docctx.owner.uuid}" class="js-no-wait-wheel" target="_blank">--}%
%{--                                <g:if test="${docctx.owner?.title}">--}%
%{--                                    ${docctx.owner.title}--}%
%{--                                </g:if>--}%
%{--                                <g:elseif test="${docctx.owner?.filename}">--}%
%{--                                    ${docctx.owner.filename}--}%
%{--                                </g:elseif>--}%
%{--                                <g:else>--}%
%{--                                    ${message(code: 'template.documents.missing')}--}%
%{--                                </g:else>--}%

%{--                            </g:link>(${docctx.owner?.type?.getI10n("value")})--}%

                            %{--ERMS-4529--}%

                        </div>

                        <div class="right aligned seven wide column">
                            <g:link controller="document" action="downloadDocument" id="${docctx.owner.uuid}" class="${Btn.MODERN.SIMPLE}" target="_blank"><i class="${Icon.CMD.DOWNLOAD}"></i></g:link>

                            <g:if test="${!(ownobj instanceof SurveyConfig)}">
                                <g:if test="${!(ownobj instanceof Org) && ownobj?.showUIShareButton()}">
                                    <g:if test="${docctx?.isShared}">

                                        <ui:remoteLink class="ui icon button green js-no-wait-wheel la-popup-tooltip"
                                                      controller="ajax" action="toggleShare"
                                                      params='[owner: "${ownobj.class.name}:${ownobj.id}", sharedObject: "${docctx.class.name}:${docctx.id}", tmpl: "documents"]'
                                                      onSuccess=""
                                                      onComplete=""
                                                      data-update="container-documents"
                                                      data-position="top right"
                                                      data-content="${message(code: 'property.share.tooltip.on')}"
                                                        role="button">
                                            <i class="${Icon.SIG.SHARED_OBJECT_ON}"></i>
                                        </ui:remoteLink>

                                    </g:if>
                                    <g:else>
                                        <button class="ui icon button js-open-confirm-modal-copycat js-no-wait-wheel">
                                            <i class="${Icon.SIG.SHARED_OBJECT_OFF}"></i>
                                        </button>
                                        <ui:remoteLink class="js-gost la-popup-tooltip"
                                                      controller="ajax" action="toggleShare"
                                                      params='[owner: "${ownobj.class.name}:${ownobj.id}", sharedObject: "${docctx.class.name}:${docctx.id}", tmpl: "documents"]'
                                                      onSuccess=""
                                                      onComplete=""
                                                      data-update="container-documents"
                                                      data-position="top right"
                                                      data-content="${message(code: 'property.share.tooltip.off')}"
                                                      data-confirm-tokenMsg="${message(code: "confirm.dialog.share.element.member", args: [docctx.owner.title])}"
                                                      data-confirm-term-how="share"
                                                        role="button">
                                        </ui:remoteLink>
                                    </g:else>
                                </g:if>
                            </g:if>
%{--                            <g:if test="${(ownobj instanceof SurveyConfig)}"> -- erms-4627 --}%
                            <g:if test="${(editable || editable2) && (ownobj instanceof SurveyConfig && docctx.owner.owner.id == contextService.getOrg().id)}">
                                <laser:render template="/templates/documents/modal" model="[ownobj: ownobj, owntp: owntp, docctx: docctx, doc: docctx.owner]" />
                                <button type="button" class="${Btn.MODERN.SIMPLE}"
                                        data-ui="modal"
                                        data-href="#modalEditDocument_${docctx.id}"
                                        aria-label="${message(code: 'ariaLabel.change.universal')}">
                                    <i class="${Icon.CMD.EDIT}"></i></button>

                                <g:link controller="survey" action="deleteDocuments"
                                        class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.document", args: [docctx.owner.title])}"
                                        data-confirm-term-how="delete"
                                        params='[surveyConfigID: surveyConfig.id, id: surveyInfo.id, deleteId: "${docctx.id}"]'
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                    <i class="${Icon.CMD.DELETE}"></i>
                                </g:link>
                            </g:if>
                        </div>
                    </div>
                </div>
            </g:if>
        </g:each>
    </ui:card>

    <laser:script file="${this.getGroovyPageFileName()}">
        docs.init('.documents'); %{--ERMS-4524--}%
    </laser:script>
</g:if>

%{--<g:if test="${editable}">
    <laser:render template="/templates/documents/modal"
              model="${[ownobj: ownobj , owntp: 'surveyConfig']}"/>
</g:if>--}%

<laser:script file="${this.getGroovyPageFileName()}">
    r2d2.initDynamicUiStuff('#container-documents')
</laser:script>
