package de.laser.titles

import de.laser.exceptions.CreationException
import de.laser.storage.BeanStorage
import de.laser.helper.RDStore
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.dao.DuplicateKeyException

/**
 * Represents a(n) (e)book instance, contains fields which apply only for books
 */
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

    static transients = ['ebookFirstAutorOrFirstEditor'] // mark read-only accessor methods

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

        dateFirstInPrint(nullable:true);
        dateFirstOnline(nullable:true);
        summaryOfContent(nullable:true, blank:false);
        volume(nullable:true, blank:false);
        firstAuthor (nullable:true, blank:false);
        firstEditor (nullable:true, blank:false);
        editionDifferentiator (nullable:true, blank:false);
        editionNumber       (nullable:true)
        editionStatement (nullable:true, blank:false);

    }

    /**
     * Creates a new book instance if it not exists with the given parameter map. The key to check against is {@link #gokbId}
     * @param params the parameter {@link Map}
     * @return the new book instance if it not exists, the existing one if it does
     * @throws CreationException
     */
    static BookInstance construct(Map<String,Object> params) throws CreationException {
        withTransaction {
            BookInstance bi = new BookInstance(params)
            bi.setGlobalUID()
            try {
                if(!bi.save())
                    throw new CreationException(bi.errors)
            }
            catch (DuplicateKeyException ignored) {
                log.info("book instance exists, returning existing one")
                bi = TitleInstance.findByGokbId(params.gokbId) as BookInstance
            }
            bi
        }
    }

    /**
     * Outputs the title type as string, i.e. for icons
     * @return the title type {@link de.laser.RefdataValue}
     */
    String printTitleType() {
        RDStore.TITLE_TYPE_EBOOK.getI10n('value')
    }

    /**
     * Outputs the first author or first editor of an ebook
     * @return a string showing the first author and / or editor of this ebook
     */
    String getEbookFirstAutorOrFirstEditor(){

        MessageSource messageSource = BeanStorage.getMessageSource()
        String label = messageSource.getMessage('title.firstAuthor.firstEditor.label',null, LocaleContextHolder.getLocale())

        if(firstEditor && firstAuthor) {
            return firstAuthor + ' ; ' + firstEditor + ' ' + label
        }
        else if(firstAuthor) {
            return firstAuthor
        }
        else if(firstEditor) {
            return firstEditor + ' ' + label
        }
    }
}
