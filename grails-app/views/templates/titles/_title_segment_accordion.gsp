<%@ page import="de.laser.convenience.Marker; de.laser.ui.Btn; de.laser.ui.Icon" %>
<laser:serviceInjection/>

<div class="ui fluid segment title" data-ajaxTippId="${tipp.id}" data-ajaxIeId="${ie ? ie.id : null}">
    <div class="ui stackable equal width grid">

        <g:if test="${(ie && ie.perpetualAccessBySub) || permanentTitle}">
            <g:if test="${ie && ie.perpetualAccessBySub && ie.perpetualAccessBySub != subscription}">
                <g:link controller="subscription" action="index" id="${ieperpetualAccessBySub.id}">
                    <span class="ui mini left corner label la-perpetualAccess la-js-notOpenAccordion la-popup-tooltip"
                          data-content="${message(code: 'subscription.start.with')} ${ie.perpetualAccessBySub.dropdownNamingConvention()}"
                          data-position="left center" data-variation="tiny">
                        <i class="star blue icon"></i>
                    </span>
                </g:link>
            </g:if>
            <g:elseif test="${permanentTitle}">
                <span class="ui mini left corner label la-perpetualAccess la-popup-tooltip"
                      data-content="${message(code: 'renewEntitlementsWithSurvey.ie.participantPerpetualAccessToTitle')} ${permanentTitle.getPermanentTitleInfo()}"
                      data-position="left center" data-variation="tiny">
                    <i class="star icon"></i>
                </span>
            </g:elseif>
            <g:else>
                <span class="ui mini left corner label la-perpetualAccess la-js-notOpenAccordion la-popup-tooltip"
                      data-content="${message(code: 'renewEntitlementsWithSurvey.ie.participantPerpetualAccessToTitle')}"
                      data-position="left center" data-variation="tiny">
                    <i class="star icon"></i>
                </span>
            </g:else>
        </g:if>


        <div class="one wide column">
            ${counter++}
        </div>

        <div class="column">
            <div class="ui list">

                <!-- START TEMPLATE -->
                <laser:render template="/templates/titles/title_short_accordion"
                              model="${[ie         : null, tipp: tipp,
                                        showPackage: showPackage, showPlattform: showPlattform, showEmptyFields: false]}"/>
                <!-- END TEMPLATE -->

            </div>
        </div>

        <div class="column">
            <laser:render template="/templates/tipps/coverages_accordion" model="${[ie: null, tipp: tipp, overwriteEditable: false]}"/>
        </div>

        <div class="three wide column">
            <!-- START TEMPLATE -->
            <laser:render template="/templates/identifier" model="${[ie: null, tipp: tipp]}"/>
            <!-- END TEMPLATE -->
        </div>

        <div class="two wide column">
            <g:each in="${tipp.priceItems}" var="priceItem" status="i">
                <g:if test="${priceItem.listCurrency}">
                    <div class="ui list">
                        <div class="item">
                            <div class="contet">
                                <div class="header"><g:message code="tipp.price.listPrice"/></div>

                                <div class="content"><g:formatNumber number="${priceItem.listPrice}" type="currency"
                                                                     currencyCode="${priceItem.listCurrency.value}"
                                                                     currencySymbol="${priceItem.listCurrency.value}"/>
                                </div>
                            </div>
                        </div>
                    </div>
                </g:if>
            </g:each>
        </div>

        <div class="one wide column">
            <div class="ui right aligned">
%{--                <g:if test="${!isPublic_gascoDetails && tipp.isMarked(contextService.getUser(), Marker.TYPE.TIPP_CHANGES)}">--}%
%{--                    <ui:cbItemMarkerAction tipp="${tipp}" type="${Marker.TYPE.TIPP_CHANGES}" simple="true"/>--}%
%{--                </g:if>--}%
                <div class="${Btn.MODERN.SIMPLE}">
                    <i class="${Icon.CMD.SHOW_MORE}"></i>
                </div>
            </div>
        </div>

        <g:if test="${showPackageLinking && editable}">
            <div class="two wide column">
                <a id="linkTitleToSubscription_${tipp.gokbId}" href="${createLink(action: 'linkTitleModal', controller: 'ajaxHtml', params: [tippID: tipp.gokbId, headerToken: "subscription.details.linkTitle.heading.title"])}" class="ui icon button"><g:message code="subscription.details.linkTitle.label.title"/></a>
            </div>

            <laser:script file="${this.getGroovyPageFileName()}">
                $('#linkTitleToSubscription_${tipp.gokbId}').on('click', function(e) {
                    e.preventDefault();

                    $.ajax({
                        url: $(this).attr('href')
                    }).done( function (data) {
                        $('.ui.dimmer.modals > #linkTitleModal').remove();
                        $('#dynamicModalContainer').empty().html(data);

                        $('#dynamicModalContainer .ui.modal').modal({
                           onShow: function () {
                                r2d2.initDynamicUiStuff('#linkTitleModal');
                                r2d2.initDynamicXEditableStuff('#linkTitleModal');
                                $("html").css("cursor", "auto");
                            },
                            detachable: true,
                            autofocus: false,
                            closable: false,
                            transition: 'scale',
                            onApprove : function() {
                                $(this).find('.ui.form').submit();
                                return false;
                            }
                        }).modal('show');
                    })
                });
            </laser:script>
        </g:if>
    </div>
</div>

