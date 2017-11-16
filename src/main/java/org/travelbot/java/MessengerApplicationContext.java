package org.travelbot.java;

import java.util.Optional;

import org.joo.scorpius.ApplicationContext;
import org.joo.scorpius.support.BaseResponse;
import org.joo.scorpius.support.TriggerExecutionException;
import org.joo.scorpius.support.builders.Factory;
import org.joo.scorpius.support.builders.TriggerExecutionContextBuilder;
import org.joo.scorpius.support.builders.id.TimeBasedIdGenerator;
import org.joo.scorpius.support.deferred.AsyncDeferredObject;
import org.joo.scorpius.support.deferred.Deferred;

import com.github.messenger4j.Messenger;

public class MessengerApplicationContext extends ApplicationContext {

	public final static String ACCESS_TOKEN = "EAABzrMo98iQBAJpePVHI5PtyxTm0jg4SZCJORFWDH6WKeCjAhWLCMA1VeuXRzk50yXxWxGWbZBNe9NnpPX4T2G7d19oZAVj0bHbJMZBO0z5IUEHxyxft1oZASAq7ZBoQsN5ddeIhJ12pEqsMLhpV51beNOrVWWuuuYNPLudeyYrwZDZD";
	
	public final static String APP_SECRET = "bc1cb829a28c5df32bc298433b2a32d2";
	
	public final static String VERIFY_TOKEN = "MeoHeoCho";

	private Messenger messenger;
	
	public MessengerApplicationContext(Factory<Deferred<BaseResponse, TriggerExecutionException>> deferredFactory,
			Factory<TriggerExecutionContextBuilder> executionContextBuilderFactory,
			Factory<Optional<String>> idGenerator) {
		super(deferredFactory, executionContextBuilderFactory, idGenerator);
		
//		final CloseableHttpClient httpClient = HttpClients.createDefault();
//		
//		final MessengerHttpClient customClient = new MessengerHttpClient() {
//			
//			@Override
//			public HttpResponse execute(HttpMethod httpMethod, String url, String jsonBody) throws IOException {
//				HttpPost request = new HttpPost(url);
//				HttpEntity entity = new ByteArrayEntity(jsonBody.getBytes());
//		        request.setEntity(entity);
//		        CloseableHttpResponse response = httpClient.execute(request);
//		        String body = EntityUtils.toString(response.getEntity());
//				return new HttpResponse(response.getStatusLine().getStatusCode(), body);
//			}
//		};
		messenger = Messenger.create(ACCESS_TOKEN, APP_SECRET, VERIFY_TOKEN);
	}
	
	public MessengerApplicationContext() {
		this(() -> new AsyncDeferredObject<>(), 
				() -> new TriggerExecutionContextBuilder(),
				new TimeBasedIdGenerator());
	}

	public Messenger getMessenger() {
		return messenger;
	}
}
