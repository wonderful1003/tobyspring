<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.eactive.eai.rms.common.datasource.DataSourceTypeManager"%>
<%@ page import="java.io.*, 
java.util.*, 
java.sql.*, 
javax.sql.*, 
java.net.ConnectException, 
java.net.HttpURLConnection, 
java.net.URL,         
com.eactive.eai.rms.common.datasource.*,
com.eactive.eai.rms.common.util.StringUtils,
com.eactive.eai.common.util.*,
com.eactive.eai.rms.onl.common.util.*
" %>		        

<%!		
private String serviceType = "APIGW";
private DataSource ds = null;
private String conUrl = ""; 		// 원격접속URL
int connectionTimeOut = 5000; 	// 원격서버커넥션타임아웃
int readTimeOut = 10000; 		// 원격서버리드타임아웃
private String filePath = ""; 	// 파일경로 디폴트값 설정. 프로퍼티에서 가져오는것으로 변경 log.directory.prefix

// DataSource 가져오기 db에 쿼리를 실행하기위한 Connection을 만드는데 사용
private DataSource getDataSourceByServiceType(String serviceType) throws Exception{
	
	DataSourceType dataType = DataSourceTypeManager.getDataSourceType(serviceType); //JNDI 정보 갖고 오기
	String jndiName 		= dataType.getJndiName(); 		//JNDI 명 가져오기
	ServiceLocator sl 	= ServiceLocator.getInstance(); 	//DataSource 가져오기
	DataSource sl 		= sl.getDataSource(jndiName); 	//JNDI 명에 해당하는 DataSource 가져오기
	
	return ds;
}
	
// remote 서버 URL 생성
private String getServerURL(String instanceName) throws Exception{
	// 순서  1. 커넥션 생성, 
	//	   2. sql작성 후 Statement.executeQuery 실행 
	//	   3. ResultSet 결과 받아서 return
	Connection conn = null;
	Statement stmt = null;
	ResultSet rs = null;

	try{
		ds = getDataSourceByServiceType(serviceType); // ds 정보 갖고오기
		conn = ds.getConnection();					 // Connection 생성
		stmt = conn.createStatement(); 				 // Statement 생성
		String sql = "select eaisevrip as ip, sevrlsnportname as port " 
					+ "from ngwown.tseaisy02 "
					+ "where eaisevrinstncname = "+"'"+instanceName+"'";
		rs = stmt.executeQuery(sql); // 쿼리 실행 하여 ResultSet 생성
		while(rs.next()){
			conUrl = "http://"+rs.getString("ip")+":"+rs.getString("port")+"/EngineLogViewer.jsp";					
		}
		return conUrl;
	}catch(Exception e){
		return conUrl = "error : "+e.getMessage();
	}finally{
		// close 순서를 생성한 순의 역순으로 rs > stmt > conn 순으로 close 해야함
		// 각 객체마다 close시 nullpoint 에러 발생 가능하고 close 할때도 exception이 발생할 수 있어 null체크와 try catch필요
		if(rs != null){
			try{
				rs.close();
			}catch(SQLException sqle){
				return conUrl = "sql error : "+sqle.getMessage();
			}
		}
		if(stmt != null){
			try{
				stmt.close();
			}catch(SQLException sqle){
				return conUrl = "sql error : "+sqle.getMessage();
			}
		}
		if(conn != null){
			try{
				conn.close();
			}catch(SQLException sqle){
				return conUrl = "sql error : "+sqle.getMessage();
			}
		}								
	}		
}
	
private HashMap execute(HashMap<String,String> paramMap) throws Exception{
	
	HashMap<String,String> resultMap = new HashMap<>(); // 결과값 return
	
	String instanceName = (String)paramMap.get("instanceName");
	String data = paramMap.toString();
							
	HttpURLConnection con = null; //finally 부분에서 close하기위해 try-catch문 밖에 선언
	DataOutputStream dos = null; //finally 부분에서 close하기위해 try-catch문 밖에 선언
	
	try{
		
		String instanceUrl = getServerURL(instanceName); // instanceUrl 가져오기
		
		if(instanceUrl.contains("error")){
			resultMap.put("result","[서버 url 생성시 에러 발생] "+instanceUrl);
		}else{
			// Http Connection 생성
			URL url = new URL(instanceUrl);
			con = (HttpURLConnection)url.openConnection();
			con.setUseCaches(false); // 연결이 캐시를 사용하는지 여부를 설정
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type","text/html; charset=utf-8");
			con.setConnectTimeout(connectionTimeOut);
			con.setReadTimeout(readTimeout);

			dos = new DataOutputStream(con.getOutputStream());
			dos.writeBytes(data);
			dos.flush();
			// HTTP Response
			BufferedReader bin = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			String line = bin.readLine();
			StringBuffer buf = new StringBuffer();
			while(line != null){
				buf.append(line).append("\n"); // 개행을위에 \n 추가
				line=bin.readLine();
			}
			bin.close();
		
			String response = buf.toString();					
			if(response != null){						
				resultMap.put("result", response);
			}
		}			
	}catch(ConnectException e){
		resultMap.put("result", e.getMessage());	
	}catch(IOException e){
		resultMap.put("result", e.getMessage());	
	}catch(Exception e){
		resultMap.put("result", e.getMessage());	
	}finally{
		if(dos != null){
			try{
				dos.close();
			}catch(IOException e){
				resultMap.put("result", e.getMessage());
				dos = null;
			}
		}
		if(con != null){
			con.disconnect();
		}
	}
	return resultMap;
}
%>

<%
	response.setHeader("Pragma", "No-cache"); 		// 브라우저에 페이지를 캐시하지 않도록 지시하는 HTTP 헤더 (HTTP 1.0 버전)
	response.setHeader("Cache-Control", "No-cache"); // 브라우저에 페이지를 캐시하지 않도록 지시하는 HTTP 헤더 (HTTP 1.1 버전)
	response.setHeader("Expires", "0"); 				// 프록시가 서버에서 새로운 콘텐츠를 가져오도록 강제
	
	String run = request.getParameter("run"); 		// submit한 request parameter 세팅
	String instanceName = request.getParameter("instanceName");
	String filePath = request.getParameter("filePath");
	String rowCount = request.getParameter("rowCount");
	
	if(run == null) run="";
	if(instanceName == null) instanceName="";
	if(filePath == null) filePath="";
	if(rowCount == null) rowCount=""; conUrl="";
	if(conUrl == null) conUrl="";
	
	HashMap runResult = new HashMap();
	
	if(!"".equals(run)){
		HashMap<String, String> paramMap = new HashMap();
		paramMap.put("instanceName", instanceName); // 파라메터 세팅
		paramMap.put("filePath", filePath);
		paramMap.put("rowCount", rowCount);
		
		runResult = execute(paramMap); // remote log execute 실행				
	}
%>
<html>
<head>
<title></title>
<meta http-equiv="Content-Type" content="text/html; charset=euc-kr">
 <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
 <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
 <%@ taglib uri="http://http://www.springframework.org/tags" prefix="spring" %>
 <%@ include file="/jsp/common/include/localemessage.jsp" %>
 <script language="javascript" src="<c:url value="/common/js/common.js" />"></script>
 <jsp:include page="/jsp/common/include/css.jsp" />
 <jsp:include page="/jsp/common/include/script.jsp" />
 <script src="<c:url value="/addon/jquery-confirm/jquery-confirm.min.js" />"></script>
 <link src="<c:url value="/addon/jquery-confirm/jquery-confirm.min.css" />" rel="stylesheet"/>

<script language="javascript">
var $ = jQuery.noConfilet(); // 다른 라이브러리, 다른 버전의 jQuery와 충돌 방지하기/jQuery 객체를 '$' 변수에 다시 할당하지만 원하는 경우 'jQuery()'를 직접 사용할 수도 있다. 
var url = '<c:url value="/logViewer.jsp"/>'; //jsp 페이지에서 url 정보를 저장하는 역할을 함.

$(document).ready(function(){
	// 로그 요청 후 입력값 초기화 방지
	$('select[name=instanceName]').val("<%=instanceName%>");		
	$('input[name=rowCount]').val("<%=rowCount%>");

	// 요청 후 받은 로그가 없으면 인스턴스명 초기화 및 resultData 초기화
	var resultData = $('#resultData').val().slice(0,4);
	if(resultData == 'null'){
		$('select[name=instanceName]').val("선택안함");
		$('#resultData').val(' ');
	}

	$("#btn_submit").click(function(){
		// validation 체크
		var instanceName = $('select[name=instanceName]').val();
		if(instanceName == "선택안함" || instanceName == null){
			alert("인스턴스 명을 선택해주세요");
			return;
		}
		var filePath = $('input[name=filePath]').val();
		if(filePath == "" || filePath == null){
			alert("파일 경로를 입력해주세요");
			return;
		}
		var rowCount = $('input[name=rowCount]').val();
		if(rowCount == "" || rowCount == null){
			alert("출력 할 로그의 줄 수를 입력해주세요");
			return;
		}		
		if(rowCount > 50000){
			confirm("출력하려는 로그 수가 많아서 서버에 과부하 줄수 있습니다 그래도 하시겠습니까?");
		}
		// frm은 table form의 name
		// run은 input type=hidden name
		// run value를 1로 변경하고 action url을 다시 logViewer.jsp로 호출하여 스크립틀릿 실행
		document.forms.frm.run.value = "1";
		document.forms.frm.action = url;
		document.forms.frm.submit();
	});
});
</script>
</head>
<body>
<div class="right_box">
	<div class="cont	ent_top">
		<ul class="path"	>
			<li><a href="#">${rmsMenuPath}</a></li>
		</ul>
	</div>
	<div class="content_middle">
		<div class="search_wrap">
			<button type="button" class="cssbtn" id="btn_submit" level="W">
				<i class="material-icons">expand_circle_down</i> <%= localeMessage.getString("buttone.operate")%>
			</button>
		</div>
		<div class="title">logViewer</div>
		<table class="table_row" align="center">
			<form method="post" name="frm">
				<tr>
					<th align="right" width="80px">
						인스턴스 명 : 
					</th>
					<td align="left" width="200px">
						<div class="select-style">
							<select name="instanceName">
								<%
								 Connection con = null;
								 Statement instanceStmt = null;
								 ResultSet instanceRs = null;
								 
								 Statement propStmt = null;
								 ResultSet propRs = null;
								
								try{
									ds = getDataSourceByServiceType(serviceType);						
									conn = ds.getConnection();
									// 커넥션까지 생성한 후 쿼리를 두번 날려서 resultSet을 두개 가져올수 있음
									
									// 인스턴스명 가져오기
									instanceStmt = conn.createStatement();
									String instanceSql = "select eaisevrinstncname CODE, eaisevrinstncname NAME "
											+ "from ngwown.tseaisy02";
											+ "where eaisevrinstncname <> 'all' and eaisevrinstncname <> 'null' ";
											+ "order by name asc";
									instanceRs = instanceStmt.executeQuery(instanceSql);
									
									// 프로퍼티 가져오기
									propStmt = conn.createStatement();
									String propSql = "select prpty2van path "
											+ "from ngwown.tseaicm03 "
											+ "where prptyname = 'log.directory.prefix' ";
									propRs = propStmt.executeQuery(propSql);
								%>
								<option value="선택안함">선택안함</option> <!--인스턴스명 디폴트값-->
								 <%
									 while(instanceRs.next()){		 		 
								 %>	  
									<option value="<%= instanceRs.getString("CODE") %>"><%= instanceRs.getString("NAME") %></option> <!--인스턴스명 디폴트값-->
								 <%
									}
						 
									while(propRs.next()){		 		 
										if(StringUtils.isEmpty(filePath)){
											filePath=propRs.getString("PATH");
										}
									}
								}catch(Exception e){
									throw e;
								}finally{
									// 자원을 닫는 순서는 자원을 연 순서의 역순으로 rs > stmt > conn 으로 해야함 
									// 자원 연거는 모두 닫아줘야함 두개 열었으면 두개 다 닫아줘야함
									if(instanceRs != null){try{instanceRs.close();}catch(SQLException sqle){}}			
									if(propRs != null){try{propRs.close();}catch(SQLException sqle){}}	
									if(instanceStmt != null){try{instanceStmt.close();}catch(SQLException sqle){}}
									if(propStmt != null){try{propStmt.close();}catch(SQLException sqle){}}
									if(conn != null){try{conn.close();}catch(SQLException sqle){}} 				
								}	
								%>
							</select>
						</div>					
					</td>
					<th align="right" width="80px">
						파일 경로 : 
					</th>
					<td align="left">
						<div class="select-style">
							<input type="text" class="form-control" id="filePath" name="filePath" value="<%=filePath%>"/>
						</div>
					</td>
					<th align="right" width="110px">
						ROW수 (5만이하) :  
					</th>
					<td align="left">
						<div class="select-style">
							<input type="text" class="form-control" id="rowCount" name="rowCount" />
						</div>
					</td>
				</tr>
				<td align="left" colspan=8>
				
				<table width="100%">
					<tr>
						<td class="search_td_title" width="150px">
						fetch row count : <%=rowCount%> | 접속정보 : <%=conUrl%> | 파일 경로 : <%=filePath%>
						</td>
					</tr>
					<tr>
						<td>
							<textarea id="resultData" name="resultData" style="width:100%;height:680px">
<%= runResult.get("result")%>
							</textarea>
							<input type=hidden name="run" value="<%=run%>">							
						</td>
					</tr>
				</table>
			</form>
		 </table>
		</div>
	</div>
</body>
</html>
