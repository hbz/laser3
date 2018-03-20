
r2d2 = {

    configs : {

        datepicker : {
            type: 'date',
            firstDayOfWeek: 1,
            monthFirst: false,
            formatter: {
                date: function (date, settings) {
                    if (!date) return '';
                    var day = date.getDate();
                    if (day<10) day="0"+day;
                    var month = date.getMonth() + 1;
                    if (month<10) month="0"+month;
                    var year = date.getFullYear();

                    if ('dd.mm.yyyy' == gspDateFormat) {
                        console.log('dd.mm.yyyy');
                        return day + '.' + month + '.' + year;
                    }
                    else if ('yyyy-mm-dd' == gspDateFormat) {
                        console.log('yyyy-mm-dd');
                        return year + '-' + month + '-' + day;
                    }
                    else {
                        // TODO
                        alert('Please report this error: ' + gspDateFormat + ' for semui-datepicker unsupported')
                    }
                }
            }
        }
    },

    go : function() {

        r2d2.legacyStuff()
        r2d2.semuiStuff()

        console.log("r2d2 / locale: " + gspLocale + " > " + gspDateFormat)
    },

    legacyStuff : function() {
        console.log("r2d2.legacyStuff()")

        $.fn.editable.defaults.mode = 'inline'
        $.fn.editableform.buttons = '<button type="submit" class="ui icon button editable-submit"><i class="check icon"></i></button>' +
            '<button type="button" class="ui icon button editable-cancel"><i class="times icon"></i></button>'
        $.fn.editableform.template = '<form class="ui form form-inline editableform"><div class="control-group"><div><div class="editable-input"></div>' +
            '<div class="editable-buttons"></div></div><div class="editable-error-block"></div></div></form>'

        // TODO $.fn.datepicker.defaults.language = gspLocale

        $('.xEditable').editable({
            language: gspLocale, /*
            datepicker: {
                language: gspLocale
            }, */
            format: gspDateFormat
        });

        $('.xEditableValue').editable({
            language: gspLocale, /*
            datepicker: {
                language: gspLocale
            }, */
            format: gspDateFormat
        });

        $(".xEditableManyToOne").editable();

        $(".simpleHiddenRefdata").editable({
            language: gspLocale, /*
            datepicker: {
                language: gspLocale
            }, */
            format: gspDateFormat,
            url: function(params) {
                var hidden_field_id = $(this).data('hidden-id');
                $("#" + hidden_field_id).val(params.value);
                // Element has a data-hidden-id which is the hidden form property that should be set to the appropriate value
            }
        });

        $(".simpleReferenceTypedown").select2({
            placeholder: "Search for...",
            minimumInputLength: 1,
            ajax: { // instead of writing the function to execute the request we use Select2's convenient helper
                url: "<g:createLink controller='ajax' action='lookup'/>",
                dataType: 'json',
                data: function (term, page) {
                    return {
                        format:'json',
                        q: term,
                        baseClass:$(this).data('domain')
                    };
                },
                results: function (data, page) {
                    return {results: data.values};
                }
            }
        });

        $('.dlpopover').popover({
            html:true,
            placement:'left',
            title:'${message(code: "spotlight.search")}',
            trigger:'click',
            template:'<div class="popover" style="width: 600px; top: 10%"><div></div><div class="popover-inner"><h3 class="popover-title"></h3><div class="popover-content"></div></div></div>',
            'max-width':600,
            content:function() {
                return r2d2.spotlightGetContent();
            }
        });
    },

    semuiStuff : function() {
        console.log("r2d2.semuiStuff()")

        // close semui:messages alerts
        $(".close.icon").click(function(){
            $(this).parent().hide();
        });

        // modal opener
        $("*[data-semui=modal]").click(function(){
            $($(this).attr('href') + '.ui.modal').modal({
                onVisible: function(){
                    $(this).find('.datepicker').calendar(r2d2.configs.datepicker);
                }
            }).modal('show')
        });

        // dropdowns
        $('.ui.dropdown').dropdown({
            duration: 150,
            transition: 'fade'
        })
        $('.ui.dropdown').on('keydown', function(event) {
            if(['Escape','Backspace','Delete'].includes(event.key)) {
                event.preventDefault()
                $(this).dropdown('clear').dropdown('hide')
            }
        })

        // accordions
        $('.ui.accordion').accordion()

        // checkboxes
        $('.ui.checkbox').not('#la-advanced').checkbox();

        // datepicker
        $('.datepicker').calendar(r2d2.configs.datepicker);

        // metaboxes
        $('.metaboxToggle').click(function(){
            $(this).next('.metaboxContent').slideToggle();
        })

        // sticky
        $('.ui.sticky').sticky({offset: 120});

        // sticky table header
        $('table').floatThead({
              position: 'fixed',
              top: 78,
              zIndex: 70
        });
    },

    spotlightGetContent : function() {
        return $.ajax({
            type: "GET",
            url: "<g:createLink controller='spotlight' action='index'/>",
            cache: false,
            async: false
        }).responseText;
    }
}

$(document).ready(function() {
    r2d2.go()
})

