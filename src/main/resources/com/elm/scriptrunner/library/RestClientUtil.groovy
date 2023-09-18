package com.elm.scriptrunner.library


import groovy.util.logging.Log4j
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import static groovyx.net.http.ContentType.*


@Log4j \
class RestClientUtil {

    @Deprecated
    def static getRestCall(String url, String path, String userName, String password) {
        try {
            def client = new RESTClient(url)
            client.auth.basic(userName, password)
            def resp = client.get(path: path, contentType: JSON)
            return (resp.data)
        } catch (HttpResponseException e) {
            log.debug ("UnExpected response: " + e.statusCode)
        }
    }

    @Deprecated
    def static postRestCall(String url, String path, String userName, String password, String body) {
        try {
            def client = new RESTClient(url)
            client.auth.basic(userName, password)
            def resp = client.post(path: path, body: body, contentType: JSON)
            return (resp.data)
        } catch (HttpResponseException e) {
            log.debug ("UnExpected response: " + e.statusCode)
        }
    }

    @Deprecated
    def static putRestCall(String url, String path, String userName, String password, String body) {
        try {
            def client = new RESTClient(url)
            client.auth.basic(userName, password)
            def resp = client.put(path: path, body: body, contentType: JSON)
            return (resp.data)
        } catch (HttpResponseException e) {
            log.debug ("UnExpected response: " + e.statusCode)
        }
    }

    def static crowdRestCall(String url, String path, def query, String userName, String password, def body) {
        try {
            def client = new RESTClient(url)
            def res = client.auth.basic(userName, password)
            client.post(path: path, query: query, body: body, contentType: JSON)

        } catch (HttpResponseException e) {
            log.debug ("UnExpected response: " + e.statusCode)
        }
    }

}