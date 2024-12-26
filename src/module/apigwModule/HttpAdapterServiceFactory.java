//package module.apigwModule;
//
//import java.util.concurrent.ConcurrentHashMap;
//
//public class HttpAdapterServiceFactory {
//
//	static private ConcurrentHashMap<String, HttpAdapterService> h = new ConcurrentHashMap<String, HttpAdapterService>();
//	
//	public HttpAdapterServiceFactory() {
//		super();
//	}
//	
//	@SuppressWarnings("rawtypes")
//	public static HttpAdapterService createFactory(String className) {
//		if(h.containsKey(className)) {
//			return h.get(className);
//		}
//		
//		HttpAdapterService service = null;
//		
//		try {
//			Class cl = Class.forName(className);
//			service = (HttpAdapterService) cl.newInstance();
//			if(service != null) {
//				h.compute(className, service);
//			}
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//		return service;
//
//	}
//}
