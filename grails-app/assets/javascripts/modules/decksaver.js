// module: assets/javascripts/modules/decksaver.js

// for future handling on other views
// 1. add class 'hidden' via markup to all cards that might be toggled
// 2. add class 'la-js-hideable' to all cards that might be toggled
// 3. add class 'la-js-dont-hide-this-card' to markup that is rendered only in case of card has content, like to a table <th>
deckSaver = {
    configs: {
        editMode : null, // from external
        ajaxUrl : null,  // from external
        // the trigger
        toggleButton : null,
        toggleIcon : null,
        // the target area
        areaThatIsAffected : null,
        card: {
            hidable : null
        },
        element: {
            hide : null,
            dontHideSurroundingCard : null,
            hideSurroundingCard : null,
        },
        button: {
            dontHide : null
        },
        icon : null
    },
    go: function() {
        console.log('deckSaver.go()')
        deckSaver.init();
        deckSaver.toggleEditableElements();
    },
    init: function() {
        deckSaver.configs.toggleButton = $(".ui.toggle.button")
        deckSaver.configs.toggleIcon = $(".ui.toggle.button .icon")

        deckSaver.configs.areaThatIsAffected = $(".la-show-context-orgMenu") // $("#collapseableSubDetails")
        deckSaver.configs.card = {
            hidable: $(".la-js-hideable")
        }
        deckSaver.configs.element = {
            hide: $(".la-js-hideMe"),
            dontHideSurroundingCard: $(".la-js-dont-hide-this-card"),
            hideSurroundingCard: $(".la-js-hide-this-card"),
        }
        deckSaver.configs.button = {
            dontHide: $(".la-js-dont-hide-button")
        }
        deckSaver.configs.icon = $(".la-js-editmode-icon")

        deckSaver.configs.toggleButton.click(function(){
            deckSaver.configs.editMode = !deckSaver.configs.editMode;
            $.ajax({
                url: deckSaver.configs.ajaxUrl,
                data: {
                    showEditMode: deckSaver.configs.editMode
                },
                success: function(){
                    deckSaver.toggleEditableElements();
                },
                complete: function () {
                }
            })
        });
    },
    removeClone: function () {
        $('.la-clone').remove();
    },
    removePopupFromClone: function () {
        var clone = $('.la-clone');
        var clonePopup = $(clone).popup('get popup');
        $(clonePopup).each(function () {
            $(this).remove();
        })

    },
    enableXeditable: function (cssClass) {
        var selection = $(cssClass).not('.ui.modal' + ' ' + cssClass);
        selection.editable('option', 'disabled', false);

    },
    disableXeditable: function (cssClass) {

        var selection = $(cssClass).not('.ui.modal' + ' ' + cssClass);
        selection.editable('option', 'disabled', true);

    },
    toggleEditableElements: function () {

        if (deckSaver.configs.editMode) {
            // ***************************
            // show Contoll Elements
            // ***************************
            $('body').removeClass('la-decksaver-active');

            $('.button').removeClass('hidden');
            deckSaver.removeClone();
            deckSaver.removePopupFromClone();

            $('.card').not('.ui.modal .card').removeClass('hidden');
            deckSaver.configs.element.hideSurroundingCard.removeClass('hidden');
            $('.ui .form').not('.ui.modal .ui.form').removeClass('hidden');
            deckSaver.configs.areaThatIsAffected.find('.button').removeClass('hidden');

            deckSaver.configs.toggleButton.removeAttr("data-content");
            deckSaver.configs.toggleButton.attr("data-content", JSPC.dict.get('statusbar.showButtons.tooltip', JSPC.currLanguage));
            tooltip.initializePopup_deprecated(deckSaver.configs.toggleButton);
            deckSaver.configs.toggleIcon.removeClass("slash");
            deckSaver.configs.toggleButton.addClass('active');

            deckSaver.enableXeditable('.xEditableValue');
            deckSaver.enableXeditable('.xEditableDatepicker');
            deckSaver.enableXeditable('.xEditableManyToOne');
            deckSaver.enableXeditable('.xEditableBoolean');

            $('.la-action-info').css('text-align', 'left').text(JSPC.dict.get('default.actions.label', JSPC.currLanguage))

            $('.la-js-toggle-hideThis').addClass('hidden');    // generic toggle selector - erms-4688
            $('.la-js-toggle-showThis').removeClass('hidden'); // generic toggle selector - erms-4688
        }
        else {
            // ***************************
            // hide Contoll Elements
            // ***************************
            $('body').addClass('la-decksaver-active');

            deckSaver.configs.icon = $(".la-js-editmode-icon");
            deckSaver.configs.icon.each(function () {
                var container = $(this).closest('.la-js-editmode-container');
                var button = $(this).closest('.button');
                var clone = $(this).clone();
                clone.appendTo(container);
                clone.addClass('la-clone grey');

                //transfer the tooltip-content from button to cloned icon
                var dataContent = button.attr("data-content");

                clone.attr('data-content', dataContent);
                tooltip.initializePopup_deprecated(clone);
            });

            $('.card').not('.ui.modal .card').removeClass('hidden');
            deckSaver.configs.card.hidable.not(":has(.la-js-dont-hide-this-card)").addClass('hidden');
            deckSaver.configs.element.hideSurroundingCard.addClass('hidden');
            $('.ui.form').not('.ui.modal .ui.form').addClass('hidden');

            deckSaver.configs.areaThatIsAffected.not('.ui.modal').find('.button').not('.ui.modal .button, .la-js-dont-hide-button').addClass('hidden');

            deckSaver.configs.toggleButton.removeAttr("data-content");
            deckSaver.configs.toggleButton.attr("data-content", JSPC.dict.get('statusbar.hideButtons.tooltip', JSPC.currLanguage));
            tooltip.initializePopup_deprecated(deckSaver.configs.toggleButton);

            deckSaver.configs.toggleIcon.addClass("slash");
            deckSaver.configs.toggleButton.removeClass('active');

            deckSaver.disableXeditable('.xEditableValue');
            deckSaver.disableXeditable('.xEditableDatepicker');
            deckSaver.disableXeditable('.xEditableManyToOne');
            deckSaver.enableXeditable('.xEditableBoolean');

            $('.la-action-info').css('text-align', 'right').text(JSPC.dict.get('default.informations', JSPC.currLanguage))

            $('.la-js-toggle-hideThis').removeClass('hidden'); // generic toggle selector - erms-4688
            $('.la-js-toggle-showThis').addClass('hidden');    // generic toggle selector - erms-4688
        }
    }
}

JSPC.modules.add( 'deckSaver', deckSaver );
