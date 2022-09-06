/*
    Copyright (C) 2014  sarin

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.ubmb.jstribog;

import java.security.MessageDigestSpi;
import java.util.ArrayList;
import java.util.List;
import ru.ubmb.jstribog.exceptions.InvalidVectorLenException;

/**
 *
 * @author sarin
 */
abstract class Stribog extends MessageDigestSpi{
    
    protected List<Integer> buffer = new ArrayList<Integer>(256);

    @Override
    protected void engineUpdate(byte input) {
        buffer.add(input & 0xFF);
    }

    @Override
    protected void engineUpdate(byte[] input, int offset, int len) {
        for (int i = offset; i < len; i++) {
            buffer.add(input[i] & 0xFF);
        }
    }    

    @Override
    protected void engineReset() {
        buffer.clear();
    }
    
    protected byte[] getDigest(int[] IV) {
        int[] ba = new int[buffer.size()];
        for (int i = 0; i < ba.length; i++) {
            ba[i] = buffer.get(i);            
        }
        int[] hashX = hashX(IV, ba);
        byte[] result = new byte[hashX.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) hashX[i];
        }
        return result;
    }
    
    private int[] xor(int[] a, int[] b) {
        if (a.length != b.length) {
            throw new InvalidVectorLenException("a.length == " + a.length + " and b.length == " + b.length + " but should be the same.");
        }
        int[] result = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] ^ b[i];
        }
        return result;
    }
    
    private int[] add(int[] a, int[] b) {
        if (a.length != b.length) {
            throw new InvalidVectorLenException("a.length == " + a.length + " and b.length == " + b.length + " but should be the same.");
        }
        int[] result = new int[a.length];
        int r = 0;
        for (int i = a.length - 1; i >= 0; i--) {
            result[i] = (a[i] + b[i] + r) & 0xFF;
            r = ((a[i] + b[i]) >> 8) & 0xFF;
        }
        return result;
    }
    
    private int[] L(int[] state) {
        if (state.length != 64) {
            throw new InvalidVectorLenException("state.length != 64");
        }
        int[] result = new int[64];
        for (int i = 0; i < 8; i++) {
            int[] v = new int[8];
            for (int k = 0; k < 8; k++) {
                for (int j = 0; j < 8; j++) {
                    if ((state[i * 8 + k] & (1 << (7 - j))) != 0) {                        
                        v = xor(v, Data.A[k * 8 + j]);
                    }
                }
            }
            System.arraycopy(v, 0, result, i * 8, 8);
        }
        return result;
    }

    private int[] P(int[] state) {
        if (state.length != 64) {
            throw new InvalidVectorLenException("state.length != 64");
        }
        int[] result = new int[64];
        for (int i = 0; i < 64; i++) {
            result[i] = state[Data.Tau[i]];
        }
        return result;
    }

    private int[] S(int[] state) {
        if (state.length != 64) {
            throw new InvalidVectorLenException("state.length != 64");
        }
        int[] result = new int[64];
        for (int i = 0; i < 64; i++) {
            result[i] = Data.SBox[state[i]];
        }
        return result;
    }
    
    private int[] LPS(int[] state) {
        return L(P(S(state)));
    }
    
    private int[] ks(int[] k, int i) {
        return LPS(xor(k, Data.C[i]));
    }
    
    private int[] E(int[] K, int[] m) {
        if (K.length != 64) {
            throw new InvalidVectorLenException("K.length != 64");
        }
        int[] result = xor(K, m);
        int[] Ki = new int[64];
        System.arraycopy(K, 0, Ki, 0, 64);
        for (int i = 0; i < 12; i++) {
            result = LPS(result);
            Ki = ks(Ki, i);
            result = xor(result, Ki);
        }
        return result;
    }
    
    private final int[] gN(int[] Nn, int[] h, int[] m) {
        int[] K = LPS(xor(h, Nn));
        
        int[] e = E(K, m);
        return xor(e, xor(h, m));
    }
    
    protected final int[] hashX(int[] IV, int[] message) {
        int[] h = new int[64];
        System.arraycopy(IV, 0, h, 0, 64);
        int[] M = new int[message.length];
        System.arraycopy(message, 0, M, 0, message.length);
        int[] N = new int[64];
        int[] Sigma = new int[64];
        int[] m = new int[64];
        int l = message.length;
        while (l >= 64) {            
            System.arraycopy(M, l - 64, m, 0, 64);
            h = gN(N, h, m);
            N = add(N, Data.bv512);
            Sigma = add(Sigma, m);
            l -= 64;
        }
        for (int i = 0; i < 63 - l; i++) {
            m[i] = 0;
        }
        m[63 - l] = 0x01;
        if (l > 0) {
            System.arraycopy(M, 0, m, 63 - l + 1, l);
        }
       
        h = gN(N, h, m);        
        int[] bv = new int[64];
        bv[62] = (l * 8) >> 8;
        bv[63] = (l * 8) & 0xFF;
        N = add(N, bv);        
        Sigma = add(Sigma, m);        
        h = gN(Data.bv00, h, N);        
        h = gN(Data.bv00, h, Sigma);
        return h;
    }
    
}
