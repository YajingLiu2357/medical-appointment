public class Driver {
    public static void main(String[] args) throws Exception{
        AdminClient adminClientMTL = new AdminClient("MTLA0001");
        adminClientMTL.addAppointment("MTLA080224", "Physician", 2);
        AdminClient adminClientQUE = new AdminClient("QUEA0001");
        adminClientQUE.addAppointment("QUEA080224", "Physician", 2);
        adminClientQUE.addAppointment("QUEM080224", "Surgeon", 2);
        adminClientMTL.listAppointmentAvailability("Physician");
        //adminMTL.removeAppointment("MTLA080224", "Physician");
        PatientClient patientClientMTL = new PatientClient("MTLP0001");
        patientClientMTL.bookAppointment("MTLP0001", "MTLA080224", "Physician");
        patientClientMTL.getAppointmentSchedule("MTLP0001");
        patientClientMTL.cancelAppointment("MTLP0001", "MTLA080224");
    }
}
