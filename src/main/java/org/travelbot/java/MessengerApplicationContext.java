package org.travelbot.java;

import java.io.IOException;
import java.util.Optional;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.joo.scorpius.ApplicationContext;
import org.joo.scorpius.support.BaseResponse;
import org.joo.scorpius.support.TriggerExecutionException;
import org.joo.scorpius.support.builders.Factory;
import org.joo.scorpius.support.builders.TriggerExecutionContextBuilder;
import org.joo.scorpius.support.deferred.AsyncDeferredObject;
import org.joo.scorpius.support.deferred.Deferred;

import com.github.messenger4j.Messenger;
import com.github.messenger4j.spi.MessengerHttpClient;

public class MessengerApplicationContext extends ApplicationContext {

	public final static String ACCESS_TOKEN = "EAABzrMo98iQBAJpePVHI5PtyxTm0jg4SZCJORFWDH6WKeCjAhWLCMA1VeuXRzk50yXxWxGWbZBNe9NnpPX4T2G7d19oZAVj0bHbJMZBO0z5IUEHxyxft1oZASAq7ZBoQsN5ddeIhJ12pEqsMLhpV51beNOrVWWuuuYNPLudeyYrwZDZD";
	
	public final static String APP_SECRET = "bc1cb829a28c5df32bc298433b2a32d2";
	
	public final static String VERIFY_TOKEN = "MeoHeoCho";

	private Messenger messenger;
	
	public MessengerApplicationContext(Factory<Deferred<BaseResponse, TriggerExecutionException>> deferredFactory,
			Factory<TriggerExecutionContextBuilder> executionContextBuilderFactory) {
		super(deferredFactory, executionContextBuilderFactory);
		
		CloseableHttpAsyncClient httpClient = HttpAsyncClients.createDefault();
		httpClient.start();
		
		MessengerHttpClient customClient = new MessengerHttpClient() {
			
			@Override
			public HttpResponse execute(HttpMethod httpMethod, String url, String jsonBody) throws IOException {
				HttpPost request = new HttpPost(url);
				HttpEntity entity = new ByteArrayEntity(jsonBody.getBytes());
		        request.setEntity(entity);
				httpClient.execute(request, null);
				return null;
			}
		};
		messenger = Messenger.create(ACCESS_TOKEN, APP_SECRET, VERIFY_TOKEN, Optional.of(customClient));
	}
	
	public MessengerApplicationContext() {
		this(() -> new AsyncDeferredObject<>(), 
				() -> new TriggerExecutionContextBuilder());
	}

	public Messenger getMessenger() {
		return messenger;
	}
}
