<%@ page import="de.laser.wekb.Provider; de.laser.wekb.ProviderRole; de.laser.wekb.ProviderLink; de.laser.wekb.Vendor; de.laser.wekb.VendorLink; de.laser.utils.DateUtils; de.laser.*;de.laser.storage.RDStore;de.laser.interfaces.CalculatedType;de.laser.storage.RDConstants" %>
<laser:serviceInjection/>
<g:if test="${editmode}">

    <a role="button"
        class="ui icon button la-modern-button la-popup-tooltip ${tmplCss}"
        data-ui="modal" href="#${tmplModalID}"
        data-content="${tmplTooltip}">
        <g:if test="${tmplIcon}">
            <i class="${tmplIcon} icon"></i>
        </g:if>
        <g:if test="${tmplButtonText}">
            ${tmplButtonText}
        </g:if>
    </a>

</g:if>

<%
    String header, thisString, lookupName, instanceType, linkType, objectPlural
    Map<String, String> urlParams = [:]
    switch(context.class.name) {
        case Subscription.class.name:
            objectPlural = message(code:"subscription.plural")
            if(subscriptionLicenseLink) {
                header = message(code:"subscription.linking.headerLicense")
                thisString = context.name
                lookupName = "lookupLicenses"
                instanceType = message(code:'license')
            }
            else {
                header = message(code:"subscription.linking.header")
                thisString = context.name
                lookupName = "lookupSubscriptions"
                instanceType = message(code:"subscription")
            }
            urlParams.controller = 'myInstitution'
            urlParams.action = 'linkObjects'
            if(context.startDate)
                thisString += " (${DateUtils.getLocalizedSDF_noTime().format(context.startDate)} - "
            if(context.endDate)
                thisString += "${DateUtils.getLocalizedSDF_noTime().format(context.endDate)}"
            thisString += ")"
            break
        case License.class.name: header = message(code:"license.linking.header")
            objectPlural = message(code:"license.plural")
            thisString = context.reference
            lookupName = "lookupLicenses"
            instanceType = message(code:"license")
            urlParams.controller = 'myInstitution'
            urlParams.action = 'linkObjects'
            if(context.startDate)
                thisString += " (${DateUtils.getLocalizedSDF_noTime().format(context.startDate)} - "
            if(context.endDate)
                thisString += "${DateUtils.getLocalizedSDF_noTime().format(context.endDate)}"
            thisString += ")"
            break
        case Provider.class.name: header = message(code:"provider.linking.header")
            thisString = context.name
            lookupName = "lookupProviders"
            instanceType = message(code:"provider.label")
            urlParams.controller = 'provider'
            urlParams.action = 'link'
            break
        case Vendor.class.name: header = message(code:"vendor.linking.header")
            thisString = context.name
            lookupName = "lookupVendors"
            instanceType = message(code:"vendor.label")
            urlParams.controller = 'vendor'
            urlParams.action = 'link'
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
                if (linkInstanceType in [ProviderLink.class.name, VendorLink.class.name]) {
                    refdataValues << RDStore.PROVIDER_LINK_FOLLOWS
                }
                else {
                    refdataValues.addAll(RefdataCategory.getAllRefdataValues(RDConstants.LINK_TYPE)-RDStore.LINKTYPE_LICENSE)
                }
                refdataValues.each { RefdataValue rv ->
                    boolean isSimpleLinkType = (rv.id == RDStore.LINKTYPE_SIMPLE.id) // forced: bidirectional
                    String[] linkArray = rv.getI10n("value").split("\\|")
                    if (isSimpleLinkType) { linkArray = [linkArray[0]] }

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
                    else if(linkInstanceType in [ProviderLink.class.name, VendorLink.class.name]) {
                        if(link && link.type == rv) {
                            if(context == link.from)
                                perspIndex = 0
                            else if(context == link.to)
                                perspIndex = 1
                            else perspIndex = 0
                            linkType = "${genericOIDService.getOID(rv)}§${perspIndex}"
                        }
                    }
                    else {
                        if(link && link.linkType == rv) {
                            if (context in [link.sourceSubscription,link.sourceLicense]) {
                                perspIndex = 0
                            }
                            else if(context in [link.destinationSubscription,link.destinationLicense]) {
                                perspIndex = 1
                            }
                            else {
                                perspIndex = 0
                            }
                            if (isSimpleLinkType) { perspIndex = 0 }

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
            <g:if test="${linkInstanceType in [Provider.class.name, Vendor.class.name]}">
                <g:set var="pair" value="${context == link.from ? link.to : link.from}"/>
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
            <g:if test="${linkInstanceType == Links.class.name}">
                <g:set var="linkComment" value="linkComment_new"/>
            </g:if>
        </g:else>
        <div class="field">
            <div id="sub_role_tab_${tmplModalID}" class="ui grid">
                <g:if test="${subscriptionLicenseLink}">
                    <g:hiddenField name="${selectLink}" value="${genericOIDService.getOID(RDStore.LINKTYPE_LICENSE)}§${1}"/>
                </g:if>
                <g:else>
                    <g:if test="${linkInstanceType == Links.class.name && !link}">
                        <div class="row">
                            <div class="four wide column">
                                <g:message code="default.linking.provider.label" args="[objectPlural]"/>
                            </div>
                            <div class="twelve wide column">
                                <div class="ui search selection dropdown la-full-width" id="providerFilter">
                                    <input type="hidden" name="providerFilter"/>
                                    <i class="dropdown icon"></i>
                                    <input type="text" class="search"/>
                                    <div class="default text"></div>
                                </div>
                            </div>
                        </div>
                    </g:if>
                    <div class="row">
                        <div class="four wide column">
                            ${thisString}
                        </div>
                        <div class="twelve wide column">
                            <g:select class="ui dropdown clearable select la-full-width" name="${selectLink}" id="${selectLink}" from="${linkTypes}" optionKey="${{it.key}}"
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
                            <input type="hidden" name="${selectPair}" value="${linkInstanceType in [Combo.class.name, ProviderLink.class.name, VendorLink.class.name] ? pair : genericOIDService.getOID(pair)}"/>
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
                <g:if test="${linkInstanceType == Links.class.name}">
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
        function initPairDropdown(selProv) {
            let providerFilter = '';
            let adminLinking = '';
            <g:if test="${context.class.name in [Provider.class.name, Vendor.class.name]}">
                adminLinking = '&adminLinking=true'
            </g:if>
            let minChars = 1;
            if(typeof(selProv) !== 'undefined' && selProv.length > 0) {
                providerFilter = '&providerFilter='+selProv;
                minChars = 0;
            }
            $("#${selectPair}").dropdown({
                apiSettings: {
                    url: "<g:createLink controller="ajaxJson" action="${lookupName}"/>?status=FETCH_ALL&query={query}&filterMembers=${atConsortialParent}&ctx=${genericOIDService.getOID(context)}"+providerFilter+adminLinking,
                    cache: false
                },
                clearable: true,
                minCharacters: minChars
            });
        }
        <g:if test="${!subscriptionLicenseLink}">
            <%
                Provider firstProvider
                if (context instanceof Subscription || context instanceof License) {
                    firstProvider = ProviderRole.findBySubscriptionOrLicense(context, context)?.provider
                }
            %>
            $("#providerFilter").dropdown({
                apiSettings: {
                    url: "<g:createLink controller="ajaxJson" action="lookupProviders"/>?query={query}",
                    cache: false
                },
                clearable: true,
                minCharacters: 1
            });
            <g:if test="${firstProvider}">
                let providerOID = "${genericOIDService.getOID(firstProvider)}";
                let providerText = "${firstProvider.name}";
                $("#providerFilter").dropdown('set value', providerOID).dropdown('set text', providerText);
                initPairDropdown(providerOID);
            </g:if>
            <g:else>
                initPairDropdown();
            </g:else>
            $("#providerFilter").change(function() {
                let selProv = $("#providerFilter").dropdown('get value');
                initPairDropdown(selProv);
            });
        </g:if>
        <g:else>
            initPairDropdown();
        </g:else>
    </laser:script>
</g:if>