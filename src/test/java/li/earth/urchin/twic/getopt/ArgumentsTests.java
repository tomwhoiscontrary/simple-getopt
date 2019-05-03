package li.earth.urchin.twic.getopt;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

@RunWith(Suite.class)
@Suite.SuiteClasses({ArgumentsTests.Parsing.class, ArgumentsTests.Querying.class})
public class ArgumentsTests {

    public static class Parsing {

        @Test
        public void empty() {
            Arguments args = Arguments.of(new String[]{}, mapOf(), setOf());

            assertThat(args.getPositional(), equalTo(listOf()));
            assertThat(args.getFlags(), equalTo(mapOf()));
        }

        @Test
        public void positional() {
            Arguments args = Arguments.of(new String[]{"one", "two", "three"}, mapOf(), setOf());

            assertThat(args.getPositional(), equalTo(listOf("one", "two", "three")));
            assertThat(args.getFlags(), equalTo(mapOf()));
        }

        @Test
        public void flags() {
            Arguments args = Arguments.of(new String[]{"--foo", "--bar", "--baz"}, mapOf(), setOf());

            assertThat(args.getPositional(), equalTo(listOf()));
            assertThat(args.getFlags(), equalTo(mapOf("foo", Arguments.NO_VALUE, "bar", Arguments.NO_VALUE, "baz", Arguments.NO_VALUE)));
        }

        @Test
        public void shortFlags() {
            Arguments args = Arguments.of(new String[]{"-f", "-b", "-B"}, mapOf("f", "foo", "b", "bar", "B", "baz"), setOf());

            assertThat(args.getPositional(), equalTo(listOf()));
            assertThat(args.getFlags(), equalTo(mapOf("foo", Arguments.NO_VALUE, "bar", Arguments.NO_VALUE, "baz", Arguments.NO_VALUE)));
        }

        @Test
        public void clubbedFlags() {
            Arguments args = Arguments.of(new String[]{"-fbB"}, mapOf("f", "foo", "b", "bar", "B", "baz"), setOf());

            assertThat(args.getPositional(), equalTo(listOf()));
            assertThat(args.getFlags(), equalTo(mapOf("foo", Arguments.NO_VALUE, "bar", Arguments.NO_VALUE, "baz", Arguments.NO_VALUE)));
        }

        @Test
        public void flagsWithValues() {
            Arguments args = Arguments.of(new String[]{"--foo", "red", "--bar", "green", "--baz", "blue"}, mapOf(), setOf("foo", "bar", "baz"));

            assertThat(args.getPositional(), equalTo(listOf()));
            assertThat(args.getFlags(), equalTo(mapOf("foo", "red", "bar", "green", "baz", "blue")));
        }

        @Test
        public void shortFlagsWithValues() {
            Arguments args = Arguments.of(new String[]{"-f", "red", "-b", "green", "-B", "blue"}, mapOf("f", "foo", "b", "bar", "B", "baz"), setOf("foo", "bar", "baz"));

            assertThat(args.getPositional(), equalTo(listOf()));
            assertThat(args.getFlags(), equalTo(mapOf("foo", "red", "bar", "green", "baz", "blue")));
        }

        @Test
        public void clubbedShortFlagsWithValues() {
            Arguments args = Arguments.of(new String[]{"-fbB", "red", "green", "blue"}, mapOf("f", "foo", "b", "bar", "B", "baz"), setOf("foo", "bar", "baz"));

            assertThat(args.getPositional(), equalTo(listOf()));
            assertThat(args.getFlags(), equalTo(mapOf("foo", "red", "bar", "green", "baz", "blue")));
        }

        @Test
        public void mixture() {
            Arguments args = Arguments.of(new String[]{"one", "--qux", "two", "-fB", "red", "three", "--bar", "green", "four"}, mapOf("f", "foo", "b", "bar", "B", "baz"), setOf("foo", "bar"));

            assertThat(args.getPositional(), equalTo(listOf("one", "two", "three", "four")));
            assertThat(args.getFlags(), equalTo(mapOf("qux", Arguments.NO_VALUE, "foo", "red", "baz", Arguments.NO_VALUE, "bar", "green")));
        }

    }

    public static class Querying {

        @Test
        public void empty() {
            Arguments args = Arguments.of(new String[]{}, mapOf(), setOf());

            assertThat(args.size(), equalTo(0));
            assertThat(args.optionalAt(0), equalTo(Optional.empty()));
            assertThat(args.optionalAt(0, String::length), equalTo(Optional.empty()));
            assertThat(() -> args.at(0), raises(instanceOf(IndexOutOfBoundsException.class)));
            assertThat(() -> args.at(0, String::length), raises(instanceOf(IndexOutOfBoundsException.class)));

            assertThat(args.getOptional("foo"), equalTo(Optional.empty()));
            assertThat(args.getOptional("foo", String::length), equalTo(Optional.empty()));
            assertThat(() -> args.get("foo"), raises(instanceOf(NoSuchElementException.class)));
            assertThat(() -> args.get("foo", String::length), raises(instanceOf(NoSuchElementException.class)));
        }

        @Test
        public void positional() {
            Arguments args = Arguments.of(new String[]{"one", "two", "three"}, mapOf(), setOf());

            assertThat(args.size(), equalTo(3));
            assertThat(args.optionalAt(2), equalTo(Optional.of("three")));
            assertThat(args.optionalAt(2, String::length), equalTo(Optional.of(5)));
            assertThat(args.optionalAt(3), equalTo(Optional.empty()));
            assertThat(args.optionalAt(3, String::length), equalTo(Optional.empty()));
            assertThat(() -> args.at(3), raises(instanceOf(IndexOutOfBoundsException.class)));
            assertThat(() -> args.at(3, String::length), raises(instanceOf(IndexOutOfBoundsException.class)));
        }

        @Test
        public void flags() {
            Arguments args = Arguments.of(new String[]{"--foo", "red", "--bar"}, mapOf(), setOf("foo"));

            assertThat(args.size(), equalTo(0));
            assertThat(args.has("foo"), equalTo(true));
            assertThat(args.has("bar"), equalTo(true));
            assertThat(args.has("baz"), equalTo(false));

            assertThat(args.get("foo"), equalTo("red"));
            assertThat(() -> args.get("bar"), raises(instanceOf(IllegalStateException.class)));
            assertThat(() -> args.get("baz"), raises(instanceOf(NoSuchElementException.class)));
            assertThat(args.get("foo", String::length), equalTo(3));
            assertThat(() -> args.get("bar", String::length), raises(instanceOf(IllegalStateException.class)));
            assertThat(() -> args.get("baz", String::length), raises(instanceOf(NoSuchElementException.class)));

            assertThat(args.getOptional("foo"), equalTo(Optional.of("red")));
            assertThat(() -> args.getOptional("bar"), raises(instanceOf(IllegalStateException.class)));
            assertThat(args.getOptional("baz"), equalTo(Optional.empty()));
            assertThat(args.getOptional("foo", String::length), equalTo(Optional.of(3)));
            assertThat(() -> args.getOptional("bar", String::length), raises(instanceOf(IllegalStateException.class)));
            assertThat(args.getOptional("baz", String::length), equalTo(Optional.empty()));
        }

    }

    private static Map<String, String> mapOf(String... keysAndValues) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            map.put(keysAndValues[i], keysAndValues[i + 1]);
        }
        return map;
    }

    private static Set<String> setOf(String... elements) {
        return new HashSet<>(Arrays.asList(elements));
    }

    private static List<String> listOf(String... elements) {
        return Arrays.asList(elements);
    }

    private static Matcher<Callable<?>> raises(Matcher<? super Throwable> subMatcher) {
        return new FeatureMatcher<Callable<?>, Throwable>(subMatcher, "thrown exception", "thrown exception") {
            @Override
            protected Throwable featureValueOf(Callable<?> actual) {
                try {
                    actual.call();
                } catch (Throwable e) {
                    return e;
                }
                return null;
            }
        };
    }

}
