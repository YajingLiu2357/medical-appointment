package com.service.dhms;

import javax.xml.ws.Endpoint;

public class Publish {
    public static void main(String[] args){
        Endpoint endpoint = Endpoint.publish("http://localhost:8080/Appointment", new MontrealServer());
        System.out.println("Service is published. " + endpoint.isPublished());
    }
}
