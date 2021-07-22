package li.earth.urchin.twic.getopt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class Arguments {

    @SuppressWarnings("StringOperationCanBeSimplified")
    public static final String NO_VALUE = new String("");

    public static Arguments of(String[] args, Map<String, String> shortFlags, Set<String> flagsWithArgs) {
        List<String> positional = new ArrayList<>();
        Map<String, String> flags = new HashMap<>();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--")) {
                String flag = arg.substring(2);
                String value;
                if (flagsWithArgs.contains(flag)) {
                    value = args[i + 1];
                    ++i;
                } else {
                    value = NO_VALUE;
                }
                flags.put(flag, value);
            } else if (arg.startsWith("-")) {
                for (int j = 1; j < arg.length(); j++) {
                    String shortFlag = arg.substring(j, j + 1);
                    String flag = shortFlags.get(shortFlag);
                    if (flag == null) throw new IllegalArgumentException("unknown short flag: " + shortFlag);
                    String value;
                    if (flagsWithArgs.contains(flag)) {
                        value = args[i + 1];
                        ++i;
                    } else {
                        value = NO_VALUE;
                    }
                    flags.put(flag, value);
                }
            } else {
                positional.add(arg);
            }
        }

        return new Arguments(positional, flags);
    }

    private final List<String> positional;
    private final Map<String, String> flags;

    private Arguments(List<String> positional, Map<String, String> flags) {
        this.positional = positional;
        this.flags = flags;
    }

    // positional

    public int size() {
        return positional.size();
    }

    public String at(int index) {
        return positional.get(index);
    }

    public <T> T at(int index, Function<String, T> parser) {
        return parser.apply(at(index));
    }

    public Optional<String> optionalAt(int index) {
        return positional.size() > index ? Optional.of(positional.get(index)) : Optional.empty();
    }

    public <T> Optional<T> optionalAt(int index, Function<String, T> parser) {
        return optionalAt(index).map(parser);
    }

    // flags

    public boolean has(String flag) {
        return flags.containsKey(flag);
    }

    public String get(String flag) {
        String value = flags.get(flag);
        if (value == null) throw new NoSuchElementException(flag);
        if (value == NO_VALUE) throw new IllegalStateException(flag);
        return value;
    }

    public <T> T get(String flag, Function<String, T> parser) {
        return parser.apply(get(flag));
    }

    public Optional<String> getOptional(String flag) {
        String value = flags.get(flag);
        if (value == NO_VALUE) throw new IllegalStateException(flag);
        return Optional.ofNullable(value);
    }

    public <T> Optional<T> getOptional(String flag, Function<String, T> parser) {
        return getOptional(flag).map(parser);
    }

    // bulk

    public List<String> getPositional() {
        return positional;
    }

    public Map<String, String> getFlags() {
        return flags;
    }

}
