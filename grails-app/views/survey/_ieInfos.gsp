<%@ page import="de.laser.RefdataValue; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataCategory" %>
<laser:serviceInjection/>

<div class="ui card">
    <div class="content">
            <div class="ui accordion la-accordion-showMore js-ie-info-accordion">
                <div class="item">
                    <div class="title">
                       <button
                                class="ui button icon blue la-modern-button la-delay right floated ">
                            <i class="ui angle double down large icon"></i>
                        </button>
                        <laser:script file="${this.getGroovyPageFileName()}">
                            $('.js-ie-info-accordion')
                              .accordion({
                                onOpen: function() {
                                  $(this).siblings('.title').children('.button').attr('data-content','<g:message code="surveyConfigsInfo.ieInfo.hide"/> ')
                                    },
                                    onClose: function() {
                                      $(this).siblings('.title').children('.button').attr('data-content','<g:message code="surveyConfigsInfo.ieInfo.show"/> ')
                                    }
                                  })
                                ;
                        </laser:script>

                        <i aria-hidden="true" class="circular la-journal icon"></i>

                        <h2 class="ui icon header la-clear-before la-noMargin-top">
                            <g:link controller="subscription" action="index" target="_blank"
                                    id="${subscription.id}"><g:message code="surveyConfigsInfo.ieInfo.show"/></g:link>
                        </h2>
                    </div>
                    <div class="content">
                        <div class="ui grid">
                            <div class="sixteen wide column">
                                <div class="la-inline-lists">
                                    <div class="item">
                                        <div class="content">
                                            <dl>
                                                <dt class="control-label">${message(code: 'myinst.selectPackages.pkg_titles')}</dt>
                                                <dd>${subscriptionService.countIssueEntitlementsFixed(subscription)}</dd>
                                            </dl>
                                        </div>
                                    </div>

                                    <g:link controller="subscription" action="index" target="_blank"
                                            id="${subscription.id}" class="ui button">
                                        <g:message code="renewEntitlementsWithSurvey.currentTitles.button"/></g:link>
                                </div>
                            </div><!-- .twelve -->
                        </div><!-- .grid -->
                    </div>
                </div>
            </div>
    </div><!-- .content -->
</div>

<laser:script file="${this.getGroovyPageFileName()}">
    $('.js-ie-info-accordion')
      .accordion({
        onOpen: function() {
          $(this).siblings('.title').children('.button').attr('data-content','<g:message code="surveyConfigsInfo.ieInfo.hide"/> ')
                                    },
                                    onClose: function() {
                                      $(this).siblings('.title').children('.button').attr('data-content','<g:message code="surveyConfigsInfo.ieInfo.show"/> ')
                                    }
                                  })
                                ;
</laser:script>