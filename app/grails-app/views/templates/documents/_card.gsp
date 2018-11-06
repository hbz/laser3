<semui:card message="license.documents" class="documents la-js-hideable" href="#modalCreateDocument" editable="${editable}">

    <g:each in="${ownobj.documents.sort{it.owner?.title}}" var="docctx">
        <g:if test="${(( (docctx.owner?.contentType==1) || ( docctx.owner?.contentType==3) ) && ( docctx.status?.value!='Deleted'))}">
            <div class="ui small feed content la-js-hide-this-card">
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

                            </g:link>(${docctx.owner.type.getI10n("value")})
                        </div>

                <!--</div>-->
            </div>
        </g:if>
    </g:each>
</semui:card>

