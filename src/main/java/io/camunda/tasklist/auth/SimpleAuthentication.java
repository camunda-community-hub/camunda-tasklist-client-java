package io.camunda.tasklist.auth;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.apollographql.apollo3.api.http.HttpHeader;

import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.exception.TaskListException;

/**
 * To use if Tasklist is not configured with Identity and Keycloak
 * 
 * @author ChristopheDame
 *
 */
public class SimpleAuthentication implements AuthInterface {

    private String taskListUsername;

    private String taskListPassword;

    public SimpleAuthentication(String taskListUsername, String taskListPassword) {
        this.taskListUsername = taskListUsername;
        this.taskListPassword = taskListPassword;
    }

    @Override
    public void authenticate(CamundaTaskListClient client) throws TaskListException {
        try {
            URL url = new URL(getLoginUrl(client.getTaskListUrl()));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setUseCaches(false);
            conn.setConnectTimeout(1000 * 5);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("charset", "utf-8");
            String loginParameters = "username=" + taskListUsername + "&password=" + taskListPassword;
            byte[] postData = loginParameters.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;
            conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            conn.setUseCaches(false);
            conn.getOutputStream().write(postData);
            conn.connect();

            if (conn.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT
                    || conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String cookie = conn.getHeaderField("Set-Cookie");
                client.getApolloClient().getHttpHeaders().clear();
                client.getApolloClient().getHttpHeaders().add(new HttpHeader("Cookie", cookie));
            } else {
                throw new TaskListException("Error "+conn.getResponseCode()+" obtaining access token : "+conn.getResponseMessage());
            }
        } catch (IOException e) {
            throw new TaskListException(e);
        }

    }

    private String getLoginUrl(String tasklistUrl) {
        if (tasklistUrl.endsWith("/graphql")) {
            return tasklistUrl.substring(0, tasklistUrl.length() - 8) + "/api/login";
        }
        if (tasklistUrl.endsWith("/")) {
            return tasklistUrl + "api/login";
        }
        return tasklistUrl + "/api/login";
    }
}
