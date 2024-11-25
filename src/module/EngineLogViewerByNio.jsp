<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="
java.io.*, 
java.nio.*,
java.util.*, 
java.nio.channels.FileChannel,
javax.servlet.http.*,
java.nio.charset.Charset,
org.apache.commons.io.IOUtils,
org.apache.commons.lang3.StringUtils
" %>	

<%!
private void call(HttpServletRequest request, HttpServletResponse response) throws Exception {
    try {
        List<String> requestLine = IOUtils.readLines(request.getInputStream(), "UTF-8");
        String reqBody = StringUtils.join(requestLine, System.lineSeparator());

        String result = execute(reqBody);

        response.setContentType("text/html; charset=utf-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write(result);

    } catch (Exception e) {
        try {
            response.sendError(500);
        } catch (Exception ex) {
            throw ex;
        }
    }
}

private String execute(String reqBody) throws Exception {
 
	String logResult = "";
    RandomAccessFile raf = null;
    FileChannel fileChannel = null;
    ByteBuffer buffer = null;

    try {
        reqBody = reqBody.substring(1, reqBody.length() - 1);
        String[] pairs = reqBody.split(",");
        Map<String, String> keyValueMap = new HashMap<>();
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                keyValueMap.put(key, value);
            }
        }

        String filePath = keyValueMap.get("filePath");
        String instanceName = keyValueMap.get("instanceName");
        String logFileEncoding = "utf-8";
        int defaultMaxLine = Integer.parseInt(keyValueMap.get("rowCount"));
        int bufferSize = defaultMaxLine * 5100;

        File logFile = new File(filePath);
        raf = new RandomAccessFile(logFile, "r");
        fileChannel = raf.getChannel();
        buffer = ByteBuffer.allocate(bufferSize);

        if (raf.length() <= bufferSize) {
            fileChannel.read(buffer);
        } else {
            fileChannel.position(raf.length() - bufferSize);
            fileChannel.read(buffer);
            buffer.flip();

            String searchString = System.lineSeparator();
            int position = findStringPosition(buffer, searchString, logFileEncoding);
            if (position != -1) {
                buffer.position(position + searchString.length());
                buffer.compact();
            }
        }

        buffer.flip();
        String text = Charset.forName(logFileEncoding).decode(buffer).toString();

        String[] texts = text.split(System.lineSeparator());
        LinkedList<String> list = new LinkedList<>();

        int currentLine = 0;
        for (int i = texts.length - 1; i >= 0 && currentLine < defaultMaxLine; i--) {
            currentLine++;
            if (texts[i] != null) {
                list.addFirst(texts[i]);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String log : list) {
            sb.append(log).append("\n");
        }
        logResult = sb.toString();
    } catch (Exception e) {
        logResult = e.getMessage();
    } finally {
        if (fileChannel != null) {
            try {
                fileChannel.close();
            } catch (IOException e) {
                logResult = e.getMessage();
            }
        }

        if (raf != null) {
            try {
                raf.close();
            } catch (IOException e) {
                logResult = e.getMessage();
            }
        }
    }

    return logResult;
}

private int findStringPosition(ByteBuffer buffer, String searchString, String encoding) {
    byte[] searchBytes = searchString.getBytes(Charset.forName(encoding));
    for (int i = 0; i < buffer.remaining(); i++) {
        boolean found = true;
        for (int j = 0; j < searchBytes.length; j++) {
            if (buffer.get(i + j) != searchBytes[j]) {
                found = false;
                break;
            }
        }
        if (found) {
            return i;
        }
    }
    return -1;
}
%>

<%
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "No-cache");
    response.setHeader("Expires", "0");

    call(request, response);
%>
