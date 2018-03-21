<semui:card message="license.documents" class="documents" href="#modalCreateDocument" editable="${editable}">

    <g:each in="${ownobj.documents}" var="docctx">
        <g:if test="${(( (docctx.owner?.contentType==1) || ( docctx.owner?.contentType==3) ) && ( docctx.status?.value!='Deleted'))}">
            <div class="ui small feed content">
                <!--<div class="event">-->

                        <div class="summary">
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

                <!--</div>-->
            </div>
        </g:if>
    </g:each>
</semui:card>

<g:render template="/templates/documents/modal" />

