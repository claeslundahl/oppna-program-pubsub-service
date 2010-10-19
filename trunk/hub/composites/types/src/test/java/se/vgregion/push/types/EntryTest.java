/**
 * Copyright 2010 Västra Götalandsregionen
 *
 *   This library is free software; you can redistribute it and/or modify
 *   it under the terms of version 2.1 of the GNU Lesser General Public
 *   License as published by the Free Software Foundation.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the
 *   Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *   Boston, MA 02111-1307  USA
 *
 */

package se.vgregion.push.types;

import org.junit.Assert;
import org.junit.Test;


public class EntryTest {

    @Test
    public void atomId() throws Exception {
        Entry entry = new Entry(SomeFeeds.ATOM_DOCUMENT.getRootElement().getFirstChildElement("entry", Feed.NS_ATOM));
        
        Assert.assertEquals("urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a", entry.getAtomId());
    }
}
