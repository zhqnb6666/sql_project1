public class Passenger {
    private String name;
    private String idNumber; // Unique
    private String phoneNumber;
    private String gender;
    private String district;

    // Constructor, getters, and setters

    public Passenger(String name, String idNumber, String phoneNumber, String gender, String district) {
        this.name = name;
        this.idNumber = idNumber;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.district = district;
    }

    // Add getters and setters here


    @Override
    public String toString() {
        return "Passenger{" +
                "name='" + name + '\'' +
                ", idNumber='" + idNumber + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", gender='" + gender + '\'' +
                ", district='" + district + '\'' +
                '}';
    }
}
