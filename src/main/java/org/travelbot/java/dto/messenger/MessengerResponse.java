package org.travelbot.java.dto.messenger;
import org.joo.scorpius.support.BaseResponse;

public class MessengerResponse extends BaseResponse {

	private static final long serialVersionUID = 8485044599299739354L;
	
	private String text;
	
	public String getText() {
		return text;
	}
}
