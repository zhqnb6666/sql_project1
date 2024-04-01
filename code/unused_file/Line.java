import java.time.LocalDateTime;
import java.util.Arrays;

public class Line {
    private String lineName;
    private String[] stations;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String intro;
    private double mileage;
    private String color;
    private LocalDateTime firstOpening;
    private String url;

    // Constructor, getters, and setters

    public Line(String lineName, String[] stations, LocalDateTime startTime, LocalDateTime endTime,
                String intro, double mileage, String color, LocalDateTime firstOpening, String url) {
        this.lineName = lineName;
        this.stations = stations;
        this.startTime = startTime;
        this.endTime = endTime;
        this.intro = intro;
        this.mileage = mileage;
        this.color = color;
        this.firstOpening = firstOpening;
        this.url = url;
    }

    // Add getters and setters here

    @Override
    public String toString() {
        return "Line{" +
                "lineName='" + lineName + '\'' +
                ", stations=" + Arrays.toString(stations) +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", intro='" + intro + '\'' +
                ", mileage=" + mileage +
                ", color='" + color + '\'' +
                ", firstOpening=" + firstOpening +
                ", url='" + url + '\'' +
                '}';
    }
}
