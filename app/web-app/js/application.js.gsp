
r2d2 = {

    configs : {

        datepicker : {
            type: 'date',
            onChange: function(date, text, mode) {
                // deal with colored input field only when in filter context
                if ($(this).parents('.la-filter').length) {
                    if (!text) {
                        $(this).removeClass("la-calendar-selected");
                    } else {
                        if( ! $(this).hasClass("la-calendar-selected") ) {
                            $(this).addClass("la-calendar-selected")
                        }
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

        $("html").css("cursor", "auto");

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

/*        // sticky table header
        $('.table').floatThead({
            position: 'fixed',
            top: 90,
            zIndex: 1
        });*/

        $('.modal .table').floatThead('destroy');
        $('.table.ignore-floatThead').floatThead('destroy');

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
                $(ctxSel + ' .editable-clear-x').click(function() {
                    $('.calendar').calendar('clear');
                });
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
            $(".table").trigger('reflow');
            $('.ui.dropdown')
                .dropdown({
                    clearable: true
                })
            ;
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

        //popup tooltips
        $(ctxSel + ' .la-popup-tooltip.la-delay').popup({delay: {
            show: 300,
            hide: 0
        }
        });
        $("a[href], input.js-wait-wheel").not("a[href^='#'], a[target='_blank'], .js-open-confirm-modal, a[data-tab], a[data-tooltip], a.la-ctrls , .close, .js-no-wait-wheel, .trigger-modal").click(function() {
            $("html").css("cursor", "wait");
        });

        // selectable table to avoid button is showing when focus after modal closed
        $(ctxSel + ' .la-selectable').hover(function() {
            $( ".button" ).blur();
        });

        // close semui:messages alerts
        $(ctxSel + ' .close.icon').click(function() {
            $(this).parent().hide();
        });

        // modals
        $(ctxSel + " *[data-semui='modal']").click(function() {
            var href = $(this).attr('data-href')
            if (! href) {
                href = $(this).attr('href')
            }
            $(href + '.ui.modal').modal({
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
            apiSettings: {
                cache: false
            }
            //showOnFocus: false
        });

        $(ctxSel + ' form').attr('autocomplete', 'off');

        $(ctxSel + ' .la-filter .ui.dropdown').dropdown({
            clearable: true
        });

        $(ctxSel + ' .ui.dropdown.la-clearable').dropdown({
            clearable: true
        });

        $(ctxSel + ' .ui.search.dropdown').dropdown({
            fullTextSearch: 'exact',
            clearable: true
        });

        // dropdowns escape
        $(ctxSel + ' .la-filter .ui.dropdown').on('keydown', function(e) {
            if(['Escape','Backspace','Delete'].includes(event.key)) {
                e.preventDefault();
                $(this).dropdown('clear').dropdown('hide').removeClass("la-filter-dropdown-selected");
            }
        });

        function toggleFilterDropdown(that) {
            $( that ).find("div.text").hasClass("default")? $(that).removeClass("la-filter-dropdown-selected") : $(that).addClass("la-filter-dropdown-selected");
        }
        $( '.la-filter .ui.dropdown' ).each(function( index ) {
            toggleFilterDropdown(this)
        });

        // SEM UI DROPDOWN CHANGE
        $(ctxSel + ' .la-filter .ui.dropdown').change(function() {
            toggleFilterDropdown(this)
        });

        // for default selected Dropdown value
        /*
        var currentDropdown = $(ctxSel + ' .la-filter .ui.dropdown > select > option[selected=selected]').parents('.ui.dropdown');

        currentDropdown.find("div.text").hasClass("default")
            ?  currentDropdown.removeClass('la-filter-dropdown-selected')
            : currentDropdown.addClass('la-filter-dropdown-selected');
        */


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
            function(that){
                var dataAttr = that.getAttribute("data-confirm-id")? that.getAttribute("data-confirm-id")+'_form':false;
                var what = that.getAttribute("data-confirm-term-what")? that.getAttribute("data-confirm-term-what"):"";
                var whatDetail = that.getAttribute("data-confirm-term-what-detail")? that.getAttribute("data-confirm-term-what-detail"):false;

                var where = that.getAttribute("data-confirm-term-where")? that.getAttribute("data-confirm-term-where"):false;
                var whereDetail = that.getAttribute("data-confirm-term-where-detail")? that.getAttribute("data-confirm-term-where-detail"):false;
                var how = that.getAttribute("data-confirm-term-how") ? that.getAttribute("data-confirm-term-how"):"delete";
                var content = that.getAttribute("data-confirm-term-content")? that.getAttribute("data-confirm-term-content"):false;
                var messageHow;

                switch (how) {
                    case "delete":
                        messageHow = "löschen";
                        break;
                    case "unlink":
                        messageHow = "aufheben";
                        break;
                    case "share":
                        messageHow = "teilen";
                        break;
                    case "inherit":
                        messageHow = "ändern";
                        break;
                    case "ok":
                        messageHow = "fortfahren";
                        break;
                    default:
                        messageHow = "löschen";
                }
                var url = that.getAttribute('href') && (that.getAttribute('class').indexOf('la-js-remoteLink') == -1) && (that.getAttribute('class') != 'js-gost') ? that.getAttribute('href'): false; // use url only if not remote link

                // INHERIT BUTTON

                var messageContent = content;
                var messageWhat = what;


                if (how == "inherit"){
                    switch (what) {
                        case "property":
                            var messageWhat = "die Vererbung des Merkmals";
                            break;
                        default:
                            var messageWhat = "die Vererbung des Merkmals";
                    }
                }
                // UNLINK BUTTON
                if (how == "unlink"){
                    switch (what) {
                        case "organisationtype":
                            var messageWhat = "die Verknüpfung des Organisationstyps";
                            break;
                        case "contact":
                            var messageWhat = "die Verknüpfung des Kontakts";
                            break;
                        case "membershipSubscription" :
                            var messageWhat = "die Teilnahme von";
                            break;
                        case "subscription":
                            var messageWhat = "die Verknüpfung der Lizenz";
                            break;
                        default:
                            var messageWhat = "die Verknüpfung des Objektes";
                    }
                    switch (where) {
                        case "organisation":
                            var messageWhere = "mit der Organisation";
                            break;
                        default:
                            var messageWhere = where;
                    }
                }
                // SHARE BUTTON
                if (how == "share"){
                    switch (what) {
                        case "element":
                            var messageWhat = "das Element";
                            break;
                        default:
                            var messageWhat = "das Element";
                    }
                    switch (where) {
                        case "member":
                            var messageWhere = "mit den Teilnehmern";
                            break;
                        default:
                            var messageWhere = where;
                    }
                }
                // DELETE BUTTON
                if (how == "delete"){
                    switch (what) {
                        case "document":
                            var messageWhat = "das Dokument";
                            break;
                        case "department":
                            var messageWhat = "das Institut";
                            break;
                        case "organisationtype":
                            var messageWhat = "den Organisationstyp";
                            break;
                        case "task":
                            var messageWhat = "die Aufgabe";
                            break;
                        case "person":
                            var messageWhat = "die Person";
                            break;
                        case "contactItems":
                            var messageWhat = "die Kontaktdaten";
                            break;
                        case "contact":
                            var messageWhat = "den Kontakt";
                            break;
                        case "address":
                            var messageWhat = "die Adresse";
                            break;
                        case "subscription":
                            var messageWhat = "die Lizenz";
                            break;
                        case "license":
                            var messageWhat = "den Vertrag";
                            break;
                        case "property":
                            var messageWhat = "das Merkmal";
                            break;
                        case "function":
                            var messageWhat = "die Funktion";
                            break;
                        case "user":
                            var messageWhat = "den Benutzer";
                            break;
                        default:
                            var messageWhat = what;
                    }
                    switch (where) {
                        case "organisation":
                            var messageWhere = "aus der Organisation";
                            break;
                        case "institution":
                            var messageWhere = "von Ihrer Einrichtung";
                            break;
                        case "addressbook":
                            var messageWhere = "aus dem Adressbuch";
                            break;
                        case "system":
                            var messageWhere = "aus dem System";
                            break;
                        default:
                            var messageWhere = "aus dem System";
                    }
                }
                $('#js-confirmation-term-what').text(messageWhat); // Should be always set - otherwise "dieses Element"
                //whatDetail ? $('#js-confirmation-term-what').text(messageWhat) : $("#js-confirmation-term-what").remove();
                whatDetail ? $('#js-confirmation-term-what-detail').text(whatDetail) : $("#js-confirmation-term-what-detail").remove();
                whereDetail ? $('#js-confirmation-term-where-detail').text(whereDetail) : $("#js-confirmation-term-where-detail").remove();
                where ? $('#js-confirmation-term-where').text(messageWhere) : $("#js-confirmation-term-where").remove();
                content ? $('#js-confirmation-term-content').text(messageContent) : $("#js-confirmation-term-content").remove();
                $('#js-confirmation-term-how').text(messageHow); // Should be always set - otherwise "delete"
                switch (how) {
                    case "delete":
                        $('#js-confirmation-button').html('Löschen<i class="trash alternate icon"></i>');
                        break;
                    case "unlink":
                        $('#js-confirmation-button').html('Aufheben<i class="la-chain broken icon"></i>');
                        break;
                    case "share":
                        $('#js-confirmation-button').html('Teilen<i class="la-share icon"></i>');
                        break;
                    case "inherit":
                        $('#js-confirmation-button').html('Vererbung ändern<i class="thumbtack icon"></i>');
                        break;
                    case "ok":
                        $('#js-confirmation-button').html('OK<i class="check icon"></i>');
                        break;
                    default:
                        $('').html('Entfernen<i class="x icon"></i>');
                }

                var remoteLink = $(that).hasClass('la-js-remoteLink')


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
                            if (remoteLink) {
                                bb8.ajax(that)
                            }
                        }
                    })
                    .modal('show')
                ;
            }

        // for links and submit buttons
        $(ctxSel + ' .js-open-confirm-modal').click(function(e) {
            e.preventDefault();
            buildConfirmationModal(this);
        });

        // for old remote links = ajax calls
        $(ctxSel + ' .js-open-confirm-modal-copycat').click(function(e) {
            var onclickString = $(this).next('.js-gost').attr("onclick");
            $('#js-confirmation-button').attr("onclick", onclickString);
            var gostObject = $(this).next('.js-gost');
            buildConfirmationModal(gostObject[0] );
        });
    },

}
// for future handling on other views
// 1. add class 'hidden' via markup to all cards that might be toggled
// 2. add class 'la-js-hideable' to all cards that might be toggled
// 3. add class 'la-js-dont-hide-this-card' to markup that is rendered only in case of card has content, like to a table <th>
deckSaver = {
    configs : {

        toggleButton: $(".ui.toggle.button"),
        toggleIcon: $(".ui.toggle.button .icon"),

        areaThatIsAffected: $("#collapseableSubDetails"),

        card: {
            hidable:$(".la-js-hideable"),
        },
        element:{
            hide: $(".la-js-hideMe"),
            dontHideSurroundingCard:    $(".la-js-dont-hide-this-card"),
            hideSurroundingCard:        $(".la-js-hide-this-card"),
        },
        button: {
            dontHide: $(".la-js-dont-hide-button")
        },
        icon: $(".la-js-editmode-icon")

    },
    initializePopup: function(obj) {

        $(obj).popup({
            hoverable: true,
            inline     : true,
            lastResort: true,
            onShow: function() {
                // generate a random ID
                var id =  'wcag_' + Math.random().toString(36).substr(2, 9);
                // add aria-label to container-span
                $(this).prev(tooltip.configs.tooltipTrigger).attr('aria-labelledby',id);
                //add role=tooltip and the generated ID to the tooltip-div (generated from semantic)
                $(this).children('.content').attr({role:'tooltip',id:id});
            },
        });

    },
    removeClone: function () {
        $('.la-clone').remove();
    },
    removePopupFromClone:  function () {

        var clone = $('.la-clone');
        var clonePopup = $(clone).popup('get popup');
        $(clonePopup).each(function(){
            $(this).remove();
        })

    },
    enableXeditable: function (cssClass){

        var selection = $(cssClass).not('.ui.modal' + ' ' + cssClass);
        selection.editable('option', 'disabled', false);

    },
    diableXeditable: function(cssClass){

        var selection = $(cssClass).not('.ui.modal' + ' ' + cssClass);
        selection.editable('option', 'disabled', true);

    },
    toggleEditableElements: function (){

        if (deckSaver.configs.editMode) {
            // show Contoll Elements
            $('.button').removeClass('hidden');
            deckSaver.removeClone();
            deckSaver.removePopupFromClone();

            $('.card').not('.ui.modal .card').removeClass('hidden');
            $(deckSaver.configs.element.hideSurroundingCard).removeClass('hidden');
            $('.ui .form').not('.ui.modal .ui.form').removeClass('hidden');
            console.log(deckSaver.configs.areaThatIsAffected);
            $(deckSaver.configs.areaThatIsAffected).find('.button').removeClass('hidden');


            $(deckSaver.configs.toggleButton).removeAttr("data-tooltip","Hide Buttons");
            $(deckSaver.configs.toggleButton).attr("data-tooltip","Show Buttons");
            $(deckSaver.configs.toggleIcon ).removeClass( "slash" );
            $(deckSaver.configs.toggleButton).addClass('active');


            deckSaver.enableXeditable ('.xEditableValue');
            deckSaver.enableXeditable ('.xEditable');
            deckSaver.enableXeditable ('.xEditableDatepicker');
            deckSaver.enableXeditable ('.xEditableManyToOne');

        }
        else {
            // hide Contoll Elements
            $(deckSaver.configs.icon).each(function(){
                var container = $(this).closest('.la-js-editmode-container');
                var button = $(this).closest('.button');
                var clone = $(this).clone();
                clone.appendTo(container);
                $(clone).addClass('la-clone blue');

                //transfer the tooltip-content from button to cloned icon
                var dataContent = button.attr( "data-content" );

                $(clone).attr('data-content', dataContent);
                deckSaver.initializePopup(clone);
            });

            $('.card').not('.ui.modal .card').removeClass('hidden');
            $(deckSaver.configs.card.hidable).not( ":has(.la-js-dont-hide-this-card)" ).addClass('hidden');
            $(deckSaver.configs.element.hideSurroundingCard).addClass('hidden');
            $('.ui.form').not('.ui.modal .ui.form').addClass('hidden');
            $(deckSaver.configs.areaThatIsAffected).not('.ui.modal').find('.button').not('.ui.modal .button, .la-js-dont-hide-button').addClass('hidden');

            //$('.button').not('.la-js-dont-hide-button').addClass('hidden');
            $(deckSaver.configs.toggleButton).removeAttr();
            $(deckSaver.configs.toggleButton).attr("data-tooltip","Hide Buttons");
            $(deckSaver.configs.toggleIcon ).addClass( "slash" );
            $(deckSaver.configs.toggleButton).removeClass('active');


            deckSaver.diableXeditable ('.xEditableValue');
            deckSaver.diableXeditable ('.xEditable');
            deckSaver.diableXeditable ('.xEditableDatepicker');
            deckSaver.diableXeditable ('.xEditableManyToOne');
        }
    }
}


bb8 = {


    go: function() {
        bb8.init('body');
    },

    init: function(ctxSel) {
        $(ctxSel + " .la-js-remoteLink").click(function (event) {

            event.preventDefault();
            if (! $(this).hasClass('js-open-confirm-modal')) {

                bb8.ajax(this);
            }
        })

    },

    ajax: function(elem) {

        var url = $(elem).attr('href')
        var before = $(elem).attr('data-before')      // before
        var done = $(elem).attr('data-done')          // onSuccess
        var fail = $(elem).attr('data-fail')
        var always = $(elem).attr('data-always')      // onComplete
        var update = '#' + $(elem).attr('data-update')

        $.ajax({
            url: url,

            beforeSend: function (xhr) {
                if (before) {
                    //console.log('before')
                    eval(before)
                }
            }
        })
            .done(function (data) {
                //console.log('done')
                $(update).empty()
                $(update).html(data)
                if (done) {
                    eval(done)
                }
            })
            .fail(function () {
                //console.log('fail')
                if (fail) {
                    eval(fail)
                }
            })
            .always(function () {
                //console.log('always')
                if (always) {
                    eval(always)
                }
            });

    }
}

$(document).ready(function() {

    r2d2.go();
    bb8.go();

})

