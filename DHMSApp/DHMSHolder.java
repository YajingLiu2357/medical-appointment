package DHMSApp;

/**
* DHMSApp/DHMSHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from DHMS.idl
* Tuesday, February 27, 2024 4:07:23 PM EST
*/

public final class DHMSHolder implements org.omg.CORBA.portable.Streamable
{
  public DHMSApp.DHMS value = null;

  public DHMSHolder ()
  {
  }

  public DHMSHolder (DHMSApp.DHMS initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = DHMSApp.DHMSHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    DHMSApp.DHMSHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return DHMSApp.DHMSHelper.type ();
  }

}