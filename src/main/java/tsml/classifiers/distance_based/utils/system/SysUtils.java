/*
 * This file is part of the UEA Time Series Machine Learning (TSML) toolbox.
 *
 * The UEA TSML toolbox is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 *
 * The UEA TSML toolbox is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with the UEA TSML toolbox. If not, see <https://www.gnu.org/licenses/>.
 */
 
package tsml.classifiers.distance_based.utils.system;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import tsml.classifiers.distance_based.utils.strings.StrUtils;

/**
 * Purpose: find details about the system we're running on.
 *
 * Contributors: goastler
 */
public class SysUtils {
    
    public static String hostName() {
        if(!getOS().equals(OS.WINDOWS)) {
            try {
                return StrUtils.join("\n", exec("hostname"));
            } catch(IOException | InterruptedException ignored) {
                
            }
        } 
        return "unknown"; // todo windows version
    }
    
    public static List<String> exec(String command) throws
                                             IOException,
                                             InterruptedException {
        Process process = Runtime.getRuntime().exec(command);
        List<String> result;
        process.waitFor();
        if(process.exitValue() == 0) {
            result = IOUtils.readLines(process.getInputStream(), StandardCharsets.UTF_8);
        } else {
            throw new IllegalStateException(StrUtils.join("\n", IOUtils.readLines(process.getErrorStream(),
                                                                                  StandardCharsets.UTF_8)));
        }
        process.destroyForcibly();
        return result;
    }

    public static String findCpuInfo() {
        try {
            OS os = getOS();
            String cpuInfo;
            switch(os) {
                case MAC:
                case LINUX:
                case SOLARIS:
                    List<String> output = exec("cat /proc/cpuinfo");
                    cpuInfo = "linux";
                    for(String line : output) {
                        if(line.contains("model name")) {
                            cpuInfo = line.split(":")[1].trim();
                            break;
                        }
                    }
                    break;
                case WINDOWS:
                default:
                    cpuInfo = "windows"; // todo windows version
                    break;
            }
            return cpuInfo;
        } catch(IOException | InterruptedException e) {
            return "unknown";
        }
    }

    public static String getOsName() {
        return getOS().name().toLowerCase();
    }

    public enum OS {
        WINDOWS, LINUX, MAC, SOLARIS
    }

    private static OS os;

    public static OS getOS() {
        if (os == null) {
            String operSys = System.getProperty("os.name").toLowerCase();
            if (operSys.contains("win")) {
                os = OS.WINDOWS;
            } else if (operSys.contains("nix") || operSys.contains("nux")
                || operSys.contains("aix")) {
                os = OS.LINUX;
            } else if (operSys.contains("mac")) {
                os = OS.MAC;
            } else if (operSys.contains("sunos")) {
                os = OS.SOLARIS;
            }
        }
        return os;
    }

    public static void main(String[] args) {
        System.out.println(findCpuInfo());
    }
}
