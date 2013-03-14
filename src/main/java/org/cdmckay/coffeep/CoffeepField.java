/*
 * Copyright (c) 2013, Cameron McKay. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.cdmckay.coffeep;

import java.lang.String;
import java.util.HashSet;
import java.util.Set;

public class CoffeepField {

    public String internalType;
    public Set<String> modifiers = new HashSet<String>();
    public String type;
    public String name;
    public Set<String> flags = new HashSet<String>();

    // TODO Add attributes.
    //public List<String> attributes = new ArrayList<String>();

}
