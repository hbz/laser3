<%@ page import="de.laser.storage.RDStore" %>
<%--
  Created by IntelliJ IDEA.
  User: galffy
  Date: 25.03.2025
  Time: 09:05
--%>
<laser:serviceInjection/>
<ui:modal id="linkTitleModal" message="subscription.details.linkTitle.label" msgSave="${message(code: 'default.button.link.label')}">

    <g:form class="ui form" id="linkTitleForm" url="[controller: 'subscription', action: 'processLinkTitle']">
        <input type="hidden" name="tippID" value="${tipp.id}"/>
        <div class="field">
            <label>${message(code: 'tipp.label')}</label>
            ${tipp.name}
        </div>
        <div class="field">
            <label>${message(code: 'package.label')}</label>
            ${tipp.pkg.name}
        </div>
        <g:if test="${fixedSubscription}">
            <input type="hidden" name="subscription" value="${fixedSubscription.id}"/>
        </g:if>
        <g:else>
            <div class="field">
                <label>${message(code: 'provider.label')}</label>
                <div class="ui search selection dropdown la-full-width" id="providerFilter">
                    <input type="hidden" name="providerFilter"/>
                    <i class="dropdown icon"></i>
                    <input type="text" class="search"/>
                    <div class="default text"></div>
                </div>
            </div>
            <div class="field">
                <label>${message(code: 'subscription')}</label>
                <div class="ui search selection dropdown la-full-width" id="subscription">
                    <input type="hidden" name="subscription"/>
                    <i class="dropdown icon"></i>
                    <input type="text" class="search"/>
                    <div class="default text"></div>
                </div>
            </div>
            <laser:script file="${this.getGroovyPageFileName()}">
                function initSubscriptionDropdown(selProv) {
                    let providerFilter = '';
                    let minChars = 1;
                    if(typeof(selProv) !== 'undefined' && selProv.length > 0) {
                        providerFilter = '&providerFilter='+selProv;
                        minChars = 0;
                    }
                    $("#subscription").dropdown({
                        apiSettings: {
                            url: "<g:createLink controller="ajaxJson" action="lookupSubscriptions"/>?status=FETCH_ALL&query={query}&holdingSelection=notEntire&titleToLink=${tipp.id}&restrictLevel=true"+providerFilter,
                        cache: false
                    },
                    clearable: true,
                    minCharacters: minChars
                });
            }
            $("#providerFilter").dropdown({
                apiSettings: {
                    url: "<g:createLink controller="ajaxJson" action="lookupProviders"/>?query={query}",
                    cache: false
                },
                clearable: true,
                minCharacters: 1
            });
            $("#providerFilter").change(function() {
                let selProv = $("#providerFilter").dropdown('get value');
                initSubscriptionDropdown(selProv);
            });
            initSubscriptionDropdown();
            </laser:script>
        </g:else>
        <g:if test="${isConsortium}">
            <div class="field">
                <label><i data-content="${message(code:'consortium.member.plural')}" data-position="top center" class="users icon la-popup-tooltip"></i> <g:message code="subscription.details.linkTitle.label"/></label>
                <div class="ui linkToChildren checkbox toggle">
                    <g:checkBox name="linkToChildren"/>
                </div>
            </div>
        </g:if>
    </g:form>
</ui:modal>