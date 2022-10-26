// module: assets/javascripts/modules/c3po.js

c3po = {

    PROP_SEARCH_NATIVE: 'PROP_SEARCH_NATIVE',
    PROP_SEARCH_GROUPED: 'PROP_SEARCH_GROUPED',

    initProperties: function (ajaxurl, cssId, tenantId) {
        // fallback for hardcoded id
        if (!cssId) {
            cssId = "#custom_props_div"
        }
        console.log ('c3po.initProperties( ' + ajaxurl + ', ' + cssId + ', ' + tenantId + ' )')

        c3po.refdataCatSearch(ajaxurl, cssId)
        c3po.searchProp(c3po.PROP_SEARCH_NATIVE, ajaxurl, cssId, tenantId)
        c3po.showModalOnSelect(cssId)
        c3po.showHideRefData(cssId)
    },

    initGroupedProperties: function (ajaxurl, cssId, tenantId) {
        // fallback for hardcoded id
        if (!cssId) {
            cssId = "#custom_props_div"
        }
        console.log ('c3po.initGroupedProperties( ' + ajaxurl + ', ' + cssId + ', ' + tenantId + ' )')

        c3po.refdataCatSearch(ajaxurl, cssId)
        c3po.searchProp(c3po.PROP_SEARCH_GROUPED, ajaxurl, cssId, tenantId)
        c3po.showModalOnSelect(cssId)
        c3po.showHideRefData(cssId)
    },

    refdataCatSearch: function (ajaxurl, cssId) {
        console.log ('c3po.refdataCatSearch() ' + ajaxurl + ', ' + cssId + ' )')

        $("#cust_prop_refdatacatsearch").select2({
            placeholder: "Kategorie angeben ..",
            minimumInputLength: 1,
            ajax: { // instead of writing the function to execute the request we use Select2's convenient helper
                url: ajaxurl,
                dataType: 'json',
                data: function (term, page) {
                    return {
                        q: term, // search term
                        page_limit: 10,
                        baseClass: 'de.laser.RefdataCategory'
                    };
                },
                results: function (data, page) {
                    return {results: data.values};
                }
            }
        });
    },

    searchProp: function (grouped, ajaxurl, cssId, tenantId) {
        console.log ('c3po.searchProp( ' + ajaxurl + ', ' + cssId + ', ' + tenantId + ' )')

        let desc = $(cssId + " .customPropSelect").attr('data-desc')
        let oid  = $(cssId + " .customPropSelect").attr('data-oid')

        let baseClass = 'de.laser.properties.PropertyDefinition'

        if (grouped == c3po.PROP_SEARCH_GROUPED) {
            baseClass = 'de.laser.properties.PropertyDefinitionGroup'
        }

        $(cssId + " .customPropSelect").select2({
            placeholder: JSPC.dict.get('property.select.placeholder', JSPC.currLanguage),
            minimumInputLength: 0,
            width: 300,
            formatSearching: function ()           { return JSPC.dict.get('property.select.searching', JSPC.currLanguage); },
            formatLoadMore:  function (pageNumber) { return JSPC.dict.get('property.select.loadMore', JSPC.currLanguage); },
            formatNoMatches: function ()           { return JSPC.dict.get('property.select.noMatches', JSPC.currLanguage); },

            ajax: { // instead of writing the function to execute the request we use Select2's convenient helper
                url: ajaxurl,
                dataType: 'json',
                data: function (term, page) {
                    return {
                        q: term, // search term
                        desc: desc,
                        oid: oid,
                        page_limit: 10,
                        baseClass: baseClass,
                        tenant: tenantId
                    };
                },
                results: function (data, page) {
                    return {results: data.values};
                }
            },
            createSearchChoice: function (term, data) {
                return null; // {id: -1, text: "Neue Eigenschaft: " + term};
            }
        });
    },

    // TODO -refactoring
    showModalOnSelect: function (cssId) {
        console.log ('c3po.showModalOnSelect( ' + cssId + ' )')

        $(cssId + " .customPropSelect").on("select2-selecting", function (e) {
            if (e.val == -1) {
                let selectedText = e.object.text;
                selectedText = selectedText.replace("Neue Eigenschaft: ", "")
                $("input[name='cust_prop_name']").val(selectedText);
                $('#cust_prop_add_modal').modal('show');

            }
        });
    },

    // TODO -refactoring
    showHideRefData: function (cssId) {
        console.log ('c3po.showHideRefData( ' + cssId + ' )')

        $('#cust_prop_modal_select').change(function () {
            let selectedText = $("#cust_prop_modal_select option:selected").val();
            if (selectedText == "de.laser.RefdataValue") {
                $("#cust_prop_ref_data_name").show();
            } else {
                $("#cust_prop_ref_data_name").hide();
            }
        });
    }
}

JSPC.modules.add( 'c3po', c3po );
