<%@ page import="de.laser.ExportClickMeService" %>
<laser:script file="${this.getGroovyPageFileName()}">
    $('.csv').hide();

    $('.action .icon.button').click(function () {
        $(this).parent('.action').find('input:file').click();
    });

    $('input:file', '.ui.action.input').on('change', function (e) {
        var name = e.target.files[0].name;
        $('input:text', $(e.target).parent()).val(name);
    });

    $('.formatSelection').on('change', function() {
        if($(this).val() === '${ExportClickMeService.FORMAT.XLS}') {
            $('.xls').show();
            $('.csv').hide();
        }
        else if($(this).val() === '${ExportClickMeService.FORMAT.CSV}') {
            $('.csv').show();
            $('.xls').hide();
        }
    });
</laser:script>