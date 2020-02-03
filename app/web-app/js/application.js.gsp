
var currLanguage = $('html').attr('lang');


<%@ page import="java.util.Locale;java.util.List" %>
<%
    Locale localeDe = new Locale.Builder().setLanguage("de").build()
    Locale localeEn = new Locale.Builder().setLanguage("en").build()

    List<String> translations = [
        'statusbar.hideButtons.tooltip',
        'statusbar.showButtons.tooltip',
        'default.informations',
        'default.actions',
        'property.select.placeholder',
        'confirm.dialog.delete',
        'confirm.dialog.unlink',
        'confirm.dialog.share',
        'confirm.dialog.inherit',
        'confirm.dialog.ok',
        'confirm.dialog.concludeBinding',
        'confirm.dialog.clearUp',
        'loc.January', 'loc.February', 'loc.March', 'loc.April', 'loc.May', 'loc.June', 'loc.July', 'loc.August', 'loc.September', 'loc.October', 'loc.November', 'loc.December',
        'loc.weekday.short.Sunday','loc.weekday.short.Monday','loc.weekday.short.Tuesday','loc.weekday.short.Wednesday','loc.weekday.short.Thursday','loc.weekday.short.Friday','loc.weekday.short.Saturday'
    ]

println """
dict = {
    get: function (key, lang) {
        return dict[key][lang]
    },"""

    translations.eachWithIndex { it, index ->
        println "    '${it}' : {"
        println "        de: '" + message(code: "${it}", locale: localeDe) + "',"
        println "        en: '" + message(code: "${it}", locale: localeEn) + "' "
        println (index < translations.size() - 1 ? "    }, " : "    }")
    }

    println "} "
%>


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
                            $(this).addClass("la-calendar-selected");
                            //r2d2.countSettedFilters();
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
            },
            text: {
                days: [
                    dict.get('loc.weekday.short.Sunday',currLanguage),
                    dict.get('loc.weekday.short.Monday',currLanguage),
                    dict.get('loc.weekday.short.Tuesday',currLanguage),
                    dict.get('loc.weekday.short.Wednesday',currLanguage),
                    dict.get('loc.weekday.short.Thursday',currLanguage),
                    dict.get('loc.weekday.short.Friday',currLanguage),
                    dict.get('loc.weekday.short.Saturday',currLanguage)
                    ],
                months: [
                    dict.get('loc.January',currLanguage),
                    dict.get('loc.February',currLanguage),
                    dict.get('loc.March',currLanguage),
                    dict.get('loc.April',currLanguage),
                    dict.get('loc.May',currLanguage),
                    dict.get('loc.June',currLanguage),
                    dict.get('loc.July',currLanguage),
                    dict.get('loc.August',currLanguage),
                    dict.get('loc.September',currLanguage),
                    dict.get('loc.October',currLanguage),
                    dict.get('loc.November',currLanguage),
                    dict.get('loc.December',currLanguage)
                ]
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

        //overwriting the template for input search (spotlight)
        // see https://jsfiddle.net/xnfkLnwe/1/
        // and https://github.com/Semantic-Org/Semantic-UI/issues/2405
        $.fn.search.settings.templates.message = function (message, type) {
            var
                html = '';
            if (message !== undefined && type !== undefined) {
                html += '' + '<div class="message ' + type + '">';
                // message type
                if (type == 'empty') {
                    html += '' + '<div class="header">${message(code:"search.API.heading.noResults")}</div class="header">' + '<div class="description">' + message + '</div class="description">';
                } else {
                    html += ' <div class="description">' + message + '</div>';
                }
                html += '</div>';
            }
            return html;
        };


        // spotlight

        $('.ui.search.spotlight').search({
            error : {
                source          : '${message(code:"search.API.source")}',
                noResults       : '',
                logging         : '${message(code:"search.API.logging")}',
                noEndpoint      : '${message(code:"search.API.noEndpoint")}',
                noTemplate      : '${message(code:"search.API.noTemplate")}',
                serverError     : '${message(code:"search.API.serverError")}',
                maxResults      : '${message(code:"search.API.maxResults")}',
                method          : '${message(code:"search.API.method")}',
            },

            type: 'category',
            minCharacters: 3,
            apiSettings: {

                url: "<g:createLink controller='search' action='spotlightSearch'/>/?query={query}",
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
                            url         : item.url,
                            description : item.description
                        });
                    });
                    return response;
                },
                onError: function(errorMessage) {
                  // invalid response

                }
            }
        });

/*  Menue Search Animated Input
       $('#btn-search').on('click', function(e) {
            e.preventDefault();

            $('#spotlightSearch').animate({width: 'toggle'}).focus();
            $(this).toggleClass('open');
        });*/

        // metaboxes
        $('.metaboxToggle').click(function() {
            $(this).next('.metaboxContent').slideToggle();
        })
    },


    initGlobalXEditableStuff : function() {
        console.log("r2d2.initGlobalXEditableStuff()");

        $.fn.editable.defaults.mode = 'inline'
        $.fn.editableform.buttons = '<button type="submit" class="ui icon button editable-submit"><i class="check icon"></i></button>' +
            '<button type="button" class="ui icon button editable-cancel"><i class="times icon"></i></button>'
        $.fn.editableform.template =
            '<form class="ui form editableform">' +
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

            highlight: false,
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

        $('.la-readmore').readmore({
                speed: 75,
                lessLink: '<a href="#">${message(code:"link.readless")}</a>',
                moreLink: '<a href="#">${message(code:"link.readmore")}</a>',
                collapsedHeight: 115
        });
    },

    countSettedFilters: function () {
        // DROPDOWN AND INPUT FIELDS
        var dropdownFilter = 0;
        var inputTextFilter = 0;
        var calendarFilter = 0;
        var checkboxFilter = 0;
        dropdownFilter = $('.la-filter-dropdown-selected').length;
        inputTextFilter = $('.la-filter-selected').length;
        calendarFilter = $('.la-calendar-selected').length;


        // CHECKBOXES
        // LOOP TROUGH CHECKBOXES
        var allCheckboxes = [];
        $('.la-filter .checkbox').each(function() {
            allCheckboxes.push($(this).children('input').attr("name"));
        });
        // ELIMINATE DUPLICATES
        var eliminateDuplicates = function (uniquecheckboxNames){
            return uniquecheckboxNames.filter (function(v,i) {
                return uniquecheckboxNames.indexOf(v) === i
            });
        };
        var uniquecheckboxNames = eliminateDuplicates(allCheckboxes);
        // COUNT SELECTED CHECKBOXES
        countSettedCheckboxes(uniquecheckboxNames);
        function countSettedCheckboxes(params) {
            var sumCheck = 0;
            for (i=0; i<params.length; i++) {
                var checkboxName = params[i];
                $('input[name='+ checkboxName +']').is(':checked')? sumCheck=sumCheck+1: sumCheck= sumCheck;
            }
            checkboxFilter = sumCheck;
        }

        // COUNT ALL SELECTIONS IN TOTAL
        var total = dropdownFilter + inputTextFilter + calendarFilter +checkboxFilter;
        $( document ).ready(function() {
            if (total == 0) {
                $('.la-js-filter-total').addClass('hidden');
                $('.la-js-filterButton i').removeClass('hidden');

            } else {
                $('.la-js-filter-total').text(total);
                $('.la-js-filter-total').removeClass('hidden');
                $('.la-js-filterButton i').addClass('hidden');
            }
        });
    },


    initDynamicSemuiStuff : function(ctxSel) {
        console.log("r2d2.initDynamicSemuiStuff( " + ctxSel + " )")
        if (! ctxSel) {
            ctxSel = 'body'
        }

        //popup tooltips
        /*$(ctxSel + ' .la-delay').popup({delay: {
            show: 300,
            hide: 1000
        }
        });
        $(ctxSel + ' .la-popup-tooltip.la-delay').popup( {
            hoverable: true,
            inline     : true,
            lastResort: true
        });*/
        $("a[href], input.js-wait-wheel").not("a[href^='#'], a[href*='ajax'], a[target='_blank'], .js-open-confirm-modal, a[data-tab], a[data-content], a.la-ctrls , .close, .js-no-wait-wheel, .trigger-modal").click(function() {
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
            var triggerElement = $(this)
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
                },
                onShow : function() {
                    var modalCallbackFunction = dcbStore.modal.show[$(this).attr('id')];
                    if (typeof modalCallbackFunction === "function") {
                        modalCallbackFunction(triggerElement)
                    }
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
        $(ctxSel + ' .ui.dropdown').not('#mainMenue .ui.dropdown').dropdown({
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
                //e.preventDefault();
                $(this).dropdown('clear').dropdown('hide').removeClass("la-filter-dropdown-selected");
            }
        });

        $( document ).ready(function() {

            $( '.la-filter .ui.dropdown' ).each(function( index ) {
                toggleFilterDropdown(this,true)
            });

        });

        function toggleFilterDropdown(that, initial) {

            $( that ).find("div.text").hasClass("default")? $(that).removeClass("la-filter-dropdown-selected") : $(that).addClass("la-filter-dropdown-selected");
            if(initial) {
                r2d2.countSettedFilters();
            }

        }

        $('.la-filter .checkbox').checkbox({
            onChange: function() {
                // r2d2.countSettedFilters();
            }
        });


        // SEM UI DROPDOWN CHANGE
        $(ctxSel + ' .la-filter .ui.dropdown').change(function() {
            toggleFilterDropdown(this, false)
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
            r2d2.countSettedFilters(true);
        });

        //  FILTER SELECT FUNCTION - INPUT CHANGE
        $(ctxSel + ' .la-filter input[type=text]').change(function() {
            $(this).val().length === 0 ? $(this).removeClass("la-filter-selected") : $(this).addClass("la-filter-selected");
            //r2d2.countSettedFilters();
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

        //WCAG Improvement
        $(ctxSel + ' .search.dropdown').children('.search').attr("aria-labelledby","la-legend-searchDropdown");

        // confirmation modal
        var buildConfirmationModal =
            function(that){
                var dataAttr = that.getAttribute("data-confirm-id")? that.getAttribute("data-confirm-id")+'_form':false;
                var how = that.getAttribute("data-confirm-term-how") ? that.getAttribute("data-confirm-term-how"):"delete";
                var tokenMsg = that.getAttribute("data-confirm-tokenMsg")? that.getAttribute("data-confirm-tokenMsg"):false;

                var url = that.getAttribute('href') && (that.getAttribute('class').indexOf('la-js-remoteLink') == -1) && (that.getAttribute('class') != 'js-gost') ? that.getAttribute('href'): false; // use url only if not remote link

                tokenMsg ? $('#js-confirmation-term').html(tokenMsg) : $("#js-confirmation-term").remove();
                
                switch (how) {
                    case "delete":
                        $('#js-confirmation-button').html(dict.get('confirm.dialog.delete',currLanguage) + '<i class="trash alternate icon"></i>');
                        break;
                    case "unlink":
                        $('#js-confirmation-button').html(dict.get('confirm.dialog.unlink',currLanguage) + '<i class="la-chain broken icon"></i>');
                        break;
                    case "share":
                        $('#js-confirmation-button').html(dict.get('confirm.dialog.share',currLanguage) + '<i class="la-share icon"></i>');
                        break;
                    case "inherit":
                        $('#js-confirmation-button').html(dict.get('confirm.dialog.inherit',currLanguage) + '<i class="thumbtack icon"></i>');
                        break;
                    case "ok":
                        $('#js-confirmation-button').html(dict.get('confirm.dialog.ok',currLanguage) + '<i class="check icon"></i>');
                        break;
                    case "concludeBinding":
                        $('#js-confirmation-button').html(dict.get('confirm.dialog.concludeBinding',currLanguage) + '<i class="check icon"></i>');
                        break;
                    case "clearUp":
                        $('#js-confirmation-button').html(dict.get('confirm.dialog.clearUp',currLanguage) + '<i class="bath icon"></i>');
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
        // the trigger
        toggleButton: $(".ui.toggle.button"),
        toggleIcon: $(".ui.toggle.button .icon"),
        // the target area
        //areaThatIsAffected: $("#collapseableSubDetails"),
        areaThatIsAffected:$(".la-show-context-orgMenu"),

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

            // ***************************
            // show Contoll Elements
            // ***************************
            $('.button').removeClass('hidden');
            deckSaver.removeClone();
            deckSaver.removePopupFromClone();

            $('.card').not('.ui.modal .card').removeClass('hidden');
            $(deckSaver.configs.element.hideSurroundingCard).removeClass('hidden');
            $('.ui .form').not('.ui.modal .ui.form').removeClass('hidden');
            $(deckSaver.configs.areaThatIsAffected).find('.button').removeClass('hidden');


            $(deckSaver.configs.toggleButton).removeAttr("data-content");
            $(deckSaver.configs.toggleButton).attr("data-content", dict.get('statusbar.showButtons.tooltip',currLanguage));
            tooltip.initializePopup(deckSaver.configs.toggleButton);
            $(deckSaver.configs.toggleIcon ).removeClass( "slash" );
            $(deckSaver.configs.toggleButton).addClass('active');


            deckSaver.enableXeditable ('.xEditableValue');
            deckSaver.enableXeditable ('.xEditable');
            deckSaver.enableXeditable ('.xEditableDatepicker');
            deckSaver.enableXeditable ('.xEditableManyToOne');

            $('.la-action-info').css('text-align', 'left').text(dict.get('default.actions',currLanguage))
        }
        else {
            // ***************************
            // hide Contoll Elements
            // ***************************
            deckSaver.configs.icon = $(".la-js-editmode-icon");
            $(deckSaver.configs.icon).each(function(){
                var container = $(this).closest('.la-js-editmode-container');
                var button = $(this).closest('.button');
                var clone = $(this).clone();
                clone.appendTo(container);
                $(clone).addClass('la-clone blue');

                //transfer the tooltip-content from button to cloned icon
                var dataContent = button.attr( "data-content" );

                $(clone).attr('data-content', dataContent);
                tooltip.initializePopup(clone);
            });

            $('.card').not('.ui.modal .card').removeClass('hidden');
            $(deckSaver.configs.card.hidable).not( ":has(.la-js-dont-hide-this-card)" ).addClass('hidden');
            $(deckSaver.configs.element.hideSurroundingCard).addClass('hidden');
            $('.ui.form').not('.ui.modal .ui.form').addClass('hidden');
            $(deckSaver.configs.areaThatIsAffected).not('.ui.modal').find('.button').not('.ui.modal .button, .la-js-dont-hide-button').addClass('hidden');

            $(deckSaver.configs.toggleButton).removeAttr("data-content");
            $(deckSaver.configs.toggleButton).attr("data-content", dict.get('statusbar.hideButtons.tooltip',currLanguage));
            tooltip.initializePopup(deckSaver.configs.toggleButton);



            $(deckSaver.configs.toggleIcon ).addClass( "slash" );
            $(deckSaver.configs.toggleButton).removeClass('active');


            deckSaver.diableXeditable ('.xEditableValue');
            deckSaver.diableXeditable ('.xEditable');
            deckSaver.diableXeditable ('.xEditableDatepicker');
            deckSaver.diableXeditable ('.xEditableManyToOne');

            $('.la-action-info').css('text-align', 'right').text(dict.get('default.informations',currLanguage))
        }
    }
}
tooltip = {
    configs : {
        tooltipTrigger: $('.la-popup-tooltip')
    },
    go : function() {
        tooltip.configs.tooltipTrigger =  $('.la-popup-tooltip');
        tooltip.initializePopup(tooltip.configs.tooltipTrigger);
        tooltip.acccessViaKeys();
    },
    initializePopup: function(obj) {

        $('.ui.toggle.button').next('.ui.popup').remove();
        $(obj).popup({
            hoverable: true,
            inline     : true,
            lastResort: true,
            position: 'left center',
            delay: {
                show: 300,
                hide: 500
            },
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
    acccessViaKeys: function(){
        // for click and focus
        $(tooltip.configs.tooltipTrigger).on('click focus', function(){$(this).popup('show'); })

        // for unfocus
        $(tooltip.configs.tooltipTrigger).on('focusout', function(){$(this).popup('hide'); })

        // for ESC
        $(tooltip.configs.tooltipTrigger).on('keydown', function(){
            if(event.keyCode==27){
                $(this).popup('hide');
            }
        })
    },
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
        var done = $(elem).attr('data-done')          // onSuccesstrigger
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
                tooltip.go();

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

// used as storage for dynamic callbacks

dcbStore = {
    modal : {
        show : {
        }
    }
}

$(document).ready(function() {
    r2d2.go();
    bb8.go();
    tooltip.go();
})