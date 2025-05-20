<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.DocContext; de.laser.Doc; de.laser.storage.RDStore" %>
<laser:serviceInjection />

<%
    List<DocContext> baseItems = []
    List<DocContext> sharedItems = []

    docstoreService.getNotes(ownobj, contextService.getOrg()).each{ it ->
        if (it.status != RDStore.DOC_CTX_STATUS_DELETED){
            if (it.sharedFrom) {
                sharedItems << it
            }
            else {
                if(it.owner.owner?.id == contextService.getOrg().id || it.owner.owner == null)
                    baseItems << it
            }
        }
    }

    boolean editable2 = contextService.isInstEditor()
%>

    <ui:card message="license.notes" class="notes ${css_class}" href="#modalCreateNote" editable="${editable || editable2}">
        <g:each in="${baseItems}" var="docctx">
            <g:if test="${docctx.isDocANote()}">
                <div class="ui small feed content">
                    <div class="ui grid summary">
                        <div class="ten wide column la-column-right-lessPadding">
                            <g:if test="${(docctx.owner.owner?.id == contextService.getOrg().id || docctx.owner.owner == null) && (editable || editable2)}">
                                <a onclick="JSPC.app.editNote(${docctx.id});">
                                    ${docctx.owner.title ?: message(code:'license.notes.noTitle')}
                                </a>
                            </g:if>
                            <g:else>
                                <a onclick="JSPC.app.readNote(${docctx.id});">
                                    ${docctx.owner.title ?: message(code:'license.notes.noTitle')}
                                </a>
                            </g:else>

                            <br />
                            <g:if test="${! docctx.owner.content}">
                                <span class="sc_darkgrey">( ${message(code:'template.notes.noContent')} )</span>
                                <br />
                            </g:if>

                            <g:if test="${docctx.owner.dateCreated == docctx.owner.lastUpdated}">
                                ${message(code:'template.notes.created')}
                            </g:if>
                            <g:else>
                                ${message(code:'template.notes.updated')}
                            </g:else>
                            <g:formatDate format="${message(code:'default.date.format.notime')}" date="${docctx.owner.lastUpdated}"/>
                        </div>
                        <div class="right aligned six wide column la-column-left-lessPadding">
                            <%-- 1 --%>
                            <g:if test="${ownobj.respondsTo('showUIShareButton') && ownobj.showUIShareButton()}">
                                <g:if test="${editable2}">
                                    <g:if test="${docctx.isShared}">
                                        <ui:remoteLink class="${Btn.MODERN.POSITIVE_TOOLTIP} js-no-wait-wheel"
                                                          controller="ajax"
                                                          action="toggleShare"
                                                          params='[owner:genericOIDService.getOID(ownobj), sharedObject:genericOIDService.getOID(docctx), tmpl:"notes", ajaxCallController: ajaxCallController ?: controllerName, ajaxCallAction: ajaxCallAction ?: actionName]'
                                                          data-content="${message(code:'property.share.tooltip.on')}"
                                                          data-done=""
                                                          data-update="container-notes"
                                                          role="button">
                                            <i class="${Icon.SIG.SHARED_OBJECT_ON}"></i>
                                        </ui:remoteLink>
                                    </g:if>
                                    <g:else>
                                        <ui:remoteLink class="${Btn.MODERN.SIMPLE_CONFIRM_TOOLTIP} js-no-wait-wheel"
                                                          controller="ajax"
                                                          action="toggleShare"
                                                          params='[owner:genericOIDService.getOID(ownobj), sharedObject:genericOIDService.getOID(docctx), tmpl:"notes", ajaxCallController: ajaxCallController ?: controllerName, ajaxCallAction: ajaxCallAction ?: actionName]'
                                                          data-content="${message(code:'property.share.tooltip.off')}"
                                                          data-confirm-tokenMsg="${message(code: "confirm.dialog.share.element.member", args: [docctx.owner.title])}"
                                                          data-confirm-term-how="share"
                                                          data-done=""
                                                          data-update="container-notes"
                                                          role="button">
                                            <i class="${Icon.SIG.SHARED_OBJECT_OFF}"></i>
                                        </ui:remoteLink>
                                    </g:else>
                                </g:if>
                                <g:else>
                                    <g:if test="${docctx.isShared}">
                                        %{-- TODO: ERMS-6253 - show shared icon --}%
                                        <span data-position="top right" class="la-popup-tooltip" data-content="${message(code:'property.share.tooltip.on')}">
                                            <span class="${Btn.MODERN.SIMPLE} disabled">
                                                <i class="${Icon.SIG.SHARED_OBJECT_ON} green"></i>
                                            </span>
                                        </span>
                                    </g:if>
                                </g:else>
                            </g:if>
                            <%-- 2 --%>
                            <g:if test="${!docctx.isShared && (editable || editable2)}">
                                <g:link controller="note" action="deleteNote" class="${Btn.MODERN.NEGATIVE_CONFIRM}"
                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.notes", args: [docctx.owner.title])}"
                                        data-confirm-term-how="delete"
                                        params='[instanceId:"${ownobj.id}", deleteId:"${docctx.id}", redirectController:"${ajaxCallController ?: controllerName}", redirectAction:"${ajaxCallAction ?: actionName}"]'
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                    <i class="${Icon.CMD.DELETE}"></i>
                                </g:link>
                            </g:if>
                            <g:else>
                                <div class="${Btn.ICON.SIMPLE} la-hidden">
                                    <icon:placeholder /><%-- Hidden Fake Button --%>
                                </div>
                            </g:else>
                        </div>
                    </div>
                </div>
            </g:if>
        </g:each>
    </ui:card>

    <g:if test="${sharedItems}">
        <ui:card message="license.notes.shared" class="documents ${css_class}" editable="${editable}">
            <g:each in="${sharedItems}" var="docctx">

                <g:if test="${docctx.isDocANote() && (docctx.status?.value != 'Deleted')}">
                    <div class="ui small feed content">

                        <div class="ui grid summary">
                            <div class="twelve wide column">
                                <a onclick="JSPC.app.readNote(${docctx.id});">
                                    ${docctx.owner.title ?: message(code:'license.notes.noTitle')}
                                </a>
                                <br />
                                <g:if test="${! docctx.owner.content}">
                                    <span class="sc_darkgrey">( ${message(code:'template.notes.noContent')} )</span>
                                    <br />
                                </g:if>

                                <g:if test="${docctx.owner.dateCreated == docctx.owner.lastUpdated}">
                                    ${message(code:'template.notes.created')}
                                </g:if>
                                <g:else>
                                    ${message(code:'template.notes.updated')}
                                </g:else>
                                <g:formatDate format="${message(code:'default.date.format.notime')}" date="${docctx.owner.lastUpdated}"/>
                            </div>
                            <div class="four wide column">
%{--                                <g:if test="${docctx.owner.owner?.id == contextService.getOrg().id}">--}%
%{--                                    <laser:render template="/templates/documents/modal" model="[ownobj: ownobj, owntp: owntp, docctx: docctx, doc: docctx.owner]" />--}%
%{--                                    <button type="button" class="${Btn.MODERN.SIMPLE}" data-ui="modal" data-href="#modalEditDocument_${docctx.id}" ><i class="${Icon.CMD.EDIT}"></i></button>--}%
%{--                                </g:if>--}%
                            </div>
                        </div>
                    </div>
                </g:if>

            </g:each>
        </ui:card>
    </g:if>

    <laser:script file="${this.getGroovyPageFileName()}">
        JSPC.app.editNote = function (dctx) {
            $.ajax({
                url: '<g:createLink controller="ajaxHtml" action="editNote"/>?dctx=' + dctx,
                success: function(result){
                    $('#dynamicModalContainer').empty();
                    $('#modalEditNote').remove();

                    $('#dynamicModalContainer').html(result);
                    $('#dynamicModalContainer .ui.modal').modal({
                        autofocus: false,
                        onVisible: function() {
                            r2d2.helper.focusFirstFormElement(this);
                        }
                    }).modal('show');
                }
            });
        }
        JSPC.app.readNote = function (dctx) {
            $.ajax({
                url: '<g:createLink controller="ajaxHtml" action="readNote"/>?dctx=' + dctx,
                success: function(result){
                    $('#dynamicModalContainer').empty();
                    $('#modalReadNote').remove();

                    $('#dynamicModalContainer').html(result);
                    $('#dynamicModalContainer .ui.modal').modal({
                        autofocus: false
                    }).modal('show');
                }
            });
        }
</laser:script>

