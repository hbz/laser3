package com.k_int.kbplus

class BookInstance extends TitleInstance {


    Date dateFirstInPrint
    Date dateFirstOnline
    String summaryOfContent
    String volume

    static mapping = {
        includes TitleInstance.mapping

        dateFirstInPrint column:'bk_dateFirstInPrint'
        dateFirstOnline column:'bk_dateFirstOnline'
        summaryOfContent column:'bk_summaryOfContent'
        volume column:'bk_volume'

    }

    static constraints = {

        dateFirstInPrint(nullable:true, blank:false);
        dateFirstOnline(nullable:true, blank:false);
        summaryOfContent(nullable:true, blank:false);
        volume(nullable:true, blank:false);

    }
}
