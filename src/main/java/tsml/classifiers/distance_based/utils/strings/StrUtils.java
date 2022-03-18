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
 
package tsml.classifiers.distance_based.utils.strings;

import tsml.classifiers.distance_based.utils.system.copy.CopierUtils;
import weka.core.OptionHandler;
import weka.core.Utils;

import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class StrUtils {

    public static String joinPath(String... parts) {
        return join("/", parts);
    }

    public static String toString(double[][] matrix) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for(double[] row : matrix) {
            if(first) {
                first = false;
            } else {
                builder.append(System.lineSeparator());
            }
            builder.append(Arrays.toString(row));
        }
        return builder.toString();
    }
    
    public static boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    public static boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    public static String[] extractAmountAndUnit(String str) {
        str = str.trim();
        StringBuilder amount = new StringBuilder();
        StringBuilder unit = new StringBuilder();
        char[] chars = str.toCharArray();
        int i = 0;
        boolean digitsEnded = false;
        for(;i < chars.length; i++) {
            char c = chars[i];
            if(!Character.isDigit(c)) {
                digitsEnded = true;
            }
            if(digitsEnded) {
                unit.append(c);
            } else {
                amount.append(c);
            }
        }
        return new String[] {amount.toString(), unit.toString()};
    }

    public static String depluralise(String str) {
        if(str.endsWith("s")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    public static String joinOptions(List<String> options) {
        return Utils.joinOptions(options.toArray(new String[0]));
    }

    public static String join(String separator, String... parts) {
        return join(separator, Arrays.asList(parts));
    }

    public static void forEachPair(String[] options, BiConsumer<String, String> function) {
        if(options.length % 2 != 0) {
            throw new IllegalArgumentException("options is not correct length, must be key-value pairs");
        }
        for(int i = 0; i < options.length; i += 2) {
            String key = options[i];
            String value = options[i + 1];
            function.accept(key, value);
        }
    }

    public static String join(String separator, double... values) {
        String[] strings = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            strings[i] = String.valueOf(values[i]);
        }
        return join(separator, strings);
    }

    public static HashMap<String, String> pairValuesToMap(String... pairValues) {
        HashMap<String, String> map = new HashMap<>();
        forEachPair(pairValues, map::put);
        return map;
    }

    public static boolean equalPairs(String[] a, String[] b) {
        HashMap<String, String> mapA = pairValuesToMap(a);
        HashMap<String, String> mapB = pairValuesToMap(b);
        for(Map.Entry<String, String> entry : mapA.entrySet()) {
            String valueA = entry.getValue();
            String keyA = entry.getKey();
            String valueB = mapB.get(keyA);
            if(!valueA.equals(valueB)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isOption(String flag, String[] options) {
        try {
            flag = unflagify(flag);
            int i = Utils.getOptionPos(flag, options);
            if(i < 0 || i + 1 >= options.length) {
                return false;
            }
            String option = options[i + 1];
            return !isFlag(option);
        } catch (Exception e) {
            return false;
        }
    }

    public static void updateOptions(OptionHandler optionHandler, String[] options) throws
            Exception {
        String[] current = optionHandler.getOptions();
        String[] next = uniteOptions(options, current);
        optionHandler.setOptions(next);
    }

    public static String[] uniteOptions(String[] next, String[] current) throws
            Exception {
        return uniteOptions(next, current, true);
    }

    public static String[] uniteOptions(String[] next, String[] current, boolean flagsFromCurrent) throws
            Exception {
        ArrayList<String> list = new ArrayList<>();
        for(int i = 0; i < current.length; i++) {
            String flag = current[i];
            flag = unflagify(flag);
            if(isOption(flag, current)) {
                String nextOption = Utils.getOption(flag, next);
                if(nextOption.length() > 0) {
                    addOption(flag, list, nextOption);
                } else {
                    String currentOption = Utils.getOption(flag, current);
                    addOption(flag, list, currentOption);
                }
                i++;
            } else {
                if(Utils.getFlag(flag, next) || flagsFromCurrent) {
                    addFlag(flag, list);
                } else {
                    // don't add the flag
                }
            }

        }
        return list.toArray(new String[0]);
    }

    public static <A> void setOption(String[] options, String flag, Consumer<A> setter) throws
            Exception {
        flag = unflagify(flag);
        String option = Utils.getOption(flag, options);
        while(option.length() != 0) {
            A result = fromOptionValue(option);
            setter.accept(result);
            option = Utils.getOption(flag, options);
        }
    }

    public static <A> A fromOptionValue(String optionsStr) throws Exception {
        return (A) fromOptionValue(optionsStr, str -> str);
    }
    
    /**
     * Given a string containing "{class name} {-flag} {value} ..." construct a new instance of the class given by "class name" and set the remaining options.
     * NOTE: to represent strings, they must be enclosed in quotes.
     * @param optionsStr
     * @return
     * @throws Exception
     */
    public static <A> A fromOptionValue(String optionsStr, Function<String, A> parser) throws Exception {
        if(optionsStr.equals("null")) {
            return null;
        }
        // split into class and options
        String[] parts = Utils.splitOptions(optionsStr);
        if(parts.length == 0) {
            throw new Exception("Invalid option: " + optionsStr);
        }
        if(parts.length == 1) {
            // then this may be a primitive type or string
            String str = parts[0];
            // if string then the str will contain whitespace at the front
            if(str.startsWith("\"") && str.endsWith("\"")) {
                str = str.substring(1, str.length() - 1); // get rid of the char added during encoding
                return parser.apply(str);
            }
            // if the first digit is a digit then dealing with a primitive number. Using the opposing case here, i.e. for it to be a non-primitive the first option is the canonical class name. These cannot begin with numbers or hyphens (for neg numbers) therefore we can assume it's a primitive number by this point
            if(!Character.isAlphabetic(str.charAt(0)) || str.equals("true") || str.equals("false")) {
                return parser.apply(str);
            }
            // otherwise proceed, the parts[0] string must be a class name and needs instantiation
        }
        // the class is always the first entry, options after that
        String className = parts[0];
        final Object result = CopierUtils.newInstanceFromClassName(className);
        // if there are options then set them
        if(parts.length > 1) {
            // get rid of the class name from the options so it won't affect any sub options later on
            parts[0] = "";
            if(result instanceof OptionHandler) {
                ((OptionHandler) result).setOptions(parts);
            } else {
                throw new IllegalArgumentException("cannot set options on " + className);
            }
        }
        return (A) result;
    }

    public static String toOptionValue(Object value, boolean withSubOptions) {
        String str;
        if(value == null) {
            str = "null";
        }
        else if(value instanceof Integer ||
                        value instanceof Double ||
                        value instanceof Float ||
                        value instanceof Byte ||
                        value instanceof Short ||
                        value instanceof Long ||
                        value instanceof Boolean) {
            str = String.valueOf(value);
        } else if(value instanceof Character || value instanceof String) {
            // add quotes to maintain whitespace (the delimited inside-most ones), delimiters to avoid parsing the inner quotes in first pass, and outer quotes to indicate dealing with a string type 
            str = "\"\\\"" + value + "\\\"\"";
        } else {
            Class<?> classValue;
            if(value instanceof Class<?>) {
                classValue = (Class<?>) value;
            } else {
                classValue = value.getClass();
            }
            str = classValue.getName();
            if(value instanceof OptionHandler && withSubOptions) {
                String options = Utils.joinOptions(((OptionHandler) value).getOptions());
                if(!options.isEmpty()) {
                    str =  str + " " + options;
                }
            }
        }
        return str;
    }
    
    public static String toOptionValue(Object value) {
        return toOptionValue(value, true);
    }

    public static void addOption(final String flag, final Collection<String> options, final Object value) {
        addFlag(flag, options);
        String str = toOptionValue(value);
        options.add(str);
    }

    public static <A> void addOption(final String flag, final Collection<String> options, final Collection<A> values) {
        for(A value : values) {
            addOption(flag, options, value);
        }
    }

    public static String flagify(String flag) {
        if(flag.charAt(0) != '-') {
            flag = "-" + flag;
        }
        return flag;
    }

    public static String unflagify(String flag) {
        if(flag.charAt(0) == '-') {
            flag = flag.substring(1);
        }
        return flag;
    }

    public static void addFlag(String flag, final Collection<String> options) {
        flag = flagify(flag);
        options.add(flag);
    }

    public static void addFlag(String flag, final Collection<String> options, boolean flagEnabled) {
        if(flagEnabled) {
            addFlag(flag, options);
        }
    }

    public static String asDirPath(String path) {
        int len = File.separator.length();
        String subStr = path.substring(path.length() - len);
        if(!subStr.equals(File.separator)) {
            return path + File.separator;
        }
        return path;
    }

    public static String join(final String separator, final List<String> parts) {
        if(parts.isEmpty()) {
            return "";
        }
        StringBuilder list = new StringBuilder();
        for(int i = 0; i < parts.size() - 1; i++){
            list.append(parts.get(i));
            list.append(separator);
        }
        list.append(parts.get(parts.size() - 1));
        return list.toString();
    }

    public static boolean isFlag(String str) {
        return str.length() >= 2 && str.charAt(0) == '-' && Character.isLetter(str.charAt(1));
    }

    public static void setOptions(OptionHandler optionHandler, final String options) throws
            Exception {
        optionHandler.setOptions(Utils.splitOptions(options));
    }

    public static void setOptions(OptionHandler optionHandler, String options, String delimiter) throws
            Exception {
        String[] optionsArray = options.split(delimiter);
        optionHandler.setOptions(optionsArray);
    }
    
    public static <A> A parse(String str, Class<A> clazz) {
        final Object output;
        if(clazz.equals(Integer.class)) {
            output = Integer.parseInt(str);
        } else if(clazz.equals(Double.class)) {
            output = Double.parseDouble(str);
        } else if(clazz.equals(Long.class)) {
            output = Long.parseLong(str);
        } else if(clazz.equals(Float.class)) {
            output = Float.parseFloat(str);
        } else if(clazz.equals(String.class)) {
            output = str;
        } else if(clazz.equals(Byte.class)) {
            output = Byte.parseByte(str);
        } else if(clazz.equals(Boolean.class)) {
            output = Boolean.parseBoolean(str);
        } else if(clazz.equals(Short.class)) {
            output = Short.parseShort(str);
        } else {
            try {
                output = fromOptionValue(str);
            } catch(Exception e) {
                throw new IllegalStateException(e);
            }
        }
        if(clazz.isInstance(output)) {
            return (A) output;
        } else {
            throw new ClassCastException("cannot cast " + output.getClass() + " to " + clazz);
        }
    }
}
