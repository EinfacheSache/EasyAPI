package de.cubeattack.api.util.versioning;

import java.math.BigInteger;
import java.util.*;

public class ComparableVersion implements Comparable<ComparableVersion> {
    private String value;
    private String canonical;
    private ListItem items;

    public ComparableVersion(String version) {
        this.parseVersion(version);
    }

    public final void parseVersion(String version) {
        this.value = version;
        this.items = new ListItem();
        version = version.toLowerCase(Locale.ENGLISH);
        ListItem list = this.items;
        Deque<Item> stack = new ArrayDeque<>();
        stack.push(list);
        boolean isDigit = false;
        int startIndex = 0;

        for(int i = 0; i < version.length(); ++i) {
            char c = version.charAt(i);
            if (c == '.') {
                if (i == startIndex) {
                    list.add(ComparableVersion.IntItem.ZERO);
                } else {
                    list.add(parseItem(isDigit, version.substring(startIndex, i)));
                }

                startIndex = i + 1;
            } else if (c == '-') {
                if (i == startIndex) {
                    list.add(ComparableVersion.IntItem.ZERO);
                } else {
                    list.add(parseItem(isDigit, version.substring(startIndex, i)));
                }

                startIndex = i + 1;
                list.add(list = new ListItem());
                stack.push(list);
            } else if (Character.isDigit(c)) {
                if (!isDigit && i > startIndex) {
                    if (!list.isEmpty()) {
                        list.add(list = new ListItem());
                        stack.push(list);
                    }

                    list.add(new StringItem(version.substring(startIndex, i), true));
                    startIndex = i;
                    list.add(list = new ListItem());
                    stack.push(list);
                }

                isDigit = true;
            } else {
                if (isDigit && i > startIndex) {
                    list.add(parseItem(true, version.substring(startIndex, i)));
                    startIndex = i;
                    list.add(list = new ListItem());
                    stack.push(list);
                }

                isDigit = false;
            }
        }

        if (version.length() > startIndex) {
            if (!isDigit && !list.isEmpty()) {
                list.add(list = new ListItem());
                stack.push(list);
            }

            list.add(parseItem(isDigit, version.substring(startIndex)));
        }

        while(!stack.isEmpty()) {
            list = (ListItem)stack.pop();
            list.normalize();
        }

    }

    private static Item parseItem(boolean isDigit, String buf) {
        if (isDigit) {
            buf = stripLeadingZeroes(buf);
            if (buf.length() <= 9) {
                return new IntItem(buf);
            } else {
                return buf.length() <= 18 ? new LongItem(buf) : new BigIntegerItem(buf);
            }
        } else {
            return new StringItem(buf, false);
        }
    }

    private static String stripLeadingZeroes(String buf) {
        if (buf != null && !buf.isEmpty()) {
            for(int i = 0; i < buf.length(); ++i) {
                char c = buf.charAt(i);
                if (c != '0') {
                    return buf.substring(i);
                }
            }

            return buf;
        } else {
            return "0";
        }
    }

    public int compareTo(ComparableVersion o) {
        return this.items.compareTo(o.items);
    }

    public String toString() {
        return this.value;
    }

    public String getCanonical() {
        if (this.canonical == null) {
            this.canonical = this.items.toString();
        }

        return this.canonical;
    }

    public boolean equals(Object o) {
        return o instanceof ComparableVersion && this.items.equals(((ComparableVersion)o).items);
    }

    public int hashCode() {
        return this.items.hashCode();
    }

    public static void main(String... args) {
        System.out.println("Display parameters as parsed by Maven (in canonical form and as a list of tokens) and comparison result:");
        if (args.length != 0) {
            ComparableVersion prev = null;
            int i = 1;

            for (String version : args) {
                ComparableVersion c = new ComparableVersion(version);
                if (prev != null) {
                    int compare = prev.compareTo(c);
                    System.out.println("   " + prev + ' ' + (compare == 0 ? "==" : (compare < 0 ? "<" : ">")) + ' ' + version);
                }

                System.out.println(i++ + ". " + version + " -> " + c.getCanonical() + "; tokens: " + c.items.toListString());
                prev = c;
            }

        }
    }

    private static class ListItem extends ArrayList<Item> implements Item {
        private ListItem() {
        }

        public int getType() {
            return 2;
        }

        public boolean isNull() {
            return this.size() == 0;
        }

        void normalize() {
            for(int i = this.size() - 1; i >= 0; --i) {
                Item lastItem = this.get(i);
                if (lastItem.isNull()) {
                    this.remove(i);
                } else if (!(lastItem instanceof ListItem)) {
                    break;
                }
            }

        }

        public int compareTo(Item item) {
            if (item == null) {
                if (this.size() == 0) {
                    return 0;
                } else {
                    Iterator<Item> left = this.iterator();

                    int result;
                    do {
                        if (!left.hasNext()) {
                            return 0;
                        }

                        Item i = left.next();
                        result = i.compareTo(null);
                    } while(result == 0);

                    return result;
                }
            } else {
                switch (item.getType()) {
                    case 0:
                    case 3:
                    case 4:
                        return -1;
                    case 1:
                        return 1;
                    case 2:
                        Iterator<Item> left = this.iterator();
                        Iterator<Item> right = ((ListItem)item).iterator();

                        int result;
                        do {
                            if (!left.hasNext() && !right.hasNext()) {
                                return 0;
                            }

                            Item l = left.hasNext() ? left.next() : null;
                            Item r = right.hasNext() ? right.next() : null;
                            result = l == null ? (r == null ? 0 : -1 * r.compareTo(l)) : l.compareTo(r);
                        } while(result == 0);

                        return result;
                    default:
                        throw new IllegalStateException("invalid item: " + item.getClass());
                }
            }
        }

        public String toString() {
            StringBuilder buffer = new StringBuilder();

            Item item;
            for(Iterator<Item> var2 = this.iterator(); var2.hasNext(); buffer.append(item)) {
                item = var2.next();
                if (buffer.length() > 0) {
                    buffer.append(item instanceof ListItem ? '-' : '.');
                }
            }

            return buffer.toString();
        }

        private String toListString() {
            StringBuilder buffer = new StringBuilder();
            buffer.append("[");

            for (Item item : this) {
                if (buffer.length() > 1) {
                    buffer.append(", ");
                }

                if (item instanceof ListItem) {
                    buffer.append(((ListItem) item).toListString());
                } else {
                    buffer.append(item);
                }
            }

            buffer.append("]");
            return buffer.toString();
        }
    }

    private static class IntItem implements Item {
        private final int value;
        public static final IntItem ZERO = new IntItem();

        private IntItem() {
            this.value = 0;
        }

        IntItem(String str) {
            this.value = Integer.parseInt(str);
        }

        public int getType() {
            return 3;
        }

        public boolean isNull() {
            return this.value == 0;
        }

        public int compareTo(Item item) {
            if (item == null) {
                return this.value == 0 ? 0 : 1;
            } else {
                switch (item.getType()) {
                    case 0:
                    case 4:
                        return -1;
                    case 1:
                        return 1;
                    case 2:
                        return 1;
                    case 3:
                        int itemValue = ((IntItem)item).value;
                        return Integer.compare(this.value, itemValue);
                    default:
                        throw new IllegalStateException("invalid item: " + item.getClass());
                }
            }
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o != null && this.getClass() == o.getClass()) {
                IntItem intItem = (IntItem)o;
                return this.value == intItem.value;
            } else {
                return false;
            }
        }

        public int hashCode() {
            return this.value;
        }

        public String toString() {
            return Integer.toString(this.value);
        }
    }

    private interface Item {

        int compareTo(Item var1);

        int getType();

        boolean isNull();
    }

    private static class StringItem implements Item {
        private static final List<String> QUALIFIERS = Arrays.asList("alpha", "beta", "milestone", "rc", "snapshot", "", "sp");
        private static final Properties ALIASES = new Properties();
        private static final String RELEASE_VERSION_INDEX;
        private final String value;

        StringItem(String value, boolean followedByDigit) {
            if (followedByDigit && value.length() == 1) {
                switch (value.charAt(0)) {
                    case 'a':
                        value = "alpha";
                        break;
                    case 'b':
                        value = "beta";
                        break;
                    case 'm':
                        value = "milestone";
                }
            }

            this.value = ALIASES.getProperty(value, value);
        }

        public int getType() {
            return 1;
        }

        public boolean isNull() {
            return comparableQualifier(this.value).compareTo(RELEASE_VERSION_INDEX) == 0;
        }

        public static String comparableQualifier(String qualifier) {
            int i = QUALIFIERS.indexOf(qualifier);
            return i == -1 ? QUALIFIERS.size() + "-" + qualifier : String.valueOf(i);
        }

        public int compareTo(Item item) {
            if (item == null) {
                return comparableQualifier(this.value).compareTo(RELEASE_VERSION_INDEX);
            } else {
                switch (item.getType()) {
                    case 0:
                    case 3:
                    case 4:
                        return -1;
                    case 1:
                        return comparableQualifier(this.value).compareTo(comparableQualifier(((StringItem)item).value));
                    case 2:
                        return -1;
                    default:
                        throw new IllegalStateException("invalid item: " + item.getClass());
                }
            }
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o != null && this.getClass() == o.getClass()) {
                StringItem that = (StringItem)o;
                return this.value.equals(that.value);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return this.value.hashCode();
        }

        public String toString() {
            return this.value;
        }

        static {
            ALIASES.put("ga", "");
            ALIASES.put("final", "");
            ALIASES.put("release", "");
            ALIASES.put("cr", "rc");
            RELEASE_VERSION_INDEX = String.valueOf(QUALIFIERS.indexOf(""));
        }
    }

    private static class LongItem implements Item {
        private final long value;

        LongItem(String str) {
            this.value = Long.parseLong(str);
        }

        public int getType() {
            return 4;
        }

        public boolean isNull() {
            return this.value == 0L;
        }

        public int compareTo(Item item) {
            if (item == null) {
                return this.value == 0L ? 0 : 1;
            } else {
                switch (item.getType()) {
                    case 0:
                        return -1;
                    case 1:
                        return 1;
                    case 2:
                        return 1;
                    case 3:
                        return 1;
                    case 4:
                        long itemValue = ((LongItem)item).value;
                        return Long.compare(this.value, itemValue);
                    default:
                        throw new IllegalStateException("invalid item: " + item.getClass());
                }
            }
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o != null && this.getClass() == o.getClass()) {
                LongItem longItem = (LongItem)o;
                return this.value == longItem.value;
            } else {
                return false;
            }
        }

        public int hashCode() {
            return (int)(this.value ^ this.value >>> 32);
        }

        public String toString() {
            return Long.toString(this.value);
        }
    }

    private static class BigIntegerItem implements Item {
        private final BigInteger value;

        BigIntegerItem(String str) {
            this.value = new BigInteger(str);
        }

        public int getType() {
            return 0;
        }

        public boolean isNull() {
            return BigInteger.ZERO.equals(this.value);
        }

        public int compareTo(Item item) {
            if (item == null) {
                return BigInteger.ZERO.equals(this.value) ? 0 : 1;
            } else {
                switch (item.getType()) {
                    case 0:
                        return this.value.compareTo(((BigIntegerItem)item).value);
                    case 1:
                        return 1;
                    case 2:
                        return 1;
                    case 3:
                    case 4:
                        return 1;
                    default:
                        throw new IllegalStateException("invalid item: " + item.getClass());
                }
            }
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o != null && this.getClass() == o.getClass()) {
                BigIntegerItem that = (BigIntegerItem)o;
                return this.value.equals(that.value);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return this.value.hashCode();
        }

        public String toString() {
            return this.value.toString();
        }
    }
}