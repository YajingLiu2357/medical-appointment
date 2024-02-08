public class Driver {
    public static void main(String[] args) throws Exception{
        Admin adminMTL = new Admin("MTL0001");
        adminMTL.addAppointment("MTLA080224", "Physician", 2);
        adminMTL.removeAppointment("MTLA080224", "Physician");
    }
}
