import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.*;
import java.util.Locale;

import static java.time.DayOfWeek.*;
import static java.time.Month.NOVEMBER;
import static java.time.Month.OCTOBER;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static java.time.temporal.TemporalAdjusters.nextOrSame;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DateTimeTest {

    @Test
    void localDate() {
        LocalDate now = LocalDate.now();
        LocalDate date1 = LocalDate.of(2023, 10, 8);
        LocalDate date = LocalDate.parse("2023-10-08");

        assertThat(date.getYear()).isEqualTo(2023);
        assertThat(date.getMonth()).isEqualTo(OCTOBER);
        assertThat(date.getMonthValue()).isEqualTo(10);
        assertThat(date.getDayOfMonth()).isEqualTo(8);
        assertThat(date.getDayOfWeek()).isEqualTo(SUNDAY);
        assertThat(date.lengthOfMonth()).isEqualTo(31);
        assertThat(date.isLeapYear()).isEqualTo(false);
    }

    @Test
    void localTime() {
        LocalTime of = LocalTime.of(19, 21, 30);
        LocalTime parse = LocalTime.parse("19:21:30");
    }

    @Test
    void localDateTime() {
        LocalDateTime localDateTime = LocalDateTime.of(2023, OCTOBER, 8, 19, 21, 30);
        LocalDate localDate = localDateTime.toLocalDate();
        LocalTime localTime = localDateTime.toLocalTime();
    }

    @Test
    void duration() {
        LocalDate date1 = LocalDate.of(2023, 10, 8);
        LocalDate date2 = LocalDate.of(2023, 10, 10);

        assertThrows(UnsupportedTemporalTypeException.class, () -> Duration.between(date1, date2));

        Period period = Period.between(date1, date2);
        assertThat(period.getDays()).isEqualTo(2);

        LocalDateTime localDateTime1 = LocalDateTime.of(2023, OCTOBER, 8, 19, 21, 30);
        LocalDateTime localDateTime2 = LocalDateTime.of(2023, NOVEMBER, 10, 19, 21, 30);

        Duration duration = Duration.between(localDateTime1, localDateTime2);
        assertThat(duration.get(ChronoUnit.SECONDS)).isEqualTo(2851200);
    }

    @Test
    @DisplayName("날짜 조정")
    void date() {
        LocalDate date = LocalDate.of(2023, 10, 8);
        assertThat(date.plusYears(1).getYear()).isEqualTo(2024);
        assertThat(date.plusMonths(3).getMonthValue()).isEqualTo(1);
        assertThat(date.plusDays(24).getDayOfMonth()).isEqualTo(1);

        LocalDate date1 = LocalDate.of(2014, 3, 18);
        LocalDate with = date1.with(ChronoField.MONTH_OF_YEAR, 9);
        assertThat(with.getYear()).isEqualTo(2014);
        assertThat(with.getMonthValue()).isEqualTo(9);

        LocalDate date2 = with.plusYears(2).minusDays(20);
        assertThat(date2.getYear()).isEqualTo(2016);
        assertThat(date2.getMonthValue()).isEqualTo(8);
        assertThat(date2.getDayOfMonth()).isEqualTo(29);
    }

    @Test
    @DisplayName("복잡한 날짜 조정")
    void temporalAdjusters() {
        LocalDate date = LocalDate.of(2023, 10, 8);
        LocalDate date1 = date.with(nextOrSame(THURSDAY));
        assertThat(date1.getDayOfMonth()).isEqualTo(12);

        LocalDate date2 = date.with(lastDayOfMonth());
        assertThat(date2.getDayOfMonth()).isEqualTo(31);
    }

    private static class NextWorkingDay implements TemporalAdjuster {

        @Override
        public Temporal adjustInto(Temporal temporal) {
            DayOfWeek dow = DayOfWeek.of(temporal.get(ChronoField.DAY_OF_WEEK));
            int dayToAdd = 1;
            if (dow == FRIDAY) dayToAdd = 3;
            else if (dow == SATURDAY) dayToAdd = 2;
            return temporal.plus(dayToAdd, ChronoUnit.DAYS);
        }
    }

    @Test
    void customTemporalAdjusters() {
        LocalDate date = LocalDate.of(2023, 10, 6); // 금요일
        LocalDate with = date.with(new NextWorkingDay());

        assertThat(with.getDayOfMonth()).isEqualTo(9);
    }

    @Test
    void formatterAndParse() {
        LocalDate date = LocalDate.of(2023, 10, 8);
        assertThat(date.format(DateTimeFormatter.ISO_LOCAL_DATE)).isEqualTo("2023-10-08");
        assertThat(date.format(DateTimeFormatter.BASIC_ISO_DATE)).isEqualTo("20231008");

        LocalDate date1 = LocalDate.parse("20231008", DateTimeFormatter.BASIC_ISO_DATE);
        LocalDate date2 = LocalDate.parse("2023-10-08", DateTimeFormatter.ISO_LOCAL_DATE);
        assertThat(date1.toString()).isEqualTo("2023-10-08");
        assertThat(date2).isEqualTo("2023-10-08");

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        assertThat(date.format(dateTimeFormatter)).isEqualTo("08/10/2023");

        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendText(ChronoField.DAY_OF_MONTH)
                .appendLiteral("|")
                .appendText(ChronoField.MONTH_OF_YEAR)
                .appendLiteral("|")
                .appendText(ChronoField.YEAR)
                .parseCaseInsensitive()
                .toFormatter(Locale.KOREA);
        assertThat(date.format(formatter)).isEqualTo("8|10월|2023");
    }

    @Test
    void zoneId() {
        ZoneId rome = ZoneId.of("Europe/Rome");
        LocalDate date = LocalDate.of(2023, 10, 8);
        ZonedDateTime zonedDateTime = date.atStartOfDay(rome);
        assertThat(zonedDateTime).isEqualTo("2023-10-08T00:00+02:00[Europe/Rome]");

        LocalDateTime localDateTime = LocalDateTime.of(2023, OCTOBER, 8, 19, 21, 30);
        ZonedDateTime zonedDateTime1 = localDateTime.atZone(rome);
        assertThat(zonedDateTime1).isEqualTo("2023-10-08T19:21:30+02:00[Europe/Rome]");
    }
}
