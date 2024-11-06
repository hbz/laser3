
<div id="infoFlyout" class="ui ten wide flyout"></div>

<laser:script file="${this.getGroovyPageFileName()}">

  $('a.infoFlyout-trigger').on ('click', function(e) {
       e.preventDefault()
          let cell = $(this);
          let data = {
              id: cell.attr("data-orgId"),
              subscription: cell.attr("data-subId"),
              surveyConfigID: cell.attr("data-surveyConfigId"),
              newLanguage: cell.attr("data-lang"),
           };

       $('#globalLoadingIndicator').show()
           $.ajax ({
               url: "<g:createLink controller="ajaxHtml" action="infoFlyout"/>",
                 data: data
                }).done (function (response) {
                    $('#infoFlyout').html (response)
                    $('#infoFlyout').flyout('show')
                    $('#globalLoadingIndicator').hide()

                    r2d2.initDynamicUiStuff ('#infoFlyout')
                    r2d2.initDynamicXEditableStuff ('#infoFlyout')
                })
        });

</laser:script>