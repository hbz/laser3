<%@ page import="com.k_int.kbplus.Doc" %>
<semui:card message="license.notes" class="notes" href="#modalCreateNote" editable="${editable}">

        <g:each in="${ownobj.documents.sort{it.owner?.title}}" var="docctx">
            <g:if test="${((docctx.owner?.contentType == Doc.CONTENT_TYPE_STRING) && !(docctx.domain) && (docctx.status?.value != 'Deleted') )}">
                <div class="ui small feed content">
                    <!--<div class="event">-->

                            <div class="summary">
                                <g:if test="${docctx.owner.title}">
                                    <a onclick="noteedit(${docctx.owner.id});">${docctx.owner.title}</a>
                                </g:if>
                                <g:else>
                                    <a onclick="noteedit(${docctx.owner.id});">Ohne Titel</a>
                                </g:else>
                                <br/>

                                ${message(code:'template.notes.created')}
                                <g:formatDate format="${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}" date="${docctx.owner.dateCreated}"/>

                                <g:if test="${docctx.alert}">
                                    ${message(code:'template.notes.shared')} ${docctx.alert.createdBy.displayName}
                                    <g:if test="${docctx.alert.sharingLevel == 1}">
                                        ${message(code:'template.notes.shared_jc')}
                                    </g:if>
                                    <g:if test="${docctx.alert.sharingLevel == 2}">
                                        ${message(code:'template.notes.shared_community')}
                                    </g:if>
                                    <div class="comments">
                                        <a href="#modalComments" class="announce" data-id="${docctx.alert.id}">
                                            ${docctx.alert?.comments != null ? docctx.alert?.comments?.size() : 0} Comment(s)
                                        </a>
                                    </div>
                                </g:if>
                                <g:else>
                                    <!--${message(code:'template.notes.not_shared')}-->
                                </g:else>
                            </div>

                    <!--</div>-->
                </div>
            </g:if>
        </g:each>
</semui:card>

<r:script>
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
</r:script>