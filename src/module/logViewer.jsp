<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.eactive.eai.rms.common.datasource.DataSourceTypeManager"%>
<%@ page import="java.io.*, 
java.util.*, 
java.sql.*, 
javax.sql.*, 
com.eactive.eai.rms.onl.common.util.*,
com.eactive.eai.rms.common.datasource.*,
com.eactive.eai.rms.common.util.StringUtils,
com.eactive.eai.common.util.*,
java.net.ConnectException, 
java.net.HttpURLConnection, 
java.net.URL,         
javax.net.ssl.*,
javax.net.SocketFactory,
java.security.cert.X509Certificate,
java.security.SecureRandom
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
	DataSource ds 		= sl.getDataSource(jndiName); 	//JNDI 명에 해당하는 DataSource 가져오기
	
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

		String sql = "";
		if(StringUtils.equals("APIGW", serviceType)){
			sql = "select eaisevrip ip, sevrlsnportname port " 
					+ "from ngwown.tseaisy02 "
					+ "where eaisevrinstncname = ? ";
		}else{
			//\'\\d+\\.\\d+.\\d+.\\d+\' -> ip주소 형식 추출
			sql = "select regexp_substr(prptygroupname, \'\\d+\\.\\d+.\\d+.\\d+\') ip, " 
					+ "max(case when prptyname='WEBSERVERPOT' then prpty2val end) port, " 
					+ "max(case when prptyname='protocol' then prpty2val end) protocol " 
					+ "from ngpown.tseairm24 "
					+ "where prptygroupname = ? "
					+ "group by prptygroupname";
		}
		
		pstmt = conn.prepareStatement(sql); // Statement 생성
		pstmt.setString(1, instanceName);
		rs = pstmt.executeQuery(); // 쿼리 실행 하여 ResultSet 생성
		while(rs.next()){
			if(StringUtils.equals("APIGW", serviceType)){
				conUrl = "http://"+rs.getString("ip")+":"+rs.getString("port")+"/EngineLogViewer.jsp";					
			}else{
				conUrl = rs.getString("protocol")+"://"+rs.getString("ip")+":"+rs.getString("port")+"/monitoring/EngineLogViewer.jsp";
			}
		}
		return conUrl;
	}catch(Exception e){
		return conUrl = "error : "+e.getMessage();
	}finally{
		// close 순서를 생성한 순의 역순으로 rs > stmt > conn 순으로 close 해야함
		// 각 객체마다 close시 nullpoint 에러 발생 가능하고 close 할때도 exception이 발생할 수 있어 null체크와 try catch필요
		if(rs != null){try{rs.close();}catch(SQLException sqle){return conUrl = "sql error : "+sqle.getMessage();}}
		if(pstmt != null){try{pstmt.close();}catch(SQLException sqle){return conUrl = "sql error : "+sqle.getMessage();}}
		if(conn != null){try{conn.close();}catch(SQLException sqle){return conUrl = "sql error : "+sqle.getMessage();}}
	}		
}
	
private HashMap execute(HashMap<String,String> paramMap) throws Exception{
	
	HashMap<String,String> resultMap = new HashMap<>(); // 결과값 return
	
	String instanceName = (String)paramMap.get("instanceName");
	String data = paramMap.toString();

	String instanceUrl = getServerURL(instanceName); // instanceUrl 가져오기
	
	if(instanceUrl.contains("error") || StringUtils.isBlank(instanceUrl)){
		resultMap.put("result","[서버 url 생성시 에러 발생] "+instanceUrl);
	}else{
		// url 생성
		URL url = new URL(instanceUrl);

		// HttpS Connection 생성
		if(StringUtils.startsWith(instanceUrl, "https")){
			HttpsURLConnection con = null; //finally 부분에서 close하기위해 try-catch문 밖에 선언
			DataOutputStream dos = null; //finally 부분에서 close하기위해 try-catch문 밖에 선언
	
/* 				1.TrustManager 구현:
					TrustManager[] trustAllCerts 배열은 모든 서버 인증서를 신뢰하도록 설정
					X509TrustManager의 checkClientTrusted와 checkServerTrusted 메서드는 
					빈 구현으로 되어 있어 인증서 검증을 수행하지 않는다.
				
				2.SSLContext 생성 및 초기화:
					SSLContext.getInstance("SSL")로 SSL/TLS 컨텍스트를 생성한다.
					sc.init()에서 신뢰 관리자를 trustAllCerts로 설정합니다.

				3.기본 SSLSocketFactory 설정:
					HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory())를 통해 
					모든 HttpsURLConnection 객체에 대해 이 SSL 설정을 사용하도록 만듭니다.

		        4.HostnameVerifier 생성:
			        - HostnameVerifier는 HTTPS 연결 중 호스트 이름(예: www.example.com)과 
			                  서버의 인증서에 있는 CN(Common Name) 또는 SAN(Subject Alternative Name)을 비교하여 
			        	   유효성을 확인하는 역할을 한다.
			        	- 여기서는 모든 호스트 이름을 신뢰하도록 HostnameVerifier를 구현합니다.

			    5.verify 메서드 구현:
			        - verify 메서드는 호스트 이름과 인증서의 일치 여부를 확인하는 로직을 정의합니다.
		        		- 여기서는 항상 true를 반환하여 모든 호스트 이름을 검증 없이 신뢰하도록 설정합니다.
		        	
		        	6.HostnameVerifier 설치:
		        		- HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid)를 호출하여 
		        		    모든 HttpsURLConnection 요청에서 기본적으로 이 allHostsValid를 사용하도록 설정합니다.
		        		- 결과적으로 HTTPS 요청 중 인증서의 CN/SAN과 호스트 이름이 일치하지 않더라도 예외가 발생하지 않습니다.

	 			7.HTTPS 연결:
					HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();를 통해 HTTPS 요청을 수행합니다.

	        		종합적으로 설명
		        		이 로직은 HTTPS 연결에서 호스트 이름 검증을 무시하는 설정입니다. 
		        		인증서의 CN/SAN이 URL의 호스트 이름과 일치하지 않아도 연결이 거부되지 않고 항상 성공합니다. */
			try{
					
				// Create a trust manager that does not validate certificate chains
		        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
		            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		                return null;
		            }
		            public void checkClientTrusted(X509Certificate[] certs, String authType){
		            }
		            public void checkServerTrusted(X509Certificate[] certs, String authType){
		            }
		        }
		        };
		
		        // Install the all-trusting trust manager
		        SSLContext sc = SSLContext.getInstance("SSL");
		        sc.init(null, trustAllCerts, new java.security.SecureRandom());
		        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		
		        // Create all-trusting host name verifier
		        HostnameVerifier allHostsValid = new HostnameVerifier() {
		            public boolean verify(String hostname, SSLSession session){
		                return true;
		            }
		        };
		
		        // Install the all-trusting host verifier
		        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
				
				con = (HttpsURLConnection)url.openConnection();
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
			}catch(ConnectException e){
				resultMap.put("result", e.getMessage());	
			}catch(IOException e){
				resultMap.put("result", e.getMessage());	
			}catch(Exception e){
				resultMap.put("result", e.getMessage());	
			}finally{
				if(dos != null){	 try{ dos.close(); }catch(IOException e){ resultMap.put("result", e.getMessage()); dos = null; }}
				if(con != null){	 con.disconnect(); }
			}	
		}else{
		// Http Connection 생성	
			HttpURLConnection con = null; //finally 부분에서 close하기위해 try-catch문 밖에 선언
			DataOutputStream dos = null; //finally 부분에서 close하기위해 try-catch문 밖에 선언
	
			try{

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
			}catch(ConnectException e){
				resultMap.put("result", e.getMessage());	
			}catch(IOException e){
				resultMap.put("result", e.getMessage());	
			}catch(Exception e){
				resultMap.put("result", e.getMessage());	
			}finally{
				if(dos != null){	 try{ dos.close(); }catch(IOException e){ resultMap.put("result", e.getMessage()); dos = null; }}
				if(con != null){	 con.disconnect(); }
			}	
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
	String filePath = request.getParameter("filePath");
	String rowCount = request.getParameter("rowCount");
	String instanceName = request.getParameter("inputInstName");
	
	if(run == null) run="";
	if(filePath == null) filePath="";
	if(rowCount == null) rowCount=""; conUrl="";
	if(conUrl == null) conUrl="";
	if(instanceName == null) instanceName="";

	if(StringUtils.containsIgnoreCase(instanceName, "Svr")){
		serviceType = "APIGW";
	}else{
		serviceType = "MONITORING";
	}
	
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

	var sysdiv = "<%=instanceName%>";
	if($('input[name=systemType]').val()=='1' && sysdiv.indexOf('Moni') == -1 ){
		$("#systemType_engine").attr('checked',true);
		$('#engineInstName').show();
		$('#emsInstName').hide();
	}else{
		$("#systemType_ems").attr('checked',true);
		$('#emsInstName').show();
		$('#engineInstName').hide();
	}

	//라디오 변경이벤트
	if($('input[name=systemType]').change(function(){
		if($('input[name=systemType]').prop('checked')){
			$('#engineInstName').val("선택안함");
			$('#engineInstName').show();
			$('#emsInstName').hide();
		}else{
			$('#emsInstName').val("선택안함");
			$('#emsInstName').show();
			$('#engineInstName').hide();
		}
	})	
	
	// 요청 후 받은 로그가 없으면 인스턴스명 초기화 및 resultData 초기화
	var resultData = $('#resultData').val().slice(0,4);
	if(resultData == 'null'){
		$('select[name=instanceName]').val("선택안함");
		$('#resultData').val(' ');
	}
	
	$("#btn_refresh").click(function(){
		const engineRadio = document.getElementById("systemType_engine");
		engineRadio.checked = true;
		
		$('#resultData').val('');
		$('input[name=filePath]').val('');
		$('input[name=rowCount]').val('');
		$('#engineInstName').val("선택안함");
		$('#engineInstName').show();
		$('#emsInstName').hide();
	});

	$("#btn_submit").click(function(){
		// validation 체크
		const systemType = $('input[name=systemType]').prop('checked') ? '1' : '0';
		const inputInstName = document.getElementById("inputInstName");
		
		if(systemType == '1'){
			var engineInstName = $('#engineInstName').val();
			inputInstName.value = engineInstName;
			if(engineInstName == "선택안함" || engineInstName == null){
				alert("인스턴스 명을 선택해주세요");
				return;
			}
		}else{
			var emsInstName = $('#emsInstName').val();
			inputInstName.value = emsInstName;
			if(emsInstName == "선택안함" || emsInstName == null){
				alert("인스턴스 명을 선택해주세요");
				return;
			}
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
		</ul>
	</div>
	<div class="content_middle">
		<div class="search_wrap">
			<button type="button" class="cssbtn" id="btn_refresh">초기화</button>
			<button type="button" class="cssbtn" id="btn_submit">실행</button>
		</div>
		<div class="title">logViewer</div>
		<table class="table_row" align="center">
			<form method="post" name="frm">
				<tr>
					<!-- 시스템선택 추가 -->
					<th align="right" width="80px">
						시스템 : 
					</th>
					<td align="left" width="180px">
						<input type="radio" value="1" name="systemType" id="systemType_engine" checked/>
						<label for="systemType_engine">engine</label>
						<input type="radio" value="0" name="systemType" id="systemType_ems" />
						<label for="systemType_ems">ems</label>
					</td>
					<th align="right" width="80px">
						인스턴스 명 : 
					</th>
					<td align="left" width="200px">
						<div class="select-style">
							<%
								Connection engineConn = null;
								Connection emsConn = null;

								PreparedStatement engineInstStmt = null;
								PreparedStatement propStmt = null;
								PreparedStatement emsInstStmt = null;
								 
								ResultSet emgineInstRs = null;
								ResultSet propRs = null;
								ResultSet emsInstRs = null;
								
								try{
									/* engine db execute */
									serviceType = "APIGW";
									ds = getDataSourceByServiceType(serviceType);						
									engineConn = ds.getConnection();
									// 커넥션까지 생성한 후 쿼리를 두번 날려서 resultSet을 두개 가져올수 있음
									
									// 인스턴스명 가져오기
									String engineInstSql = "select eaisevrinstncname CODE, eaisevrinstncname NAME "
											+ "from ngwown.tseaisy02"
											+ "where eaisevrinstncname <> 'all' and eaisevrinstncname <> 'null' "
											+ "order by name asc";
									engineInstStmt = engineConn.prepareStatement(engineInstSql);
									engineInstRs = engineInstStmt.executeQuery();
									
									// 프로퍼티 가져오기
									String propSql = "select prpty2van path "
											+ "from ngwown.tseaicm03 "
											+ "where prptyname = 'log.directory.prefix' ";
									propStmt = engineConn.prepareStatement(propSql);
									propRs = propStmt.executeQuery();
									
									/* ems db execute */
									String emsInstSql = "select prptygroupname code, prptygroupdesc name "
											+ "from ngpown.tseairm23 "
											+ "where prptygroupname <> 'Simulator' and prptygroupname <> 'null' "
											+ "order by prptygroupname asc ";
									emsInstStmt = emsConn.prepareStatement(emsInstSql);
									emsInstRs = emsInstStmt.executeQuery();
							%>
							<select name="instanceName" id = "engineInstName">
								<option value="선택안함">선택안함 engine</option> <!--인스턴스명 디폴트값-->
								 <% while(engineInstRs.next()){ %>	  
									<option value="<%= engineInstRs.getString("CODE") %>"><%= engineInstRs.getString("NAME") %></option>
								 <% } %>
							</select>	 
							<select name="instanceName" id = "emsInstName">
								<option value="선택안함">선택안함 ems</option> <!--인스턴스명 디폴트값-->
								 <% while(emsInstRs.next()){ %>	  
									<option value="<%= emsInstRs.getString("CODE") %>"><%= emsInstRs.getString("NAME") %></option>
								 <% } %>
							</select>	 
							<% 						 
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
									if(emsInstRs != null){try{emsInstRs.close();}catch(SQLException sqle){}}			
									if(propRs != null){try{propRs.close();}catch(SQLException sqle){}}	
									if(emgineInstRs != null){try{emgineInstRs.close();}catch(SQLException sqle){}}			
									if(emsInstStmt != null){try{emsInstStmt.close();}catch(SQLException sqle){}}
									if(propStmt != null){try{propStmt.close();}catch(SQLException sqle){}}
									if(engineInstStmt != null){try{engineInstStmt.close();}catch(SQLException sqle){}}
									if(emsConn != null){try{emsConn.close();}catch(SQLException sqle){}} 				
									if(engineConn != null){try{engineConn.close();}catch(SQLException sqle){}} 				
								}	
							%>
						</div>		
							<input type="hidden" class="form-control" id="inputInstName" name="inputInstName"/>
					</td>
					<th align="right" width="110px">
						ROW수 (5만이하) :  
					</th>
					<td align="left" width="110px">
						<div class="select-style">
							<input type="text" class="form-control" id="rowCount" name="rowCount" />
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
