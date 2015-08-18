package gov.va.semoss.com.codec;

public interface CodecFactory {

	public WebCodec getCodec(Class<?> targetClass);

	
}
