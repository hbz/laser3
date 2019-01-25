<%@ page import="com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.OrgRole;com.k_int.kbplus.Subscription;com.k_int.kbplus.RefdataValue;com.k_int.kbplus.DocContext;com.k_int.kbplus.Doc" %>
<g:if test="${editmode}">
    <a class="ui button ${tmplCss}" data-semui="modal" href="#${tmplModalID}">
        <g:if test="${tmplButtonText}">
            <i class="${tmplIcon} icon"></i>
        </g:if>
        ${tmplButtonText}
    </a>
</g:if>

<semui:modal id="${tmplModalID}" text="${tmplText}">
    <g:form id="link_subs" class="ui form" url="[controller: 'ajax', action: 'linkSubscriptions']" method="post">
        <input type="hidden" name="context" value="${context}"/>
        <%
            int perspIndex
            if(context == Subscription.class.name+":"+link?.source) {
                perspIndex = 0
            }
            else if(context == Subscription.class.name+":"+link?.destination) {
                perspIndex = 1
            }
            else perspIndex = 0
        %>
        <g:if test="${link}">
            <g:set var="pair" value="${link.getOther(context)}"/>
            <g:set var="linkType" value="${link.linkType}"/>
            <g:set var="comment" value="${DocContext.findByLink(link)}"/>
            <g:set var="selectPair" value="pair_${link.id}"/>
            <g:set var="selectLink" value="linkType_${link.id}"/>
            <g:set var="linkComment" value="linkComment_${link.id}"/>
            <input type="hidden" name="link" value="${link.class.name}:${link.id}" />
            <g:if test="${comment}">
                <input type="hidden" name="commentID" value="${comment.owner.class.name}:${comment.owner.id}" />
            </g:if>
        </g:if>
        <g:else>
            <g:set var="selectPair" value="pair_new"/>
            <g:set var="selectLink" value="linkType_new"/>
            <g:set var="linkComment" value="linkComment_new"/>
        </g:else>
        <div class="field">
            <div id="sub_role_tab_${tmplModalID}" class="ui grid">
                <div class="row">
                    <div class="column">
                        Diese Lizenz mit einer anderen Lizenz verknüpfen
                    </div>
                </div>
                <div class="row">
                    <div class="four wide column">
                        Diese Lizenz
                    </div>
                    <div class="twelve wide column">
                        <g:set var="linkTypes" value="${RefdataValue.findAllByOwner(RefdataCategory.getByI10nDesc('Link type'))}"/>
                        <g:select name="${selectLink}" id="${selectLink}" from="${linkTypes}" optionKey="${{it.class.name+":"+it.id}}"
                                  optionValue="${{it.getI10n('value').split("\\|")[perspIndex]}}" value="${linkType?.class?.name}:${linkType?.id}" noSelection="['':'']"/>
                    </div>
                </div>
                <div class="row">
                    <div class="four wide column">
                        Lizenz
                    </div>
                    <div class="twelve wide column">
                        <input name="${selectPair}" id="${selectPair}" />
                    </div>
                </div>
                <div class="row">
                    <div class="four wide column">
                        Kommentar
                    </div>
                    <div class="twelve wide column">
                        <g:textArea name="${linkComment}" id="${linkComment}" value="${comment?.owner?.content}"/>
                    </div>
                </div>
            </div>
        </div>
    </g:form>
</semui:modal>
<%-- for that one day, we may move away from that ... --%>
<g:javascript>
    <g:if test="${pair}">
        <%
            OrgRole owner = OrgRole.findBySub(pair)
            String ownerName = owner.org.name
        %>
        var data = {
                      id: "${pair.class.name}:${pair.id}",
                      sortKey: "${pair.name}",
                      text: "#${pair.id}: ${pair.name} (${ownerName})"
                    };
    </g:if>
    $("#${selectPair}").select2({
        ajax: {
            url: "<g:createLink controller="ajax" action="lookupSubscriptions" />",
            data: function(term, page) {
                return {
                    q: term,
                    page_limit: 20,
                    baseClass: 'com.k_int.kbplus.Subscription'
                };
            },
            results: function (data, page) {
                return {results: data.values};
            },
            allowClear: true,
            formatSelection: function(data) {
                return data.text;
            }
        }
    });
    //duplicate call needed to preselect value
    if(typeof(data) !== "undefined")
        $("#${selectPair}").select2('data',data);
</g:javascript>