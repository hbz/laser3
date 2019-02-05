package com.k_int.kbplus

import org.grails.datastore.mapping.query.Query

class BookInstance extends TitleInstance {


    Date dateFirstInPrint
    Date dateFirstOnline
    String summaryOfContent
    String volume

    String firstAuthor
    String firstEditor

    Integer editionNumber
    String  editionStatement
    String editionDifferentiator


    static mapping = {
        includes TitleInstance.mapping

        dateFirstInPrint column:'bk_dateFirstInPrint'
        dateFirstOnline column:'bk_dateFirstOnline'
        summaryOfContent column:'bk_summaryOfContent'
        volume column:'bk_volume'
        firstEditor column: 'bk_firstEditor'
        firstAuthor column: 'bk_firstAuthor'
        editionNumber column: 'bk_editionNumber'
        editionStatement column: 'bk_editionStatement'
        editionDifferentiator column: 'bk_editionDifferentiator'


    }

    static constraints = {

        dateFirstInPrint(nullable:true, blank:false);
        dateFirstOnline(nullable:true, blank:false);
        summaryOfContent(nullable:true, blank:false);
        volume(nullable:true, blank:false);
        firstAuthor (nullable:true, blank:false);
        firstEditor (nullable:true, blank:false);
        editionDifferentiator (nullable:true, blank:false);
        editionNumber (nullable:true, blank:false);
        editionStatement (nullable:true, blank:false);

    }
}
