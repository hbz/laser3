
// submodules/decksaver.js

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

            $('.la-action-info').css('text-align', 'left').text(dict.get('default.actions.label',currLanguage))
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
