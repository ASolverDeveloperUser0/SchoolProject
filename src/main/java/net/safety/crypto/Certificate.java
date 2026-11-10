package net.safety.crypto;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Date;

public class Certificate {
    public X509Certificate generateSelfCertificate(KeyPair pair, String peerIDCN){
        try {
            long timeNow = System.currentTimeMillis();
            Date dateStart = new Date(timeNow);
            Date dateFinish = new Date(timeNow + 365L * 24 * 60 * 60 * 1000);
            X500Name CommonName = new X500Name("CN=" + peerIDCN);

            X509v3CertificateBuilder builder = new X509v3CertificateBuilder(CommonName, BigInteger.valueOf(timeNow),
                    dateStart, dateFinish, CommonName, SubjectPublicKeyInfo.getInstance(pair.getPublic().getEncoded()));

            ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256withRSA").build(pair.getPrivate());

            return new JcaX509CertificateConverter().getCertificate(builder.build(contentSigner));
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }
}
