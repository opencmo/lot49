package com.enremmeta.rtb.cli;

import java.io.BufferedReader;
import java.io.FileReader;

import com.enremmeta.util.ServiceRunner;
import com.enremmeta.util.Utils;

/**
 * Command-line utility to execute {@link Utils#cookieToLogModUid(String)} and
 * {@link Utils#logToCookieModUid(String)} on files.
 * 
 * @author Gregory Golberg (grisha@alum.mit.edu)
 * 
 *         Copyright © <a href="http://www.enremmeta.com">Enremmeta LLC</a> 2014. All Rights
 *         Reserved. 
 *
 */
public class ModUid extends ServiceRunner {

    public ModUid() {
        super();
    }


    private static String fixup(String s, int depth) {
        if (depth > 10) {
            return s;
        }
        int start = 0;
        if (s.length() >= Utils.MOD_UID_COOKIE_LENGTH_MIN
                        && s.length() <= Utils.MOD_UID_COOKIE_LENGTH_MAX) {
            return s;
        }

        while (true) {
            final int percIdx = s.indexOf("%", start);
            if (percIdx < 0) {
                break;
            }
            if (percIdx < s.length() - 2) {
                String maybeEncoded = s.substring(percIdx + 1, percIdx + 3);
                if (percIdx > 22 && maybeEncoded.equals("25")) {
                    start = percIdx + 3;
                    continue;
                }
                if (Character.digit(maybeEncoded.charAt(0), 16) > -1
                                && Character.digit(maybeEncoded.charAt(1), 16) > -1) {
                    s = s.replace("%" + maybeEncoded, String.valueOf(
                                    Character.toChars(Integer.parseInt(maybeEncoded, 16))));
                    continue;
                } else {
                    start = percIdx + 3;
                    continue;
                }
            } else {
                break;
            }
        }
        final int slen = s.length();
        if (slen >= 25 && s.substring(22, 25).equals("%25")) {
            // These are not magic numbers, these are mod UID cookie version
            // lengths.
            // look those up
            s = s.substring(0, 22) + "==";
        } else if (slen > Utils.MOD_UID_COOKIE_LENGTH_MAX) {
            final int per25Idx0 = s.indexOf("%25");
            final int per25Idx1 = per25Idx0 + 3;
            String goodS = s.substring(0, per25Idx0);
            boolean inCorruptedSubstring = false;
            if (per25Idx1 < Utils.MOD_UID_COOKIE_LENGTH_MIN) {
                int i = per25Idx1;
                for (; i < slen - 1; i += 2) {
                    if (!s.substring(i, i + 2).equals("25")) {
                        break;
                    }
                }
                final String rest = s.substring(i);
                final String maybeEncoded = rest.substring(0, 2);
                final String rest2 = rest.substring(2);
                if (!maybeEncoded.equals("25") && (Character.digit(maybeEncoded.charAt(0), 16) > -1
                                && Character.digit(maybeEncoded.charAt(1), 16) > -1)) {
                    goodS += String.valueOf(Character.toChars(Integer.parseInt(maybeEncoded, 16)));
                } else {
                    goodS += maybeEncoded;
                }
                goodS += rest2;
                s = fixup(goodS, depth + 1);
            }

        }
        return s;
    }

    public static void main(String argv[]) throws Exception {
        System.out.println(Utils.cookieToLogModUid(Utils.TEST_MOD_UID_COOKIE_3));
        ModUid modUid = new ModUid();
        modUid.opts.addOption("c", true, "File of cookie values.");
        modUid.opts.addOption("l", true, "File of log values.");
        modUid.parseCommandLineArgs(argv);
        String cookieFile = modUid.cl.getOptionValue("c");
        if (cookieFile != null) {
            BufferedReader br = new BufferedReader(new FileReader(cookieFile));
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }
                if (line.equals("-")) {
                    continue;
                }
                if (line.startsWith("odsp")) {
                    continue;
                }
                String fixedLine = fixup(line, 0);
                String logValue = Utils.cookieToLogModUid(fixedLine);
                String lineDisplay = line;
                if (lineDisplay.length() > 40) {
                    lineDisplay = lineDisplay.substring(0, 40) + "...";
                }
                System.out.println(lineDisplay + " => " + fixedLine + " => " + logValue);
            }
        } else {
            String logFile = modUid.cl.getOptionValue("l");
            throw new UnsupportedOperationException();
            // if (logFile == null) {
            // modUid.usage();
            // }
        }
    }
}
