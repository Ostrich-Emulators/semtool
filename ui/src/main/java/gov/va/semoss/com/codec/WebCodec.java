package gov.va.semoss.com.codec;

public interface WebCodec {
	
	public String encode(Object instance);
	
	public Object decode(String encoding);

}
