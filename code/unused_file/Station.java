public class Station {
    private String stationEnglishName;
    private String district;
    private String busInfo;
    private String outInfo;
    private String intro;
    private String chineseName;

    // Constructor, getters, and setters

    public Station(String stationEnglishName, String district, String busInfo, String outInfo,
                   String intro, String chineseName) {
        this.stationEnglishName = stationEnglishName;
        this.district = district;
        this.busInfo = busInfo;
        this.outInfo = outInfo;
        this.intro = intro;
        this.chineseName = chineseName;
    }

    // Add getters and setters here

    @Override
    public String toString() {
        return "Station{" +
                "stationEnglishName='" + stationEnglishName + '\'' +
                ", district='" + district + '\'' +
                ", busInfo='" + busInfo + '\'' +
                ", outInfo='" + outInfo + '\'' +
                ", intro='" + intro + '\'' +
                ", chineseName='" + chineseName + '\'' +
                '}';
    }
}
