<%@ page import="com.k_int.kbplus.DocContext" %>
<div id="container-documents">

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

<semui:card message="license.documents" class="documents la-js-hideable ${css_class}" href="#modalCreateDocument" editable="${editable}">
    <g:each in="${baseItems}" var="docctx">

        <g:if test="${(( (docctx.owner?.contentType==1) || ( docctx.owner?.contentType==3) ) && ( docctx.status?.value!='Deleted'))}">
            <div class="ui small feed content la-js-dont-hide-this-card">

                <div class="summary">
                    <g:link controller="docstore" id="${docctx.owner.uuid}" class="js-no-wait-wheel">
                        <g:if test="${docctx.owner?.title}">
                            ${docctx.owner.title}
                        </g:if>
                        <g:elseif test="${docctx.owner?.filename}">
                            ${docctx.owner.filename}
                        </g:elseif>
                        <g:else>
                            ${message(code:'template.documents.missing', default: 'Missing title and filename')}
                        </g:else>

                    </g:link>(${docctx.owner.type.getI10n("value")})

                    <g:if test="${docctx.isShared}">
                        [ Wird geteilt ]
                    </g:if>

                    <g:if test="${instance.showShareButton()}">
                        <span data-position="top right" data-tooltip="${message(code:'property.share.tooltip')}">

                            <g:remoteLink class="js-gost js-no-wait-wheel"
                                          controller="ajax" action="toggleShare"
                                          params='[owner:"${ownobj.class.name}:${ownobj.id}", sharedObject:"${docctx.class.name}:${docctx.id}"]'
                                          onSuccess=""
                                          onComplete=""
                                          update="container-documents">
                                <g:if test="${docctx.isShared}">
                                    <button class="ui mini icon button green">
                                        <i class="alternate share icon"></i>
                                    </button>
                                </g:if>
                                <g:else>
                                    <button class="ui mini icon button">
                                        <i class="alternate share icon"></i>
                                    </button>
                                </g:else>
                            </g:remoteLink>

                        </span>
                    </g:if>
                </div>

            </div>
        </g:if>

    </g:each>
</semui:card>

<g:if test="${sharedItems}">
    <semui:card text="Geteilte Dokumente" class="documents la-js-hideable ${css_class}" editable="${editable}">
        <g:each in="${sharedItems}" var="docctx">

            <g:if test="${(( (docctx.owner?.contentType==1) || ( docctx.owner?.contentType==3) ) && ( docctx.status?.value!='Deleted'))}">
                <div class="ui small feed content la-js-dont-hide-this-card">

                    <div class="summary">
                        <g:link controller="docstore" id="${docctx.owner.uuid}" class="js-no-wait-wheel">
                            <g:if test="${docctx.owner?.title}">
                                ${docctx.owner.title}
                            </g:if>
                            <g:elseif test="${docctx.owner?.filename}">
                                ${docctx.owner.filename}
                            </g:elseif>
                            <g:else>
                                ${message(code:'template.documents.missing', default: 'Missing title and filename')}
                            </g:else>

                        </g:link>(${docctx.owner.type.getI10n("value")})
                    </div>
                </div>
            </g:if>

        </g:each>
    </semui:card>
</g:if>

</div>