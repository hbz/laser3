
<div id="infoFlyout" class="ui very wide flyout"></div>

<laser:script file="${this.getGroovyPageFileName()}">
    $('.infoFlyout-trigger').on ('click', function(e) {
        e.preventDefault();
        $('#globalLoadingIndicator').show();

        let cell = $(this);
        let data = {
            template: cell.attr('data-template')
        }

        switch (data.template) {
            case 'org':
                data.id_org          = cell.attr('data-org')
                data.id_subscription = cell.attr('data-sub')
                data.id_surveyConfig = cell.attr('data-surveyConfig')
                break;
            case 'reportTitleToProvider':
                data.id_tipp         = cell.attr('data-tipp')
                break;
            case 'contactStats':
                data.id_platform     = cell.attr('data-platform')
                break;
        }

        $.ajax ({
            url: "<g:createLink controller="info" action="flyout"/>",
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