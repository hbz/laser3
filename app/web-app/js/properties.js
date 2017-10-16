
function initPropertiesScript(ajaxurl, contextId, tenantId){
    // fallback for hardcoded id
    if(!contextId){
        contextId = "#custom_props_div"
    }
    console.log( "initPropertiesScript " + ajaxurl + " : " + contextId + " : " + tenantId)

    refdatacatsearch(ajaxurl, contextId)
    searchProp(ajaxurl, contextId, tenantId)
    showModalOnSelect(contextId)
    showHideRefData(contextId)
    hideModalOnSubmit(contextId)
    //Needs to run to make the xEditable visible
    $('.xEditableValue').editable()
    $('.xEditableManyToOne').editable()
}

function refdatacatsearch (ajaxurl, contextId){
    console.log( "refdatacatsearch " + ajaxurl + " : " + contextId)

    $("#cust_prop_refdatacatsearch").select2({
        placeholder: "Type category...",
        minimumInputLength: 1,
        ajax: { // instead of writing the function to execute the request we use Select2's convenient helper
            url: ajaxurl,
            dataType: 'json',
            data: function (term, page) {
                return {
                    q: term, // search term
                    page_limit: 10,
                    baseClass:'com.k_int.kbplus.RefdataCategory'
                };
            },
            results: function (data, page) {
                return {results: data.values};
            }
        }
    });
}

function searchProp(ajaxurl, contextId, tenantId){
    console.log( "searchProp " + ajaxurl + " : " + contextId + " : " + tenantId)
    // store
    var desc = $(contextId + " .customPropSelect").attr('desc')

    $(contextId + " .customPropSelect").select2({
        placeholder: "Search for a property...",
        minimumInputLength: 0,
        ajax: { // instead of writing the function to execute the request we use Select2's convenient helper
            url: ajaxurl,
            dataType: 'json',
            data: function (term, page) {
                return {
                    q: term, // search term
                    desc: desc,
                    page_limit: 10,
                    baseClass:'com.k_int.properties.PropertyDefinition',
                    tenant: tenantId
                };
            },
            results: function (data, page) {
                return {results: data.values};
            }
        },
        createSearchChoice:function(term, data) {
            return {id:-1, text:"New Property: "+term};
        }
    });
}

// TODO -refactoring
function showModalOnSelect(contextId){
    console.log( "showModalOnSelect " + contextId)

    $(contextId + " .customPropSelect").on("select2-selecting", function(e) {
        if(e.val == -1){
            var selectedText = e.object.text;
            selectedText = selectedText.replace("New Property: ","")
            $("input[name='cust_prop_name']" ).val(selectedText);
            $('#cust_prop_add_modal').modal('show');

        }
    });
}
// TODO -refactoring
function showHideRefData(contextId) {
    console.log( "showHideRefData " + contextId)

    $('#cust_prop_modal_select').change(function() {
        var selectedText = $( "#cust_prop_modal_select option:selected" ).val();
        if( selectedText == "class com.k_int.kbplus.RefdataValue") {
            $("#cust_prop_ref_data_name").show();
        }else{
            $("#cust_prop_ref_data_name").hide();
        }
    });
}
// TODO -refactoring
function hideModalOnSubmit(contextId){
    console.log( "hideModalOnSubmit " + contextId)

    $("#new_cust_prop_add_btn").click(function(){
        $('#cust_prop_add_modal').modal('hide');
    });
}
