// module: assets/javascripts/modules/r2d2.js

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

                    if ('dd.mm.yyyy' == JSPC.config.dateFormat) {
                        return day + '.' + month + '.' + year;
                    }
                    else if ('yyyy-mm-dd' == JSPC.config.dateFormat) {
                        return year + '-' + month + '-' + day;
                    }
                    else {
                        alert('Please report this error: ' + JSPC.config.dateFormat + ' for ui-datepicker unsupported');
                    }
                }
            },
            text: {
                days: [
                    JSPC.dict.get('loc.weekday.short.Sunday', JSPC.config.language),
                    JSPC.dict.get('loc.weekday.short.Monday', JSPC.config.language),
                    JSPC.dict.get('loc.weekday.short.Tuesday', JSPC.config.language),
                    JSPC.dict.get('loc.weekday.short.Wednesday', JSPC.config.language),
                    JSPC.dict.get('loc.weekday.short.Thursday', JSPC.config.language),
                    JSPC.dict.get('loc.weekday.short.Friday', JSPC.config.language),
                    JSPC.dict.get('loc.weekday.short.Saturday', JSPC.config.language)
                ],
                months: [
                    JSPC.dict.get('loc.January', JSPC.config.language),
                    JSPC.dict.get('loc.February', JSPC.config.language),
                    JSPC.dict.get('loc.March', JSPC.config.language),
                    JSPC.dict.get('loc.April', JSPC.config.language),
                    JSPC.dict.get('loc.May', JSPC.config.language),
                    JSPC.dict.get('loc.June', JSPC.config.language),
                    JSPC.dict.get('loc.July', JSPC.config.language),
                    JSPC.dict.get('loc.August', JSPC.config.language),
                    JSPC.dict.get('loc.September', JSPC.config.language),
                    JSPC.dict.get('loc.October', JSPC.config.language),
                    JSPC.dict.get('loc.November', JSPC.config.language),
                    JSPC.dict.get('loc.December', JSPC.config.language)
                ]
            }
        },
        yearpicker : {
            type: 'year',
            onChange: function(date, text, mode) {
                // deal with colored input field only when in filter context
                if ($(this).parents('.la-filter').length) {
                    if (!text) {
                        $(this).removeClass("la-calendar-selected");
                    } else {
                        if( ! $(this).hasClass("la-calendar-selected") ) {
                            $(this).addClass("la-calendar-selected");
                        }
                    }
                }
            },
            onShow: function() {
                $('.ui.popup.calendar .table .link').attr( {
                    'role' : 'button'
                });
            },
            minDate: new Date('1582-10-15'), //this is the start of the gregorian calendar
            maxDate: new Date('2099-12-31'), //our grand-grandchildren may update this date ...
            /*formatter: {
                date: function (date, settings) {
                    if (!date) return '';
                    var day = date.getDate();
                    if (day<10) day="0"+day;
                    var month = date.getMonth() + 1;
                    if (month<10) month="0"+month;
                    var year = date.getFullYear();

                    if ('dd.mm.yyyy' == JSPC.config.dateFormat) {
                        return day + '.' + month + '.' + year;
                    }
                    else if ('yyyy-mm-dd' == JSPC.config.dateFormat) {
                        return year + '-' + month + '-' + day;
                    }
                    else {
                        alert('Please report this error: ' + JSPC.config.dateFormat + ' for ui-datepicker unsupported');
                    }
                }
            }*/
        }
    },

    go : function() {
        r2d2.initGlobalAjaxLogin();

        r2d2.initGlobalUiStuff();
        r2d2.initGlobalXEditableStuff();

        r2d2.initDynamicUiStuff('body');
        r2d2.initDynamicXEditableStuff('body');

        $('html').css('cursor', 'auto');
    },

    initGlobalAjaxLogin : function() {
        console.log('r2d2.initGlobalAjaxLogin()');

        $.ajaxSetup({
            statusCode: {
                401: function() {
                    $('*[class^=xEditable]').editable('hide');
                    loadAjaxLoginModal();
                }
            }
        });

        function loadAjaxLoginModal() {
            $.ajax({
                url: JSPC.config.ajax.openLogin,
                success: function (data) {
                    $('body').append(data);

                    $('#ajaxLoginModal').modal('setting', 'closable', false).modal('show');
                    $('#ajaxLoginForm').submit(function(event) {
                        event.preventDefault();
                        sendAjaxAuth();
                    });
                },
                error: function (xhr, textStatus, errorThrown) {
                    alert("Unbekannter Fehler. Bitte melden Sie sich erneut über die Startseite an.")
                    window.location.href = '/'
                }
            })
        }

        function sendAjaxAuth() {
            $.ajax({
                url: $('#ajaxLoginForm').attr('action'),
                data: $('#ajaxLoginForm').serialize(),
                method: 'POST',
                dataType: 'JSON',
                success: function (json, textStatus, xhr) {
                    if (json.success) {
                        $('#ajaxLoginForm')[0].reset();
                        $('#ajaxLoginMessage').empty();
                        $('#ajaxLoginModal').modal('hide');
                    }
                    else if (json.error) {
                        $('#ajaxLoginMessage').html('<div class="ui error message">' + json.error + '</div>');
                    }
                    else {
                        $('#loginMessage').html(xhr.responseText);
                    }
                },
                error: function (xhr, textStatus, errorThrown) {
                    if (xhr.status == 401 && xhr.getResponseHeader('Location')) {
                        // the login request itself wasn't allowed, possibly because the
                        // post url is incorrect and access was denied to it
                        $('#loginMessage').html('<div class="ui error message">Unbekannter Fehler beim Login. Melden Sie sich bitte über die Startseite an.</div>');
                    }
                    else {
                        var responseText = xhr.responseText;
                        if (responseText) {
                            var json = $.parseJSON(responseText);
                            if (json.error) {
                                $('#loginMessage').html('<div class="ui error message">' + json.error + '</div>');
                                return;
                            }
                        }
                        else {
                            responseText = 'Status: ' + textStatus + ', Fehler: ' + errorThrown + ')';
                        }
                        $('#ajaxLoginMessage').html('<div class="ui error message">' + responseText + '</div>');
                    }
                }
            })
        }
    },

    initGlobalUiStuff : function() {
        console.log("r2d2.initGlobalUiStuff()");

        // universal copy item
        $('.js-copyTrigger').click(function(){
            var element = $(this).parents('.js-copyTriggerParent').find('.js-copyTopic');
            var html = $(element).html();
            var $temp = $("<input>");
            var dontShow;
            $(element).hasClass('la-display-none') ? dontShow = true : dontShow = false;
            $("body").append($temp);
            $temp.val($.trim($(element).text())).select();
            document.execCommand("copy");
            clearTimeout(timeout);
            dontShow ?  $(element).css('display','inline'): $(element).addClass('');
            $(element).html(JSPC.dict.get('copied', JSPC.config.language));
            var timeout = setTimeout(function() {
                $(element).html(html);
                dontShow ? $(element).css('display','none') : $(element).addClass('');
            }, 2000); // change the HTML after 2 seconds
            $temp.remove();
        });
        $('.js-copyTrigger').hover(
            function(){ $(this).parent().find('.la-js-copyTriggerIcon').addClass('open') },
            function(){ $(this).parent().find('.la-js-copyTriggerIcon').removeClass('open') }
        )

        $('.js-linkGoogle').hover(
            function(){ $(this).removeClass('alternate') },
            function(){ $(this).addClass('alternate') }
        )

        //JS Library readmore.js
        $('.la-readmore').readmore({
            speed: 75,
            lessLink: '<a href="#">' + JSPC.dict.get('link.readless', JSPC.config.language) + '</a>',
            moreLink: '<a href="#">' + JSPC.dict.get('link.readmore', JSPC.config.language) + '</a>',
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
                if (type == 'empty') {
                    html += '' + '<div class="header">' + JSPC.dict.get('search.API.heading.noResults', JSPC.config.language) + '</div class="header">' + '<div class="description">' + message + '</div class="description">';
                } else {
                    html += ' <div class="description">' + message + '</div>';
                }
                html += '</div>';
            }
            return html;
        };

        // ----- filter -----

        $('.la-filter').on( 'submit.laser', function() {
            let fields = $(this).find('input[type=text], input[type=hidden], select');
            let emptyFields = fields.filter(function(){ return !this.value; });
            // remove unused filter params
            emptyFields.attr('disabled', 'disabled');
        });

        // ----- spotlight -----

        $('.ui.search.spotlight').search({
            error : {
                source          : '"' + JSPC.dict.get('search.API.source', JSPC.config.language) + '"',
                noResults       : '',
                logging         : '"' + JSPC.dict.get('search.API.logging', JSPC.config.language) + '"',
                noEndpoint      : '"' + JSPC.dict.get('search.API.noEndpoint', JSPC.config.language) + '"',
                noTemplate      : '"' + JSPC.dict.get('search.API.noTemplate', JSPC.config.language) + '"',
                serverError     : '"' + JSPC.dict.get('search.API.serverError', JSPC.config.language) + '"',
                maxResults      : '"' + JSPC.dict.get('search.API.maxResults', JSPC.config.language) + '"',
                method          : '"' + JSPC.dict.get('search.API.method', JSPC.config.language) + '"'
            },

            type: 'category',
            minCharacters: 3,
            apiSettings: {

                url: JSPC.config.searchSpotlightSearch + "/?query={query}",
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
                }
            }
        });

        // ----- pagination -----

        $('nav.pagination').each (function () {
            const $pagination = $(this)

            const $input      = $pagination.find('.la-pagination-custom-input');
            const $inputInput = $pagination.find('.la-pagination-custom-input input');
            const $inputForm  = $pagination.find('.la-pagination-custom-input .ui.form');
            const $link       = $pagination.find('.la-pagination-custom-link');

            let stepsValue = $input.attr('data-steps');
            let baseHref   = $link.attr('href');

            $.fn.form.settings.rules.smallerEqualThanTotal = function (inputValue, validationValue) {
                return parseInt(inputValue) <= validationValue;
            };
            $.fn.form.settings.rules.biggerThan = function (inputValue, validationValue) {
                return parseInt(inputValue) > validationValue;
            };

            $inputInput
                .on ('input', function() {
                    let newOffset = ($(this).val() - 1) * $input.attr('data-max');
                    $link.attr('href', baseHref + '&offset=' + newOffset);
                })
                .bind ('keypress', function(event) {
                    if (event.keyCode === 13){
                        if ( validateInput() ) {
                            $('html').css('cursor', 'wait');
                            location.href = $link.attr('href');
                        }
                        else { event.preventDefault(); }
                    }
                });

            $link.on ('click', function(event) {
                if ( validateInput() ) {
                    $('html').css('cursor', 'wait');
                }
                else { event.preventDefault(); }
            });

            let validateInput = function() {
                $inputForm.form({
                    inline: true,
                    fields: {
                        paginationCustomValidation: {
                            identifier: $inputInput.attr('data-validate'),
                            rules: [
                                {
                                    type: "empty", prompt: JSPC.dict.get('pagination.keyboardInput.validation.integer', JSPC.config.language)
                                },
                                {
                                    type: "integer", prompt: JSPC.dict.get('pagination.keyboardInput.validation.integer', JSPC.config.language)
                                },
                                {
                                    type: "smallerEqualThanTotal[" + stepsValue + "]", prompt: JSPC.dict.get('pagination.keyboardInput.validation.smaller', JSPC.config.language)
                                },
                                {
                                    type: "biggerThan[0]", prompt: JSPC.dict.get('pagination.keyboardInput.validation.biggerZero', JSPC.config.language)
                                }
                            ]
                        }
                    },
                    onInvalid: function() { return false; },
                    onValid: function()   { return true; }
                });

                return $inputForm.form('validate form');
            }
        })

        // ----- other stuff -----

        $('.dropdown.la-js-sorting').on('change', function(e) {
            //console.log($(this).find('option:contains("'+$(this).val()+'")').attr('data-value'));
            let url = new URL(window.location.href)
            let urlParams = new URLSearchParams(url.search)
            if(urlParams.has('sort'))
                urlParams.set('sort', $(this).find('option:contains("'+$(this).val()+'")').attr('data-value'));
            else
                urlParams.append('sort', $(this).find('option:contains("'+$(this).val()+'")').attr('data-value'));
            if(urlParams.has('order'))
                urlParams.set('order', $(this).find('option:contains("'+$(this).val()+'")').attr('data-order'));
            else
                urlParams.append('order', $(this).find('option:contains("'+$(this).val()+'")').attr('data-order'));
            url.search = urlParams.toString()
            window.location.href = url.toString()
        });
    },


    initGlobalXEditableStuff : function() {
        console.log("r2d2.initGlobalXEditableStuff()");

        $.fn.editable.defaults.mode = 'inline'
        $.fn.editableform.buttons = '<button aria-label="' + JSPC.dict.get('xEditable.button.ok', JSPC.config.language) + '" type="submit" class="ui icon button editable-submit"><i aria-hidden="true" class="check icon"></i></button>' +
            '<button aria-label="' + JSPC.dict.get('xEditable.button.cancel', JSPC.config.language) + '" type="button" class="ui icon button editable-cancel"><i aria-hidden="true" class="times icon"></i></button>'
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

        // TODO $.fn.datepicker.defaults.language = JSPC.config.language
    },


    initDynamicXEditableStuff : function(ctxSel) {
        console.log("r2d2.initDynamicXEditableStuff( " + ctxSel + " )");

        if (! ctxSel) { return null }

        $(ctxSel + ' .xEditableValue').editable({

            highlight: false,
            language: JSPC.config.language,
            format:   JSPC.config.dateFormat,
            validate: function(value) {
                if ($(this).attr('data-format') && value) {
                    if($(this).attr('data-format') === 'YYYY') {
                        if(! (value.match(/^\d{4}$/) ) ) {
                            return  JSPC.dict.get('xEditable.validation.dataFormat', JSPC.config.language);
                        }
                    }
                    else {
                        if(! (value.match(/^\d{1,2}\.\d{1,2}\.\d{4}$/) || value.match(/^\d{4}-\d{1,2}-\d{1,2}$/)) ) {
                            return  JSPC.dict.get('xEditable.validation.dataFormat', JSPC.config.language);
                        }
                    }
                }
                // custom validate functions via ui:xEditable.validation."xy"
                var dVal = $(this).attr('data-validation')
                if (dVal) {
                    if (dVal.includes('notEmpty')) {
                        if($.trim(value) == '') {
                            return  JSPC.dict.get('xEditable.validation.notEmpty', JSPC.config.language);
                        }
                    }
                    if (dVal.includes('url')) {
                        var regex = /^(https?|ftp):\/\/(.)*/;
                        var test = regex.test($.trim(value)) || $.trim(value) == ''
                        if (! test) {
                            //return "Ein URL muss mit 'http://' oder 'https://' oder 'ftp://' beginnen."
                            return  JSPC.dict.get('xEditable.validation.url', JSPC.config.language);
                        }
                    }
                    if (dVal.includes('email')) {
                        let regex = /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)+$/
                        let test = regex.test($.trim(value)) || $.trim(value) === ''
                        if(!test) {
                            return  JSPC.dict.get('xEditable.validation.mail', JSPC.config.language)
                        }
                    }
                    if (dVal.includes('leitwegID')) {
                        let regex = /^([0-9]{2,12})+-+([a-zA-Z0-9]{0,30})+-+([0-9]{2,2})+$/
                        let test = regex.test($.trim(value)) || $.trim(value) === ''
                        if(!test) {
                            return  JSPC.dict.get('xEditable.validation.leit', JSPC.config.language)
                        }
                    }
                    if (dVal.includes('number')) {
                        let regex =  /^[0-9]+$/;
                        let test = regex.test($.trim(value)) || $.trim(value) === ''
                        if(!test) {
                            return  JSPC.dict.get('xEditable.validation.number', JSPC.config.language);
                        }
                    }
                    if (dVal.includes('datesCheck')) {
                        var thisInput = $.trim(value), startDateInput, endDateInput, startDate, endDate;
                        if($(this).attr("data-name") === "startDate") {
                            startDateInput = thisInput;
                            endDateInput = $('a[data-name="endDate"][data-pk="'+$(this).attr("data-pk")+'"]').text();
                        }
                        else if($(this).attr("data-name") === "endDate") {
                            startDateInput = $('a[data-name="startDate"][data-pk="'+$(this).attr("data-pk")+'"]').text();
                            endDateInput = thisInput
                        }
                        if(startDateInput !== '' && endDateInput !== '') {
                            startDate = Date.parse(JSPC.helper.formatDate(startDateInput));
                            endDate = Date.parse(JSPC.helper.formatDate(endDateInput));
                            console.log(startDate+" "+endDate);
                            if(startDate > endDate)
                                return  JSPC.dict.get('xEditable.validation.endDateNotBeforStartDate', JSPC.config.language);
                        }
                    }
                    if (dVal.includes('maxlength')) {
                        if(value.length > $(this).attr("data-maxlength")) {
                            return   JSPC.dict.get('xEditable.validation.tooLong', JSPC.config.language)

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
                if($(this).attr('data-format') === 'YYYY')
                    $(ctxSel + ' .xEditable-datepicker').calendar(r2d2.configs.yearpicker);
                else
                    $(ctxSel + ' .xEditable-datepicker').calendar(r2d2.configs.datepicker);
                $(ctxSel + ' .editable-clear-x').click(function() {
                    $('.xEditable-datepicker.calendar').calendar('clear');
                });
            }else {
                var dType = $(this).attr('data-type')
                if (dType == "text" && $(this).attr('data-validation') && $(this).attr('data-validation').includes("maxlength")) {
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
        });

        $(ctxSel + ' .xEditableDatepicker').editable({});

        $(ctxSel + ' .xEditableManyToOne').editable({
            tpl: '<select class="ui search selection dropdown clearable "></select>',
            validate: function(value) {
                var dVal = $(this).attr('data-validation')
                if (dVal) {
                    if (dVal.includes('notEmpty')) {
                        if($.trim(value) == '') {
                            return "Das Feld darf nicht leer sein";
                        }
                    }
                }
            },
            success: function(response, newValue) {
                if(response.status == 'error') return response.msg; //msg will be shown in editable form
            }
        }).on('shown', function(e, obj) {
            obj.input.$input.dropdown({clearable: true}) // reference to current dropdown
        });

        // boolean values are only allowed to be 0 or 1, so clearable is not suitable
        // role values are not allowed to be null, so clearable is not suitable
        $(ctxSel + ' .xEditableBoolean, ' + ctxSel + ' .xEditableRole').editable({
            tpl: '<select class="ui search selection dropdown clearable"></select>',
            success: function(response, newValue) {
                if(response.status == 'error') return response.msg; //msg will be shown in editable form
            }
        }).on('shown', function(e, obj) {
            obj.input.$input.dropdown({clearable: false}) // reference to current dropdown
        });
    },


    initDynamicUiStuff : function(ctxSel) {
        console.log("r2d2.initDynamicUiStuff( " + ctxSel + " )")

        if (! ctxSel) { return null }
        let confirmationModalXeditableFlag = false;

        //tooltip
        tooltip.init(ctxSel);

        $(ctxSel + " a[href], " + ctxSel + " input.js-wait-wheel").not("a[href^='#'], a[href*='ajax'], a[href*='mailto'], a[target='_blank'], .js-open-confirm-modal, a[data-tab], a[data-content], .close, .js-no-wait-wheel, .trigger-modal").click(function() {
            $('html').css('cursor', 'wait');
        });

        // selectable table to avoid button is showing when focus after modal closed
        $(ctxSel + ' .la-selectable').hover(function() {
            $( ".button" ).blur();
        });

        // close ui:messages alerts
        $(ctxSel + ' .close.icon').click(function() {
            $(this).parent().transition('fade');

        });

        // modals
        $(ctxSel + " *[data-ui='modal']").click(function() {
            var $triggerElement = $(this)
            var href = $(this).attr('data-href')
            if (! href) {
                href = $(this).attr('href')
            }
            let keyboardHandler = function(e) {
                if (e.keyCode === 27) {
                    $(href + '.ui.modal').modal('hide');
                }
            };
            $(href + '.ui.modal').modal({
                onVisible: function() {
                    $(this).find('.datepicker').calendar(r2d2.configs.datepicker);
                    $(this).find('.yearpicker').calendar(r2d2.configs.yearpicker);

                    r2d2.helper.focusFirstFormElement(this);

                    let modalCallbackFunction = JSPC.callbacks.modal.onVisible[$(this).attr('id')];
                    if (typeof modalCallbackFunction === "function") {
                        console.debug('%cJSPC.callbacks.modal.onVisible found: #' + $(this).attr('id') + ' - trigger: ' + $triggerElement.attr('id'), 'color:grey');
                        modalCallbackFunction($triggerElement);
                    }
                },
                detachable: true,
                autofocus: false,
                transition: 'scale',
                onApprove : function() {
                    $(this).find('.ui.form').submit();
                    return false;
                },
                onShow : function() {
                    a11yModal.go({
                        el: document.getElementById($(this).attr('id')),
                        focusElement: '',
                        escCallback:''
                    });
                    document.addEventListener('keyup', keyboardHandler);

                    let modalCallbackFunction = JSPC.callbacks.modal.onShow[$(this).attr('id')];
                    if (typeof modalCallbackFunction === "function") {
                        console.debug('%cJSPC.callbacks.modal.onShow found: #' + $(this).attr('id') + ' - trigger: ' + $triggerElement.attr('id'), 'color:grey');
                        modalCallbackFunction($triggerElement);
                    }
                },
                onHide : function() {
                    document.removeEventListener('keyup', keyboardHandler);
                }
            }).modal('show')
        });

        // accordions

        $(ctxSel + ' .ui.accordion').not('.la-accordion-showMore').accordion();

        $(ctxSel + ' .la-accordion-showMore').accordion({
                exclusive: false
            }
        );
        $('.la-js-closeAll-showMore').on('click', function () {
            $('.accordion.la-accordion-showMore.la-js-showMoreCloseArea .la-accordion-segments .segment.content').each(function (i, e) {
                if ($(e).hasClass("active")) {
                    $('.la-accordion-showMore').accordion("close", i);
                }
            });
        });


        $(ctxSel + ' .ui.la-metabox.accordion').accordion({

            onOpening: function() {
                //$(".table").trigger('reflow');
                $(".la-metabox ").css({'box-shadow':'0 1px 10px 0 rgb(34 36 38 / 40%)','transition' :'box-shadow 0.3s ease-in-out'});
            },
            onOpen: function() {
                //$(".table").trigger('reflow')
            },
            onClose: function() {
                $(".la-metabox ").css('box-shadow','none');
            }
        });
        $(ctxSel + ' .accordion.la-accordion-showMore').find('a, .la-js-notOpenAccordion').click(function(event){
            event.stopPropagation();
        });


        // tabs
        $(ctxSel + ' .la-tab-with-js .item').tab({
            onVisible: function(){
                var longerModal = $('.longer.modal');
                if (longerModal.length != 0) {
                    longerModal.modal('refresh');
                }
            }
        });

        // checkboxes
        $(ctxSel + ' .ui.checkbox').not('#la-advanced').checkbox();

        // datepicker + yearpicker
        $(ctxSel + ' .datepicker').calendar(r2d2.configs.datepicker);
        $(ctxSel + ' .yearpicker').calendar(r2d2.configs.yearpicker);

        $(ctxSel + ' form').attr('autocomplete', 'off');

        // tables
        $(ctxSel + ' .la-hover-table tbody tr td').hover(
            function(){ $(this).parent('tr').addClass('la-active') },
            function(){ $(this).parent('tr').removeClass('la-active') }
        )

        // DROPDOWN

        // simple dropdown
        $(ctxSel + ' .ui.dropdown').not('#mainMenue .ui.dropdown').dropdown({
            selectOnKeydown: false
        });
        // search dropdown
        $(ctxSel + ' .ui.search.dropdown').dropdown({
            forceSelection: false,
            selectOnKeydown: false,
            fullTextSearch: 'exact',
            message: {noResults:JSPC.dict.get('select2.noMatchesFound', JSPC.config.language)}
        });

        // FILTER
        // simple dropdown
        $(ctxSel + ' .la-filter .ui.dropdown').dropdown({
            selectOnKeydown: false,
            onChange: function(value, text, $choice){
                (value !== '') ? _addFilterDropdown(this) : _removeFilterDropdown(this);
            }
        });
        // search dropdown
        $(ctxSel + ' .la-filter .ui.search.dropdown').dropdown({
            forceSelection: false,
            selectOnKeydown: false,
            fullTextSearch: 'exact',
            message: {noResults:JSPC.dict.get('select2.noMatchesFound', JSPC.config.language)},
            onChange: function(value, text, $choice){
                (value !== '') ? _addFilterDropdown(this) : _removeFilterDropdown(this);
            }
        });
        $(ctxSel + ' .la-filter .ui.dropdown.allowAdditions').dropdown({
            allowAdditions: true,
            forceSelection: false,
            hideAdditions: false,
            message: {addResult:JSPC.dict.get('dropdown.message.addResult', JSPC.config.language)},
            onChange: function(value, text, $choice){
                (value !== '') ? _addFilterDropdown(this) : _removeFilterDropdown(this);
            }
        });



        // dropdowns escape
        $(ctxSel + ' .la-filter .ui.dropdown').on('keydown', function(e) {
            if(['Escape','Backspace','Delete'].includes(event.key)) {
                //e.preventDefault();
                $(this).dropdown('clear').dropdown('hide').removeClass("la-filter-dropdown-selected");
            }
        });

        $(ctxSel + ' .la-filter .ui.dropdown').each(function(index, elem){
            if ($(elem).dropdown('get value') != "") {
                _addFilterDropdown(elem);
            }
            r2d2.countSettedFilters();
        })

        function _addFilterDropdown(elem) {
            $(elem).is('select') ? $( elem ).parent().addClass("la-filter-dropdown-selected" ) : $( elem ).addClass("la-filter-dropdown-selected" );
        }

        function _removeFilterDropdown(elem) {
            $(elem).is('select') ? $( elem ).parent().removeClass("la-filter-dropdown-selected" ) : $( elem ).removeClass("la-filter-dropdown-selected" );
        }

        $(ctxSel + '.la-filter .checkbox').checkbox();

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
        function _buildConfirmationModal(elem) {
                var ajaxUrl = elem ? elem.getAttribute("data-confirm-messageUrl") : false;

                if (ajaxUrl) {
                    $.ajax({
                        url: ajaxUrl
                    })
                        .done(function (data) {
                            if(elem.getAttribute("data-confirm-replaceHeader"))
                                $('#js-modal [class="header"]').html(data)
                            else
                                $('#js-confirmation-content-term').html(data)
                        })
                        .fail(function (data) {
                            $('#js-confirmation-content-term').html('WARNING: AJAX-CALL FAILED')
                        })
                }

                var tokenMsg = elem ? elem.getAttribute("data-confirm-tokenMsg") : false;
                tokenMsg ? $('#js-confirmation-term').html(tokenMsg) : $("#js-confirmation-term").remove();

                var dataAttr = elem ? elem.getAttribute("data-confirm-id")+'_form':false;
                var how = elem ? elem.getAttribute("data-confirm-term-how"):"delete";
                if (elem) {
                    var url = elem.getAttribute('href') && (elem.getAttribute('class').indexOf('la-js-remoteLink') == -1) && (elem.getAttribute('class') != 'js-gost') ? elem.getAttribute('href'): false; // use url only if not remote link
                }
                var $jscb = $('#js-confirmation-button')

                let map = {
                    'delete'            : JSPC.icons.CMD.DELETE,
                    'unlink'            : JSPC.icons.CMD.UNLINK,
                    'unset'             : JSPC.icons.CMD.ERASE,
                    'share'             : JSPC.icons.SIG.SHARED_OBJECT_ON,
                    'inherit'           : JSPC.icons.SIG.INHERITANCE,
                    'ok'                : JSPC.icons.SYM.YES,
                    'concludeBinding'   : JSPC.icons.SYM.YES
                }
                if (map[how]) {
                    $jscb.html(JSPC.dict.get('confirm.dialog.' + how, JSPC.config.language) + '<i aria-hidden="true" class="' + map[how] + '"></i>');
                } else {
                    $('').html('Entfernen<i aria-hidden="true" class="' + JSPC.icons.SYM.NO + '"></i>');
                }

                var remoteLink = $(elem).hasClass('la-js-remoteLink')

                $('.tiny.modal')
                    .modal({
                        onShow : function() {
                            a11yModal.go({
                                el: document.getElementById($(this).attr('id')),
                                focusElement: '',
                                escCallback:''
                            });
                            keyboardHandler = function (e) {
                                if (e.keyCode === 27) {
                                    $(this).modal('hide');
                                }
                            }
                            this.addEventListener('keyup', keyboardHandler);

                            //only in form context
                            if (dataAttr) {
                                // only if the button that triggers the confirmation modal has the attribute value set
                                if ($(elem).val()) {
                                    // than find the form that wraps the button
                                    // and insert the hidden field with name and value
                                    var name = $(elem).attr('name')
                                    var  hiddenField = $('<input id="additionalHiddenField" type="hidden"/>')
                                        .attr( 'name',name )
                                        .val($(elem).val());
                                    $('[data-confirm-id='+dataAttr+']').prepend(hiddenField);
                                }
                            }
                        },
                        onHide : function() {
                            this.removeEventListener('keyup', keyboardHandler);
                        },
                        closable  : false,
                        onApprove : function() {
                            // open confirmation modal from inside a form
                            if (dataAttr) {
                                $('[data-confirm-id='+dataAttr+']').submit();
                            }
                            // open confirmation modal and open a new url after conirmation
                            if (url) {
                                window.location.href = url;
                            }
                            if (remoteLink) {
                                bb8.ajax4remoteLink(elem)
                            }
                            // custom callback calls
                            if ($(elem).attr('data-callback')) {
                                let callback = JSPC.app[$(elem).attr('data-callback')];
                                if (callback) { callback($(elem)); }
                            }
                            // x-editable
                            if (confirmationModalXeditableFlag == true) {
                                $(document).on('click', '#js-confirmation-button', function(event) {
                                    confirmationModalXeditableFlag = true;
                                    $('button.editable-submit').click();
                                });
                            }
                            $('#js-confirmation-content-term').html('');
                        },
                        onDeny : function() {
                            $('#js-confirmation-content-term').html('');
                            // delete hidden field
                            if ($('#additionalHiddenField')) {
                                $('#additionalHiddenField').remove();
                            }
                            // x-editable
                            if (confirmationModalXeditableFlag == true) {
                                $('button.editable-cancel').click();
                            }
                        }

                    })
                    .modal('show');
        }

        // for links and submit buttons
        $(ctxSel + ' .js-open-confirm-modal').click(function(e) {
            e.preventDefault();
            _buildConfirmationModal(this);
        });

        // x-editable xEditableRefData - one refData was selected
        $('.js-open-confirm-modal-xEditableRefData')
            .next('.editable-container.editable-inline')
            .find('.button.editable-submit')
            .click(function(e) {

            if ($('.js-open-confirm-modal-xEditableRefData').next('.editable-container.editable-inline').find('select').val() ==  $('.js-open-confirm-modal-xEditableRefData').data('confirm-value')) {

                if (confirmationModalXeditableFlag == false) {
                    e.preventDefault();
                    confirmationModalXeditableFlag = true;

                    let x = $('.js-open-confirm-modal-xEditableRefData')[0];
                    _buildConfirmationModal(x);
                }
            }
        });
        // x-editable
        $('.js-open-confirm-modal-xEditable')
        .next('.editable-container.editable-inline')
        .find('.button.editable-submit')
        .click(function(e) {
            if (confirmationModalXeditableFlag == false) {
                e.preventDefault();
                confirmationModalXeditableFlag = true;

                let x = $('.js-open-confirm-modal-xEditable')[0];
                _buildConfirmationModal(x);
            }
        });
        // for old remote links = ajax calls
        $(ctxSel + ' .js-open-confirm-modal-copycat').click(function(e) {
            var onclickString = $(this).next('.js-gost').attr("onclick");
            $('#js-confirmation-button').attr("onclick", onclickString);
            var gostObject = $(this).next('.js-gost');
            _buildConfirmationModal(gostObject[0]);
        });

        r2d2.initCoreComponents(ctxSel);
    },

    initCoreComponents : function (ctxSel) {

        // new ui core components - do NOT modify
        // new ui core components - do NOT modify

        $(ctxSel + ' .cc-toggle > .toggle').checkbox({
            onChecked : function() {
                jQuery.ajax({
                    type: 'POST', url: '/ajax/genericSetData',
                    data: { name: $(this).data('name'), value: 'de.laser.RefdataValue:1', pk: $(this).data('pk') } // TODO
                })
            },
            onUnchecked : function() {
                jQuery.ajax({
                    type: 'POST', url: '/ajax/genericSetData',
                    data: { name: $(this).data('name'), value: 'de.laser.RefdataValue:2', pk: $(this).data('pk') } // TODO
                })
            }
        })

        $(ctxSel + ' .cc-boogle > .toggle').checkbox({
            onChecked : function() {
                jQuery.ajax({
                    type: 'POST', url: '/ajax/editableSetValue',
                    data: { name: $(this).data('name'), value: 1, pk: $(this).data('pk') } // TODO
                })
            },
            onUnchecked : function() {
                jQuery.ajax({
                    type: 'POST', url: '/ajax/editableSetValue',
                    data: { name: $(this).data('name'), value: 0, pk: $(this).data('pk') } // TODO
                })
            }
        })
    },

    countSettedFilters : function () {
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
            _countSettedCheckboxes(uniquecheckboxNames);
            function _countSettedCheckboxes(params) {
                var sumCheck = 0;
                for (i=0; i<params.length; i++) {
                    var checkboxName = params[i];
                    $('input[name='+ checkboxName +']').is(':checked')? sumCheck=sumCheck+1: sumCheck= sumCheck;
                }
                checkboxFilter = sumCheck;
            }

            // COUNT ALL SELECTIONS IN TOTAL
            var total = dropdownFilter + inputTextFilter + calendarFilter + checkboxFilter;

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

    helper : {

        focusFirstFormElement: function (elem) {
            // console.log('r2d2.helper.focusFirstFormElement: #' + $(elem).attr('id') + ' .(' + $(elem).attr('class') + ')');

            let ffe = $(elem).find('input:not([disabled]):not([type=hidden]), textarea:not([disabled]):not([type=hidden])').first();
            if (ffe) {
                ffe.focus();
            }
        },

        resetModalForm: function (modalCssSel) {
            console.log('r2d2.helper.resetModalForm: ' + modalCssSel);

            let $modal = $(modalCssSel);
            let $form = $modal.find('.content form');

            $form.form('reset');
            $form.find('.field > input[type=text][name]').val('');
            $form.find('.field > input[type=file][name]').val('');
            $form.find('.field > textarea[name]').val('');
            $form.find('.field > .dropdown > select[name]').dropdown('restore defaults');
            $form.find('.field > .calendar').calendar('clear');

            // documents/_modal - todo

            $form.find('dl > dd input[type=text][name]').val('');
            $form.find('dl > dd input[type=file][name]').val('');
            $form.find('dl > dd input[type=checkbox][name]').prop('checked', '')
            $form.find('dl > dd .dropdown > select[name]').dropdown('restore defaults');
        },

        clearGlobalModalsContainer: function () {
            console.log('r2d2.helper.clearGlobalModalsContainer: ' + modalCssSel);

            $('.ui.dimmer.modals.page').empty();
        }
    }
}

JSPC.modules.add( 'r2d2', r2d2 );
