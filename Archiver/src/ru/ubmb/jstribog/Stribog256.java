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

/**
 *
 * @author sarin
 */
public class Stribog256 extends Stribog{

    @Override
    protected byte[] engineDigest() {
        byte[] digest = getDigest(IV);
        byte[] result = new byte[32];
        System.arraycopy(digest, 0, result, 0, result.length);
        return result;
    }
    public static final int[] IV = new int[64];
    
    static {
        for (int i = 0; i < IV.length; i++) {
            IV[i] = 0x01;
        }
    }
    
}