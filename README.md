# medical-appointment
This is the assignment for COMP 6231 Distributed System Design in Concordia University, Canada.

This project is a distributed health care management systems for managing medical appointment. Admin can manage the informaiton about medical apointments and patient can book, cancel and swap appointments. 

In this project, client can communicate with three servers through RMI or CORBA and servers can communicate each other through UDP/IP.

### RMI

I created local registry and rebind three new server objects with names in `AppointmentRegistry`. I searched the server objects by names and call the corresponding methods in client side. I used three different servers to implement the interface `AppointmentInterfaces` and to handle method calling. 

**Run**: main methods of `AppointmentRegistry`, `MontrealServer`, `QuebecServer`, `SherbrookeServer`, `Driver` in sequence.

### CORBA

I defined module and interface in `DHMS.idl`. I generated DHMSApp folder by using idl-to-java complier. I created three server objects and bound them with proper names in `Server`. I used name service to get server instances and called servers’ methods in `Driver`. 

**Run**: 

(1) Use java 8

(2) Edit configuration of `Server`

```
-ORBInitialPort 1050 -ORBInitialHost localhost
```

(3) Edit configuration of `Driver`

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

(7) Run main methods of `Server`,  `MontrealServer`, `QuebecServer`, `SherbrookeServer`, `Driver` in sequence

### UDP/IP

Server can send requests to get other servers’ data (appointments or booking records) and to book or cancel appointments in other cities. And other servers can listen to requests and give responses. In order to reply different requests parallelly, servers use different threads (RelplyAppointment and ReplyRecord)

