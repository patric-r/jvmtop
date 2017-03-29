/**
 * jvmtop - java monitoring for the command-line
 * <p>
 * Copyright (C) 2013 by Patric Rufflar. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * <p>
 * <p>
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.jvmtop.view;

import java.util.*;


/**
 * Base class for all console views, providing some helper methods
 * for formatting.
 *
 * @author paru
 */
public abstract class AbstractConsoleView implements ConsoleView {
    private static final int MIN_WIDTH = 80;
    private boolean shouldExit_ = false;
    protected final int width;

    /**
     *
     */
    public AbstractConsoleView(Integer width) {
        super();
        if (width == null) { width = MIN_WIDTH; }
        if (width < MIN_WIDTH) { width = MIN_WIDTH; }
        this.width = width;
    }

    /**
     * Formats a long value containing "number of bytes" to its megabyte representation.
     * If the value is negative, "n/a" will be returned.
     * <p>
     * TODO: implement automatic scale to bigger units if this makes sense
     * (e.g. output 4.3g instead of 4324m)
     *
     * @param bytes
     * @return
     */
    public String toMB(long bytes) {
        if (bytes < 0) {
            return "n/a";
        }
        return "" + (bytes / 1024 / 1024) + "m";
    }

    /**
     * Formats number of milliseconds to a HH:MM representation
     * <p>
     * TODO: implement automatic scale (e.g. 1d 7h instead of 31:13m)
     *
     * @param millis
     * @return
     */
    public String toHHMM(long millis) {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        formatter
                .format("%2d:%2dm", millis / 1000 / 3600,
                        (millis / 1000 / 60) % 60);
        return sb.toString();
    }

    /**
     * Returns a substring of the given string, representing the 'length' most-right characters
     *
     * @param str
     * @param length
     * @return
     */
    public String rightStr(String str, int length) {
        return str.substring(Math.max(0, str.length() - length));
    }

    /**
     * Returns a substring of the given string, representing the 'length' most-left characters
     *
     * @param str
     * @param length
     * @return
     */
    public String leftStr(String str, int length) {
        return str.substring(0, Math.min(str.length(), length));
    }

    /**
     * Joins the given list of strings using the given delimiter delim
     *
     * @param list
     * @param delim
     * @return
     */
    public String join(List<String> list, String delim) {

        StringBuilder sb = new StringBuilder();

        String loopDelim = "";

        for (String s : list) {

            sb.append(loopDelim);
            sb.append(s);

            loopDelim = delim;
        }

        return sb.toString();
    }

    @Override
    public boolean shouldExit() {
        return shouldExit_;
    }

    /**
     * Requests the disposal of this view - it should be called again.
     * TODO: refactor / remove this functional, use proper exception handling instead.
     */
    protected void exit() {
        shouldExit_ = true;
    }

    /**
     * Sorts a Map by its values, using natural ordering.
     *
     * @param map
     * @param reverse
     * @return
     */
    public Map sortByValue(Map map, boolean reverse) {
        List list = new LinkedList(map.entrySet());
        Collections.sort(list, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue())
                        .compareTo(((Map.Entry) (o2)).getValue());
            }
        });

        if (reverse) {
            Collections.reverse(list);
        }

        Map result = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Override
    public void sleep(long millis) throws Exception {
        Thread.sleep(millis);
    }
}
