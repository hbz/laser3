package com.k_int.kbplus

import de.laser.Doc
import de.laser.DocContext
import de.laser.License
import de.laser.Links
import de.laser.Org
import de.laser.Subscription
import de.laser.SurveyConfig
import de.laser.helper.RDStore
import de.laser.interfaces.CalculatedType
import grails.gorm.transactions.Transactional
import org.apache.commons.io.IOUtils
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Transactional
class DocstoreService {

  File zipDirectory(File directory) throws IOException {
    File testZip = File.createTempFile("bag.", ".zip");
    String path = directory.getAbsolutePath();
    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(testZip));

    ArrayList<File> fileList = getFileList(directory);
    for (File file : fileList) {
      ZipEntry ze = new ZipEntry(file.getAbsolutePath().substring(path.length() + 1));
      zos.putNextEntry(ze);
  
      FileInputStream fis = new FileInputStream(file);
      IOUtils.copy(fis, zos);
      fis.close();
  
      zos.closeEntry();
    }
  
    zos.close();
    return testZip;
  }

  ArrayList<File> getFileList(File file) {
    ArrayList<File> fileList = new ArrayList<File>();
    if (file.isFile()) {
      fileList.add(file);
    }
    else if (file.isDirectory()) {
      for (File innerFile : file.listFiles()) {
        fileList.addAll(getFileList(innerFile));
      }
    }
    return fileList;
  }

    def unifiedDeleteDocuments(params) {

        // copied from / used in ..
        // licenseController
        // MyInstitutionsController
        // PackageController
        // SubscriptionDetailsController

        params.each { p ->
            if (p.key.startsWith('_deleteflag.') ) {
                String docctx_to_delete = p.key.substring(12)
                log.debug("Looking up docctx ${docctx_to_delete} for delete")

                DocContext docctx = DocContext.get(docctx_to_delete)
                docctx.status = RDStore.DOC_CTX_STATUS_DELETED
                docctx.save()
            }
            if (p.key.startsWith('_deleteflag"@.') ) { // PackageController
                String docctx_to_delete = p.key.substring(12);
                log.debug("Looking up docctx ${docctx_to_delete} for delete")

                DocContext docctx = DocContext.get(docctx_to_delete)
                docctx.status = RDStore.DOC_CTX_STATUS_DELETED
                docctx.save()
            }
        }

        if (params.deleteId) {
            String docctx_to_delete = params.deleteId
            log.debug("Looking up docctx ${docctx_to_delete} for delete")

            DocContext docctx = DocContext.get(docctx_to_delete)
            docctx.status = RDStore.DOC_CTX_STATUS_DELETED
            docctx.save()
        }
    }

    List getTargettedDocuments(Org org) {
        List<DocContext> furtherDocs = DocContext.findAllByTargetOrgAndShareConf(org,RDStore.SHARE_CONF_UPLOADER_AND_TARGET)
        furtherDocs
    }

    //this method may serve as document view refactoring base
    Map<String, SortedSet<DocContext>> getDocumentsForExport(Org contextOrg, instance) {
        boolean parentAtChild = false
        Set<DocContext> documentSet = instance.documents
        SortedSet<DocContext> filteredDocuments = new TreeSet<DocContext>(), sharedItems = new TreeSet<DocContext>()
        if(instance instanceof Subscription) {
            if(contextOrg.id == instance.getConsortia()?.id && instance.instanceOf) {
                if(instance._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION)
                    parentAtChild = true
            }
        }
        else if(instance instanceof License) {
            if(contextOrg.id == instance.getLicensingConsortium()?.id && instance.instanceOf) {
                parentAtChild = true
            }
        }
        else if(instance instanceof Org && instance.id == contextOrg.id) {
            documentSet.addAll(getTargettedDocuments((Org) instance))
        }
        documentSet.each { DocContext docctx ->
            boolean visible = false
            boolean inOwnerOrg = docctx.owner.owner?.id == contextOrg.id
            boolean inTargetOrg = contextOrg.id == docctx.targetOrg?.id

            if(docctx.org) {
                switch(docctx.shareConf) {
                    case RDStore.SHARE_CONF_UPLOADER_ORG: visible = inOwnerOrg
                        break
                    case RDStore.SHARE_CONF_UPLOADER_AND_TARGET: visible = inOwnerOrg || inTargetOrg
                        break
                    case RDStore.SHARE_CONF_CONSORTIUM:
                    case RDStore.SHARE_CONF_ALL: visible = true //this remark still counts - definition says that everyone with "access" to target org. How are such access roles defined and where?
                        break
                    default:
                        if(docctx.shareConf)
                            log.debug(docctx.shareConf)
                        else visible = true
                        break
                }
            }
            else if(inOwnerOrg || docctx.sharedFrom)
                visible = true
            else {
                if((parentAtChild && docctx.sharedFrom) || !parentAtChild && docctx.owner?.owner?.id == contextOrg.id)
                    visible = true
            }
            if(docctx.owner?.contentType == Doc.CONTENT_TYPE_FILE && docctx.status != RDStore.DOC_CTX_STATUS_DELETED) {
                if((docctx.sharedFrom || inTargetOrg) && visible)
                    sharedItems.add(docctx)
                else if(visible)
                    filteredDocuments.add(docctx)
            }
        }
        [filteredDocuments: filteredDocuments, sharedItems: sharedItems]
    }

    Map<String, SortedSet<DocContext>> getNotesForExport(Org contextOrg, instance) {
        String instanceClause
        SortedSet<DocContext> filteredDocuments = new TreeSet<DocContext>(), sharedItems = new TreeSet<DocContext>()
        if(instance instanceof License)
            instanceClause = 'dc.license = :instance'
        else if(instance instanceof Subscription)
            instanceClause = 'dc.subscription = :instance'
        else if(instance instanceof Package)
            instanceClause = 'dc.pkg = :instance'
        else if(instance instanceof Links)
            instanceClause = 'dc.link = :instance'
        else if(instance instanceof Org)
            instanceClause = 'dc.org = :instance'
        else if(instance instanceof SurveyConfig)
            instanceClause = 'dc.surveyConfig = :instance'
        if(instanceClause) {
            Set<DocContext> documents = DocContext.executeQuery('select dc from DocContext dc join dc.owner doc where ' + instanceClause + ' and doc.contentType = :string and doc.status != :deleted and ((dc.sharedFrom = null and (doc.owner = :contextOrg or doc.owner = null)) or dc.sharedFrom != null) order by lower(doc.title)', [string: Doc.CONTENT_TYPE_STRING, deleted: RDStore.DOC_CTX_STATUS_DELETED, contextOrg: contextOrg, instance: instance])
            documents.each { DocContext dc ->
                if(dc.sharedFrom)
                    sharedItems << dc
                else filteredDocuments << dc
            }
        }
        [filteredDocuments: filteredDocuments, sharedItems: sharedItems]
    }
}
