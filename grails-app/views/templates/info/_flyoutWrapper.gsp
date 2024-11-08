
<div id="infoFlyout" class="ui ten wide flyout"></div>

<laser:script file="${this.getGroovyPageFileName()}">
    $('a.infoFlyout-trigger').on ('click', function(e) {
        e.preventDefault();
        $('#globalLoadingIndicator').show();

        let cell = $(this);
        let data = {
            template: cell.attr('data-template')
        }

        if (data.template == 'org') {
            data.id_org          = cell.attr('data-org')
            data.id_subscription = cell.attr('data-sub')
            data.id_surveyConfig = cell.attr('data-surveyConfig')
        }

        $.ajax ({
            url: "<g:createLink controller="ajaxHtml" action="infoFlyout"/>",
            data: data
        }).done (function (response) {
            $('#infoFlyout').html (response)
            $('#infoFlyout').flyout('show')
            $('#globalLoadingIndicator').hide()

            r2d2.initDynamicUiStuff ('#infoFlyout')
            r2d2.initDynamicXEditableStuff ('#infoFlyout')
        });
    });

</laser:script>