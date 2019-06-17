<%@ page import="com.k_int.kbplus.*;de.laser.helper.RDStore;de.laser.interfaces.TemplateSupport" %>
<g:if test="${editmode}">
    <a class="ui button ${tmplCss}" data-semui="modal" href="#${tmplModalID}">
        <g:if test="${tmplIcon}">
            <i class="${tmplIcon} icon"></i>
        </g:if>
        <g:if test="${tmplButtonText}">
            ${tmplButtonText}
        </g:if>
    </a>
</g:if>

<semui:modal id="${tmplModalID}" text="${tmplText}">
    <g:form id="link_subs" class="ui form" url="[controller: 'ajax', action: 'linkSubscriptions']" method="post">
        <input type="hidden" name="context" value="${context}"/>
        <%
            List<RefdataValue> refdataValues = RefdataValue.findAllByOwner(RefdataCategory.getByI10nDesc('Link type'))
            LinkedHashMap linkTypes = [:]
            refdataValues.each { rv ->
                String[] linkArray = rv.getI10n("value").split("\\|")
                linkArray.eachWithIndex { l, int perspective ->
                    linkTypes.put(rv.class.name+":"+rv.id+"§"+perspective,l)
                }
                if(link && link.linkType == rv) {
                    int perspIndex
                    if(context == Subscription.class.name+":"+link.source) {
                        perspIndex = 0
                    }
                    else if(context == Subscription.class.name+":"+link.destination) {
                        perspIndex = 1
                    }
                    else {
                        perspIndex = 0
                    }
                    linkType = "${rv.class.name}:${rv.id}§${perspIndex}"
                }
            }
        %>
        <g:if test="${link}">
            <g:set var="pair" value="${link.getOther(context)}"/>
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
                        <g:message code="subscription.linking.header"/>
                    </div>
                </div>
                <div class="row">
                    <div class="four wide column">
                        <g:message code="subscription.linking.this" />
                    </div>
                    <div class="twelve wide column">
                        <g:select class="ui dropdown select la-full-width" name="${selectLink}" id="${selectLink}" from="${linkTypes}" optionKey="${{it.key}}"
                                  optionValue="${{it.value}}" value="${linkType ?: null}" noSelection="['':'']"/>
                    </div>
                </div>
                <div class="row">
                    <div class="four wide column">
                        <g:message code="subscription" />
                    </div>
                    <div class="twelve wide column">
                        <div class="ui search selection dropdown la-full-width" id="${selectPair}">
                            <input type="hidden" name="${selectPair}" value="${pair?.class?.name}:${pair?.id}"/>
                            <i class="dropdown icon"></i>
                            <input type="text" class="search"/>
                            <div class="default text"></div>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="four wide column">
                        <g:message code="subscription.linking.comment" />
                    </div>
                    <div class="twelve wide column">
                        <g:textArea class="ui" name="${linkComment}" id="${linkComment}" value="${comment?.owner?.content}"/>
                    </div>
                </div>
            </div>
        </div>
    </g:form>
</semui:modal>
<%-- for that one day, we may move away from that ... --%>
<r:script>
    $(document).ready(function(){
        $("#${selectPair}").dropdown({
            apiSettings: {
                url: "<g:createLink controller="ajax" action="lookupSubscriptions"/>?query={query}&ctx=${context}",
                cache: false
            },
            clearable: true,
            minCharacters: 0
        });
    });
</r:script>