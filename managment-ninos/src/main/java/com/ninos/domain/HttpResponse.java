package com.ninos.domain;

import java.util.Date;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Data
public class HttpResponse {
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM-dd-yyy hh:mm:ss", timezone = "America/Chicago") // "timeStamp": "06-20-2020 03:34:22"
	private Date timeStamp;
	
	private int httpStatusCode;   // ex: OK:200, created:201, client error:400, server error:500
	private HttpStatus httpStatis;  // ex: NO_CONTENT(204, "No Content") , NO_CONTENT it will HttpStatus
	private String reason;      //  ex: inside HttpStatus there is NO_CONTENT(204, "No Content") , so reason is a phrase "No Content"
	private String message;    // ex: this it will developer message (your message) like : "your request is created"
	
	public HttpResponse(int httpStatusCode, HttpStatus httpStatis, String reason, String message) {
		this.timeStamp = new Date();
		this.httpStatusCode = httpStatusCode;
		this.httpStatis = httpStatis;
		this.reason = reason;
		this.message = message;
	}
}

// this is will be response to the client
/** ex:
 * httpStatusCode:"204",
 * httpStatis:"NO_CONTENT",
 * reason:"No Content",
 * message:"there are no content"
 *
 */



