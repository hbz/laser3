<laser:script file="${this.getGroovyPageFileName()}">
    $('.triggerClickMeExport').on('click', function(e) {
        e.preventDefault();

        $.ajax({
            url: $(this).attr('href')
        }).done( function (data) {
            $('.ui.dimmer.modals > #exportClickMeModal').remove();
            $('#dynamicModalContainer').empty().html(data);

            $('#dynamicModalContainer .ui.modal').modal({
               detachable: true,
               closable: false,
               onShow: function () {
                    r2d2.initDynamicUiStuff('#exportClickMeModal');
                    r2d2.initDynamicXEditableStuff('#exportClickMeModal');
                    $("html").css("cursor", "auto");
                    keyboardHandler = function (e) {
                        if (e.keyCode === 27) {
                            $(this).modal('hide');
                        }
                    };
                    this.addEventListener('keyup', keyboardHandler);
               },

                onHide : function() {
                    this.removeEventListener('keyup', keyboardHandler);
                },

                onApprove : function() {
                    $(this).find('#exportClickMeModal .ui.form').submit();
                    return false;
                }
            }).modal('show');
        })
    });

        $('#exportClickMeModal form').submit(function () {
        $("#tab").val($('div.tab.active').attr('data-tab'));
    });
</laser:script>