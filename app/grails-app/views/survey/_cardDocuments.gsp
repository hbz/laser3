<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils; com.k_int.kbplus.*;de.laser.helper.RDStore;" %>
<laser:serviceInjection/>

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

<g:if test="${accessService.checkPerm("ORG_BASIC_MEMBER,ORG_CONSORTIUM")}">
    <semui:card message="license.documents" class="documents la-js-hideable ${css_class}" href="${controllerName == 'survey' ? '#modalCreateDocument' : ''}" editable="${controller == 'survey' ? false : (editable || editable2)}">
        <g:each in="${baseItems}" var="docctx">
           <g:if test="${(((docctx.owner?.contentType == 1) || (docctx.owner?.contentType == 3)) && (docctx.status?.value != 'Deleted'))}">
                <div class="ui small feed content la-js-dont-hide-this-card">
                    <div class="ui grid summary">
                        <div class="twelve wide column">
                            <g:link controller="docstore" id="${docctx.owner.uuid}" class="js-no-wait-wheel">
                                <g:if test="${docctx.owner?.title}">
                                    ${docctx.owner.title}
                                </g:if>
                                <g:elseif test="${docctx.owner?.filename}">
                                    ${docctx.owner.filename}
                                </g:elseif>
                                <g:else>
                                    ${message(code: 'template.documents.missing', default: 'Missing title and filename')}
                                </g:else>

                            </g:link>(${docctx.owner?.type?.getI10n("value")})
                        </div>

                        <div class="center aligned four wide column">
                            <g:if test="${!(ownobj instanceof SurveyConfig)}">
                                <g:if test="${!(ownobj instanceof Org) && ownobj?.showUIShareButton()}">
                                    <g:if test="${docctx?.isShared}">

                                        <g:remoteLink class="ui mini icon button green js-no-wait-wheel"
                                                      controller="ajax" action="toggleShare"
                                                      params='[owner: "${ownobj.class.name}:${ownobj.id}", sharedObject: "${docctx.class.name}:${docctx.id}", tmpl: "documents"]'
                                                      onSuccess=""
                                                      onComplete=""
                                                      update="container-documents"
                                                      data-position="top right"
                                                      data-tooltip="${message(code: 'property.share.tooltip.on')}">
                                            <i class="la-share icon"></i>
                                        </g:remoteLink>

                                    </g:if>
                                    <g:else>
                                        <button class="ui mini icon button js-open-confirm-modal-copycat js-no-wait-wheel">
                                            <i class="la-share slash icon"></i>
                                        </button>
                                        <g:remoteLink class="js-gost"
                                                      controller="ajax" action="toggleShare"
                                                      params='[owner: "${ownobj.class.name}:${ownobj.id}", sharedObject: "${docctx.class.name}:${docctx.id}", tmpl: "documents"]'
                                                      onSuccess=""
                                                      onComplete=""
                                                      update="container-documents"
                                                      data-position="top right"
                                                      data-tooltip="${message(code: 'property.share.tooltip.off')}"

                                                      data-confirm-term-what="element"
                                                      data-confirm-term-what-detail="${docctx.owner.title}"
                                                      data-confirm-term-where="member"
                                                      data-confirm-term-how="share">
                                        </g:remoteLink>
                                    </g:else>
                                </g:if>
                            </g:if>
                        </div>
                    </div>
                </div>
            </g:if>
        </g:each>
    </semui:card>
</g:if>

<g:if test="${editable}">
    <g:render template="/templates/documents/modal"
              model="${[ownobj: ownobj , owntp: 'surveyConfig']}"/>
</g:if>

<script>
    $(document).ready(function () {
        if (r2d2) {
            r2d2.initDynamicSemuiStuff('#container-documents');
        }


    });

</script>
