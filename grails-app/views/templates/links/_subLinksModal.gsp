<%@ page import="de.laser.*;de.laser.storage.RDStore;de.laser.interfaces.CalculatedType;de.laser.storage.RDConstants" %>
<laser:serviceInjection/>
<g:if test="${editmode}">

        <a role="button"
           class="ui button la-modern-button ${tmplCss}"
           data-semui="modal" href="#${tmplModalID}"
           class="la-popup-tooltip la-delay"
           data-content="${message(code:'license.details.editLink')}">
            <g:if test="${tmplIcon}">
                <i class="${tmplIcon} icon"></i>
            </g:if>
            <g:if test="${tmplButtonText}">
                ${tmplButtonText}
            </g:if>
        </a>

</g:if>

<%
    String header, thisString, lookupName, instanceType, linkType
    Map<String, String> urlParams = [:]
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
                instanceType = message(code:"subscription")
            }
            urlParams.controller = 'myInstitution'
            urlParams.action = 'linkObjects'
            break
        case License.class.name: header = message(code:"license.linking.header")
            thisString = message(code:"license.linking.this")
            lookupName = "lookupLicenses"
            instanceType = message(code:"license")
            urlParams.controller = 'myInstitution'
            urlParams.action = 'linkObjects'
            break
        case Org.class.name: header = message(code:"org.linking.header")
            thisString = message(code:"org.linking.this")
            lookupName = "lookupOrgs"
            instanceType = message(code:"org.label")
            urlParams.controller = 'organisation'
            urlParams.action = 'linkOrgs'
            break
    }
%>

<ui:modal id="${tmplModalID}" text="${tmplText}">
    <g:form id="link_${tmplModalID}" class="ui form" url="${urlParams}" method="post">
        <input type="hidden" name="context" value="${linkInstanceType == Combo.class.name ? context.id : genericOIDService.getOID(context)}"/>
        <%
            LinkedHashMap linkTypes = [:]
            if(!subscriptionLicenseLink) {
                List<RefdataValue> refdataValues = []
                if(linkInstanceType == Combo.class.name) {
                    refdataValues << RDStore.COMBO_TYPE_FOLLOWS
                }
                else refdataValues.addAll(RefdataCategory.getAllRefdataValues(RDConstants.LINK_TYPE)-RDStore.LINKTYPE_LICENSE)
                refdataValues.each { RefdataValue rv ->
                    String[] linkArray = rv.getI10n("value").split("\\|")
                    linkArray.eachWithIndex { l, int perspective ->
                        linkTypes.put(genericOIDService.getOID(rv)+"§"+perspective,l)
                    }
                    int perspIndex
                    if(linkInstanceType == Combo.class.name) {
                        if(link && link.type == rv) {
                            if(context == link.fromOrg)
                                perspIndex = 0
                            else if(context == link.toOrg)
                                perspIndex = 1
                            else perspIndex = 0
                            linkType = "${genericOIDService.getOID(rv)}§${perspIndex}"
                        }
                    }
                    else {
                        if(link && link.linkType == rv) {
                            if(context in [link.sourceSubscription,link.sourceLicense]) {
                                perspIndex = 0
                            }
                            else if(context in [link.destinationSubscription,link.destinationLicense]) {
                                perspIndex = 1
                            }
                            else {
                                perspIndex = 0
                            }
                            linkType = "${genericOIDService.getOID(rv)}§${perspIndex}"
                        }
                    }

                }
            }
        %>
        <g:if test="${link}">
            <g:if test="${linkInstanceType == Combo.class.name}">
                <g:set var="pair" value="${context == link.fromOrg ? link.toOrg : link.fromOrg}"/>
            </g:if>
            <g:else>
                <g:set var="pair" value="${link.getOther(context)}"/>
                <g:set var="comment" value="${DocContext.findByLink(link)}"/>
            </g:else>
            <g:set var="selectPair" value="pair_${link.id}"/>
            <g:set var="selectLink" value="linkType_${link.id}"/>
            <g:set var="linkComment" value="linkComment_${link.id}"/>
            <input type="hidden" name="link" value="${genericOIDService.getOID(link)}" />
            <g:if test="${comment}">
                <input type="hidden" name="commentID" value="${genericOIDService.getOID(comment.owner)}" />
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
            <g:if test="${linkInstanceType != Combo.class.name}">
                <g:set var="linkComment" value="linkComment_new"/>
            </g:if>
        </g:else>
        <div class="field">
            <div id="sub_role_tab_${tmplModalID}" class="ui grid">
                <div class="row">
                    <div class="column">
                        ${header}
                    </div>
                </div>
                <g:if test="${subscriptionLicenseLink}">
                    <g:hiddenField name="${selectLink}" value="${genericOIDService.getOID(RDStore.LINKTYPE_LICENSE)}§${1}"/>
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
                            <input type="hidden" name="${selectPair}" value="${linkInstanceType == Combo.class.name ? pair : genericOIDService.getOID(pair)}"/>
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
                <g:if test="${linkInstanceType != Combo.class.name}">
                    <div class="row">
                        <div class="four wide column">
                            <g:message code="default.linking.comment" />
                        </div>
                        <div class="twelve wide column">
                            <g:textArea class="ui" name="${linkComment}" id="${linkComment}" value="${comment?.owner?.content}"/>
                        </div>
                    </div>
                </g:if>
            </div>
        </div>
    </g:form>
</ui:modal>
<g:if test="${!link}">
    <laser:script file="${this.getGroovyPageFileName()}">
        $("#${selectPair}").dropdown({
            apiSettings: {
                url: "<g:createLink controller="ajaxJson" action="${lookupName}"/>?status=FETCH_ALL&query={query}&filterMembers=${atConsortialParent}&ctx=${genericOIDService.getOID(context)}",
                cache: false
            },
            clearable: true,
            minCharacters: 1
        });
    </laser:script>
</g:if>