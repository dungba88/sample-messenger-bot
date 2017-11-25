package org.travelbot.java;

import org.joo.scorpius.ApplicationContext;
import org.joo.scorpius.support.di.ApplicationModuleInjector;

import com.github.messenger4j.Messenger;

public class MessengerApplicationContext extends ApplicationContext {

    public final static String ACCESS_TOKEN = System.getenv("AccessToken");

    public final static String APP_SECRET = System.getenv("AppSecret");

    public final static String VERIFY_TOKEN = System.getenv("VerifyToken");

    public final static String PORT = System.getenv("PORT");

    private Messenger messenger;

    public MessengerApplicationContext(ApplicationModuleInjector injector) {
        super(injector);

        // final CloseableHttpClient httpClient = HttpClients.createDefault();
        //
        // final MessengerHttpClient customClient = new MessengerHttpClient() {
        //
        // @Override
        // public HttpResponse execute(HttpMethod httpMethod, String url, String
        // jsonBody) throws IOException {
        // HttpPost request = new HttpPost(url);
        // HttpEntity entity = new ByteArrayEntity(jsonBody.getBytes());
        // request.setEntity(entity);
        // CloseableHttpResponse response = httpClient.execute(request);
        // String body = EntityUtils.toString(response.getEntity());
        // return new HttpResponse(response.getStatusLine().getStatusCode(), body);
        // }
        // };
        messenger = Messenger.create(ACCESS_TOKEN, APP_SECRET, VERIFY_TOKEN);
    }

    public Messenger getMessenger() {
        return messenger;
    }

    public int getPort() {
        if (PORT != null && !PORT.isEmpty()) {
            return Integer.parseInt(PORT);
        }
        return 9090;
    }
}
