package com.sersolutions.doxis4helpers.commons.types;

import com.ser.blueline.signature.ISignature;

/**
 * Class of signature of document
 */
public class Signature {
    private byte[] signature;
    private String signatureType;

    /**
     * Inits new signature
     * @param signature signature as byte array
     * @param signatureType name of signature
     */
    public Signature(byte[] signature, String signatureType) {
        this.signature = signature;
        this.signatureType = signatureType;
    }

    /**
     * Init signature from Doxis4 object
     * @param doxis4Signature Doxis4 signature
     *                        @see ISignature
     */
    public Signature(ISignature doxis4Signature) {

        this.signature = doxis4Signature.getSignature();
        this.signatureType = doxis4Signature.getSignatureAlgorithm();
    }

    public byte[] GetSignature()
    {
        return signature;
    }
    public String GetSignatureType()
    {
        return signatureType;
    }

}
