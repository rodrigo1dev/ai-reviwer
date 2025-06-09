package org.example.codereview.service.github;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Service
public class GitHubAppAuthService {

    @Value("${github.app.private-key}")
    private String privateKeyContent;

    @Value("${github.app.id}")
    private String appId;

    public String createJWT() {
        try {
            PrivateKey key = loadPrivateKey();

            return Jwts.builder()
                    .setIssuedAt(Date.from(Instant.now()))
                    .setExpiration(Date.from(Instant.now().plusSeconds(600))) // 10 minutes
                    .setIssuer(appId)
                    .signWith(key, SignatureAlgorithm.RS256)
                    .compact();
        } catch (Exception e) {
            log.error("Error creating JWT", e);
            throw new RuntimeException("Failed to create JWT", e);
        }
    }

    private PrivateKey loadPrivateKey() {
        try {
            String pemContent = privateKeyContent.trim();
            if (!pemContent.startsWith("-----BEGIN")) {
                pemContent = "-----BEGIN PRIVATE KEY-----\n" +
                           pemContent + "\n" +
                           "-----END PRIVATE KEY-----";
            }

            log.debug("Loading private key: {}", pemContent.substring(0, Math.min(pemContent.length(), 50)) + "...");

            try (PEMParser pemParser = new PEMParser(new StringReader(pemContent))) {
                Object pemObject = pemParser.readObject();

                if (pemObject == null) {
                    throw new RuntimeException("Failed to parse PEM content");
                }

                if (pemObject instanceof PEMKeyPair) {
                    return new JcaPEMKeyConverter().getPrivateKey(((PEMKeyPair) pemObject).getPrivateKeyInfo());
                } else if (pemObject instanceof PrivateKeyInfo) {
                    return new JcaPEMKeyConverter().getPrivateKey((PrivateKeyInfo) pemObject);
                } else {
                    throw new RuntimeException("Unexpected PEM object type: " + pemObject.getClass().getName());
                }
            }
        } catch (Exception e) {
            log.error("Error loading private key", e);
            throw new RuntimeException("Failed to load private key", e);
        }
    }
}
