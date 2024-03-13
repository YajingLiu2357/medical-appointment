package com.service.dhms;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URL;

public class Client {
    public static void main(String[] args) throws MalformedURLException {
        URL url = new URL("http://localhost:8080/Appointment?wsdl");
        QName qname = new QName("http://dhms.service.com/", "MontrealServerService");
        Service service = Service.create(url, qname);
        Appointment appointment = service.getPort(Appointment.class);
        System.out.println(appointment.hello());
        System.out.println(appointment.addAppointment("MTLA080224", "Physician", 4));
        System.out.println(appointment.listAppointmentAvailability("Physician"));
        System.out.println(appointment.bookAppointment("MTLP0001", "MTLA080224", "Physician"));
        System.out.println(appointment.getAppointmentSchedule("MTLP0001"));
        System.out.println(appointment.cancelAppointment("MTLP0001", "MTLA080224"));
        System.out.println(appointment.removeAppointment("MTLA080224", "Physician"));
    }
}
