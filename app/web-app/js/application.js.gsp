
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
            top: 90,
            zIndex: 1
        });

        $('.modal .table').floatThead('destroy');
        $('.table.ignore-floatThead').floatThead('destroy');

        // modals
        $("*[data-semui='modal']").click(function() {
            $($(this).attr('href') + '.ui.modal').modal({
                onVisible: function() {
                    $(this).find('.datepicker').calendar(r2d2.configs.datepicker);

                },
                detachable: true,
                autofocus: false,
                closable: false,
                transition: 'scale',
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
        $.fn.editableform.template =
            '<form class="ui form form-inline editableform">' +
            '	<div class="control-group">' +
            '		<div class="ui calendar xEditable-datepicker">' +
            '			<div class="ui input right icon editable-input">' +
            '			</div>' +
            '			<div class="editable-buttons">' +
            '			</div>' +
            '		</div>' +
            '		<div class="editable-error-block">' +
            '		</div>' +
            '	</div>' +
            '</form>'
        $.fn.editableform.loading =
            '<div class="ui active inline loader"></div>'

        // TODO $.fn.datepicker.defaults.language = gspLocale
    },

    initDynamicXEditableStuff : function(ctxSel) {
        console.log("r2d2.initDynamicXEditableStuff( " + ctxSel + " )");

        if (! ctxSel) {
            ctxSel = 'body'
        }

        // DEPRECATED ?? never used
        $(ctxSel + ' .xEditable').editable({
            language: gspLocale,
            format:   gspDateFormat,
            validate: function(value) {
                // custom validate functions via semui:xEditable validation="xy"
                if ('notEmpty' == $(this).attr('data-validation')) {
                    if($.trim(value) == '') {
                        return "Das Feld darf nicht leer sein";
                    }
                }
            },
            error: function (xhr, status, error) {
                alert(xhr.status + ": " + xhr.statusText);
            }
        });

        $(ctxSel + ' .xEditableValue').editable({
            language: gspLocale,
            format:   gspDateFormat,
            validate: function(value) {
                if ($(this).attr('data-format') && value) {
                    if(! (value.match(/^\d{1,2}\.\d{1,2}\.\d{4}$/) || value.match(/^\d{4}-\d{1,2}-\d{1,2}$/)) ) {
                        return "Ungültiges Format";
                    }
                }
                // custom validate functions via semui:xEditable validation="xy"
                if ('notEmpty' == $(this).attr('data-validation')) {
                    if($.trim(value) == '') {
                        return "Das Feld darf nicht leer sein";
                    }
                }
            },
            success: function(response) {
                // override newValue with response from backend
                return {newValue: (response != 'null' ? response : null)}
            },
            error: function (xhr, status, error) {
                alert(xhr.status + ": " + xhr.statusText);
            }
        }).on('save', function(e, params){
            if ($(this).attr('data-format')) {
                console.log(params)
            }
        }).on('shown', function() {
            if ($(this).attr('data-format')) {
                $(ctxSel + ' .xEditable-datepicker').calendar(r2d2.configs.datepicker);
            }
            $(".table").trigger('reflow')
        }).on('hidden', function() {
            $(".table").trigger('reflow')
        });

        $(ctxSel + ' .xEditableDatepicker').editable({
        });

        $(ctxSel + ' .xEditableManyToOne').editable({
            tpl: '<select class="ui dropdown"></select>'
        }).on('shown', function() {
            $(".table").trigger('reflow')
        }).on('hidden', function() {
            $(".table").trigger('reflow')
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

        // selectable table to avoid button is showing when focus after modal closed
        $(ctxSel + ' .la-selectable').hover(function() {
            $( ".button" ).blur();
        });

        // close semui:messages alerts
        $(ctxSel + ' .close.icon').click(function() {
            $(this).parent().hide();
            $(".table").trigger('reflow');
        });

        // accordions
        $(ctxSel + ' .ui.accordion').accordion({
            onOpening: function() {
                $(".table").trigger('reflow')
            },
            onOpen: function() {
                $(".table").trigger('reflow')
            }
        });

        // checkboxes
        $(ctxSel + ' .ui.checkbox').not('#la-advanced').checkbox();

        // datepicker
        $(ctxSel + ' .datepicker').calendar(r2d2.configs.datepicker);

        // dropdowns
        $(ctxSel + ' .ui.dropdown').dropdown({
            duration: 150,
            transition: 'fade',
            //showOnFocus: false
        });
        $(ctxSel + ' .ui.search.dropdown').dropdown({
            fullTextSearch: 'exact'
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

        //
        $(ctxSel + ' .js-click-control').click(function(e) {

            var lastClicked = $(this).data("lastClicked");

            if ( lastClicked ) {
                if ((e.timeStamp - lastClicked) < 2000) {
                    e.preventDefault();
                }
            }
            $(this).data("lastClicked", e.timeStamp);

        });
        // confirmation modal
        var buildConfirmationModal =
            function(event,that){
                var dataAttr = that.getAttribute("data-confirm-id")? that.getAttribute("data-confirm-id")+'_form':false;
                var what = that.getAttribute("data-confirm-term-what")? that.getAttribute("data-confirm-term-what"):"dieses Element";
                var whatDetail = that.getAttribute("data-confirm-term-what-detail")? that.getAttribute("data-confirm-term-what-detail"):false;
                var where = that.getAttribute("data-confirm-term-where")? that.getAttribute("data-confirm-term-where"):false;
                var whereDetail = that.getAttribute("data-confirm-term-where-detail")? that.getAttribute("data-confirm-term-where-detail"):false;
                var how = that.getAttribute("data-confirm-term-how") ? that.getAttribute("data-confirm-term-how"):"delete";
                var messageHow = how == "delete" ? "löschen" :"aufheben";
                var url = that.getAttribute('href') && (that.getAttribute('class') != 'js-gost') ? that.getAttribute('href'): false; // use url only if not remote link

                event.preventDefault();

                // UNLINK BUTTON
                if (how == "unlink"){
                    switch (what) {
                        case "organisationtype":
                            messageWhat = "die Verknüpfung des Organisationstyps";
                            break;
                        case "contact":
                            messageWhat = "die Verknüpfung des Kontakts";
                            break;
                        case "membershipSubscription" :
                            messageWhat = "die Teilnahme der";
                            break;
                        default:
                            messageWhat = "die Verknüpfung des Objektes";
                    }
                    switch (where) {
                        case "organisation":
                            messageWhere = "mit der Organisation";
                            break;
                        default:
                            messageWhere = where;
                    }
                }
                // DELETE BUTTON
                if (how == "delete"){
                    switch (what) {
                        case "organisationtype":
                            messageWhat = "den Organisationstyp";
                            break;
                        case "task":
                            messageWhat = "die Aufgabe";
                            break;
                        case "person":
                            messageWhat = "die Person";
                            break;
                        case "contactItems":
                            messageWhat = "die Kontaktdaten";
                            break;
                        case "contact":
                            messageWhat = "den Kontakt";
                            break;
                        case "address":
                            messageWhat = "die Adresse";
                            break;
                        case "subscription":
                            messageWhat = "die Lizenz";
                            break;
                        case "license":
                            messageWhat = "den Vertrag";
                            break;
                        case "property":
                            messageWhat = "das Merkmal";
                            break;
                        case "function":
                            messageWhat = "die Funktion";
                            break;
                        case "user":
                            messageWhat = "den Benutzer";
                            break;
                        default:
                            messageWhat = what;
                    }
                    switch (where) {
                        case "organisation":
                            messageWhere = "aus der Organisation";
                            break;
                        case "addressbook":
                            messageWhere = "aus dem Adressbuch";
                            break;
                        case "system":
                            messageWhere = "aus dem System";
                            break;
                        default:
                            messageWhere = "aus dem System";
                    }
                }
                $('#js-confirmation-term-what').text(messageWhat); // Should be always set - otherwise "dieses Element"
                whatDetail ? $('#js-confirmation-term-what-detail').text(whatDetail) : $("#js-confirmation-term-what-detail").remove();
                whereDetail ? $('#js-confirmation-term-where-detail').text(whereDetail) : $("#js-confirmation-term-where-detail").remove();
                where ? $('#js-confirmation-term-where').text(messageWhere) : $("#js-confirmation-term-where").remove();
                $('#js-confirmation-term-how').text(messageHow); // Should be always set - otherwise "delete"
                switch (how) {
                    case "delete":
                        $('#js-confirmation-button').html('Löschen<i class="trash alternate icon"></i>');
                        break;
                    case "unlink":
                        $('#js-confirmation-button').html('Aufheben<i class="chain broken icon"></i>');
                        break;
                    default:
                        $('').html('Entfernen<i class="x icon"></i>');
                }

                $('.tiny.modal')
                    .modal({
                        closable  : false,
                        onApprove : function() {
                            // open confirmation modal from inside a form
                            if (dataAttr){
                                $('[data-confirm-id='+dataAttr+']').submit();
                            }
                            // open confirmation modal and open a new url after conirmation
                            if (url){
                                window.location.href = url;
                            }
                        },
                    })
                    .modal('show')
                ;
            }
        // for links and submit buttons
        $(ctxSel + ' .js-open-confirm-modal').click(function(e) {
            e.preventDefault();
            buildConfirmationModal(event,this);
        });
        // for remote links = ajax calls
        $(ctxSel + ' .js-open-confirm-modal-copycat').click(function(e) {
            var onclickString = $(this).next('.js-gost').attr("onclick");
            $('#js-confirmation-button').attr("onclick", onclickString);
            var gostObject = $(this).next('.js-gost');
            buildConfirmationModal(event,gostObject[0] );
        });
    }
}

$(document).ready(function() {
    r2d2.go()
})

