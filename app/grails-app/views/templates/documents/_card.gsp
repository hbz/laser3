<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils; com.k_int.kbplus.*;de.laser.helper.RDStore;" %>
<laser:serviceInjection/>
<%
    List<DocContext> baseItems = []
    List<DocContext> sharedItems = []

    String documentMessage
    switch(ownobj.class.name) {
        case Org.class.name: documentMessage = "menu.my.documents"
            editable = accessService.checkMinUserOrgRole(user, contextService.org, 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')
            break
        default: documentMessage = "license.documents"
            break
    }

    boolean editable2 = accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR")
    Set<DocContext> documentSet = ownobj.documents

    if(ownobj instanceof Org && ownobj.id == contextService.org.id) {
        documentSet.addAll(orgDocumentService.getTargettedDocuments(ownobj))
    }

    documentSet.sort{it.owner?.title}.each{ it ->
        boolean visible = false
        boolean inOwnerOrg = false
        boolean inTargetOrg = false
        boolean isCreator = false
        if(it.org) {

            if(it.owner.owner?.id == contextService.org.id){
                inOwnerOrg = true
            }

            else if(contextService.org.id == it.targetOrg?.id) {
                inTargetOrg = true
            }

            if(it.owner.creator?.id == user.id)
                isCreator = true

            switch(it.shareConf) {
                case RDStore.SHARE_CONF_CREATOR: if(isCreator) visible = true
                    break
                case RDStore.SHARE_CONF_UPLOADER_ORG: if(inOwnerOrg) visible = true
                    break
                case RDStore.SHARE_CONF_UPLOADER_AND_TARGET: if(inOwnerOrg || inTargetOrg) visible = true
                    break
                case RDStore.SHARE_CONF_CONSORTIUM:
                case RDStore.SHARE_CONF_ALL: visible = true //definition says that everyone with "access" to target org. How are such access roles defined and where?
                    break
                default:
                    if (!it.shareConf) visible = true
                    break
            }
        }
        else visible = true
        if ((it.sharedFrom || inTargetOrg) && visible) {
            sharedItems << it
        }
        else if(visible) {
            baseItems << it
        }
    }
    //println "EDITABLE: ${editable}"
    //println "EDITABLE2: ${editable2}"
%>
<g:if test="${accessService.checkPerm("ORG_INST,ORG_CONSORTIUM") && !parentAtChild}">
    <semui:card message="${documentMessage}" class="documents la-js-hideable ${css_class}" href="#modalCreateDocument" editable="${editable || editable2}">
        <g:each in="${baseItems}" var="docctx">
            <g:if test="${(( (docctx.owner?.contentType==1) || ( docctx.owner?.contentType==3) ) && ( docctx.status?.value!='Deleted'))}">
                <div class="ui small feed content la-js-dont-hide-this-card">
                    <div class="ui grid summary">
                        <div class="eight wide column la-column-right-lessPadding">
                            <g:link controller="docstore" id="${docctx.owner.uuid}" class="js-no-wait-wheel la-break-all">
                                <g:if test="${docctx.owner?.title}">
                                    ${docctx.owner.title}
                                </g:if>
                                <g:elseif test="${docctx.owner?.filename}">
                                    ${docctx.owner?.filename}
                                </g:elseif>
                                <g:else>
                                    ${message(code:'template.documents.missing')}
                                </g:else>
                            </g:link>(${docctx.owner?.type?.getI10n("value")})
                        </div>
                        <div class="right aligned eight wide column la-column-left-lessPadding">
                            <g:if test="${docctx.owner.owner?.id == contextService.org.id}">
                                <%-- START First Button --%>
                                <g:render template="/templates/documents/modal" model="[ownobj: ownobj, owntp: owntp, docctx: docctx, doc: docctx.owner]" />
                                <button type="button" class="ui icon mini button editable-cancel" data-semui="modal" data-href="#modalEditDocument_${docctx.id}" ><i class="pencil icon"></i></button>
                                <%-- STOP First Small Column --%>
                                <g:if test="${!docctx.isShared && accessService.checkMinUserOrgRole(user,docctx.owner.owner,"INST_EDITOR")}">
                                <%-- START Second Button --%>
                                    <g:link controller="${controllerName}" action="deleteDocuments" class="ui icon mini negative button js-open-confirm-modal"
                                            data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.document", args: [docctx.owner.title])}"
                                            data-confirm-term-how="delete"
                                            params='[instanceId:"${ownobj.id}", deleteId:"${docctx.id}", redirectAction:"show"]'>
                                        <i class="trash alternate icon"></i>
                                    </g:link>
                                </g:if>
                                <g:else>
                                    <%-- Hidden Fake Button To hold the other Botton in Place --%>
                                    <div class="ui icon mini button la-hidden">
                                        <i class="coffe icon"></i>
                                    </div>
                                </g:else>
                                <%-- STOP Second Button --%>
                            </g:if>
                            <g:else>
                                <%-- Hidden Fake Button To hold the other Botton in Place --%>
                                <div class="ui icon mini button la-hidden">
                                    <i class="coffe icon"></i>
                                </div>

                                <%-- Hidden Fake Button To hold the other Botton in Place --%>
                                <div class="ui icon mini button la-hidden">
                                    <i class="coffe icon"></i>
                                </div>
                            </g:else>
                            <%-- START Third Button --%>
                            <g:if test="${!(ownobj instanceof Org) && ownobj?.showUIShareButton()}">
                                <g:if test="${docctx?.isShared}">
                                    <laser:remoteLink class="ui mini icon green button js-no-wait-wheel la-popup-tooltip la-delay"
                                                      controller="ajax"
                                                      action="toggleShare"
                                                      params='[owner:"${ownobj.class.name}:${ownobj.id}", sharedObject:"${docctx.class.name}:${docctx.id}", tmpl:"documents"]'
                                                      data-content="${message(code:'property.share.tooltip.on')}"
                                                      data-done=""
                                                      data-always="bb8.init('#container-documents')"
                                                      data-update="container-documents"
                                                      role="button"
                                    >
                                        <i class="icon la-share la-js-editmode-icon"></i>
                                    </laser:remoteLink>
                                </g:if>
                                <g:else>
                                    <laser:remoteLink class="ui mini icon button js-no-wait-wheel la-popup-tooltip la-delay js-open-confirm-modal"
                                                      controller="ajax"
                                                      action="toggleShare"
                                                      params='[owner:"${ownobj.class.name}:${ownobj.id}", sharedObject:"${docctx.class.name}:${docctx.id}", tmpl:"documents"]'
                                                      data-content="${message(code:'property.share.tooltip.off')}"
                                                      data-confirm-tokenMsg="${message(code: "confirm.dialog.share.element.member", args: [docctx.owner.title])}"
                                                      data-confirm-term-how="share"

                                                      data-done=""
                                                      data-always="bb8.init('#container-documents')"
                                                      data-update="container-documents"
                                                      role="button"
                                    >
                                        <i class="la-share slash icon la-js-editmode-icon"></i>
                                    </laser:remoteLink>
                                </g:else>
                            </g:if>
                        </div>
                    </div>
                </div>
            </g:if>
        </g:each>
    </semui:card>
</g:if>
<g:if test="${sharedItems}">
    <semui:card message="license.documents.shared" class="documents la-js-hideable ${css_class}" editable="${editable}">
        <g:each in="${sharedItems}" var="docctx">
            <g:if test="${(( (docctx.owner?.contentType==1) || ( docctx.owner?.contentType==3) ) && ( docctx.status?.value!='Deleted'))}">
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
                                    ${message(code:'template.documents.missing')}
                                </g:else>

                            </g:link>(${docctx.owner?.type?.getI10n("value")})
                        </div>
                        <g:if test="${docctx.owner.owner?.id == contextService.org.id}">
                            <div class="two wide column">
                                <g:render template="/templates/documents/modal" model="[ownobj: ownobj, owntp: owntp, docctx: docctx, doc: docctx.owner]" />
                                <button type="button" class="ui icon mini button editable-cancel" data-semui="modal" data-href="#modalEditDocument_${docctx.id}" ><i class="pencil icon"></i></button>
                            </div>
                        </g:if>
                    </div>
                </div>
            </g:if>

        </g:each>
    </semui:card>
</g:if>

<script>
    $( document ).ready(function() {
        if (r2d2) {
            r2d2.initDynamicSemuiStuff('#container-documents');
        }


    });

</script>
