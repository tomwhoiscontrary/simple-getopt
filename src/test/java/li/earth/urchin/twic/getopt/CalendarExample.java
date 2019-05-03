package li.earth.urchin.twic.getopt;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * <p>A really bad calendar utility, and a slightly better example of how to use this library.
 *
 * <p>Try running this with:
 *
 * <dl>
 * <dt>only the required argument</dt><dd>2019-04-01</dd>
 * <dt>an optional argument</dt><dd>2019-04-01 2019-04-20</dd>
 * <dt>a flag with a value</dt><dd>2019-04-01 2019-04-20 --format dd/MM/yyyy</dd>
 * <dt>short flags</dt><dd>2019-04-01 2019-04-20 -wf EEE.dd/MM/yyyy</dd>
 * </dl>
 */
public class CalendarExample {

    private static final Map<String, String> SHORT_FLAGS = new HashMap<>();
    private static final Set<String> FLAGS_WITH_ARGS = new HashSet<>();

    static {
        SHORT_FLAGS.put("w", "weekdays");
        SHORT_FLAGS.put("f", "format");

        FLAGS_WITH_ARGS.add("format");
    }

    public static void main(String[] args) {
        Arguments arguments = Arguments.of(args, SHORT_FLAGS, FLAGS_WITH_ARGS);

        LocalDate startDate = arguments.at(0, LocalDate::parse);
        LocalDate endDate = arguments.optionalAt(1, LocalDate::parse).orElseGet(() -> startDate.plusMonths(1));
        boolean skipWeekends = arguments.has("weekdays");

        Optional<DateTimeFormatter> format = arguments.getOptional("format", DateTimeFormatter::ofPattern);

        printDateRange(startDate, endDate, skipWeekends, format);
    }

    private static void printDateRange(LocalDate startDate, LocalDate endDate, boolean skipWeekends, Optional<DateTimeFormatter> format) {
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (skipWeekends && (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY)) {
                continue;
            }

            printDates(date, format);
        }
    }

    private static void printDates(LocalDate date, Optional<DateTimeFormatter> format) {
        System.out.println(format.map(date::format).orElseGet(date::toString));
    }

}
