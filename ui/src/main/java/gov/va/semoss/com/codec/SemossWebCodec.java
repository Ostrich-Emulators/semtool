package gov.va.semoss.com.codec;

import gov.va.semoss.com.codec.json.JSONCodecFactory;

public class SemossWebCodec {
	
	private static SemossWebCodec instance = null;
	
	private static int PREFERRED_CODEC = 0;
	
	public static final int JSON_CODEC = 0;
	
	private static CodecFactory codecFactory;
	
	private SemossWebCodec(){
		if (codecFactory == null){
			
		}
	}
	
	public static SemossWebCodec instance(){
		if (instance == null){
			instance = new SemossWebCodec();
		}
		return instance;
	}
	
	public void setPreferredCodec(int codecType){
		PREFERRED_CODEC = codecType;
		initCodec();
	}
	
	private void initCodec(){
		switch (PREFERRED_CODEC){
		case 0: {
			codecFactory = new JSONCodecFactory();
			return;
		}
		}
	}

	public String encode(Object object) {
		WebCodec codec = this.codecFactory.getCodec(object.getClass());
		return codec.encode(object);
	}

	public Object decode(Class<?> theClass, String encoding) {
		WebCodec codec = this.codecFactory.getCodec(theClass);
		return codec.decode(encoding);
	}

}
