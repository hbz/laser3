
// modules/r2d2.js

var currLanguage = $('html').attr('lang');

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
            onShow: function() {
                $('.ui.popup.calendar .table .link').attr( {
                    'role' : 'button'
                });
            },
            firstDayOfWeek: 1,
            monthFirst: false,
            minDate: new Date('1582-10-15'), //this is the start of the gregorian calendar
            maxDate: new Date('2099-12-31'), //our grand-grandchildren may update this date ...
            formatter: {
                date: function (date, settings) {
                    if (!date) return '';
                    var day = date.getDate();
                    if (day<10) day="0"+day;
                    var month = date.getMonth() + 1;
                    if (month<10) month="0"+month;
                    var year = date.getFullYear();

                    if ('dd.mm.yyyy' == laser.gspDateFormat) {
                        console.log('dd.mm.yyyy');
                        return day + '.' + month + '.' + year;
                    }
                    else if ('yyyy-mm-dd' == laser.gspDateFormat) {
                        console.log('yyyy-mm-dd');
                        return year + '-' + month + '-' + day;
                    }
                    else {
                        // TODO
                        alert('Please report this error: ' + laser.gspDateFormat + ' for semui-datepicker unsupported');
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
        console.log('r2d2.go()')

        r2d2.initGlobalAjaxLogin();

        r2d2.initGlobalSemuiStuff();
        r2d2.initGlobalXEditableStuff();

        r2d2.initDynamicSemuiStuff('body');
        r2d2.initDynamicXEditableStuff('body');

        $("html").css("cursor", "auto");

        console.log("r2d2 - laser.gspLocale: " + laser.gspLocale + ", laser.gspDateFormat: " + laser.gspDateFormat);
    },

    initGlobalAjaxLogin : function() {
        console.log('r2d2.initGlobalAjaxLogin()')

        $.ajaxSetup({
            statusCode: {
                401: function() {
                    $('.select2-container').select2('close')
                    $('*[class^=xEditable]').editable('hide')
                    showAjaxLoginModal()
                }
            }
        })

        function showAjaxLoginModal() {
            $('#ajaxLoginModal').modal('setting', 'closable', false).modal('show')
        }

        function ajaxAuth() {
            $.ajax({
                url: $('#ajaxLoginForm').attr('action'),
                data: $('#ajaxLoginForm').serialize(),
                method: 'POST',
                dataType: 'JSON',
                success: function (json, textStatus, xhr) {
                    if (json.success) {
                        $('#ajaxLoginForm')[0].reset()
                        $('#ajaxLoginMessage').empty()
                        $('#ajaxLoginModal').modal('hide')
                    }
                    else if (json.error) {
                        $('#ajaxLoginMessage').html('<div class="ui negative message">' + json.error + '</div>')
                    }
                    else {
                        $('#loginMessage').html(xhr.responseText)
                    }
                },
                error: function (xhr, textStatus, errorThrown) {
                    if (xhr.status == 401 && xhr.getResponseHeader('Location')) {
                        // the login request itself wasn't allowed, possibly because the
                        // post url is incorrect and access was denied to it
                        $('#loginMessage').html('<div class="ui negative message">Unbekannter Fehler beim Login. Melden Sie sich bitte über die Startseite an.</div>')
                    }
                    else {
                        var responseText = xhr.responseText
                        if (responseText) {
                            var json = $.parseJSON(responseText)
                            if (json.error) {
                                $('#loginMessage').html('<div class="ui negative message">' + json.error + '</div>')
                                return
                            }
                        }
                        else {
                            responseText = 'Status: ' + textStatus + ', Fehler: ' + errorThrown + ')'
                        }
                        $('#ajaxLoginMessage').html('<div class="ui negative message">' + responseText + '</div>')
                    }
                }
            })
        }

        $('#ajaxLoginForm').submit(function(event) {
            event.preventDefault()
            ajaxAuth()
        })
    },

    initGlobalSemuiStuff : function() {
        console.log("r2d2.initGlobalSemuiStuff()")
        // copy email adress next to icon and putting it in cache

        $('.js-copyTrigger').click(function(){
            var element = $(this).parents('.js-copyTriggerParent').find('.js-copyTopic')
            var $temp = $("<input>");
            $("body").append($temp);
            $temp.val($(element).text()).select();
            document.execCommand("copy");
            $temp.remove();
        });
        $('.js-copyTrigger').hover(
            function(){ $(this).addClass('open') },
            function(){ $(this).removeClass('open') }
        )
        $('.js-linkGoogle').hover(
            function(){ $(this).removeClass('alternate') },
            function(){ $(this).addClass('alternate') }
        )
        //JS Library readmore.js
        $('.la-readmore').readmore({
            speed: 75,
            lessLink: '<a href="#">' + dict.get('link.readless', currLanguage) + '</a>',
            moreLink: '<a href="#">' + dict.get('link.readmore', currLanguage) + '</a>',
            collapsedHeight: 115
        });
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
                    html += '' + '<div class="header">' + dict.get('search.API.heading.noResults', currLanguage) + '</div class="header">' + '<div class="description">' + message + '</div class="description">';
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
                source          : '"' + dict.get('search.API.source', currLanguage) + '"',
                noResults       : '',
                logging         : '"' + dict.get('search.API.logging', currLanguage) + '"',
                noEndpoint      : '"' + dict.get('search.API.noEndpoint', currLanguage) + '"',
                noTemplate      : '"' + dict.get('search.API.noTemplate', currLanguage) + '"',
                serverError     : '"' + dict.get('search.API.serverError', currLanguage) + '"',
                maxResults      : '"' + dict.get('search.API.maxResults', currLanguage) + '"',
                method          : '"' + dict.get('search.API.method', currLanguage) + '"'
            },

            type: 'category',
            minCharacters: 3,
            apiSettings: {

                url: laser.gspSpotlightSearchUrl + "/?query={query}",
                onResponse: function(elasticResponse) {
                    var response = { results : {} };

                    // translate Elasticsearch API response to work with semantic ui search
                    $.each(elasticResponse.results, function(index, item) {

                        var category   = item.category || 'Unknown';
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

    },


    initGlobalXEditableStuff : function() {
        console.log("r2d2.initGlobalXEditableStuff()");

        $.fn.editable.defaults.mode = 'inline'
        $.fn.editableform.buttons = '<button aria-label="' + dict.get('xEditable.button.ok', currLanguage) + '" type="submit" class="ui icon button editable-submit"><i aria-hidden="true" class="check icon"></i></button>' +
            '<button aria-label="' + dict.get('xEditable.button.cancel', currLanguage) + '" type="button" class="ui icon button editable-cancel"><i aria-hidden="true" class="times icon"></i></button>'
        $.fn.editableform.template =
            '<form class="ui form editableform">' +
            '	<div class="control-group">' +
            '		<div class="ui calendar xEditable-datepicker">' +
            '			<div class="ui input right icon editable-input">' +
            '			</div>' +
            '			<div class="editable-buttons">' +
            '			</div>' +
            '		</div>' +
            '        <div id="characters-count"></div>' +
            '		<div class="editable-error-block">' +
            '		</div>' +
            '	</div>' +
            '</form>'
        $.fn.editableform.loading =
            '<div class="ui active inline loader"></div>'

        // TODO $.fn.datepicker.defaults.language = laser.gspLocale
    },


    initDynamicXEditableStuff : function(ctxSel) {
        console.log("r2d2.initDynamicXEditableStuff( " + ctxSel + " )");

        if (! ctxSel) { return null }

        // DEPRECATED ?? never used
        $(ctxSel + ' .xEditable').editable({
            language: laser.gspLocale,
            format:   laser.gspDateFormat,
            validate: function(value) {
                // custom validate functions via semui:xEditable validation="xy"
                var dVal = $(this).attr('data-validation')
                if (dVal) {
                    if (dVal.includes('notEmpty')) {
                        if($.trim(value) == '') {
                            return "Das Feld darf nicht leer sein";
                        }
                    }
                    if (dVal.includes('url')) {
                        var regex = /^(https?|ftp):\/\/(.)*/;
                        var test = regex.test($.trim(value)) || $.trim(value) == ''
                        if (! test) {
                            return "Ein URL muss mit 'http://' oder 'https://' oder 'ftp://' beginnen."
                        }
                    }
                }
            },
            error: function (xhr, status, error) {
                return xhr.status + ": " + xhr.statusText
            },
        })

        $(ctxSel + ' .xEditableValue').editable({

            highlight: false,
            language: laser.gspLocale,
            format:   laser.gspDateFormat,
            validate: function(value) {
                if ($(this).attr('data-format') && value) {
                    if(! (value.match(/^\d{1,2}\.\d{1,2}\.\d{4}$/) || value.match(/^\d{4}-\d{1,2}-\d{1,2}$/)) ) {
                        return "Ungültiges Format";
                    }
                }
                // custom validate functions via semui:xEditable validation="xy"
                var dVal = $(this).attr('data-validation')
                if (dVal) {
                    if (dVal.includes('notEmpty')) {
                        if($.trim(value) == '') {
                            return "Das Feld darf nicht leer sein";
                        }
                    }
                    if (dVal.includes('url')) {
                        var regex = /^(https?|ftp):\/\/(.)*/;
                        var test = regex.test($.trim(value)) || $.trim(value) == ''
                        if (! test) {
                            return "Ein URL muss mit 'http://' oder 'https://' oder 'ftp://' beginnen."
                        }
                    }
                    if (dVal.includes('datesCheck')) {
                        let thisInput = $.trim(value), startDateInput, endDateInput, startDate, endDate;
                        if($(this).attr("data-name") === "startDate") {
                            startDateInput = thisInput;
                            endDateInput = $('a[data-name="endDate"][data-pk="'+$(this).attr("data-pk")+'"]').text();
                        }
                        else if($(this).attr("data-name") === "endDate") {
                            startDateInput = $('a[data-name="startDate"][data-pk="'+$(this).attr("data-pk")+'"]').text();
                            endDateInput = thisInput
                        }
                        if(startDateInput !== '' && endDateInput !== '') {
                            startDate = Date.parse(formatDate(startDateInput));
                            endDate = Date.parse(formatDate(endDateInput));
                            console.log(startDate+" "+endDate);
                            if(startDate > endDate)
                                return "Das Enddatum darf nicht vor dem Anfangsdatum liegen.";
                        }
                    }
                }
            },
            success: function(response) {
                // override newValue with response from backend
                return {newValue: (response != 'null' ? response : null)}
            },
            error: function (xhr, status, error) {
                return xhr.status + ": " + xhr.statusText
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
            }else {
                var dType = $(this).attr('data-type')
                if (dType == "text") {
                    var maxLength = 255;
                    $('input').keyup(function () {
                        if($(this).attr('type') == 'text') {
                            var textlen = maxLength - $(this).val().length;
                            $('#characters-count').text(textlen + '/' + maxLength);
                        }
                    });
                }
            }

            $(".table").trigger('reflow')
        })

        function formatDate(input) {
            if(input.match(/^\d{2}[\.\/-]\d{2}[\.\/-]\d{2,4}$/)) {
                var inArr = input.split(/[\.\/-]/g);
                return inArr[2]+"-"+inArr[1]+"-"+inArr[0];
            }
            else {
                return input;
            }
        }

        $(ctxSel + ' .xEditableDatepicker').editable({});

        $(ctxSel + ' .xEditableManyToOne').editable({
            tpl: '<select class="ui dropdown"></select>',
            success: function(response, newValue) {
                if(response.status == 'error') return response.msg; //msg will be shown in editable form
            }
        }).on('shown', function(e, obj) {

            $('.table').trigger('reflow');
            obj.input.$input.dropdown({clearable: true}) // reference to current dropdown
        })

        $(ctxSel + ' .simpleHiddenRefdata').editable({
            language: laser.gspLocale,
            format:   laser.gspDateFormat,
            url: function(params) {
                var hidden_field_id = $(this).data('hidden-id');
                $("#" + hidden_field_id).val(params.value);
                // Element has a data-hidden-id which is the hidden form property that should be set to the appropriate value
            }
        })

        $(ctxSel + ' .simpleReferenceTypedown').select2({
            placeholder: "Search for...",
            minimumInputLength: 1,
            ajax: { // instead of writing the function to execute the request we use Select2's convenient helper
                url: laser.gspAjaxLookupUrl,
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
        })
    },


    initDynamicSemuiStuff : function(ctxSel) {
        console.log("r2d2.initDynamicSemuiStuff( " + ctxSel + " )")

        if (! ctxSel) { return null }

        $(ctxSel + " a[href], " + ctxSel + " input.js-wait-wheel").not("a[href^='#'], a[href*='ajax'], a[target='_blank'], .js-open-confirm-modal, a[data-tab], a[data-content], a.la-ctrls , .close, .js-no-wait-wheel, .trigger-modal").click(function() {
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
                    var modalCallbackFunction = JSPC.CB.modal.show[$(this).attr('id')];
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
            forceSelection: false,
            selectOnKeydown: false,
            apiSettings: {
                cache: false
            }
        });

        $(ctxSel + ' form').attr('autocomplete', 'off');

        $(ctxSel + ' .la-filter .ui.dropdown').dropdown({
            forceSelection: false,
            selectOnKeydown: false,
            clearable: true,
            onChange: function(value, text, $choice){
                (value !== '') ? addFilterDropdown(this) : removeFilterDropdown(this);
            }
        });

        $(ctxSel + ' .ui.dropdown.la-clearable').dropdown({
            forceSelection: false,
            selectOnKeydown: false,
            clearable: true,
            onChange: function(value, text, $choice){
                (value !== '') ? addFilterDropdown(this) : removeFilterDropdown(this);
            }
        });

        $(ctxSel + ' .ui.search.dropdown:not(.la-not-clearable)').dropdown({ // default behaviour
            forceSelection: false,
            selectOnKeydown: false,
            fullTextSearch: 'exact',
            clearable: true,
            onChange: function(value, text, $choice){
                (value !== '') ? addFilterDropdown(this) : removeFilterDropdown(this);
            }
        });
        $(ctxSel + ' .ui.search.dropdown.la-not-clearable').dropdown({
            forceSelection: false,
            selectOnKeydown: false,
            fullTextSearch: 'exact',
            onChange: function(value, text, $choice){
                value !== '' ? addFilterDropdown(this) : removeFilterDropdown(this);
            }
        });

        // dropdowns escape
        $(ctxSel + ' .la-filter .ui.dropdown').on('keydown', function(e) {
            if(['Escape','Backspace','Delete'].includes(event.key)) {
                //e.preventDefault();
                $(this).dropdown('clear').dropdown('hide').removeClass("la-filter-dropdown-selected");
            }
        });

        $(ctxSel + ' .la-filter .ui.dropdown').each(function(index,elem){
            if ($(elem).dropdown('get value') != "") {
                addFilterDropdown(elem)
            }
            r2d2.countSettedFilters();
        })

        function addFilterDropdown(elem){
            $(elem).is('select') ? $( elem ).parent().addClass("la-filter-dropdown-selected" ) : $( elem ).addClass("la-filter-dropdown-selected" );
        }

        function removeFilterDropdown(elem){
            $(elem).is('select') ? $( elem ).parent().removeClass("la-filter-dropdown-selected" ) : $( elem ).removeClass("la-filter-dropdown-selected" );
        }

        $(ctxSel + '.la-filter .checkbox').checkbox({
            onChange: function() {
                // r2d2.countSettedFilters();
            }
        });

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

        // confirmation modal
        var buildConfirmationModal =
            function(that){
                //var $body = $('body');
                //var $modal = $('#js-modal');
                //var focusableElementsString = "a[href], area[href], input:not([type='hidden']):not([disabled]), select:not([disabled]), textarea:not([disabled]), button:not([disabled]), iframe, object, embed, *[tabindex], *[contenteditable]";
                var ajaxUrl = that.getAttribute("data-confirm-messageUrl")
                if (ajaxUrl) {
                    $.ajax({
                        url: ajaxUrl
                    })
                        .done(function (data) {
                            $('#js-confirmation-content-term').html(data)
                        })
                        .fail(function (data) {
                            $('#js-confirmation-content-term').html('WARNING: AJAX-CALL FAILED')
                        })
                }

                var tokenMsg = that.getAttribute("data-confirm-tokenMsg") ? that.getAttribute("data-confirm-tokenMsg") : false;
                tokenMsg ? $('#js-confirmation-term').html(tokenMsg) : $("#js-confirmation-term").remove();

                var dataAttr = that.getAttribute("data-confirm-id")? that.getAttribute("data-confirm-id")+'_form':false;
                var how = that.getAttribute("data-confirm-term-how") ? that.getAttribute("data-confirm-term-how"):"delete";
                var url = that.getAttribute('href') && (that.getAttribute('class').indexOf('la-js-remoteLink') == -1) && (that.getAttribute('class') != 'js-gost') ? that.getAttribute('href'): false; // use url only if not remote link
                var $jscb = $('#js-confirmation-button')

                switch (how) {
                    case "delete":
                        $jscb.html(dict.get('confirm.dialog.delete',currLanguage) + '<i aria-hidden="true" class="trash alternate icon"></i>');
                        break;
                    case "unlink":
                        $jscb.html(dict.get('confirm.dialog.unlink',currLanguage) + '<i aria-hidden="true" class="la-chain broken icon"></i>');
                        break;
                    case "share":
                        $jscb.html(dict.get('confirm.dialog.share',currLanguage) + '<i aria-hidden="true" class="la-share icon"></i>');
                        break;
                    case "inherit":
                        $jscb.html(dict.get('confirm.dialog.inherit',currLanguage) + '<i aria-hidden="true" class="thumbtack icon"></i>');
                        break;
                    case "ok":
                        $jscb.html(dict.get('confirm.dialog.ok',currLanguage) + '<i aria-hidden="true" class="check icon"></i>');
                        break;
                    case "concludeBinding":
                        $jscb.html(dict.get('confirm.dialog.concludeBinding',currLanguage) + '<i aria-hidden="true" class="check icon"></i>');
                        break;
                    case "clearUp":
                        $jscb.html(dict.get('confirm.dialog.clearUp',currLanguage) + '<i aria-hidden="true" class="bath icon"></i>');
                        break;
                    default:
                        $('').html('Entfernen<i aria-hidden="true" class="x icon"></i>');
                }

                var remoteLink = $(that).hasClass('la-js-remoteLink')


                $('.tiny.modal')
                    .modal({
                        onShow : function() {
                            //only in form context
                            if (dataAttr) {
                                // only if the button that triggers the confirmation modal has the attribute value set
                                if ($(that).val()) {
                                    // than find the form that wraps the button
                                    // and insert the hidden field with name and value
                                    var name = $(that).attr('name')
                                    var  hiddenField = $('<input id="additionalHiddenField" type="hidden"/>')
                                        .attr( 'name',name )
                                        .val($(that).val());
                                    $('[data-confirm-id='+dataAttr+']').prepend(hiddenField);
                                }
                            }
                        },
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
                                bb8.ajax4remoteLink(that)
                            }
                            $('#js-confirmation-content-term').html('');
                        },
                        onDeny : function() {
                            $('#js-confirmation-content-term').html('');
                            // delete hidden field
                            if ($('#additionalHiddenField')) {
                                $('#additionalHiddenField').remove();
                            }
                        }
                        /*                        onShow : function() {
                                                    $modal.removeAttr('aria-hidden');
                                                    // is needed to hide the rest of the page from Screenreaders in case of open the modal
                                                    if ($('#js-modal-page').length === 0) { // just to avoid missing #js-modal-page
                                                        $body.wrapInner('<div id="js-modal-page"></div>');
                                                    }
                                                    $page = $('#js-modal-page');
                                                    $page.attr('aria-hidden', 'true');
                                                    $body.on("keydown", "#js-modal", function(event) {
                                                        var $this = $(this);
                                                        if (event.keyCode == 9) { // tab or Strg tab

                                                            // get list of all children elements in given object
                                                            var children = $this.find('*');

                                                            // get list of focusable items
                                                            var focusableItems = children.filter(focusableElementsString).filter(':visible');

                                                            // get currently focused item
                                                            var focusedItem = $(document.activeElement);

                                                            // get the number of focusable items
                                                            var numberOfFocusableItems = focusableItems.length;

                                                            var focusedItemIndex = focusableItems.index(focusedItem);

                                                            if (!event.shiftKey && (focusedItemIndex == numberOfFocusableItems - 1)) {
                                                                focusableItems.get(0).focus();
                                                                event.preventDefault();
                                                            }
                                                            if (event.shiftKey && focusedItemIndex == 0) {
                                                                focusableItems.get(numberOfFocusableItems - 1).focus();
                                                                event.preventDefault();
                                                            }
                                                        }

                                                    })
                                                },
                                                onHidden : function() {
                                                    $page.removeAttr('aria-hidden');
                                                    $modal.attr('aria-hidden', 'true');
                                                }*/
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


    countSettedFilters: function () {
        // DROPDOWN AND INPUT FIELDS
        $( document ).ready(function() {
            var dropdownFilter  = $('main > .la-filter .la-filter-dropdown-selected').length;
            var inputTextFilter = $('main > .la-filter .la-filter-selected').length;
            var calendarFilter  = $('main > .la-filter .la-calendar-selected').length;
            var checkboxFilter  = 0;

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

            if (total == 0) {
                $('.la-js-filter-total').addClass('hidden');
                $('.la-js-filterButton i').removeClass('hidden');

            } else {
                $('.la-js-filter-total').text(total);
                $('.la-js-filter-total').removeClass('hidden');
                $('.la-js-filterButton i').addClass('hidden');
            }
        });
    }
}
