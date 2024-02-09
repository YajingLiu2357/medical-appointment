public class Driver {
    public static void main(String[] args) throws Exception{
        Admin adminMTL = new Admin("MTLA0001");
        adminMTL.addAppointment("MTLA080224", "Physician", 2);
        Admin adminQUE = new Admin("QUEA0001");
        adminQUE.addAppointment("QUEA080224", "Physician", 2);
        adminQUE.addAppointment("QUEM080224", "Surgeon", 2);
        adminMTL.listAppointmentAvailability("Physician");
        //adminMTL.removeAppointment("MTLA080224", "Physician");
        Patient patientMTL = new Patient("MTLP0001");
        patientMTL.bookAppointment("MTLP0001", "MTLA080224", "Physician");
        patientMTL.getAppointmentSchedule("MTLP0001");
        patientMTL.cancelAppointment("MTLP0001", "MTLA080224");
    }
}
