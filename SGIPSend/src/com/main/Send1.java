package com.main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

import com.main.buesiness.Buesiness;
import com.main.buesiness.IBuesiness;

import static com.main.BusinessInfo.*;

public class Send1 extends Thread
{
	private IBuesiness buesiness=new Buesiness();
	private DataOutputStream Dos1;
	private DataInputStream Dis1;
	private Socket mySocket1;
	private SendServices ss;

	
	public Send1(int t){		
		try{
		if(mySocket1==null||mySocket1.isClosed()){			
		mySocket1 = new Socket("10.143.4.71",8801);		
		System.out.println("�������ӵ�"+mySocket1.getInetAddress().getHostAddress());
		}
		Dis1=new DataInputStream(mySocket1.getInputStream());
		Dos1=new DataOutputStream(mySocket1.getOutputStream());
		}catch(Exception e){e.printStackTrace();}
				
	}
	public void run() {
		try {
			
			sendBind();//�󶨲���			
			sendSumbit();//�ύ����			
			sendUnBind();//ע������
			
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally{		
			
			closemySocket1(); //�ر�����	
			
		}
		
	}
	
	
	public void sendBind() throws Exception{
		
		Bind bind = new Bind();
		ByteBuffer buffer = bind.getAllBindBuffer();
		
		Dos1.write(buffer.array());
		Dos1.flush();
		System.out.println("���Ͱ����󣬳���Ϊ:"+bind.getAllBindBuffer().capacity());
		
		int messagelength = Dis1.readInt();
		byte b[] = new byte[messagelength - 4];
		Dis1.readFully(b, 0, messagelength - 4);        
		ByteBuffer buffer2=ByteBuffer.allocate(4);
		buffer.order(byteOrder);
		buffer2.put(b[16]);
		buffer2.put((byte)0);
		buffer2.put((byte)0);
		buffer2.put((byte)0);		
		buffer2.flip();
		
		System.out.println("�󶨽����:"+buffer2.getInt());

	}
	
	public void sendSumbit() {	
		ss=new SendServices();
		String[] r=ss.getSerInfo();  //info[0]=serviceNumber; info[1]=userNumber; info[2]=messageContent; info[3]=reserve;		
		if("".equals(r[0])||"".equals(r[1])){
			System.out.println("û�����ݣ�����ͣ��..");
			return;
		}
		
		Submit submit=new Submit();
		
		Object[] objects=getServiceInfo(r[0]);		
		submit.UserNumber=r[1];		
		submit.ServiceType=(String)objects[0];  //ҵ�����
		submit.FeeType=(Byte)objects[1];           //�Ʒ����� 1���,2,�����Ʒ�,3���¼Ʒ�
		submit.FeeValue=(String)objects[2];            //�Ʒ���������λ�Ƿ�
		submit.MorelatetoMTFlag=(Byte)objects[3];          //1B ����MT��Ϣ��ԭ��0-MO�㲥����ĵ�һ��MT��Ϣ��1-MO�㲥����ķǵ�һ��MT��Ϣ��2-��MO�㲥�����MT��Ϣ��3-ϵͳ���������MT��Ϣ
		submit.MessageContent=r[2];   //����Ϣ����		
		submit.Reserve=((Byte)objects[1]==(byte)3)?"":r[3];             //linkid
		
		
		/*
		Object[] objects=getServiceInfo("10628997");
		submit.UserNumber="8618602861176";
		submit.ServiceType="9081012601";  
		submit.FeeType=(byte)3;           
		submit.FeeValue="500";            
		submit.MorelatetoMTFlag=(byte)2;
		submit.MessageContent="ehruh�ɷɷ�";   
	    submit.Reserve=""; 
	    */            
		submit.idnum=r[4];
		System.out.println(submit.UserNumber+":"+submit.ServiceType+";FeeType:"+submit.FeeType+";FeeValue:"+submit.FeeValue+";MorelatetoMTFlag:"+submit.MorelatetoMTFlag+";Reserve:"+submit.Reserve);
		ByteBuffer buffer=submit.getAllSubmitBuffer();
		ByteBuffer buffer1=buffer;
		try {
			Dos1.write(buffer.array());
			Dos1.flush();
			System.out.println("write�ɹ�����");
		} catch (IOException e) {	
			System.out.println("Dos1�쳣��");
			e.printStackTrace();
		}finally{
			
		}
		
		
		
		
		int messagelength;
		try {
			messagelength = Dis1.readInt();
			byte b[] = new byte[messagelength - 4];
			Dis1.readFully(b, 0, messagelength - 4); 
		
		       
		ByteBuffer buffer2=ByteBuffer.allocate(4);
		buffer2.order(byteOrder);
		buffer2.put(b[16]);
		buffer2.put((byte)0);
		buffer2.put((byte)0);
		buffer2.put((byte)0);		
		buffer2.flip();
		
		System.out.println("���ͽ����:"+buffer2.getInt());
		} catch (IOException e) {
			System.out.println("Dis1������");
		}finally{
		int a1=buffer1.getInt();
		int a2=buffer1.getInt();
		int a3=buffer1.getInt();
		int a4=buffer1.getInt();
		int a5=buffer1.getInt();
		
		int yy=ss.updateFlag(submit.idnum);
		int insertno = ss.insertLog(submit.UserNumber,String.valueOf(submit.head.createTime) , submit.ServiceType, submit.MessageContent,r[3]);
		System.out.println("����ʱ��:"+a4+"::insertCount="+insertno+"updateCount="+yy+"::spcode="+r[0]);
		}
	}
	
	public void sendUnBind() throws Exception{
		UnBind UB=new UnBind();
		Dos1.write(UB.getUnBindBuffer().array());
		Dos1.flush();
		
		byte b[] = new byte[20];
		Dis1.readFully(b, 0, 20);
		ByteBuffer buffer2=ByteBuffer.allocate(20);
		buffer2.order(byteOrder);
		for(int i=0;i<20;i++){
			buffer2.put(b[i]);
		}
		buffer2.flip();
		int a1=buffer2.getInt();
		int a2=buffer2.getInt();
		//System.out.println(a2);
		switch (a2) {
		case SGIPCmd.SGIP_UNBIND_RESP:
			System.out.println("ע����Ӧ");	
			break;

		default:
			break;
		}
			
		
	}
	
	public Object[] getServiceInfo(String serviceNum){
		Object[] bu=null;
		if("106289977".equals(serviceNum)){
			bu=buesiness.get106289977();
		}else if("106289975".equals(serviceNum)){
			bu=buesiness.get106289975();
		}else if("106558411".equals(serviceNum)){
			bu=buesiness.get106558411();
		}else if("10628997".equals(serviceNum)){
			bu=buesiness.get10628997();
		}else if("106289978".equals(serviceNum)){
			bu=buesiness.get106289978();
		}else if("106289976".equals(serviceNum)){
			bu=buesiness.get106289976();
		}else if("106558412".equals(serviceNum)){
			bu=buesiness.get106558412();
		}
		return bu;
	}

	public void closemySocket1(){
		try {
			this.Dis1.close();
			this.Dos1.close();
			this.mySocket1.close();
			System.out.println("socket�ر�");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
