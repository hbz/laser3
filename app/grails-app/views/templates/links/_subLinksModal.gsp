<%@ page import="com.k_int.kbplus.*;de.laser.helper.RDStore;de.laser.interfaces.CalculatedType;de.laser.helper.RDConstants" %>
<g:if test="${editmode}">
    <a role="button" class="ui button ${tmplCss}" data-semui="modal" href="#${tmplModalID}">
        <g:if test="${tmplIcon}">
            <i class="${tmplIcon} icon"></i>
        </g:if>
        <g:if test="${tmplButtonText}">
            ${tmplButtonText}
        </g:if>
    </a>
</g:if>

<%
    String header, thisString, lookupName, instanceType
    switch(context.class.name) {
        case Subscription.class.name:
            if(subscriptionLicenseLink) {
                header = message(code:"subscription.linking.headerLicense")
                thisString = message(code:"subscription.linking.this")
                lookupName = "lookupLicenses"
                instanceType = message(code:'license')
            }
            else {
                header = message(code:"subscription.linking.header")
                thisString = message(code:"subscription.linking.this")
                lookupName = "lookupSubscriptions"
                instanceType = controllerName
            }
            break
        case License.class.name: header = message(code:"license.linking.header")
            thisString = message(code:"license.linking.this")
            lookupName = "lookupLicenses"
            instanceType = controllerName
            break
    }
%>

<semui:modal id="${tmplModalID}" text="${tmplText}">
    <g:form id="link_${tmplModalID}" class="ui form" url="[controller: 'myInstitution', action: 'linkObjects']" method="post">
        <input type="hidden" name="context" value="${GenericOIDService.getOID(context)}"/>
        <%
            LinkedHashMap linkTypes = [:]
            if(!subscriptionLicenseLink) {
                List<RefdataValue> refdataValues = RefdataCategory.getAllRefdataValues(RDConstants.LINK_TYPE)-RDStore.LINKTYPE_LICENSE
                refdataValues.each { rv ->
                    String[] linkArray = rv.getI10n("value").split("\\|")
                    linkArray.eachWithIndex { l, int perspective ->
                        linkTypes.put(GenericOIDService.getOID(rv)+"§"+perspective,l)
                    }
                    if(link && link.linkType == rv) {
                        int perspIndex
                        if(context == link.source) {
                            perspIndex = 0
                        }
                        else if(context == link.destination) {
                            perspIndex = 1
                        }
                        else {
                            perspIndex = 0
                        }
                        linkType = "${GenericOIDService.getOID(rv)}§${perspIndex}"
                    }
                }
            }

        %>
        <g:if test="${link}">
            <g:set var="pair" value="${link.getOther(context)}"/>
            <g:set var="comment" value="${DocContext.findByLink(link)}"/>
            <g:set var="selectPair" value="pair_${link.id}"/>
            <g:set var="selectLink" value="linkType_${link.id}"/>
            <g:set var="linkComment" value="linkComment_${link.id}"/>
            <input type="hidden" name="link" value="${GenericOIDService.getOID(link)}" />
            <g:if test="${comment}">
                <input type="hidden" name="commentID" value="${GenericOIDService.getOID(comment.owner)}" />
            </g:if>
        </g:if>
        <g:elseif test="${subscriptionLicenseLink}">
            <g:set var="selectPair" value="pair_sl_new"/>
            <g:set var="selectLink" value="linkType_sl_new"/>
            <g:set var="linkComment" value="linkComment_sl_new"/>
        </g:elseif>
        <g:else>
            <g:set var="selectPair" value="pair_new"/>
            <g:set var="selectLink" value="linkType_new"/>
            <g:set var="linkComment" value="linkComment_new"/>
        </g:else>
        <div class="field">
            <div id="sub_role_tab_${tmplModalID}" class="ui grid">
                <div class="row">
                    <div class="column">
                        ${header}
                    </div>
                </div>
                <g:if test="${subscriptionLicenseLink}">
                    <g:hiddenField name="${selectLink}" value="${GenericOIDService.getOID(RDStore.LINKTYPE_LICENSE)}§${1}"/>
                </g:if>
                <g:else>
                    <div class="row">
                        <div class="four wide column">
                            ${thisString}
                        </div>
                        <div class="twelve wide column">
                            <g:select class="ui dropdown select la-full-width" name="${selectLink}" id="${selectLink}" from="${linkTypes}" optionKey="${{it.key}}"
                                      optionValue="${{it.value}}" value="${linkType ?: null}" noSelection="${['' : message(code:'default.select.choose.label')]}"/>
                        </div>
                    </div>
                </g:else>
                <div class="row">
                    <div class="four wide column">
                        <g:message code="${instanceType}" />
                    </div>
                    <div class="twelve wide column">
                        <g:if test="${link}">
                            ${pair}
                        </g:if>
                        <g:else>
                            <div class="ui search selection dropdown la-full-width" id="${selectPair}">
                                <input type="hidden" name="${selectPair}"/>
                                <i class="dropdown icon"></i>
                                <input type="text" class="search"/>
                                <div class="default text"></div>
                            </div>
                        </g:else>
                    </div>
                </div>
                <div class="row">
                    <div class="four wide column">
                        <g:message code="default.linking.comment" />
                    </div>
                    <div class="twelve wide column">
                        <g:textArea class="ui" name="${linkComment}" id="${linkComment}" value="${comment?.owner?.content}"/>
                    </div>
                </div>
            </div>
        </div>
    </g:form>
</semui:modal>
<g:if test="${!link}">
<%-- for that one day, we may move away from that ... --%>
    <r:script>
    $(document).ready(function(){
       console.log("${lookupName}");
        $("#${selectPair}").dropdown({
            apiSettings: {
                url: "<g:createLink controller="ajax" action="${lookupName}"/>?status=FETCH_ALL&query={query}&filterMembers=${atConsortialParent}&ctx=${GenericOIDService.getOID(context)}",
                cache: false
            },
            clearable: true,
            minCharacters: 1
        });
    });
    </r:script>
</g:if>
