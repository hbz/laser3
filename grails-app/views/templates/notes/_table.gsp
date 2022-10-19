<laser:serviceInjection/>

    <table class="ui celled sortable table la-table la-js-responsive-table">
        <thead>
        <tr>
            <th class="ten wide" rowspan="2" scope="col">${message(code:'default.note.label')}</th>
            <th class="two wide la-smaller-table-head" scope="col">${message(code:'default.lastUpdated.label')}</th>
            <th class="two wide" rowspan="2" scope="col">${message(code:'default.actions.label')}</th>
        </tr>
        <tr>
            <th class="two wide la-smaller-table-head" scope="col">${message(code:'default.dateCreated.label')}</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${docstoreService.getNotes(instance, contextService.getOrg())}" var="docctx">
            <tr>
                    <td id="note-wrapper-${docctx.owner.id}">
                        <div style="margin-bottom:1em; border-bottom:1px dashed darkgrey">
                            <strong>${docctx.owner.title ?: message(code: 'license.notes.noTitle')}</strong>
                        </div>
                        <article id="note-${docctx.owner.id}" class="trumbowyg-editor trumbowyg-reset-css">
                            ${raw(docctx.owner.content)}
                        </article>
                        <laser:script file="${this.getGroovyPageFileName()}">
                            wysiwyg.analyzeNote_TMP( $("#note-${docctx.owner.id}"), $("#note-wrapper-${docctx.owner.id}"), true );
                        </laser:script>
                    </td>
                    <td>
                        <g:formatDate format="${message(code:'default.date.format.notime')}" date="${docctx.owner.lastUpdated}"/>

                        <g:if test="${docctx.owner.dateCreated != docctx.owner.lastUpdated}">
                            <br />
                            <span class="sc_darkgrey"><g:formatDate format="${message(code:'default.date.format.notime')}" date="${docctx.owner.dateCreated}"/></span>
                        </g:if>
                    </td>
                    <td class="center aligned">
                        <g:if test="${docctx.sharedFrom}">
                            <span  class="la-popup-tooltip la-delay" data-content="${message(code:'property.share.tooltip.on')}">
                                <i class="grey alternate share icon"></i>
                            </span>
                        </g:if>

                        <g:if test="${instance.respondsTo('showUIShareButton') && instance.showUIShareButton()}">
                            <g:if test="${docctx.isShared}">
                                <span data-position="top right" class="la-popup-tooltip la-delay" data-content="${message(code:'property.share.tooltip.on')}">
                                    <g:link controller="ajax" action="toggleShare" class="ui icon button green la-modern-button"
                                            params='[owner:genericOIDService.getOID(instance), sharedObject:genericOIDService.getOID(docctx), ajaxCallController: ajaxCallController ?: controllerName, ajaxCallAction: ajaxCallAction ?: actionName]'>
                                        <i class="alternate share icon"></i>
                                    </g:link>
                                </span>
                            </g:if>
                            <g:else>
                                <span data-position="top right" class="la-popup-tooltip la-delay" data-content="${message(code:'property.share.tooltip.off')}">
                                    <g:link controller="ajax" action="toggleShare" class="ui icon button blue la-modern-button"
                                            params='[owner:genericOIDService.getOID(instance), sharedObject:genericOIDService.getOID(docctx), ajaxCallController: ajaxCallController ?: controllerName, ajaxCallAction: ajaxCallAction ?: actionName]'>
                                        <i class="la-share slash icon"></i>
                                    </g:link>
                                </span>
                            </g:else>
                        </g:if>

                        <g:if test="${! docctx.sharedFrom}">
                            <a onclick="JSPC.app.noteedit(${docctx.owner.id});" class="ui icon button blue la-modern-button"
                               role="button"
                               aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                <i aria-hidden="true" class="write icon"></i>
                            </a>
                            <g:link controller="${controllerName}" action="deleteDocuments" class="ui icon negative button la-modern-button js-open-confirm-modal"
                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.notes", args: [docctx.owner.title])}"
                                    data-confirm-term-how="delete"
                                    params='[instanceId:"${instance.id}", deleteId:"${docctx.id}", redirectAction:"${actionName}"]'
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                <i class="trash alternate outline icon"></i>
                            </g:link>
                        </g:if>
                    </td>
                </tr>
        </g:each>
        </tbody>
    </table>

<laser:script file="${this.getGroovyPageFileName()}">
    JSPC.app.noteedit = function (id) {

        $.ajax({
            url: '<g:createLink controller="ajaxHtml" action="editNote"/>?id='+id,
            success: function(result){
                $("#dynamicModalContainer").empty();
                $("#modalEditNote").remove();

                $("#dynamicModalContainer").html(result);
                $("#dynamicModalContainer .ui.modal").modal('show');
            }
        });
    }
</laser:script>
