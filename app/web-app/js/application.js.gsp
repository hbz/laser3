
r2d2 = {

    configs : {

        datepicker : {
            type: 'date',
            onChange: function(date, text, mode) {
                if (!text) {
                    $(this).removeClass("la-calendar-selected");
                } else {
                    if( ! $(this).hasClass("la-calendar-selected") ) {
                        $(this).addClass("la-calendar-selected")
                    }
                }
            },
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
                        alert('Please report this error: ' + gspDateFormat + ' for semui-datepicker unsupported');
                    }
                }
            }
        }
    },

    go : function() {

        r2d2.initGlobalSemuiStuff();
        r2d2.initGlobalXEditableStuff();

        r2d2.initDynamicSemuiStuff('body');
        r2d2.initDynamicXEditableStuff('body');

        console.log("r2d2 @ locale: " + gspLocale + " > " + gspDateFormat);
    },

    initGlobalSemuiStuff : function() {
        console.log("r2d2.initGlobalSemuiStuff()")

        // spotlight
        $('.ui.search').search({
            type: 'category',
            searchFields   : [
                'title'
            ],
            apiSettings: {
                onResponse: function(elasticResponse) {
                    var response = { results : {} };

                    // translate Elasticsearch API response to work with semantic ui search
                    $.each(elasticResponse.results, function(index, item) {

                        var category   = item.category || 'Unknown';
                        //var maxResults = 15;

                        //if (index >= maxResults) {
                        //    return false;
                        //}
                        // create new object category
                        if (response.results[category] === undefined) {
                            response.results[category] = {
                                name    : category,
                                results : []
                            };
                        }
                        // add result to category
                        response.results[category].results.push({
                            title       : item.title,
                            url         : item.url
                        });
                    });
                    return response;
                },
                url: "<g:createLink controller='spotlight' action='search'/>/?query={query}"
            },
            minCharacters: 3
        });
        $('#btn-search').on('click', function(e) {
            e.preventDefault();

            $('#spotlightSearch').animate({width: 'toggle'}).focus();
            $(this).toggleClass('open');
        });

        // metaboxes
        $('.metaboxToggle').click(function() {
            $(this).next('.metaboxContent').slideToggle();
        })

        // stickies
        $('.ui.sticky').sticky({offset: 120});

        // sticky table header
        $('.table').floatThead({
            position: 'fixed',
            top: 78,
            zIndex: 1
        });

        $('.modal .table').floatThead('destroy');
        $('.table.ignore-floatThead').floatThead('destroy');

        // modals
        $("*[data-semui=modal]").click(function() {
            $($(this).attr('href') + '.ui.modal').modal({
                onVisible: function() {
                    $(this).find('.datepicker').calendar(r2d2.configs.datepicker);
                },
                detachable: true,
                closable: true,
                transition: 'fade up',
                onApprove : function() {
                    $(this).find('.ui.form').submit();
                    return false;
                }
            }).modal('show')
        });
    },

    initGlobalXEditableStuff : function() {
        console.log("r2d2.initGlobalXEditableStuff()");

        $.fn.editable.defaults.mode = 'inline'
        $.fn.editableform.buttons = '<button type="submit" class="ui icon button editable-submit"><i class="check icon"></i></button>' +
            '<button type="button" class="ui icon button editable-cancel"><i class="times icon"></i></button>'
        $.fn.editableform.template = '<form class="ui form form-inline editableform"><div class="control-group">' +
            '                               <div>' +
            '                                   <div class="editable-input"></div>' +
            '                                   <div class="editable-buttons"></div>' +
            '                               </div>' +
            '                               <div class="editable-error-block"></div>' +
            '                           </div></form>'

        // TODO $.fn.datepicker.defaults.language = gspLocale
    },

    initDynamicXEditableStuff : function(ctxSel) {
        console.log("r2d2.initDynamicXEditableStuff( " + ctxSel + " )");
        if (! ctxSel) {
            ctxSel = 'body'
        }

        $(ctxSel + ' .xEditable').editable({
            language: gspLocale,
            format:   gspDateFormat
        });

        $(ctxSel + ' .xEditableValue').editable({
            language: gspLocale,
            format:   gspDateFormat,
            validate: function(value) {
                if ($(this).attr('data-format')) {
                    //console.log( 'todo: frontend validation')
                }
            },
            success: function(response) {
                // override newValue with response from backend
                return {newValue: (response != 'null' ? response : null)}
            }
        }).on('save', function(e, params){
            if ($(this).attr('data-format')) {
                console.log(params)
            }
        });

        $(ctxSel + ' .xEditableDatepicker').editable({
        });

        $(ctxSel + ' .xEditableManyToOne').editable({
            tpl: '<select class="ui dropdown"></select>'
        });

        $(ctxSel + ' .simpleHiddenRefdata').editable({
            language: gspLocale,
            format:   gspDateFormat,
            url: function(params) {
                var hidden_field_id = $(this).data('hidden-id');
                $("#" + hidden_field_id).val(params.value);
                // Element has a data-hidden-id which is the hidden form property that should be set to the appropriate value
            }
        });

        $(ctxSel + ' .simpleReferenceTypedown').select2({
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
    },

    initDynamicSemuiStuff : function(ctxSel) {
        console.log("r2d2.initDynamicSemuiStuff( " + ctxSel + " )")
        if (! ctxSel) {
            ctxSel = 'body'
        }

        // close semui:messages alerts
        $(ctxSel + ' .close.icon').click(function() {
            $(this).parent().hide();
        });

        // accordions
        $(ctxSel + ' .ui.accordion').accordion();

        // checkboxes
        $(ctxSel + ' .ui.checkbox').not('#la-advanced').checkbox();

        // datepicker
        $(ctxSel + ' .datepicker').calendar(r2d2.configs.datepicker);

        // dropdowns
        $(ctxSel + ' .ui.dropdown').dropdown({
            duration: 150,
            transition: 'fade'
        });

        // dropdowns escape
        $(ctxSel + ' .la-filter .ui.dropdown').on('keydown', function(e) {
            if(['Escape','Backspace','Delete'].includes(event.key)) {
                e.preventDefault();
                $(this).dropdown('clear').dropdown('hide').removeClass("la-filter-dropdown-selected");
            }
        });

        // SEM UI DROPDOWN CHANGE
        $(ctxSel + ' .la-filter .ui.dropdown').change(function() {
            ($(this).hasClass("default")) ? $(this).removeClass("la-filter-dropdown-selected") : $(this).addClass("la-filter-dropdown-selected");
        });

        // for default selected Dropdown value
        var currentDropdown = $(ctxSel + ' .la-filter .ui.dropdown > select > option[selected=selected]').parents('.ui.dropdown');
        currentDropdown.find("div.text").hasClass("default")
            ?  currentDropdown.removeClass('la-filter-dropdown-selected')
            : currentDropdown.addClass('la-filter-dropdown-selected');



        // FILTER SELECT FUNCTION - INPUT LOADING
        $(ctxSel + ' .la-filter input[type=text]').each(function() {
            $(this).val().length === 0 ? $(this).removeClass("la-filter-selected") : $(this).addClass("la-filter-selected");
        });

        //  FILTER SELECT FUNCTION - INPUT CHANGE
        $(ctxSel + ' .la-filter input[type=text]').change(function() {
            $(this).val().length === 0 ? $(this).removeClass("la-filter-selected") : $(this).addClass("la-filter-selected");
        });
    }
}

$(document).ready(function() {
    r2d2.go()
})

