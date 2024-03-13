package com.service.dhms;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;

public class TestMultipleThreads {
    static Appointment mtl;
    static Appointment que;
    static Appointment she;
    static Patient patientMTL;
    static Patient patientQUE;
    public static void main (String args[]){
        try {
            URL urlMTL = new URL("http://localhost:8080/appointment/mtl?wsdl");
            QName qnameMTL = new QName("http://dhms.service.com/", "MontrealServerService");
            Service serviceMTL = Service.create(urlMTL, qnameMTL);
            mtl = serviceMTL.getPort(Appointment.class);
            URL urlQUE = new URL("http://localhost:8080/appointment/que?wsdl");
            QName qnameQUE = new QName("http://dhms.service.com/", "QuebecServerService");
            Service serviceQUE = Service.create(urlQUE, qnameQUE);
            que = serviceQUE.getPort(Appointment.class);
            URL urlSHE = new URL("http://localhost:8080/appointment/she?wsdl");
            QName qnameSHE = new QName("http://dhms.service.com/", "SherbrookeServerService");
            Service serviceSHE = Service.create(urlSHE, qnameSHE);
            she = serviceSHE.getPort(Appointment.class);
            Admin adminMTL = new Admin("MTLA0001", mtl);
            adminMTL.addAppointment("MTLA080224", "Physician", 4);
            Admin adminQUE = new Admin("QUEA0001", que);
            adminQUE.addAppointment("QUEA100224", "Physician", 1);
            patientMTL = new Patient("MTLP0001", mtl);
            patientQUE = new Patient("QUEP0001", que);
            patientMTL.bookAppointment("MTLP0001", "MTLA080224", "Physician");
            ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(2);
            executor.execute(new BookAppointment());
            executor.execute(new SwapAppointment());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    public static class BookAppointment implements Runnable{
        @Override
        public void run() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            patientQUE.bookAppointment("QUEP0001", "QUEA100224", "Physician");
        }
    }
    public static class SwapAppointment implements Runnable{
        @Override
        public void run() {
            patientMTL.swapAppointment("MTLP0001", "MTLA080224", "Physician", "QUEA100224", "Physician");
        }
    }
}
