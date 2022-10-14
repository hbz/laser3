// module: assets/javascripts/modules/responsiveTables.js

responsiveTables = {

  go: function() {
    responsiveTables.init('body')
  },

  init: function(ctxSel) {
    console.log('responsiveTables.init( ' + ctxSel + ' )')

    // smaller then 1200px
    if (window.matchMedia('(max-width: 1200px)').matches) {
      responsiveTables.setDataLabel()
    }

    // Resize the Window
    $(window).resize(function () {
      // smaller then 1200px
      if (window.matchMedia('(max-width: 1200px)').matches) {
        responsiveTables.setDataLabel()
      }
    });
  },
  setDataLabel: function() {
    $('.ui.la-js-responsive-table').each(function () {
      let currentTable = $(this);
      $('>tbody>tr', this).each(function () {
        $('>td', this).each(function () {
          let th = $(currentTable.find('th')).eq($(this).index());

          // table header is icon
          if( th.html().includes("la-popup-tooltip"))  {
            let dataContent = th.find('.la-popup-tooltip').attr("data-content");
            $(this).attr('data-label', dataContent + ':');
          }
          // table header is checkbox
          else if (  th.html().includes("input") ) {
            $(this).attr('data-label', JSPC.dict.get('responsive.table.selectElement', JSPC.currLanguage)+ ':');
          }

          else
            // table header is empty
            if (th.text() == 0) {
            }
            // table header is dropdown menu
            else if( th.html().includes("menu"))  {
              $(this).attr('data-label',th.find('.text').text() + ':');
            }
            else {
              // table header has only text
              $(this).attr('data-label',th.text() + ':');
            }
        });
      });
    });
  }
}

JSPC.modules.add( responsiveTables, 'responsiveTables' );