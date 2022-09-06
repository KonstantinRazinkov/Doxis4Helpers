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

import java.security.Provider;

/**
 *
 * @author sarin
 */
public final class StribogProvider extends Provider{

    public StribogProvider() {
        super("JStribog", 0.01, "Stribog (34.11-2012) Java implementation");
        put("MessageDigest.Stribog512", Stribog512.class.getCanonicalName());
        put("MessageDigest.Stribog256", Stribog256.class.getCanonicalName());
    }
    
    
    
}
