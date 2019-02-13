<%@ page import="com.k_int.kbplus.DocContext" %>

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

                    <g:if test="${ownobj.showShareButton()}">

                        <g:if test="${docctx.isShared}">
                            <span data-position="top right" data-tooltip="${message(code:'property.share.tooltip.on')}">
                                <g:remoteLink class="ui mini icon button green js-gost js-no-wait-wheel"
                                          controller="ajax" action="toggleShare"
                                          params='[owner:"${ownobj.class.name}:${ownobj.id}", sharedObject:"${docctx.class.name}:${docctx.id}", tmpl:"documents"]'
                                          onSuccess=""
                                          onComplete=""
                                          update="container-documents">
                                        <i class="alternate share icon"></i>
                                </g:remoteLink>
                            </span>
                        </g:if>
                        <g:else>
                            <span data-position="top right" data-tooltip="${message(code:'property.share.tooltip.off')}">
                                <g:remoteLink class="ui mini icon button js-gost js-no-wait-wheel"
                                              controller="ajax" action="toggleShare"
                                              params='[owner:"${ownobj.class.name}:${ownobj.id}", sharedObject:"${docctx.class.name}:${docctx.id}", tmpl:"documents"]'
                                              onSuccess=""
                                              onComplete=""
                                              update="container-documents">
                                    <i class="alternate share icon"></i>
                                </g:remoteLink>
                            </span>
                        </g:else>

                    </g:if>
                </div>

            </div>
        </g:if>

    </g:each>
</semui:card>

<g:if test="${sharedItems}">
    <semui:card message="license.documents.shared" class="documents la-js-hideable ${css_class}" editable="${editable}">
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
