package de.laser

import de.laser.config.ConfigMapper
import de.laser.system.SystemEvent
import de.laser.utils.RandomUtils
import grails.gorm.transactions.Transactional

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.spec.KeySpec

@Transactional
class FileCryptService {

    public final static Map CONFIG = [
        DEFAULT : [
            ALGORITHM        : 'AES',
            ALGORITHM_PRF    : 'PBKDF2WithHmacSHA256',
            ALGORITHM_XFORM  : 'AES/CBC/PKCS5Padding',
            ALGORITHM_IC     : 65536,
            ALGORITHM_KL     : 256,
            CKEY             : 64,
            SALT             : 32,
            IV               : 16,
            PASSPHRASE       : 'd3f4Ul7_P4zzphr4Z3', // local
            TMP_DIR          : '/laser-docs'
        ]
    ]

    boolean encryptRawFile(File rawFile, Doc doc) {
        log.debug '[encryptRawFile] ' + rawFile.toPath() + ', doc #' + doc.id

        Path rfPath     = rawFile.toPath()
        File encTmpFile = encryptToTmpFile(rawFile, doc.ckey)
        File decTmpFile = decryptToTmpFile(encTmpFile, doc.ckey)

        if (validateFiles(decTmpFile, rawFile)) {
            Files.copy(encTmpFile.toPath(), rfPath, StandardCopyOption.REPLACE_EXISTING)
        }
        else {
            log.debug '[encryptRawFile: FAILED] ' + rfPath
            doc.ckey = null
            doc.save()
        }
        encTmpFile.delete()
        decTmpFile.delete()

        true
    }

    File encryptToTmpFile(File inFile, String ckey) {
        File outFile = File.createTempFile('doc_', '.enc', getTempDirectory())
        doCrypto(inFile, outFile, Cipher.ENCRYPT_MODE, ckey)
        outFile
    }

    File decryptToTmpFile(File inFile, String ckey) {
        File outFile = File.createTempFile('doc_', '.dec', getTempDirectory())
        if (ckey) {
            doCrypto(inFile, outFile, Cipher.DECRYPT_MODE, ckey)
        }
        else {
            log.debug'[decryptToTmpFile: FAILED] -> no ckey -> raw file copied'
            Files.copy(inFile.toPath(), outFile.toPath(), StandardCopyOption.REPLACE_EXISTING) // todo: ERMS-6382 FALLBACK
        }
        outFile
    }

    void doCrypto(File inFile, File outFile, int cipherMode, String ckey) {
        try {
            Map cfg           = getConfig()

            String passphrase = ConfigMapper.getDocumentStorageKey() ?: cfg.PASSPHRASE
            String salt       = ckey.take(cfg.SALT)
            String iv         = ckey.takeRight(cfg.IV)

            SecretKeyFactory factory = SecretKeyFactory.getInstance(cfg.ALGORITHM_PRF)
            KeySpec spec    = new PBEKeySpec(passphrase.toCharArray(), salt.getBytes(), cfg.ALGORITHM_IC, cfg.ALGORITHM_KL)
            SecretKey key   = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), cfg.ALGORITHM)

    //        byte[] iv = new byte[16]
    //        new SecureRandom().nextBytes(iv)
    //        IvParameterSpec ivspec = new IvParameterSpec(iv)

            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes())

            Cipher cipher = Cipher.getInstance(cfg.ALGORITHM_XFORM)
            cipher.init(cipherMode, key, ivspec)

            FileInputStream fis  = new FileInputStream(inFile)
            FileOutputStream fos = new FileOutputStream(outFile)

            byte[] buffer = new byte[64]
            int bytesRead

            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] output = cipher.update(buffer, 0, bytesRead)
                if (output != null) {
                    fos.write(output)
                }
            }

            byte[] outBytes = cipher.doFinal()
            if (outBytes != null) {
                fos.write(outBytes)
            }

            fis.close()
            fos.close()
        }
        catch (Exception e) {
            log.error e.toString()

            String pck = ckey.take(4) + '..' + ckey.takeRight(4)
            if (cipherMode == Cipher.DECRYPT_MODE) {
                SystemEvent.createEvent('CRYPTO_DECRYPT_ERROR', ['error': e.toString(), file: inFile.getName(), ckey: pck])
            }
            if (cipherMode == Cipher.ENCRYPT_MODE) {
                SystemEvent.createEvent('CRYPTO_ENCRYPT_ERROR', ['error': e.toString(), file: inFile.getName(), ckey: pck])
            }
        }
    }

    boolean validateFiles(File aFile, File bFile) {
        (-1L == Files.mismatch(aFile.toPath(), bFile.toPath()))
    }

    Map getConfig() {
        CONFIG.DEFAULT
    }

    File getTempDirectory() {
        File dir = new File(System.getProperty('java.io.tmpdir') + getConfig().TMP_DIR)
        if (! dir.exists()) {
            dir.mkdirs()
        }
        dir
    }

    String generateCKey() {
        RandomUtils.getAlphaNumeric(getConfig().CKEY)
    }
}