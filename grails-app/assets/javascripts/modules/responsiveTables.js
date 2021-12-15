
// modules/responsiveTables.js

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
    $('.ui.la-responsive-table tbody>tr').each(function () {
      $('td', this).each(function () {
        let th = $('.ui.la-responsive-table th').eq($(this).index());

        // table header is icon
        if( th.html().includes("icon")) {
          let dataContent = th.find('.la-popup-tooltip').attr("data-content");
          $(this).attr('data-label', dataContent);
        }

        else if ( th.html().includes("checkbox")) {
          let dataLabel = th.attr("data-label");
          $(this).attr('data-label', dataLabel);
        }

        else
          $(this).attr('data-label',th.text());
      });
    });
  }
}