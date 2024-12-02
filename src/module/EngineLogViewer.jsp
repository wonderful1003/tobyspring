<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="
java.io.*, 
java.nio.*,
java.util.*, 
org.apache.mina.common.ByteBuffer,
java.nio.channels.FileChannel,
javax.servlet.http.*,
java.nio.charset.Charset,
org.apache.commons.io.IOUtils,
org.apache.commons.lang3.StringUtils
" %>	

 <%!
private void call(HttpServletRequest request, HttpServletResponse response) throws Exception{
	
	 try{
		List<String> requestLine = IOUtils.readLines(request.getInputStream(), "UTF-8");
		String reqBody = StringUtils.join(requestLine, System.lineSeparator());
		
		String result = execute(reqBody); // file read
		
		response.setContentType("text/html; charset=utf-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer = response.getWriter();
		writer.write(result);	

		// HttpServletRequest.getInputStream()
		// HttpServletResponse.getWriter()
		// 스트림을 명식적으로 닫는 습관을 갖는게 좋긴하나 서블릿컨테이너에서 관리하는 객체들이여서 
		// 자동으로 닫기 때문에 명시적으로 닫을 필요는 없음.
	}catch(Exception e){
		try{
			response.sendError(500);
		}catch(Exception ex){
			throw ex;
		}
	}		
}
	
private String execute(String reqBody) throws Exception{

	String logResult = ""; 			// 타겟 파일 내용
	RandomAccessFile raf = null; 	// 랜덤엑세스파일 : 파일 읽고쓰기를 동시에
	FileChannel fileChannel = null; 	// 파일 채널 : 파일 읽기 안전성을 위해
	ByteBuffer buffer = null; 		// 바이트 버퍼
		
	try{
		// 요청 파라메터 매핑
		reqBody = reqBody.substring(1, reqBody.length()-1);
		String[] pairs = reqBody.split(",");
		Map<String, String> keyValueMap = new HashMap<>();
		for(String pair : pairs){
			String[] keyValue = pair.split("=",2);
			if(keyValue.length == 2){
				String key = keyValue[0].trim();
				String value = keyValue[1].trim();
				keyValueMap.put(key, value);
			}
		}
		//각정 파라메터값 세팅
		String filePath = keyValueMap.get("filePath");
		String instanceName = keyValueMap.get("instanceName");				
		String logFileEncoding = "utf-8";
		int defaultMaxLine = Integer.parseInt(keyValueMap.get("rowCount"));
		int bufferSize = defaultMaxLine * 5100;
		
		String text = "";				
					
		File logFile = new File(filePath); 			// 타겟 로그파일 인스턴스 생성
		raf = new RandomAccessFile(logFile, "r"); 	// 타겟 로그파일 랜덤엑세스파일 읽기모드 생성
		fileChannel = raf.getChannel(); 				// 랜덤엑세스파일의 파일 채널 
		buffer = ByteBuffer.allocate(bufferSize); 	// 힙영역에 버퍼사이즈 만큼 버퍼 할당
		
		if(raf.length() <= bufferSize){
			// bufferSize 바이트 보다 작으면.. 로그파일의 크기가 요청 로우 수 보다 작으면 파일을 그냥 읽어버림
			fileChannel.read(buffer.buf());
		}else{
			// bufferSize 바이트 보다 크면 뒤에서 부터 bufferSize 바이트만큼 읽는다.
			fileChannel.read(buffer.buf(), raf.length() - bufferSize);
			//FileChannel.read(ByteBuffer dst, long position)은 파일의 특정 위치(`position`)에서 바이트 데이터를 읽어서 
			//주어진 `ByteBuffer`에 채운다.
			//  raf.length() - bufferSize -> position과 마찬가지
			buffer.flip(); // 버퍼를 읽기 모드로 전환
			String searchString = System.getProperty("line.separator"); // value : \r\n
			int position = findStringPosition(buffer, searchString, logFileEncoding); // value : 45
			if(position != -1){
				buffer.position(position + searchString.length());
				buffer.compact();
			}				
		}
		
		buffer.flip();
		
		text = buffer.getString(Charset.forName(logFileEncoding).newDecoder());
		
		String[]  texts = text.split(System.lineSeparator());
		
		LinkedList<String> list = new LinkedList<String>();
		
		int currentLine = 0;
		for(int i = texts.length-1; i >= 0 && currentLine < defaultMaxLine; i--){
			currentLine++;
			if(texts[i] != null){
				list.addFirst(texts[i]);
			}
		}
		
		StringBuilder sb = new StringBuilder();
		for(String log : list){
			 sb.append(log).append("\n");
		}
		logResult = sb.toString();
	}catch(Exception e){
		logResult = e.getMessage();
	}finally{
		if(buffer != null){
			buffer.release();
		}
		
		if(fileChannel != null){
			try{
				fileChannel.close();
			}catch(IOException e){
				logResult = e.getMessage();
			}
		}
		
		if(raf != null){
			try{
				raf.close();
			}catch(IOException e){
				logResult = e.getMessage();
			}
		}
	}	
	
	return logResult;
	
}
	
private int findStringPosition(ByteBuffer buffer, String searchString, String encoding){
	byte[] searchBytes = searchString.getBytes(Charset.forName(encoding));
	for(int i = 0; i < buffer.remaining(); i++){
		boolean found = true;
		for(int j = 0; j < searchBytes.length; j++){
			if(buffer.get(i+j) != searchBytes[j]){
				found = false;
				break;
			}
		}
		if(found){
			return i;
		}
	}
	return -1;
}
%>

<%
	response.setHeader("Pragma", "No-cache"); 		// 브라우저에 페이지를 캐시하지 않도록 지시하는 HTTP 헤더 (HTTP 1.0 버전)
	response.setHeader("Cache-Control", "No-cache"); // 브라우저에 페이지를 캐시하지 않도록 지시하는 HTTP 헤더 (HTTP 1.1 버전)
	response.setHeader("Expires", "0"); 				// 프록시가 서버에서 새로운 콘텐츠를 가져오도록 강제

	call(request,response)
%>
