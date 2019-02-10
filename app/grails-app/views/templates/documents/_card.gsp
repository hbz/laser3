<%@ page import="com.k_int.kbplus.DocContext" %>

<semui:card message="license.documents" class="documents la-js-hideable ${css_class}" href="#modalCreateDocument" editable="${editable}">

    <%
        List<DocContext> baseItems = []
        List<DocContext> sharedItems = []
        ownobj.documents.sort{it.owner?.title}.each{ it ->
            if (it.sharedFrom) {
                sharedItems << it
            }
            else {
                baseItems << it
            }
        }
    %>

    <g:each in="${baseItems}" var="docctx">
        <g:if test="${(( (docctx.owner?.contentType==1) || ( docctx.owner?.contentType==3) ) && ( docctx.status?.value!='Deleted'))}">
            <div class="ui small feed content la-js-dont-hide-this-card">
                <!--<div class="event">-->

                        <div class="summary">
                            <g:link controller="docstore" id="${docctx.owner.uuid}" class="js-no-wait-wheel">
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

                            <g:link controller="ajax" action="toggleShare" params="${[]}" />

                            <span data-position="top right" data-tooltip="${message(code:'property.share.tooltip')}">

                                    <g:remoteLink class="js-gost"
                                                  controller="ajax" action="toggleShare"
                                                  params='[owner:"${ownobj.class.name}:${ownobj.id}", sharedObject:"${docctx.class.name}:${docctx.id}"]'
                                                  onSuccess=""
                                                  onComplete=""
                                                  update="">
                                        <button class="ui icon button">
                                            <i class="alternate share icon"></i>
                                        </button>
                                    </g:remoteLink>

                            </span>

                        </div>

                <!--</div>-->
            </div>
        </g:if>
    </g:each>

    <g:if test="${sharedItems}">
        <hr />
    </g:if>

    <g:each in="${sharedItems}" var="docctx">
        <g:if test="${(( (docctx.owner?.contentType==1) || ( docctx.owner?.contentType==3) ) && ( docctx.status?.value!='Deleted'))}">
            <div class="ui small feed content la-js-dont-hide-this-card">
                <!--<div class="event">-->

                <div class="summary">
                    <g:link controller="docstore" id="${docctx.owner.uuid}" class="js-no-wait-wheel">
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

