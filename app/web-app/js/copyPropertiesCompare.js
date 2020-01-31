function jsConfirmation() {
    if ($( "td input[data-action='delete']" ).is( ":checked" )){
        return confirm('Wollen Sie wirklich diese(s) Element(e) in der Ziellizenz löschen?')
    }
}
// FOR ALL THE OTHER TABLES THEN PROPERTIES
function toggleAllCheckboxes(source) {
    var action = $(source).attr("data-action")
    var checkboxes = document.querySelectorAll('input[data-action="'+action+'"]');
    for (var i = 0; i < checkboxes.length; i++) {
        if (source.checked && ! checkboxes[i].checked) {
            $(checkboxes[i]).trigger('click')
        }
        else if (! source.checked && checkboxes[i].checked) {
            $(checkboxes[i]).trigger('click')
        }
    }
}

// ONLY FOR PROPERIES
var takeProperty = $('input[name="subscription.takeProperty"]');
var deleteProperty = $('input[name="subscription.deleteProperty"]');

function selectAllTake(source) {
    var table = $(source).closest('table');
    var thisBulkcheck = $(table).find(takeProperty);
    $( thisBulkcheck ).each(function( index, elem ) {
        elem.checked = source.checked;
        markAffectedTake($(this));
    })
}

function selectAllDelete(source) {
    var table = $(source).closest('table');
    var thisBulkcheck = $(table).find(deleteProperty);
    $( thisBulkcheck ).each(function( index, elem ) {
        elem.checked = source.checked;
        markAffectedDelete($(this));
    })
}

$(takeProperty).change( function() {
    markAffectedTake($(this));
});
$(deleteProperty).change( function() {
    markAffectedDelete($(this));
});

markAffectedTake = function (that) {
    var indexOfTakeCheckbox = ($(that).closest('.la-replace').index()) ;
    var numberOfCheckedTakeCheckbox = $(that).closest('td').find("[type='checkbox']:checked").length;
    var multiPropertyIndex = $(that).closest('tr').find('.la-copyElements-flex-container').index() ;
    var sourceElem = $(that).closest('tr').find('.la-colorCode-source');
    var targetElem = $(that).closest('tr').find('.la-colorCode-target');
    //  _
    // |x|
    //
    if ($(that).is(":checked") ) {
        // Properties with multipleOccurence do
        // - not receive a deletion mark because they are not overwritten but copied
        // - need to have the specific child of la-copyElements-flex-container
        if ($(that).attr('data-multipleOccurrence') == 'true') {
            sourceElem = $(that).closest('tr').find('.la-colorCode-source:nth-child(' + (indexOfTakeCheckbox + 1) + ')').addClass('willStay');
            sourceElem.addClass('willStay');
            targetElem.addClass('willStay'); // mark all the target elemnts green because they will not be deleted
        }
        else {
            sourceElem.addClass('willStay');
            targetElem.addClass('willBeReplaced');
        }
    }
    //  _
    // |_|
    //
    else {
        if ($(that).attr('data-multipleOccurrence') == 'true') {
            if (numberOfCheckedTakeCheckbox == 0 ) {
                targetElem.removeClass('willStay');
            }
            sourceElem = $(that).closest('tr').find('.la-colorCode-source:nth-child(' + (indexOfTakeCheckbox + 1) + ')').addClass('willStay');
            sourceElem.removeClass('willStay');
        }
        else {
            sourceElem.removeClass('willStay');
            if ( (that).parents('tr').find('input[name="subscription.deleteProperty"]').is(':checked')){
            } else {
                targetElem.removeClass('willBeReplaced');
            }
        }
    }
}
markAffectedDelete = function (that) {
    var indexOfDeleteCheckbox = ($(that).closest('.la-noChange').index()) ;
    var targetElem = $(that).closest('tr').find('.la-colorCode-target');
    //  _
    // |x|
    //
    if ($(that).is(":checked")) {
        if ($(that).attr('data-multipleOccurrence') == 'true') {
            targetElem = $(that).closest('tr').find('.la-colorCode-target:nth-child(' + (indexOfDeleteCheckbox + 1) + ')').addClass('willBeReplaced');
            targetElem.addClass('willBeReplaced')
        }
        else {
            targetElem.addClass('willBeReplaced')
        }
    }
    //  _
    // |_|
    //
    else {
        if ($(that).parents('tr').find('input[name="subscription.takeProperty"]').is(':checked')) {
            if ($(that).attr('data-multipleOccurrence') == 'true') {
                targetElem = $(that).closest('tr').find('.la-colorCode-target:nth-child(' + (indexOfDeleteCheckbox + 1) + ')').addClass('willBeReplaced');
                targetElem.removeClass('willBeReplaced');
            }
            else {
            }
        }
        else {
            targetElem.removeClass('willBeReplaced');
        }
    }
}


$(takeProperty).each(function( index, elem ) {
    if (elem.checked){
        markAffectedTake(elem)
    }
});