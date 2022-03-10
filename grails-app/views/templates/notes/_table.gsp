<laser:serviceInjection/>
<g:form id="delete_doc_form" url="[controller:"${controllerName}",action:'deleteDocuments']" method="post">

    <table class="ui celled la-js-responsive-table la-table table license-documents">
        <thead>
        <tr>
            <%--<g:if test="${editable}"><th>${message(code:'default.select.label')}</th></g:if> : REMOVED BULK --%>
            <th>${message(code:'title.label')}</th>
            <th>${message(code:'default.note.label')}</th>
            <th>${message(code:'default.date.label')}</th>
            <th>${message(code:'default.actions.label')}</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${instance.documents.sort{it.owner?.title?.toLowerCase()}}" var="docctx">
            <g:if test="${docctx.owner.contentType == 0 && (docctx.status == null || docctx.status?.value != 'Deleted') && ((!docctx.sharedFrom && docctx.owner?.owner?.id == contextService.getOrg().id) || docctx.sharedFrom)}">
                <tr>
                    <%--<g:if test="${editable}"><td><input type="checkbox" name="_deleteflag.${docctx.id}" value="true"/></td></g:if> : REMOVED BULK --%>

                    <th scope="row" class="la-th-column la-main-object" >
                        ${docctx.owner.title}
                    </th>
                    <td>
                        ${docctx.owner.content}
                    </td>
                    <td>
                        <g:formatDate format="${message(code:'default.date.format.notime')}" date="${docctx.owner.dateCreated}"/>
                    </td>
                    <td class="x">
                        <g:if test="${docctx.sharedFrom}">
                            <span  class="la-popup-tooltip la-delay" data-content="${message(code:'property.share.tooltip.on')}">
                                <i class="grey alternate share icon"></i>
                            </span>
                        </g:if>

                        <g:if test="${instance.respondsTo('showUIShareButton') && instance.showUIShareButton()}">
                            <g:if test="${docctx.isShared}">
                                <span data-position="top right"  class="la-popup-tooltip la-delay" data-content="${message(code:'property.share.tooltip.on')}">
                                    <g:link controller="ajax" action="toggleShare" class="ui icon button green la-modern-button"
                                            params='[owner:genericOIDService.getOID(instance), sharedObject:genericOIDService.getOID(docctx), ajaxCallController: ajaxCallController ?: controllerName, ajaxCallAction: ajaxCallAction ?: actionName]'>
                                        <i class="alternate share icon"></i>
                                    </g:link>
                                </span>
                            </g:if>
                            <g:else>
                                <span data-position="top right"  class="la-popup-tooltip la-delay" data-content="${message(code:'property.share.tooltip.off')}">
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
                            <g:link controller="${controllerName}" action="deleteDocuments" class="ui icon negative button la-modern-button"
                                    params='[instanceId:"${instance.id}", deleteId:"${docctx.id}", redirectAction:"${actionName}"]'
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                <i class="trash alternate outline icon"></i>
                            </g:link>
                        </g:if>
                    </td>
                </tr>
            </g:if>
        </g:each>
        </tbody>
    </table>
</g:form>


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
