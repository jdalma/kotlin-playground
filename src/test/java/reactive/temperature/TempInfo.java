package reactive.temperature;

import java.util.Random;

public class TempInfo {

    public static final Random random = new Random();

    private final String town;
    private final int temp;

    public TempInfo(String town, int temp) {
        this.town = town;
        this.temp = temp;
    }

    public static TempInfo fetch(String town) {
        if (random.nextInt(10) == 0) {
            throw new RuntimeException("Error");
        }
        return new TempInfo(town, random.nextInt(100));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TempInfo{");
        sb.append("town='").append(town).append('\'');
        sb.append(", temp=").append(temp);
        sb.append('}');
        return sb.toString();
    }

    public String getTown() {
        return town;
    }

    public int getTemp() {
        return temp;
    }
}
