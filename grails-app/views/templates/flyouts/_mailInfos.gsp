
<div id="mailInfosFlyout" class="ui ten wide flyout">

</div>

<laser:script file="${this.getGroovyPageFileName()}">

  $('a.mailInfos-flyout-trigger').on ('click', function(e) {
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
               url: "<g:createLink controller="ajaxHtml" action="mailInfosFlyout"/>",
                 data: data
                }).done (function (response) {
                    $('#mailInfosFlyout').html (response)
                    $('#mailInfosFlyout').flyout('show')
                    $('#globalLoadingIndicator').hide()

                    r2d2.initDynamicUiStuff ('#mailInfosFlyout')
                    r2d2.initDynamicXEditableStuff ('#mailInfosFlyout')
                })
        });

</laser:script>