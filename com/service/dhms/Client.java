package com.service.dhms;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URL;

public class Client {
    public static void main(String[] args) throws MalformedURLException {
        URL url = new URL("http://localhost:8080/appointment/mtl?wsdl");
        QName qname = new QName("http://dhms.service.com/", "MontrealServerService");
        Service service = Service.create(url, qname);
        Appointment appointment = service.getPort(Appointment.class);
        System.out.println(appointment.hello());
        System.out.println(appointment.addAppointment("MTLA080224", "Physician", 4));
//        System.out.println(appointment.listAppointmentAvailability("Physician"));
//        System.out.println(appointment.bookAppointment("MTLP0001", "MTLA080224", "Physician"));
//        System.out.println(appointment.getAppointmentSchedule("MTLP0001"));
//        System.out.println(appointment.cancelAppointment("MTLP0001", "MTLA080224"));
//        System.out.println(appointment.removeAppointment("MTLA080224", "Physician"));
        URL url2 = new URL("http://localhost:8080/appointment/que?wsdl");
        QName qname2 = new QName("http://dhms.service.com/", "QuebecServerService");
        Service service2 = Service.create(url2, qname2);
        Appointment appointment2 = service2.getPort(Appointment.class);
        System.out.println(appointment2.hello());
        System.out.println(appointment2.addAppointment("QUEA080224", "Physician", 4));
//        System.out.println(appointment2.listAppointmentAvailability("Physician"));
//        System.out.println(appointment2.bookAppointment("QUEP0001", "QUEA080224", "Physician"));
//        System.out.println(appointment2.getAppointmentSchedule("QUEP0001"));
//        System.out.println(appointment2.cancelAppointment("QUEP0001", "QUEA080224"));
//        System.out.println(appointment2.removeAppointment("QUEA080224", "Physician"));
        System.out.println(appointment.listAppointmentAvailability("Physician"));
        System.out.println(appointment.bookAppointment("MTLP0001", "QUEA080224", "Physician"));
        System.out.println(appointment.getAppointmentSchedule("MTLP0001"));
        URL url3 = new URL("http://localhost:8080/appointment/she?wsdl");
        QName qname3 = new QName("http://dhms.service.com/", "SherbrookeServerService");
        Service service3 = Service.create(url3, qname3);
        Appointment appointment3 = service3.getPort(Appointment.class);
        System.out.println(appointment3.hello());
        System.out.println(appointment3.addAppointment("SHEA080224", "Physician", 4));
        System.out.println(appointment3.listAppointmentAvailability("Physician"));
        System.out.println(appointment3.bookAppointment("SHEP0001", "SHEA080224", "Physician"));
        System.out.println(appointment3.getAppointmentSchedule("SHEP0001"));
        System.out.println(appointment3.cancelAppointment("SHEP0001", "SHEA080224"));
        System.out.println(appointment3.removeAppointment("SHEA080224", "Physician"));
    }
}
