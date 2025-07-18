package com.lumadd.protohackers.problem08.ciphers;

public interface Cipher {
    byte encrypt(byte plainText, int pos);

    byte decrypt(byte cipherText, int pos);
}
