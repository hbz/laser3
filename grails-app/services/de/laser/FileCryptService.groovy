package de.laser

import de.laser.config.ConfigMapper
import de.laser.system.SystemEvent
import de.laser.utils.DateUtils
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
import java.security.MessageDigest
import java.security.spec.KeySpec

@Transactional
class FileCryptService {

    public final static Map CONFIG = [
        DEFAULT : [
            ID               : 'DEFAULT',
            ALGORITHM        : 'AES',
            ALGORITHM_PRF    : 'PBKDF2WithHmacSHA256',
            ALGORITHM_XFORM  : 'AES/CBC/PKCS5Padding',
            ALGORITHM_IC     : 65536,
            ALGORITHM_KL     : 256,
            SALT             : 32,
            IV               : 16,
            DIGEST           : 4,
            DIGEST_DELIM     : ':',
            PASSPHRASE       : 'd3f4Ul7_P4zzphr4Z3', // local
            TMP_DIR          : '/laser-docs'
        ]
    ]

    void encryptRawFileAndUpdateDoc(File rawFile, Doc doc) {
        log.debug '[encryptRawFileAndUpdateDoc] file: ' + rawFile.getName() + ', doc: ' + doc.id

        String ckey     = generateCKey()
        Path rfPath     = rawFile.toPath()
        File encTmpFile = encryptToTmpFile(rawFile, ckey)
        File decTmpFile = decryptToTmpFile(encTmpFile, ckey)

        if (validateFiles(decTmpFile, rawFile)) {
            Files.copy(encTmpFile.toPath(), rfPath, StandardCopyOption.REPLACE_EXISTING)
            doc.ckey = ckey
            doc.save()
        }
        else {
            log.debug '[encryptRawFileAndUpdateDoc] FAILED -> file: ' + rfPath
        }
        encTmpFile.delete()
        decTmpFile.delete()
    }

    File encryptToTmpFile(File inFile, String ckey) {
        String pf = DateUtils.getSDF_yyyyMMdd().format(new Date()) + '-'

        File outFile = File.createTempFile(pf, '.enc', getTmpDir())
        doCrypto(inFile, outFile, Cipher.ENCRYPT_MODE, ckey)
        outFile
    }

    File decryptToTmpFile(File inFile, String ckey) {
        String pf = DateUtils.getSDF_yyyyMMdd().format(new Date()) + '-'

        File outFile = File.createTempFile(pf, '.dec', getTmpDir())
        if (ckey) {
            doCrypto(inFile, outFile, Cipher.DECRYPT_MODE, ckey)
        }
        else {
            log.debug'[decryptToTmpFile] FAILED -> no ckey -> raw file copied'
            Files.copy(inFile.toPath(), outFile.toPath(), StandardCopyOption.REPLACE_EXISTING) // todo: ERMS-6382 FALLBACK
        }
        outFile
    }

    void doCrypto(File inFile, File outFile, int cipherMode, String ckey) {
        try {
            Map cfg           = getConfig()

            String passphrase = ConfigMapper.getDocumentStorageKey() ?: cfg.PASSPHRASE
            String ckeyValue  = ckey.split(cfg.DIGEST_DELIM).last()
            String salt       = ckeyValue.take(cfg.SALT)
            String iv         = ckeyValue.takeRight(cfg.IV)

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

            if (cipherMode == Cipher.DECRYPT_MODE) {
                SystemEvent.createEvent('CRYPTO_DECRYPT_ERROR', ['error': e.toString(), file: inFile.getName(), ckey: ckey.take(16) + '..'])
            }
            if (cipherMode == Cipher.ENCRYPT_MODE) {
                SystemEvent.createEvent('CRYPTO_ENCRYPT_ERROR', ['error': e.toString(), file: inFile.getName(), ckey: ckey.take(16) + '..'])
            }
        }
    }

    boolean validateFiles(File aFile, File bFile) {
        (-1L == Files.mismatch(aFile.toPath(), bFile.toPath()))
    }

    File getTmpDir() {
        File dir = new File(System.getProperty('java.io.tmpdir') + getConfig().TMP_DIR)
        if (! dir.exists()) {
            dir.mkdirs()
        }
        dir
    }

    Map getConfig() {
        CONFIG.DEFAULT
    }

    String getMessageDigest(String message, int length = 64) {
        MessageDigest md = MessageDigest.getInstance('SHA-256')
        md.update(message.getBytes())
        md.digest().encodeHex().toString().take(length)
    }

    String generateCKey() {
        Map cfg     = getConfig()
        String salt = RandomUtils.getAlphaNumeric(cfg.SALT)
        String iv   = RandomUtils.getAlphaNumeric(cfg.IV)

        String passphrase = ConfigMapper.getDocumentStorageKey() ?: cfg.PASSPHRASE
        String cfgMd      = getMessageDigest(cfg.ID, cfg.DIGEST)
        String ppMd       = getMessageDigest(passphrase, cfg.DIGEST)

        cfgMd + cfg.DIGEST_DELIM + ppMd + cfg.DIGEST_DELIM + salt + iv
    }
}