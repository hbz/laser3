<laser:script file="${this.getGroovyPageFileName()}">
    $('.triggerClickMeExport').on('click', function(e) {
        e.preventDefault();

        $.ajax({
            url: $(this).attr('href')
        }).done( function (data) {
            $('.ui.dimmer.modals > #exportClickMeModal').remove();
            $('#dynamicModalContainer').empty().html(data);

            $('#dynamicModalContainer .ui.modal').modal({
               onShow: function () {
                    r2d2.initDynamicUiStuff('#exportClickMeModal');
                    r2d2.initDynamicXEditableStuff('#exportClickMeModal');
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

        $('#exportClickMeModal form').submit(function () {
        $("#tab").val($('div.tab.active').attr('data-tab'));
    });
</laser:script>