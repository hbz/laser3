<semui:card message="license.documents" class="card-grey documents">

    <g:each in="${ownobj.documents}" var="docctx">
        <g:if test="${(( (docctx.owner?.contentType==1) || ( docctx.owner?.contentType==3) ) && ( docctx.status?.value!='Deleted'))}">
            <div class="ui small feed">
                <!--<div class="event">-->
                    <div class="content">
                        <div class="summary">
                            (${docctx.owner.id})
                            <g:link controller="docstore" id="${docctx.owner.uuid}">
                                <g:if test="${docctx.owner?.title}">
                                    ${docctx.owner.title}
                                </g:if>
                                <g:else>
                                    <g:if test="${docctx.owner?.filename}">
                                        ${docctx.owner.filename}
                                    </g:if>
                                    <g:else>
                                        ${message(code:'template.documents.missing', default: 'Missing title and filename')}
                                    </g:else>
                                </g:else>
                            </g:link>
                        </div>
                    </div>
                <!--</div>-->
            </div>
        </g:if>
    </g:each>

    <g:if test="${editable}">
        </div>
        <div class="extra content">
            <input type="submit" class="ui button" value="${message(code:'default.button.create_new.label')}" data-semui="modal" href="#modalCreateDocument" />
    </g:if>
</semui:card>

<g:render template="/templates/documents/modal"  />

