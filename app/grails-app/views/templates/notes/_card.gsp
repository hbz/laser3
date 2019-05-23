<%@ page import="com.k_int.kbplus.Doc;com.k_int.kbplus.DocContext" %>

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
                            <g:if test="${docctx.owner.title}">
                                <a onclick="noteedit(${docctx.owner.id});">${docctx.owner.title}</a>
                            </g:if>
                            <g:else>
                                <a onclick="noteedit(${docctx.owner.id});">Ohne Titel</a>
                            </g:else>
                            <br/>

                            ${message(code:'template.notes.created')}
                            <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${docctx.owner.dateCreated}"/>
                        </div>
                        <div class="center aligned four wide column">

                            <g:if test="${ownobj.showUIShareButton()}">
                            <g:if test="${docctx.isShared}">
                                    <g:remoteLink class="ui mini icon button green js-gost js-no-wait-wheel"
                                                  controller="ajax" action="toggleShare"
                                                  params='[owner:"${ownobj.class.name}:${ownobj.id}", sharedObject:"${docctx.class.name}:${docctx.id}", tmpl:"notes"]'
                                                  onSuccess=""
                                                  onComplete=""
                                                  update="container-notes"
                                                  data-position="top right" data-tooltip="${message(code:'property.share.tooltip.on')}"
                                    >
                                        <i class="la-share icon"></i>
                                    </g:remoteLink>
                            </g:if>
                            <g:else>
                                    <button class="ui mini icon button js-open-confirm-modal-copycat js-no-wait-wheel">
                                        <i class="la-share slash icon"></i>
                                    </button>
                                    <g:remoteLink class="js-gost"
                                                  controller="ajax" action="toggleShare"
                                                  params='[owner:"${ownobj.class.name}:${ownobj.id}", sharedObject:"${docctx.class.name}:${docctx.id}", tmpl:"notes"]'
                                                  onSuccess=""
                                                  onComplete=""
                                                  update="container-notes"
                                                  data-position="top right" data-tooltip="${message(code:'property.share.tooltip.off')}"

                                                  data-confirm-term-what="element"
                                                  data-confirm-term-what-detail="${docctx.owner.title}"
                                                  data-confirm-term-where="member"
                                                  data-confirm-term-how="share"
                                    >
                                    </g:remoteLink>
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
