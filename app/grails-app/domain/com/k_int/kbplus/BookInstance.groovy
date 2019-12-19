package com.k_int.kbplus

import grails.util.Holders
import org.springframework.context.i18n.LocaleContextHolder

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

        dateFirstInPrint column:'bk_datefirstinprint'
        dateFirstOnline column:'bk_datefirstonline'
        summaryOfContent column:'bk_summaryofcontent'
        volume column:'bk_volume'
        firstEditor column: 'bk_first_editor'
        firstAuthor column: 'bk_first_author'
        editionNumber column: 'bk_edition_number'
        editionStatement column: 'bk_edition_statement'
        editionDifferentiator column: 'bk_edition_differentiator'
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

    static BookInstance construct(Map<String,Object> params) {
        BookInstance bi = new BookInstance(params)
        bi.setGlobalUID()
        bi
    }

    String printTitleType() {
        RefdataValue.getByValueAndCategory('EBook',RefdataCategory.TI_MEDIUM).getI10n('value')
    }

    String getEbookFirstAutorOrFirstEditor(){

        def messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        String label = messageSource.getMessage('title.firstAuthor.firstEditor.label',null, LocaleContextHolder.getLocale())

        if(firstEditor && firstAuthor)
        {
            return firstAuthor + ' ; ' + firstEditor + ' ' + label
        }
        else if(firstAuthor)
        {
            return firstAuthor
        }

        else if(firstEditor)
        {
            return firstEditor + ' ' + label
        }
    }
}
