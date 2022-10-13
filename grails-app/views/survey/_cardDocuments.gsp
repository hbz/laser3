<%@ page import="de.laser.survey.SurveyConfig; grails.plugin.springsecurity.SpringSecurityUtils; de.laser.*; de.laser.storage.RDStore; de.laser.storage.RDConstants" %>
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
    boolean editable2 = accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_EDITOR")

%>

<g:if test="${accessService.checkPerm("ORG_BASIC_MEMBER,ORG_CONSORTIUM")}">
    <ui:card message="${controllerName == 'survey' ? 'surveyConfigsInfo.docs' : 'license.documents'}" class="documents la-js-hideable ${css_class}" href="${controllerName == 'survey' ? '#modalCreateDocument' : ''}" editable="${(controllerName == 'survey')  ? (actionName == 'show') : (editable || editable2)}">
        <g:each in="${baseItems}" var="docctx">
           <g:if test="${((docctx.owner?.contentType == Doc.CONTENT_TYPE_FILE) && (docctx.status?.value != 'Deleted'))}">
                <div class="ui small feed content la-js-dont-hide-this-card">
                    <div class="ui grid summary">
                        <div class="eleven wide column">

                            <g:set var="supportedMimeType" value="${Doc.getPreviewMimeTypes().contains(docctx.owner.mimeType)}" />
                            <g:if test="${supportedMimeType}">
                                <a href="#documentPreview" data-documentKey="${docctx.owner.uuid + ':' + docctx.id}">${docctx.owner.title ?: docctx.owner.filename}</a>
                            </g:if>
                            <g:else>
                                ${docctx.owner.title ?: docctx.owner.filename}
                            </g:else>
                            (${docctx.owner?.type?.getI10n("value")})

%{--                            <g:link controller="docstore" id="${docctx.owner.uuid}" class="js-no-wait-wheel" target="_blank">--}%
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

                            %{--//Vorerst alle Umfrage Dokumente als geteilt nur Kennzeichen--}%
                            <span  class="la-popup-tooltip la-delay" data-content="${message(code:'property.share.tooltip.on')}">
                                <i class="green alternate share icon"></i>
                            </span>
                        </div>

                        <div class="right aligned five wide column">
                            <g:link controller="docstore" id="${docctx.owner.uuid}" class="ui icon blue button la-modern-button" target="_blank"><i class="download icon"></i></g:link>

                            <g:if test="${!(ownobj instanceof SurveyConfig)}">
                                <g:if test="${!(ownobj instanceof Org) && ownobj?.showUIShareButton()}">
                                    <g:if test="${docctx?.isShared}">

                                        <ui:remoteLink class="ui icon button green js-no-wait-wheel la-popup-tooltip la-delay"
                                                      controller="ajax" action="toggleShare"
                                                      params='[owner: "${ownobj.class.name}:${ownobj.id}", sharedObject: "${docctx.class.name}:${docctx.id}", tmpl: "documents"]'
                                                      onSuccess=""
                                                      onComplete=""
                                                      data-update="container-documents"
                                                      data-position="top right"
                                                      data-content="${message(code: 'property.share.tooltip.on')}"
                                                        role="button">
                                            <i class="la-share icon"></i>
                                        </ui:remoteLink>

                                    </g:if>
                                    <g:else>
                                        <button class="ui icon button js-open-confirm-modal-copycat js-no-wait-wheel">
                                            <i class="la-share slash icon"></i>
                                        </button>
                                        <ui:remoteLink class="js-gost la-popup-tooltip la-delay"
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
                            <g:if test="${(ownobj instanceof SurveyConfig)}">
                                <laser:render template="/templates/documents/modal" model="[ownobj: ownobj, owntp: owntp, docctx: docctx, doc: docctx.owner]" />
                                <button type="button" class="ui icon blue button la-modern-button editable-cancel"
                                        data-ui="modal"
                                        data-href="#modalEditDocument_${docctx.id}"
                                        aria-label="${message(code: 'ariaLabel.change.universal')}">
                                    <i class="pencil icon"></i></button>
                            </g:if>
                        </div>
                    </div>
                </div>
            </g:if>
        </g:each>
    </ui:card>

    %{--ERMS-4524--}%
    <laser:script file="${this.getGroovyPageFileName()}">
        $('a[data-documentKey]').on('click', function(e) {
            e.preventDefault();
            let docKey = $(this).attr('data-documentKey')
            let previewModalId = '#document-preview-' + docKey.split(':')[0]

            $.ajax({
                url: '${g.createLink(controller: 'ajaxHtml', action: 'documentPreview')}?key=' + docKey
            }).done( function (data) {
                $( '#dynamicModalContainer' ).html(data)
                $( previewModalId ).modal({
                        onVisible: function() { },
                        onApprove: function() { return false; },
                        onHidden:  function() { $(previewModalId).remove() }
                }).modal('show')
            })
        })
    </laser:script>
</g:if>

<g:if test="${editable}">
    <laser:render template="/templates/documents/modal"
              model="${[ownobj: ownobj , owntp: 'surveyConfig']}"/>
</g:if>

<laser:script file="${this.getGroovyPageFileName()}">
    r2d2.initDynamicUiStuff('#container-documents')
</laser:script>
