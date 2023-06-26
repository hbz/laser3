<div class="ui fluid segment title" data-ajaxTippId="${tipp.id}" data-ajaxIeId="${ie ? ie.id : null}">
    <div class="ui stackable equal width grid">

        <g:if test="${perpetualAccessBySub || permanentTitle}">
            <g:if test="${perpetualAccessBySub && perpetualAccessBySub != subscription}">
                <g:link controller="subscription" action="index" id="${perpetualAccessBySub.id}">
                    <span class="ui mini left corner label la-perpetualAccess la-js-notOpenAccordion la-popup-tooltip la-delay"
                          data-content="${message(code: 'subscription.start.with')} ${perpetualAccessBySub.dropdownNamingConvention()}"
                          data-position="left center" data-variation="tiny">
                        <i class="star blue icon"></i>
                    </span>
                </g:link>
            </g:if>
            <g:else>
                <span class="ui mini left corner label la-perpetualAccess la-js-notOpenAccordion la-popup-tooltip la-delay"
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
                <laser:render template="/templates/title_short_accordion"
                              model="${[ie         : null, tipp: tipp,
                                        showPackage: showPackage, showPlattform: showPlattform, showEmptyFields: false]}"/>
                <!-- END TEMPLATE -->

            </div>
        </div>

        <div class="column">
            <laser:render template="/templates/tipps/coverages_accordion"
                          model="${[ie: null, tipp: tipp, overwriteEditable: false]}"/>
        </div>

        <div class="four wide column">

            <!-- START TEMPLATE -->
            <laser:render template="/templates/identifier"
                          model="${[ie: null, tipp: tipp]}"/>
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
            <div class="ui right floated buttons">
                <div class="right aligned wide column">

                </div>

                <div class="ui icon blue button la-modern-button "><i
                        class="ui angle double down icon"></i>
                </div>
            </div>
        </div>
    </div>
</div>

