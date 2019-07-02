<%@ page import="com.k_int.kbplus.Doc;com.k_int.kbplus.DocContext" %>
<laser:serviceInjection />

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

    boolean editable2 = accessService.checkPermAffiliation("ORG_BASIC_MEMBER","INST_EDITOR")
    //println "EDITABLE: ${editable}"
    //println "EDITABLE2: ${editable2}"
%>

    <semui:card message="license.notes" class="notes la-js-hideable ${css_class}" href="#modalCreateNote" editable="${editable || editable2}">

        <g:each in="${baseItems}" var="docctx">
            <g:if test="${((docctx.owner?.contentType == Doc.CONTENT_TYPE_STRING) && !(docctx.domain) && (docctx.status?.value != 'Deleted') )}">
                <div class="ui small feed content la-js-dont-hide-this-card">
                    <!--<div class="event">-->

                    <div class="ui grid summary">
                        <div class="twelve wide column">
                            <g:if test="${editable || editable2}">
                                <a onclick="noteedit(${docctx.owner.id});">
                                    <g:if test="${docctx.owner.title}">
                                        ${docctx.owner.title}</a>
                                    </g:if>
                                    <g:else>
                                        Ohne Titel
                                    </g:else>
                                </a>
                            </g:if>
                            <g:else>
                                <a onclick="noteread(${docctx.owner.id});">
                                    <g:if test="${docctx.owner.title}">
                                        ${docctx.owner.title}</a>
                                    </g:if>
                                    <g:else>
                                        Ohne Titel
                                    </g:else>
                                </a>
                            </g:else>
                            <br/>

                            ${message(code:'template.notes.created')}
                            <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${docctx.owner.dateCreated}"/>
                        </div>
                        <div class="center aligned four wide column la-js-editmode-container">

                            <g:if test="${ownobj?.showUIShareButton()}">
                            <g:if test="${docctx?.isShared}">
                                <laser:remoteLink class="ui mini icon green button js-no-wait-wheel la-popup-tooltip la-delay"
                                                  controller="ajax"
                                                  action="toggleShare"
                                                  params='[owner:"${ownobj.class.name}:${ownobj.id}", sharedObject:"${docctx.class.name}:${docctx.id}", tmpl:"notes"]'
                                                  data-content="${message(code:'property.share.tooltip.on')}"
                                                  data-done=""
                                                  data-always="bb8.init('#container-notes')"
                                                  data-update="container-notes"
                                                  role="button"
                                >
                                    <i class="icon la-share la-js-editmode-icon"></i>
                                </laser:remoteLink>
                            </g:if>
                            <g:else>
                                <laser:remoteLink class="ui mini icon button js-no-wait-wheel la-popup-tooltip la-delay js-open-confirm-modal"
                                                  controller="ajax"
                                                  action="toggleShare"
                                                  params='[owner:"${ownobj.class.name}:${ownobj.id}", sharedObject:"${docctx.class.name}:${docctx.id}", tmpl:"notes"]'
                                                  data-content="${message(code:'property.share.tooltip.off')}"
                                                  data-confirm-term-what="element"
                                                  data-confirm-term-what-detail="${docctx.owner.title}"
                                                  data-confirm-term-where="member"
                                                  data-confirm-term-how="share"
                                                  data-done=""
                                                  data-always="bb8.init('#container-notes')"
                                                  data-update="container-notes"
                                                  role="button"
                                >
                                    <i class="la-share slash icon la-js-editmode-icon"></i>
                                </laser:remoteLink>
                            </g:else>

                        </g:if>
                        </div>
                    </div>
                    <!--</div>-->
                </div>
            </g:if>
        </g:each>
    </semui:card>

    <g:if test="${sharedItems}">
        <semui:card message="license.notes.shared" class="documents la-js-hideable ${css_class}" editable="${editable}">
            <g:each in="${sharedItems}" var="docctx">

                <g:if test="${((docctx.owner?.contentType == Doc.CONTENT_TYPE_STRING) && !(docctx.domain) && (docctx.status?.value != 'Deleted') )}">
                    <div class="ui small feed content la-js-dont-hide-this-card">

                        <div class="summary">
                            <g:if test="${docctx.owner.title}">
                                <a onclick="noteread(${docctx.owner.id});">${docctx.owner.title}</a>
                            </g:if>
                            <g:else>
                                <a onclick="noteread(${docctx.owner.id});">Ohne Titel</a>
                            </g:else>
                            (${docctx.owner.type.getI10n("value")})
                        </div>
                    </div>
                </g:if>

            </g:each>
        </semui:card>
    </g:if>

    <script>
        function noteedit(id) {
            $.ajax({
                url: '<g:createLink controller="ajax" action="NoteEdit"/>?id='+id,
                success: function(result){
                    $("#dynamicModalContainer").empty();
                    $("#modalEditNote").remove();

                    $("#dynamicModalContainer").html(result);
                    $("#dynamicModalContainer .ui.modal").modal('show');
                }
            });
        }
        function noteread(id) {
            $.ajax({
                url: '<g:createLink controller="ajax" action="readNote"/>?id='+id,
                success: function(result){
                    $("#dynamicModalContainer").empty();
                    $("#modalEditNote").remove();

                    $("#dynamicModalContainer").html(result);
                    $("#dynamicModalContainer .ui.modal").modal('show');
                }
            });
        }
        $( document ).ready(function() {
            if (r2d2) {
                r2d2.initDynamicSemuiStuff('#container-notes');
            }
        });
    </script>
