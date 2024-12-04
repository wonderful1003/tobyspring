package module.apigwModule;

import java.nio.ByteBuffer;

public class fileUploadByByte {
	
	HttpServletRequest request;
	HttpServletReseponse reseponse;

	byte[] dataBytes = null;
	if(request.getHeader(HttpHeaders.CONTENT_LENGTH) != null ||
			request.getheader(HttpHeaders.TRANSFER_ENCODING) != null) {
		ServletInputStream sis = request.getInputStream();
		ByteBuffer bb = ByteBuffer.allocate(1024).setAutoExpand(true);
		int i = 0;
		byte[] cbuf = new byte[1024];
		while((i = sis.read(cbuf, 0, 1024)) != -1) {
			if(i == 1024) {
				bb.put(cbuf);
			}else {
				
			}
		}
		
		
	}
}
