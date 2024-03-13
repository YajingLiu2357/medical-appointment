# medical-appointment
This is the assignment for COMP 6231 Distributed System Design in Concordia University, Canada.

This project is a distributed health care management systems for managing medical appointment. Admin can manage the informaiton about medical apointments and patient can book, cancel and swap appointments. 

In this project, client can communicate with three servers through RMI, CORBA or Web Service and servers can communicate with each other through UDP/IP.

### RMI

I created local registry and rebind three new server objects with names in [AppointmentRegistry](./rmi/AppointmentRegistry.java). I searched the server objects by names and call the corresponding methods in client side. I used three different servers to implement the interface [AppointmentInterfaces](./rmi/AppointmentInterface.java) and to handle method calling. 

**Run**: main methods of [AppointmentRegistry](./rmi/AppointmentRegistry.java), [MontrealServer](./rmi/MontrealServer.java), [QuebecServer](./rmi/QuebecServer.java), [SherbrookeServer](./rmi/SherbrookeServer.java), [Driver](./rmi/Driver.java) in sequence.

### CORBA

I defined module and interface in [DHMS.idl](./DHMS.idl). I generated DHMSApp folder by using idl-to-java complier. I created three server objects and bound them with proper names in [Server](./Server.java). I used name service to get server instances and called servers’ methods in [Driver](./Driver.java). 

**Run**: 

(1) Use java 8

(2) Edit configuration of [Server](./Server.java)

```
-ORBInitialPort 1050 -ORBInitialHost localhost
```

(3) Edit configuration of [Driver](./Driver.java)

```
-ORBInitialPort 1050 -ORBInitialHost localhost
```

(4) Generate DHMSApp folders through idl-to-java complier

```
idlj -fall DHMS.idl 
```

(5) Start ORB server

```
orbd -ORBInitialPort 1050 -ORBInitialHost localhost 
```

(7) Run main methods of [Server](./Server.java),  [MontrealServer](./MontrealServer.java), [QuebecServer](./QuebecServer.java), [SherbrookeServer](./SherbrookeServer.java), [Driver](./Driver.java) in sequence

### Web Service

I developed an interface [Appointment](./com/service/dhms/Appointment.java).  And I developed three different servers to implement this interface. I published these three servers in [Publish](./com/service/dhms/Publish.java).  I used url and qname to get the server instances and called servers' methods in [Driver](./com/service/dhms/Driver.java).

**Run:** main methods of [Publish](./com/service/dhms/Publish.java), [MontrealServer](./com/service/dhms/MontrealServer.java), [QuebecServer](./com/service/dhms/QuebecServer.java), [SherbrookeServer](./com/service/dhms/SherbrookeServer.java), [Driver](./com/service/dhms/Driver.java) in sequence.

### UDP/IP

Server can send requests to get other servers’ data (appointments or booking records) and to book or cancel appointments in other cities. And other servers can listen to requests and give responses. In order to reply different requests parallelly, servers use different threads (RelplyAppointment and ReplyRecord)

